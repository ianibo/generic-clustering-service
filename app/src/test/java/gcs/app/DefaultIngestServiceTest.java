package gcs.app;

import gcs.app.pgvector.*;
import gcs.app.pgvector.storage.*;
import gcs.app.util.TestRecordLoader;
import gcs.core.InputRecord;
import gcs.app.InputRecordEntity;
import gcs.app.InputRecordRepository;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@MicronautTest
@DisabledIfSystemProperty(named = "skipIT", matches = "true")
class DefaultIngestServiceTest {

	private static final String RECORD_PARENT_DIRECTORY = "cs00000002m001";
	private static final List<String> TEST_RECORD_FILES = List.of(
		"4bcc8bff-2de9-50db-86ea-af75a84de228",
		"4f497bbd-5523-543d-905b-cb9a9aa8ceeb",
		"541947fe-8192-547d-be9b-f23db4778d72",
		"a33a02a1-9d4d-53c4-8d98-aeffba31466d"
	);
	private static final String UPDATED_TITLE = "Updated integration test title";
	private static final List<InputRecord> TEST_RECORDS = loadTestRecords();
	private static final Set<String> TARGET_RECORD_IDS = TEST_RECORDS.stream()
		.map(DefaultIngestServiceTest::sourceRecordIdOf)
		.collect(Collectors.toUnmodifiableSet());

	@Inject
	DefaultIngestService service;

	@Inject
	WorkClusterMemberRepository workClusterMemberRepository;

	@Inject
	WorkClusterRepository workClusterRepository;

	@Inject
	InstanceClusterMemberRepository instanceClusterMemberRepository;

	@Inject
	InstanceClusterRepository instanceClusterRepository;

	@Inject
	InputRecordRepository inputRecordRepository;

	@Test
	void testIngest() {
		for (InputRecord record : TEST_RECORDS) {
			InputRecord result = service.ingest(record);
			assertNotNull(result, "Ingest should return a versioned record for " + sourceRecordIdOf(record));
		}
		logClusterMemberships();
		logAllRecords();
	}

	@Test
	void testReingestUpdatesExistingRecord() {
		InputRecord originalRecord = TEST_RECORDS.get(0);
		InputRecord updatedRecord = withUpdatedTitle(originalRecord, UPDATED_TITLE);

		String recordId = versionedIdOf(originalRecord);
		Set<UUID> knownMemberIds = new HashSet<>(workMembersForRecord(recordId).stream()
			.map(WorkClusterMember::getId)
			.collect(Collectors.toSet()));

		InputRecord firstResult = service.ingest(originalRecord);
		InputRecordEntity firstEntity = inputRecordRepository.findById(firstResult.id()).orElseThrow();
		Instant firstModified = firstEntity.getDateModified();

		WorkClusterMember initialMember = newestMember(workMembersForRecord(firstResult.id()), knownMemberIds);
		assertNotNull(initialMember, "Initial ingest should create a work cluster member");
		knownMemberIds.add(initialMember.getId());

		InputRecord secondResult = service.ingest(updatedRecord);
		InputRecordEntity secondEntity = inputRecordRepository.findById(secondResult.id()).orElseThrow();

		assertEquals(firstResult.id(), secondResult.id(), "Versioned record id should be stable across ingests");
		assertEquals(firstEntity.getDateCreated(), secondEntity.getDateCreated(), "Entity should be updated instead of recreated");
		assertEquals(UPDATED_TITLE, secondEntity.getRecord().titles().get(0).value(), "Stored record must reflect the update");
		assertTrue(secondEntity.getDateModified().isAfter(firstModified), "Reingest should advance the modification timestamp");

		WorkClusterMember updatedMember = newestMember(workMembersForRecord(secondResult.id()), knownMemberIds);
		assertNotNull(updatedMember, "Reingest should add another work cluster member");
		knownMemberIds.add(updatedMember.getId());

		assertNotNull(initialMember.getEmbedding(), "Initial embedding should be stored");
		assertNotNull(updatedMember.getEmbedding(), "Updated embedding should be stored");
		float[] initialEmbedding = initialMember.getEmbedding().toArray();
		float[] updatedEmbedding = updatedMember.getEmbedding().toArray();
		assertFalse(Arrays.equals(initialEmbedding, updatedEmbedding), "Embedding should change when canonical details change");
	}

	private static List<InputRecord> loadTestRecords() {
		return TEST_RECORD_FILES.stream()
			.map(DefaultIngestServiceTest::loadRecordOrThrow)
			.collect(Collectors.toUnmodifiableList());
	}

	private static InputRecord loadRecordOrThrow(String recordFile) {
		try {
			return TestRecordLoader.loadRecord(RECORD_PARENT_DIRECTORY, recordFile);
		} catch (IOException e) {
			throw new UncheckedIOException("Unable to load test record " + recordFile, e);
		}
	}

	private static String sourceRecordIdOf(InputRecord record) {
		InputRecord.Provenance provenance = record.provenance();
		if (provenance == null || provenance.sourceRecordId() == null) {
			throw new IllegalStateException("Test record is missing provenance.sourceRecordId: " + record.id());
		}
		return provenance.sourceRecordId();
	}

	private static String versionedIdOf(InputRecord record) {
		InputRecord.Provenance provenance = record.provenance();
		if (provenance == null) {
			throw new IllegalStateException("Test record is missing provenance: " + record.id());
		}
		return provenance.authorityId() + ":" + provenance.sourceRecordId();
	}

	private static InputRecord withUpdatedTitle(InputRecord record, String newTitle) {
		if (record.titles() == null || record.titles().isEmpty()) {
			throw new IllegalStateException("Test record is missing titles: " + record.id());
		}
		List<InputRecord.Title> titles = new ArrayList<>(record.titles());
		InputRecord.Title firstTitle = titles.get(0);
		titles.set(0, new InputRecord.Title(newTitle, firstTitle.type(), firstTitle.language()));
		return new InputRecord(
			record.id(),
			record.provenance(),
			record.domain(),
			record.licenseDeclaration(),
			record.identifiers(),
			List.copyOf(titles),
			record.contributors(),
			record.languages(),
			record.edition(),
			record.publication(),
			record.physical(),
			record.subjects(),
			record.series(),
			record.relations(),
			record.classification(),
			record.notes(),
			record.rights(),
			record.admin(),
			record.media(),
			record.ext(),
			record.classifierVersion()
		);
	}

	private List<WorkClusterMember> workMembersForRecord(String recordId) {
		return streamOf(workClusterMemberRepository.findAll())
			.filter(member -> recordId.equals(member.getRecordId()))
			.collect(Collectors.toList());
	}

	private static WorkClusterMember newestMember(List<WorkClusterMember> members, Set<UUID> knownMemberIds) {
		return members.stream()
			.filter(member -> !knownMemberIds.contains(member.getId()))
			.max(Comparator.comparing(WorkClusterMember::getDateCreated))
			.orElse(null);
	}

	private void logClusterMemberships() {
		Map<UUID, List<String>> workClusters = streamOf(workClusterMemberRepository.findAll())
			.filter(member -> TARGET_RECORD_IDS.contains(member.getRecordId()))
			.filter(member -> member.getWorkCluster() != null)
			.collect(Collectors.groupingBy(member -> member.getWorkCluster().getId(),
				Collectors.mapping(WorkClusterMember::getRecordId, Collectors.toList())));

		if (workClusters.isEmpty()) {
			log.info("No work clusters found for test records {}", TARGET_RECORD_IDS);
		} else {
			workClusters.forEach((clusterId, members) ->
				log.info("Work cluster {} contains records {}", clusterId, members));
		}

		Map<UUID, List<String>> instanceClusters = streamOf(instanceClusterMemberRepository.findAll())
			.filter(member -> TARGET_RECORD_IDS.contains(member.getRecordId()))
			.filter(member -> member.getInstanceCluster() != null)
			.collect(Collectors.groupingBy(member -> member.getInstanceCluster().getId(),
				Collectors.mapping(InstanceClusterMember::getRecordId, Collectors.toList())));

		if (instanceClusters.isEmpty()) {
			log.info("No instance clusters found for test records {}", TARGET_RECORD_IDS);
		} else {
			instanceClusters.forEach((clusterId, members) ->
				log.info("Instance cluster {} contains records {}", clusterId, members));
		}
	}

	private void logAllRecords() {

		for (WorkClusterMember wcm : workClusterMemberRepository.findAll()) {
			log.info("WCM: {} member of {} ({})",wcm.getId(),wcm.getWorkCluster().getId(),wcm.getRecordId());
		}

		for (WorkCluster wc : workClusterRepository.findAll()) {
			log.info("WC: {}",wc.getId());
		}

    for (InstanceClusterMember icm : instanceClusterMemberRepository.findAll()) {
      log.info("ICM: {} member of {} ({})",icm.getId(),icm.getInstanceCluster().getId(),icm.getRecordId());
    }

    for (InstanceCluster ic : instanceClusterRepository.findAll()) {
      log.info("IC: {}",ic.getId());
    }

    for (InputRecordEntity ir : inputRecordRepository.findAll()) {
      log.info("IR: {}",ir.getId());
    }

	}

	private static <T> Stream<T> streamOf(Iterable<T> iterable) {
		return StreamSupport.stream(iterable.spliterator(), false);
	}
}
