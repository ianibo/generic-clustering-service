package gcs.app;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dev.langchain4j.model.embedding.EmbeddingModel;
import gcs.app.embedding.OpenAIEmbeddingService;
import gcs.core.EmbeddingService;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Replaces;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.env.Environment;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.Iterator;
import java.util.Map;

/**
 * Provides deterministic test embeddings by hashing the input text and serving pre-recorded vectors.
 * Falls back to {@link OpenAIEmbeddingService} when a hash is missing so new embeddings can be captured.
 */
@Factory
@Requires(env = Environment.TEST)
public final class TestEmbeddingFactory {

	private static final String RESOURCE_NAME = "test-embeddings.json";

	@Singleton
	@Named("openai")
	@Replaces(OpenAIEmbeddingService.class)
	EmbeddingService testEmbeddingService(EmbeddingModel embeddingModel) {
		return new StaticEmbeddingService(RESOURCE_NAME, new OpenAIEmbeddingService(embeddingModel));
	}

	private static final class StaticEmbeddingService implements EmbeddingService {

		private static final Logger LOG = LoggerFactory.getLogger(StaticEmbeddingService.class);
		private static final int DEFAULT_DIMENSION = 1536;
		private static final ObjectMapper MAPPER = new ObjectMapper();

		private final Map<String, float[]> embeddings;
		private final String resourceName;
		private final int dimension;
		private final EmbeddingService delegate;
		private final Path writableResource;

		StaticEmbeddingService(String resourceName, EmbeddingService delegate) {
			this.resourceName = resourceName;
			this.delegate = delegate;
			EmbeddingStore store = loadEmbeddings(resourceName);
			this.embeddings = new HashMap<>(store.vectors());
			this.dimension = store.dimension() > 0 ? store.dimension() : DEFAULT_DIMENSION;
			this.writableResource = locateWritableResource(resourceName);
		}

		@Override
		public float[] embed(String text) {
			String hash = hash(text);
			float[] cached = embeddings.get(hash);
			if (cached != null) {
				return cached.clone();
			}
			LOG.warn("Missing test embedding for hash {} (len {}). Please add it to {}", hash, text.length(), resourceName);
			float[] generated = delegate.embed(text);
			cacheEmbedding(hash, generated);
			return generated.clone();
		}

		@Override
		public int dim() {
			return dimension;
		}

		private void cacheEmbedding(String hash, float[] vector) {
			synchronized (embeddings) {
				if (vector.length != dimension) {
					throw new IllegalStateException("Vector dimension mismatch. Expected %d got %d".formatted(dimension, vector.length));
				}
				embeddings.put(hash, vector.clone());
				if (writableResource == null) {
					LOG.error("Unable to locate writable test embeddings resource; vector will not be persisted.");
					return;
				}
				try {
					writeEmbeddings();
					LOG.info("Persisted new test embedding {} to {}", hash, writableResource);
				} catch (IOException e) {
					LOG.error("Failed to write embeddings to {}", writableResource, e);
				}
			}
		}

		private void writeEmbeddings() throws IOException {
			Files.createDirectories(writableResource.getParent());
			ObjectNode root = MAPPER.createObjectNode();
			root.put("dimension", dimension);
			ObjectNode vectorsNode = root.putObject("vectors");
			ArrayList<String> hashes = new ArrayList<>(embeddings.keySet());
			hashes.sort(String::compareTo);
			for (String key : hashes) {
				ArrayNode vectorNode = vectorsNode.putArray(key);
				float[] values = embeddings.get(key);
				for (float value : values) {
					vectorNode.add(value);
				}
			}
			MAPPER.writerWithDefaultPrettyPrinter().writeValue(writableResource.toFile(), root);
		}

		private static EmbeddingStore loadEmbeddings(String resourceName) {
			try (InputStream stream = TestEmbeddingFactory.class.getClassLoader().getResourceAsStream(resourceName)) {
				if (stream == null) {
					LOG.warn("Embedding resource {} not found; falling back to delegate for all texts.", resourceName);
					return new EmbeddingStore(DEFAULT_DIMENSION, Collections.emptyMap());
				}
				JsonNode root = MAPPER.readTree(stream);
				int dimension = root.path("dimension").asInt(DEFAULT_DIMENSION);
				JsonNode vectorsNode = root.path("vectors");
				Map<String, float[]> vectors = new HashMap<>();
				if (vectorsNode.isObject()) {
					Iterator<Map.Entry<String, JsonNode>> fields = vectorsNode.fields();
					while (fields.hasNext()) {
						Map.Entry<String, JsonNode> entry = fields.next();
						float[] vector = toFloatArray(entry.getValue());
						if (dimension <= 0) {
							dimension = vector.length;
						} else if (vector.length != dimension) {
							throw new IllegalStateException("Vector length mismatch for hash " + entry.getKey());
						}
						vectors.put(entry.getKey(), vector);
					}
				}
				return new EmbeddingStore(dimension, Collections.unmodifiableMap(vectors));
			} catch (IOException e) {
				throw new IllegalStateException("Unable to load test embeddings from " + resourceName, e);
			}
		}

		private static Path locateWritableResource(String resourceName) {
			Path current = Path.of("").toAbsolutePath();
			while (current != null) {
				Path moduleResource = current.resolve("src/test/resources").resolve(resourceName);
				if (Files.exists(moduleResource) || Files.isDirectory(moduleResource.getParent())) {
					return moduleResource;
				}
				Path appResource = current.resolve("app/src/test/resources").resolve(resourceName);
				if (Files.exists(appResource) || Files.isDirectory(appResource.getParent())) {
					return appResource;
				}
				current = current.getParent();
			}
			LOG.warn("Unable to locate source resource directory for {}", resourceName);
			return null;
		}

		private static float[] toFloatArray(JsonNode node) {
			if (!node.isArray()) {
				throw new IllegalStateException("Embedding entry must be an array: " + node);
			}
			float[] vector = new float[node.size()];
			for (int i = 0; i < node.size(); i++) {
				vector[i] = (float) node.get(i).asDouble();
			}
			return vector;
		}

		private static String hash(String text) {
			try {
				MessageDigest digest = MessageDigest.getInstance("SHA-256");
				byte[] hash = digest.digest(text.getBytes(StandardCharsets.UTF_8));
				return HexFormat.of().formatHex(hash);
			} catch (NoSuchAlgorithmException e) {
				throw new IllegalStateException("SHA-256 not available", e);
			}
		}

	}

	private record EmbeddingStore(int dimension, Map<String, float[]> vectors) {
	}
}
