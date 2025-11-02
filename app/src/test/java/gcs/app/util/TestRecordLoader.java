package gcs.app.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import gcs.core.InputRecord;
import java.io.IOException;

public class TestRecordLoader {

    private static final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    public static InputRecord loadRecord(String fileName) throws IOException {
        try (var in = TestRecordLoader.class.getResourceAsStream("/testrecs/cs00000002m001/" + fileName)) {
            return objectMapper.readValue(in, InputRecord.class);
        }
    }
}
