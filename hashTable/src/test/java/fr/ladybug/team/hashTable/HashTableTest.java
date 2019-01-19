package fr.ladybug.team.hashTable;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HashTableTest {

    @Test
    void testEmptyTableContainsNothing() {
        HashTable table = new HashTable();
        assertFalse(table.contains("Test1"));
        assertFalse(table.contains(""));
        assertFalse(table.contains("??"));
        assertEquals(0, table.getSize());
    }

    @Test
    void testPutGet() {
        HashTable table = new HashTable();
        table.put("Test1", "A");
        assertEquals("A", table.get("Test1"));

        table.put("Test1", "B");
        assertEquals("B", table.get("Test1"));
    }

    @Test
    void testAddNoExcept() {
        HashTable table = new HashTable();
        assertNull(table.put("Test1", "A"));
        assertNull(table.put("", "A"));
        assertEquals("A", table.put("Test1", ""));
    }

    /* Check whether table can handle expansion or not */
    @Test
    void test100PutGetNoExcept100Get() {
        HashTable table = new HashTable();
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
        HashTable table = new HashTable();
        table.put("Test1", "A");
        assertEquals("A", table.remove("Test1"));
        assertFalse(table.contains("Test1"));

    }

    @Test
    void testEmptyAfterClear() {
        HashTable table = new HashTable();
        table.clear();
        table.put("Test1", "1");
        table.clear();
        assertFalse(table.contains("Test1"));
        assertEquals(0, table.getSize());
    }
}