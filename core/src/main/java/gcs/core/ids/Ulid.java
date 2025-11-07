package gcs.core.ids;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Generates monotonic ULID-style identifiers that are compatible with a 26-character VARCHAR column.
 * The implementation follows the canonical 48-bit time / 80-bit entropy layout using Crockford's Base32 alphabet.
 */
public final class Ulid {

	private static final char[] ENCODING = "0123456789ABCDEFGHJKMNPQRSTVWXYZ".toCharArray();
	private static final int TIME_LENGTH = 10;
	private static final int TOTAL_LENGTH = 26;

	private Ulid() {
	}

	/**
	 * @return A new 26-character ULID string.
	 */
	public static String nextUlid() {
		char[] buffer = new char[TOTAL_LENGTH];
		encodeTime(System.currentTimeMillis(), buffer);
		encodeRandom(buffer);
		return new String(buffer);
	}

	private static void encodeTime(long timestamp, char[] buffer) {
		for (int i = TIME_LENGTH - 1; i >= 0; i--) {
			buffer[i] = ENCODING[(int) (timestamp & 0x1F)];
			timestamp >>>= 5;
		}
	}

	private static void encodeRandom(char[] buffer) {
		byte[] randomness = new byte[10]; // 80 bits
		ThreadLocalRandom.current().nextBytes(randomness);

		int bufferIndex = TIME_LENGTH;
		int bitBuffer = 0;
		int bitsInBuffer = 0;

		for (byte randomByte : randomness) {
			bitBuffer = (bitBuffer << 8) | (randomByte & 0xFF);
			bitsInBuffer += 8;
			while (bitsInBuffer >= 5) {
				int shift = bitsInBuffer - 5;
				int value = (bitBuffer >> shift) & 0x1F;
				bitsInBuffer -= 5;
				buffer[bufferIndex++] = ENCODING[value];
			}
		}

		if (bitsInBuffer > 0) {
			buffer[bufferIndex] = ENCODING[(bitBuffer << (5 - bitsInBuffer)) & 0x1F];
		}
	}
}
