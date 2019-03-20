package fr.ladybug.team;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

class MyTreeSetImplTest {

    private MyTreeSetImpl<String> basic, advanced;
    @BeforeEach
    void setsInitialise() {
        basic = new MyTreeSetImpl<>();
        advanced = new MyTreeSetImpl<>(String.CASE_INSENSITIVE_ORDER);
    }

    @SuppressWarnings("EmptyMethod")
    @Test
    void testSetInitialiseNoExcept() {
    }

    @Test
    void testIteratorsInitializeNoExcept() {
        Iterator<String> iterator = basic.iterator();
        Iterator<String> descendingIterator = basic.descendingIterator();
    }

    @Test
    void testActualReverseIterating() {
        basic.add("1");
        basic.add("2");
        basic.add("3");
        basic.add("4");
        basic.add("5");

        final String[] variations = {"", ""};
        basic.iterator().forEachRemaining(x -> variations[0] += x);
        basic.descendingIterator().forEachRemaining(x -> variations[1] += x);

        assertEquals(variations[0].length(), variations[1].length());
        for (int i = 0; i < variations[0].length(); i++) {
            assertEquals(variations[0].charAt(i), variations[1].charAt(variations[0].length() - i - 1));
        }
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

    @Test
    void testAddNull() {
        assertThrows(IllegalArgumentException.class, () -> basic.add(null));
    }

    @Test
    void testEmptySetFirstLast() {
        assertNull(basic.last());
        assertNull(basic.first());
    }

    @Test
    void testWrongIteratorRemoving() {
        Iterator<String> it = basic.iterator();
        assertThrows(IllegalStateException.class, it::remove);
    }

    @Test
    void testIteratorNextAfterEnd() {
        basic.add("1");
        basic.add("2");
        basic.add("3");
        basic.add("4");
        basic.add("5");
        Iterator<String> it = basic.iterator();
        it.forEachRemaining(s -> {
        });

        assertThrows(NoSuchElementException.class, it::next);
    }
}