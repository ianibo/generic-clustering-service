package gcs.core;

import com.sangupta.murmur.Murmur3;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.nio.ByteOrder;
import java.text.Normalizer;
import java.util.Arrays;
import java.util.HashSet;
import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;
import java.util.stream.Collectors;

public class HashingEmbeddingService implements EmbeddingService {
    private static final HashSet<String> STOP_WORDS = new HashSet<>(Arrays.asList(
        "a", "an", "and", "are", "as", "at", "be", "but", "by", "for", "if", "in", "into", "is",
        "it", "no", "not", "of", "on", "or", "such", "that", "the", "their", "then", "there",
        "these", "they", "this", "to", "was", "will", "with"));

    private static final int DIMENSION = 384;

    private static final VarHandle V =
        MethodHandles.byteArrayViewVarHandle(int[].class, ByteOrder.LITTLE_ENDIAN);

    private final RandomGenerator rng = RandomGeneratorFactory.of("L64X128MixRandom").create(0x5eed);

    /**
     * Embeds a given text into a 384-dimensional vector space using a hashing technique.
     * The process involves several steps:
     * 1.  Tokenization: The input text is normalized to NFKC, converted to lower-case, and split
     *     into tokens based on non-word characters. Stop words are removed.
     * 2.  Feature Hashing: Each token is converted into 4-gram character shingles. These shingles
     *     are then hashed using the Murmur3 32-bit algorithm. The signed hash value is used to
     *     update a sparse vector by adding a random value to the corresponding index modulo the
     *     vector dimension.
     * 3.  L2-Normalization: The resulting sparse vector is L2-normalized to produce the final float
     *     array representation.
     *
     * @param text The input text to be embedded.
     * @return A float array of 384 dimensions representing the embedded vector.
     */
    @Override
    public float[] embed(String text) {
        var sparse = new double[DIMENSION];
        var tokens = tokenize(text);
        for (var token : tokens) {
            var shingles = toShingles(token, 4);
            for (var shingle : shingles) {
                var bytes = shingle.getBytes();
                var hash = Murmur3.hash_x86_32(bytes, bytes.length, 0);
                var idx = Math.floorMod((int) hash, DIMENSION);
                sparse[idx] += rng.nextGaussian();
            }
        }
        return normalize(sparse);
    }

    private static float[] normalize(double[] vector) {
        var norm = 0.0;
        for (var v : vector) {
            norm += v * v;
        }
        norm = Math.sqrt(norm);
        var result = new float[vector.length];
        for (var i = 0; i < vector.length; i++) {
            result[i] = (float) (vector[i] / norm);
        }
        return result;
    }

    /**
     * Tokenizes and normalizes a given text.
     *
     * @param text The input string to be tokenized.
     * @return A list of strings, where each string is a token that has been normalized and is not a
     *     stop word.
     */
    private static HashSet<String> tokenize(String text) {
        // NFKC normalization, lowercase, split on non-word characters, drop stop words
        var normalized = Normalizer.normalize(text, Normalizer.Form.NFKC).toLowerCase();
        var tokens = normalized.split("\\W+");
        var result = new HashSet<String>();
        for (var token : tokens) {
            if (!STOP_WORDS.contains(token)) {
                result.add(token);
            }
        }
        return result;
    }

    private static HashSet<String> toShingles(String text, int size) {
        var shingles = new HashSet<String>();
        if (text.length() < size) {
            shingles.add(text);
        } else {
            for (var i = 0; i <= text.length() - size; i++) {
                shingles.add(text.substring(i, i + size));
            }
        }
        return shingles;
    }

    @Override
    public int dim() {
        return DIMENSION;
    }
}
