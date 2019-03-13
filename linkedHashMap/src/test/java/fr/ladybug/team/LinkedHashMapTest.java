package fr.ladybug.team;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LinkedHashMapTest {

    LinkedHashMap<Integer, String> map;

    @BeforeEach
    void testInitialize() {
        map = new LinkedHashMap<>();
    }

    @Test
    void testAddThenContains() {
        map.put(4, "Rothbard");
        assertTrue(map.containsKey(4));
    }

    @Test
    void testAddInOrder() {
        for (int i = 0; i < 5; i++) {
            map.put(i, String.valueOf(i));
        }
        var iterator = map.entrySet().iterator();
        final int[] begin = {0};
        iterator.forEachRemaining(value -> {
            assertEquals(String.valueOf(begin[0]), value.getValue());
            begin[0]++;
        });
    }

}