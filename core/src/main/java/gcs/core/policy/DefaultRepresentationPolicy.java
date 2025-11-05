package gcs.core.policy;

import gcs.core.InputRecord;
import jakarta.inject.Singleton;

/**
 * Default implementation of the RepresentationPolicy interface.
 * This implementation contains basic scoring logic.
 */
@Singleton
public class DefaultRepresentationPolicy implements RepresentationPolicy {

    @Override
    public boolean fieldAgreementOk(InputRecord record1, InputRecord record2) {
        return true;
    }

    @Override
    public double conflictPenalty(InputRecord record1, InputRecord record2) {
        if (record1.physical() != null && record2.physical() != null &&
            !record1.physical().contentType().equals(record2.physical().contentType())) {
            return 0.1;
        }
        return 0.0;
    }

    @Override
    public double scorePublicationYear(InputRecord record1, InputRecord record2) {
        if (record1.publication() == null || record2.publication() == null ||
            record1.publication().year() == null || record2.publication().year() == null) {
            return 0.0;
        }

        int year1 = record1.publication().year();
        int year2 = record2.publication().year();
        int diff = Math.abs(year1 - year2);

        if (diff == 0) {
            return 1.0;
        } else if (diff <= 2) {
            return 0.5;
        } else {
            return 0.0;
        }
    }
}
