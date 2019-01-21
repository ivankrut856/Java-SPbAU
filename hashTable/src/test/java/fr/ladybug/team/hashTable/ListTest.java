package fr.ladybug.team.hashTable;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ListTest {

    @Test
    void testEmptyListContainsNothing() {
        List list = new List();
        assertFalse(list.contains("Test1"));
        assertNull(list.get("Test1"));
        assertFalse(list.contains(""));
        assertFalse(list.contains("??"));
        assertEquals(0, list.getSize());
    }

    @Test
    void testListPutGetContains() {
        List list = new List();
        assertFalse(list.contains("Test1"));
        list.put("Test1", "A");
        assertEquals("A", list.get("Test1"));
        assertTrue(list.contains("Test1"));

        list.put("Test1", "B");
        assertTrue(list.contains("Test1"));
        assertEquals("B", list.get("Test1"));
    }

    @Test
    void testListAddNoExcept() {
        List list = new List();
        list.put("Test1", "A");
        list.put("Test2", "");
        list.put("", "Test1");
    }

    @Test
    void testRemove() {
        List list = new List();
        list.put("Test1", "A");
        list.remove("Test1");
        assertFalse(list.contains("Test1"));
    }
}