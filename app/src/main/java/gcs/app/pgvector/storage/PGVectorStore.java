package gcs.app.pgvector.storage;

import gcs.app.pgvector.*;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import jakarta.inject.Named;

@Slf4j
@Singleton
@Named("pgvector")
public class PGVectorStore {

	private final WorkClusterRepository workClusterRepository;
	private final WorkClusterMemberRepository workClusterMemberRepository;
	private final InstanceClusterRepository instanceClusterRepository;
	private final InstanceClusterMemberRepository instanceClusterMemberRepository;

	public PGVectorStore(
		WorkClusterRepository workClusterRepository,
		WorkClusterMemberRepository workClusterMemberRepository,
		InstanceClusterRepository instanceClusterRepository,
		InstanceClusterMemberRepository instanceClusterMemberRepository
	) {
		this.workClusterRepository = workClusterRepository;
		this.workClusterMemberRepository = workClusterMemberRepository;
		this.instanceClusterRepository = instanceClusterRepository;
		this.instanceClusterMemberRepository = instanceClusterMemberRepository;
	}

	@Transactional
	public WorkCluster saveWorkCluster(WorkCluster cluster) {
		log.info("Save work cluster {} via repo with class {}",cluster,workClusterRepository.getClass().getName());
		WorkCluster result = workClusterRepository.save(cluster);
		log.info("Returning {} from call to save via {}",result);
		return result;
	}

	@Transactional
	public WorkClusterMember saveWorkClusterMember(WorkClusterMember member) {
		return workClusterMemberRepository.save(member);
	}

	@Transactional
	public InstanceCluster saveInstanceCluster(InstanceCluster cluster) {
		return instanceClusterRepository.save(cluster);
	}

	@Transactional
	public InstanceClusterMember saveInstanceClusterMember(InstanceClusterMember member) {
		return instanceClusterMemberRepository.save(member);
	}
}
