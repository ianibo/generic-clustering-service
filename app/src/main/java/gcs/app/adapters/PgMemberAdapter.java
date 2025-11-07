package gcs.app.adapters;

import gcs.app.InputRecordEntity;
import gcs.app.InputRecordRepository;
import gcs.app.pgvector.storage.InstanceClusterMemberRepository;
import gcs.app.pgvector.storage.WorkClusterMemberRepository;
import gcs.core.InputRecord;
import gcs.core.consolidation.MemberPort;
import jakarta.inject.Singleton;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * An adapter that implements the MemberPort interface for a PostgreSQL database.
 */
@Singleton
public class PgMemberAdapter implements MemberPort {

    private final WorkClusterMemberRepository workClusterMemberRepository;
    private final InstanceClusterMemberRepository instanceClusterMemberRepository;
    private final InputRecordRepository inputRecordRepository;

    public PgMemberAdapter(
        WorkClusterMemberRepository workClusterMemberRepository,
        InstanceClusterMemberRepository instanceClusterMemberRepository,
        InputRecordRepository inputRecordRepository
    ) {
        this.workClusterMemberRepository = workClusterMemberRepository;
        this.instanceClusterMemberRepository = instanceClusterMemberRepository;
        this.inputRecordRepository = inputRecordRepository;
    }

    @Override
    public List<InputRecord> getMembers(UUID clusterId) {
        List<String> recordIds;
        if (workClusterMemberRepository.existsByWorkClusterId(clusterId)) {
            recordIds = workClusterMemberRepository.findByWorkClusterId(clusterId).stream()
                .map(gcs.app.pgvector.WorkClusterMember::getRecordId)
                .collect(Collectors.toList());
        } else {
            recordIds = instanceClusterMemberRepository.findByInstanceClusterId(clusterId).stream()
                .map(gcs.app.pgvector.InstanceClusterMember::getRecordId)
                .collect(Collectors.toList());
        }

        return inputRecordRepository.findByIdInList(recordIds).stream()
            .map(InputRecordEntity::getRecord)
            .collect(Collectors.toList());
    }
}
