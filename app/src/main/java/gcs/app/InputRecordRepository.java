package gcs.app;

import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.CrudRepository;
import java.util.List;
import java.util.Objects;

public interface InputRecordRepository extends CrudRepository<InputRecordEntity, String> {
    List<InputRecordEntity> findByIdInList(List<String> ids);

    default InputRecordEntity saveOrUpdate(InputRecordEntity entity) {
        Objects.requireNonNull(entity, "InputRecordEntity must not be null");
        String id = entity.getId();
        if (id != null && existsById(id)) {
            return update(entity);
        }
        return save(entity);
    }
}
