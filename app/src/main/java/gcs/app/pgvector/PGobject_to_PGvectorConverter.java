package gcs.app.pgvector;

import com.pgvector.PGvector;
import com.pgvector.PGvector;
import io.micronaut.core.convert.ConversionContext;
import io.micronaut.core.convert.TypeConverter;
import jakarta.inject.Singleton;
import java.util.Optional;
import org.postgresql.util.PGobject;

@Singleton
public class PGobject_to_PGvectorConverter implements TypeConverter<PGobject, PGvector> {

    @Override
    public Optional<PGvector> convert(PGobject object, Class<PGvector> targetType, ConversionContext context) {
        try {
            return Optional.of(new PGvector(object.getValue()));
        } catch (java.sql.SQLException e) {
            return Optional.empty();
        }
    }
}
