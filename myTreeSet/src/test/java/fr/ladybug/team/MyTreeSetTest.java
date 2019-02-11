package fr.ladybug.team;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.CollectionUtils;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class MyTreeSetTest {

    MyTreeSet<String> basic, advanced;
    @BeforeEach
    void SetsInitialise() {
        basic = new MyTreeSet<>();
        advanced = new MyTreeSet<>(String.CASE_INSENSITIVE_ORDER);
    }

    @Test
    void testSetInitialiseNoExcept() {
    }

    @Test
    void testIteratorInitializeNoExcept() {
        Iterator<String> it = basic.iterator();
    }

    @Test
    void testContains() {
        assertTrue(basic.add("hello"));
        assertTrue(basic.contains("hello"));

        assertTrue(advanced.add("hello"));
        assertTrue(advanced.contains("hello"));
    }

    @Test
    void testAddSome() {
        assertTrue(basic.add("Hello"));
        assertTrue(basic.add("hello"));
        assertTrue(basic.add("World"));
        assertTrue(basic.add("How are you?"));

        assertTrue(advanced.add("Hello"));
        assertFalse(advanced.add("hello"));
        assertTrue(advanced.add("World"));
        assertTrue(advanced.add("How are you?"));
    }

    @Test
    void testContainsAddContainsRemoveContains() {
        int testSize = 10007;
        String[] toTest = new String[testSize];
        for (int i = 0; i < testSize; i++) {
            toTest[i] = String.valueOf(i);
        }
        for (String testString : toTest) {
            assertFalse(basic.contains(testString));
            assertTrue(basic.add(testString));
            assertTrue(basic.contains(testString));
        }
        for (String testString : toTest) {
            assertTrue(basic.contains(testString));
            assertTrue(basic.remove(testString));
            assertFalse(basic.contains(testString));
        }
    }

    @Test
    void testDescendingStuff() {
        int testSize = 17;
        ArrayList<String> toTest = new ArrayList<>();
        for (int i = 0; i < testSize; i++) {
            toTest.add(String.valueOf(i));
        }
        basic.addAll(toTest);
        toTest.sort(String::compareTo);
        Collections.reverse(toTest);
        assertEquals(toTest, Arrays.asList(basic.descendingSet().toArray()));
    }

    @Test
    void testBinaryWaydown() {
        basic.add("1");
        basic.add("2");
        basic.add("3");
        basic.add("4");
        basic.add("5");
        assertEquals("2", basic.lower("3"));
        assertEquals("4", basic.descendingSet().lower("3"));

        assertEquals("4", basic.higher("3"));
        assertEquals("2", basic.descendingSet().higher("3"));

        assertEquals("3", basic.ceiling("3"));
        assertEquals("3", basic.descendingSet().ceiling("3"));

        assertEquals("3", basic.floor("3"));
        assertEquals("3", basic.descendingSet().floor("3"));


    }

    @Test
    void testFirstLast() {
        basic.add("1");
        basic.add("2");
        basic.add("3");
        basic.add("4");
        basic.add("5");
        assertEquals("1", basic.first());
        assertEquals("5", basic.last());
        assertEquals("5", basic.descendingSet().first());
        assertEquals("1", basic.descendingSet().last());
    }

}