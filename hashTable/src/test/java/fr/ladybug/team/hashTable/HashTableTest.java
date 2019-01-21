package fr.ladybug.team.hashTable;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HashTableTest {

    HashTable table;

    @BeforeEach
    void initializeEmptyHashTable() {
        table = new HashTable();
    }

    @Test
    void testEmptyTableContainsNothing() {
        assertFalse(table.contains("Test1"));
        assertFalse(table.contains(""));
        assertFalse(table.contains("??"));
        assertEquals(0, table.getSize());
    }

    @Test
    void testPutGet() {
        table.put("Test1", "A");
        assertEquals("A", table.get("Test1"));
        assertEquals(1, table.getSize());

        table.put("Test1", "B");
        assertEquals("B", table.get("Test1"));
        assertEquals(1, table.getSize());
    }

    @Test
    void testAddNoExcept() {
        assertNull(table.put("Test1", "A"));
        assertNull(table.put("", "A"));
        assertEquals("A", table.put("Test1", ""));
    }

    /** Check whether table can handle expansion or not */
    @Test
    void test100PutGetNoExcept100Get() {
        for (int i = 0; i < 100; i++) {
            table.put("Test" + i, String.valueOf(i));
            assertEquals(String.valueOf(i), table.get("Test" + i));
        }
        for (int i = 0; i < 100; i++) {
            assertEquals(String.valueOf(i), table.get("Test" + i));
        }
    }

    @Test
    void testRemove() {
        table.put("Test1", "A");
        assertEquals("A", table.remove("Test1"));
        assertFalse(table.contains("Test1"));
    }

    @Test
    void testEmptyAfterClear() {
        table.clear();
        table.put("Test1", "1");
        table.clear();
        assertFalse(table.contains("Test1"));
        assertEquals(0, table.getSize());
    }
}