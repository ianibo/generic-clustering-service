package gcs.app;

import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.CrudRepository;

public interface InputRecordRepository extends CrudRepository<InputRecordEntity, String> {
}
