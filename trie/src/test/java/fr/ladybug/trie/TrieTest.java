package fr.ladybug.trie;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.*;

import static org.junit.jupiter.api.Assertions.*;

class TrieTest {

    Trie trie;

    @Disabled
    @Test
    void manualTestSerialize() {
        var file = new File("test1.subl");
//        trie.add("B");
//        trie.add("A");
        trie.add("");
        trie.add("Hello");
        trie.add("Hello_ultimate");
        trie.add("Rumba");
        try (var output = new FileOutputStream(file)) {
            trie.serialize(output);
        } catch (IOException e) {
            e.printStackTrace();
        }

        trie = new Trie();
        try (var input = new FileInputStream("test1.subl")) {
            trie.deserialize(input);
        } catch (IOException e) {
            e.printStackTrace();
        }
        assertTrue(trie.contains(""));
        assertTrue(trie.contains("Hello"));
        assertTrue(trie.contains("Hello_ultimate"));
        assertTrue(trie.contains("Rumba"));
    }

    @BeforeEach
    void initializeTrie() {
        trie = new Trie();
    }

    @Test
    void addNoExcept() {
        trie.add("");
        trie.add("Hello");
        trie.add("Hello_ultimate");
        trie.add("Rumba");
    }

    @Test void addContains() {
        assertFalse(trie.contains(""));
        assertFalse(trie.add(""));
        assertTrue(trie.contains(""));

        assertFalse(trie.contains("Hello"));
        assertFalse(trie.add("Hello"));
        assertTrue(trie.contains("Hello"));

        assertFalse(trie.contains("Hello_ultimate"));
        assertFalse(trie.add("Hello_ultimate"));
        assertTrue(trie.contains("Hello_ultimate"));

        assertFalse(trie.contains("Rumba"));
        assertFalse(trie.add("Rumba"));
        assertTrue(trie.contains("Rumba"));
    }

    @Test
    void removeContains() {
        trie.add("");
        trie.add("Hello");
        trie.add("Hello_ultimate");
        trie.add("Rumba");

        assertTrue(trie.contains(""));
        assertTrue(trie.remove(""));
        assertFalse(trie.contains(""));

        assertTrue(trie.contains("Hello"));
        assertTrue(trie.remove("Hello"));
        assertFalse(trie.contains("Hello"));

        assertTrue(trie.contains("Hello_ultimate"));
        assertTrue(trie.remove("Hello_ultimate"));
        assertFalse(trie.contains("Hello_ultimate"));

        assertTrue(trie.contains("Rumba"));
        assertTrue(trie.remove("Rumba"));
        assertFalse(trie.contains("Rumba"));
    }

    @Test
    void containsTrashRemoveOnes(){
        trie.add("");
        trie.add("Hello");
        trie.add("Hello_ultimate");
        trie.add("Rumba");

        assertFalse(trie.contains("Rumba?!!"));
        assertFalse(trie.remove("Rumba?!!"));
    }

    @Test
    void getSize() {
        assertEquals(0, trie.getSize());

        trie.add("");
        assertEquals(1, trie.getSize());


        trie.add("Hello");
        assertEquals(2, trie.getSize());

        trie.add("Hello_ultimate");
        assertEquals(3, trie.getSize());

        trie.add("Rumba");
        assertEquals(4, trie.getSize());

        trie.remove("Rumba");
        assertEquals(3, trie.getSize());

        trie.remove("");
        assertEquals(2, trie.getSize());

        trie.remove("What?");
        assertEquals(2, trie.getSize());

        trie.remove("Hello");
        assertEquals(1, trie.getSize());

        trie.remove("Hello_ultimate");
        assertEquals(0, trie.getSize());
    }

    @Test
    void howManyStartWithPrefix() {
        trie.add("");
        trie.add("Hello");
        trie.add("Hello_ultimate");
        trie.add("Rumba");

        assertEquals(4, trie.howManyStartWithPrefix(""));
        assertEquals(2, trie.howManyStartWithPrefix("H"));
        assertEquals(2, trie.howManyStartWithPrefix("Hello"));
        assertEquals(1, trie.howManyStartWithPrefix("Hello_"));
        assertEquals(1, trie.howManyStartWithPrefix("R"));
        assertEquals(0, trie.howManyStartWithPrefix("Vodka"));
    }
}