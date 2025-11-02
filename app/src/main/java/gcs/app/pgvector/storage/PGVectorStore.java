package gcs.app.pgvector.storage;

import gcs.app.pgvector.*;
import jakarta.inject.Singleton;

@Singleton
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

    public WorkCluster saveWorkCluster(WorkCluster cluster) {
        return workClusterRepository.save(cluster);
    }

    public WorkClusterMember saveWorkClusterMember(WorkClusterMember member) {
        return workClusterMemberRepository.save(member);
    }

    public InstanceCluster saveInstanceCluster(InstanceCluster cluster) {
        return instanceClusterRepository.save(cluster);
    }

    public InstanceClusterMember saveInstanceClusterMember(InstanceClusterMember member) {
        return instanceClusterMemberRepository.save(member);
    }
}
