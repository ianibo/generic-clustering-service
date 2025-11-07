package gcs.core.synthesis;

import gcs.core.InputRecord;
import jakarta.inject.Singleton;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Default implementation of the Synthesizer interface.
 * This implementation creates a synthetic record by merging fields from the member records.
 */
@Singleton
public class DefaultSynthesizer implements Synthesizer {

    @Override
    public InputRecord synthesize(List<InputRecord> members) {
        if (members == null || members.isEmpty()) {
            return null;
        }

        InputRecord firstRecord = members.get(0);

        List<InputRecord.Title> titles = members.stream()
            .flatMap(r -> r.titles().stream())
            .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
            .entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .map(List::of)
            .orElse(firstRecord.titles());

        List<InputRecord.Contributor> contributors = members.stream()
            .flatMap(r -> r.contributors().stream())
            .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
            .entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .map(List::of)
            .orElse(firstRecord.contributors());

        return new InputRecord(
            firstRecord.id(),
            firstRecord.provenance(),
            firstRecord.domain(),
            firstRecord.licenseDeclaration(),
            firstRecord.identifiers(),
            titles,
            contributors,
            firstRecord.languages(),
            firstRecord.edition(),
            firstRecord.publication(),
            firstRecord.physical(),
            firstRecord.subjects(),
            firstRecord.series(),
            firstRecord.relations(),
            firstRecord.classification(),
            firstRecord.notes(),
            firstRecord.rights(),
            firstRecord.admin(),
            firstRecord.media(),
            firstRecord.ext(),
            firstRecord.classifierVersion(),
            firstRecord.rawChecksum()
        );
    }
}
