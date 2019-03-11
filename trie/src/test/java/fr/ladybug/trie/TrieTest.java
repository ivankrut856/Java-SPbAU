package fr.ladybug.trie;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class TrieTest {

    Trie trie;

    @BeforeEach
    void initializeTrie() {
        trie = new Trie();
    }

    @Test
    void testNoRoomForDataException() {
        trie.add("");
        trie.add("Hello");
        trie.add("Hello_ultimate");
        trie.add("Rumba");
        try {
            FileOutputStream closedStream = new FileOutputStream("hey");
            closedStream.close();
            assertThrows(IOException.class, () -> trie.serialize(closedStream));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void testSerializeDeserializeEqualsId() {
        var result = new ByteArrayOutputStream();
        trie.add("");
        trie.add("Hello");
        trie.add("Hello_ultimate");
        trie.add("Rumba");
        assertDoesNotThrow(() -> trie.serialize(result));

        trie = new Trie();
        assertDoesNotThrow(() -> trie.deserialize(new ByteArrayInputStream(result.toByteArray())));

        assertTrue(trie.contains(""));
        assertTrue(trie.contains("Hello"));
        assertTrue(trie.contains("Hello_ultimate"));
        assertTrue(trie.contains("Rumba"));
    }

    @Test
    void testSerializeEmpty() throws IOException {
        var result = new ByteArrayOutputStream();
        assertDoesNotThrow(() -> trie.serialize(result));

        var expected = new ByteArrayOutputStream();
        var dataExpected = new DataOutputStream(expected);
        dataExpected.writeInt(0);
        dataExpected.writeInt(0);
        dataExpected.writeInt(0);

        assertArrayEquals(expected.toByteArray(), result.toByteArray());
    }

    @Test
    void testDeserializeFromEmpty() {
        assertThrows(IOException.class, () -> trie.deserialize(new ByteArrayInputStream(new byte[0])));
    }

    @Test
    void testDeserializeCorrupted() throws IOException {
        var corrupted1 = new ByteArrayOutputStream();
        var dataCorrupted1 = new DataOutputStream(corrupted1);
        dataCorrupted1.writeInt(-1);
        dataCorrupted1.writeInt(0);
        dataCorrupted1.writeInt(0);


        var corrupted2 = new ByteArrayOutputStream();
        var dataCorrupted2 = new DataOutputStream(corrupted2);
        dataCorrupted2.writeInt(0);
        dataCorrupted2.writeInt(-1);
        dataCorrupted2.writeInt(0);


        var corrupted3 = new ByteArrayOutputStream();
        var dataCorrupted3 = new DataOutputStream(corrupted3);
        dataCorrupted3.writeInt(0);
        dataCorrupted3.writeInt(0);
        dataCorrupted3.writeInt(-1);

        assertThrows(StreamCorruptedException.class, () ->
                trie.deserialize(new ByteArrayInputStream(corrupted1.toByteArray())));
        assertThrows(StreamCorruptedException.class, () ->
                trie.deserialize(new ByteArrayInputStream(corrupted2.toByteArray())));
        assertThrows(StreamCorruptedException.class, () ->
                trie.deserialize(new ByteArrayInputStream(corrupted3.toByteArray())));
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