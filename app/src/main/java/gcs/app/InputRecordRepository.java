package gcs.app;

import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.CrudRepository;
import java.util.List;

public interface InputRecordRepository extends CrudRepository<InputRecordEntity, String> {
    List<InputRecordEntity> findByIdInList(List<String> ids);
}
