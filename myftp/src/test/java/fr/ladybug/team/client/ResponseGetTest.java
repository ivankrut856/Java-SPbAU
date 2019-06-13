package fr.ladybug.team.client;

import com.google.common.primitives.Ints;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class ResponseGetTest {
    @Test
    void testFailedGetCorrupted() {
        byte[] corrupted = new byte[]{0, 0};
        var response = ResponseGet.fromBytes(corrupted);
        assertFalse(response.isValid());
        assertEquals("Response was corrupted.", response.getError());
    }

    @Test
    void testFailedGetBadQuery() {
        byte[] badQuery = Ints.toByteArray(-5);
        var response = ResponseGet.fromBytes(badQuery);
        assertFalse(response.isValid());
        assertEquals("Query execution failed.", response.getError());
    }

    @Test
    void testFailedGetNonexistentFile() {
        byte[] badQuery = Ints.toByteArray(-1);
        var response = ResponseGet.fromBytes(badQuery);
        assertFalse(response.isValid());
        assertEquals("File does not exist.", response.getError());
    }

    @Test
    void testSucceededGet() {
        String expected = "ok";
        byte[] toSend = ArrayUtils.addAll(Ints.toByteArray(expected.getBytes(StandardCharsets.UTF_8).length), expected.getBytes(StandardCharsets.UTF_8));
        var response = ResponseGet.fromBytes(toSend);
        assertTrue(response.isValid());
        assertEquals(expected, new String(response.getFileContent()));
    }
}