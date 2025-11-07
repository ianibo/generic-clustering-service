// PGvectorAttributeConverter.java
package gcs.app.pgvector;

import com.pgvector.PGvector;
import io.micronaut.core.convert.ConversionContext;
import io.micronaut.data.model.runtime.convert.AttributeConverter;
import jakarta.inject.Singleton;
import org.postgresql.util.PGobject;
import java.sql.SQLException;

@Singleton
public class PGvectorAttributeConverter implements AttributeConverter<PGvector, Object> {

	@Override
	public Object convertToPersistedValue(PGvector attribute, ConversionContext context) {
		// For writes: pass PGvector straight through (pgvector-java knows how to serialize it)
		return attribute;
	}

	@Override
	public PGvector convertToEntityValue(Object persisted, ConversionContext context) {
		if (persisted == null) return null;

		if (persisted instanceof PGvector v) {
			return v; // ideal case when the driver returns a PGvector
		}
		if (persisted instanceof PGobject pgo) {
			// When the driver doesn't know the type, it returns a PGobject with text value "[…]"
			return parseVectorUnchecked(pgo.getValue());
		}
		// Some pools/drivers hand back a plain String
		return parseVectorUnchecked(persisted.toString());
	}

	private static PGvector parseVectorUnchecked(String text) {
		try {
			// Your pgvector-java version uses a String ctor that throws SQLException
			return new PGvector(text);
		} catch (SQLException e) {
			throw new IllegalArgumentException("Invalid VECTOR literal: " + text, e);
		}
	}
}

