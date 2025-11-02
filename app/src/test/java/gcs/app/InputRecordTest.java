package gcs.app;

import gcs.core.InputRecord;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import io.micronaut.serde.ObjectMapper;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

@MicronautTest
class InputRecordTest {

    @Inject
    ObjectMapper objectMapper;

    private static Stream<Path> findTestRecords() throws IOException, URISyntaxException {
        URI uri = InputRecordTest.class.getResource("/testrecs").toURI();
        Path path = Paths.get(uri);
        return Files.walk(path)
                .filter(Files::isRegularFile);
    }

    @ParameterizedTest
    @MethodSource("findTestRecords")
    void testDeserializationOfTestRecords(Path recordPath) throws IOException {
        String content = Files.readString(recordPath);
        InputRecord record = objectMapper.readValue(content, InputRecord.class);
        assertNotNull(record);
        assertNotNull(record.id());
    }
}
