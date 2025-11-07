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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.assertNotNull;

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
