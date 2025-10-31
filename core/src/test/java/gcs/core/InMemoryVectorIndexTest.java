package gcs.core;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;

class InMemoryVectorIndexTest {

    @Test
    void testTopK() {
        var index = new InMemoryVectorIndex<String>();
        index.add("a", new float[]{1.0f, 0.0f}, "a");
        index.add("b", new float[]{0.0f, 1.0f}, "b");
        index.add("c", new float[]{0.5f, 0.5f}, "c");

        var query = new float[]{0.7f, 0.3f};
        var neighbors = index.topK(query, 2);

        assertEquals(2, neighbors.size());
        assertEquals("a", neighbors.get(0).id());
        assertEquals("c", neighbors.get(1).id());
    }

    @Test
    void testRadius() {
        var index = new InMemoryVectorIndex<String>();
        index.add("a", new float[]{1.0f, 0.0f}, "a");
        index.add("b", new float[]{0.0f, 1.0f}, "b");
        index.add("c", new float[]{0.5f, 0.5f}, "c");

        var query = new float[]{0.7f, 0.3f};
        var neighbors = index.radius(query, 0.8f);

        assertEquals(2, neighbors.size());
        assertEquals("a", neighbors.get(0).id());
        assertEquals("c", neighbors.get(1).id());
    }

    @Test
    void testTieBreaking() {
        var index = new InMemoryVectorIndex<String>();
        index.add("a", new float[]{1.0f, 0.0f}, "a");
        index.add("b", new float[]{1.0f, 0.0f}, "b");
        index.add("c", new float[]{0.0f, 1.0f}, "c");

        var query = new float[]{1.0f, 0.0f};
        var neighbors = index.topK(query, 2);

        assertEquals(2, neighbors.size());
        assertEquals("a", neighbors.get(0).id());
        assertEquals("b", neighbors.get(1).id());
    }
}
