package gcs.app.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import gcs.core.InputRecord;
import java.io.IOException;

public class TestRecordLoader {

    private static final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private static final String DEFAULT_PARENT_DIRECTORY = "cs00000002m001";

    public static InputRecord loadRecord(String parentDirectory, String fileName) throws IOException {
        String resourcePath = String.format("/testrecs/%s/%s", parentDirectory, fileName);
        try (var in = TestRecordLoader.class.getResourceAsStream(resourcePath)) {
            if (in == null) {
                throw new IOException("Unable to load test record at " + resourcePath);
            }
            return objectMapper.readValue(in, InputRecord.class);
        }
    }

    public static InputRecord loadRecord(String fileName) throws IOException {
        return loadRecord(DEFAULT_PARENT_DIRECTORY, fileName);
    }
}
