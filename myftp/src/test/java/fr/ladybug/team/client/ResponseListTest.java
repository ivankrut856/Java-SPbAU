package fr.ladybug.team.client;

import com.google.common.primitives.Ints;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ResponseListTest {
    @Test
    void testFailedListCorrupted() {
        byte[] corrupted = new byte[]{0, 0};
        var response = ResponseList.fromBytes(corrupted);
        assertFalse(response.isValid());
        assertEquals("Response was corrupted.", response.getError());
    }

    @Test
    void testFailedListBadQuery() {
        byte[] badQuery = Ints.toByteArray(-5);
        var response = ResponseList.fromBytes(badQuery);
        assertFalse(response.isValid());
        assertEquals("Query execution failed.", response.getError());
    }

    @Test
    void testFailedListNonexistentFile() {
        byte[] badQuery = Ints.toByteArray(-1);
        var response = ResponseList.fromBytes(badQuery);
        assertFalse(response.isValid());
        assertEquals("Directory does not exist.", response.getError());
    }

    @Test
    void testSucceededList() {
        String expected = "ok";
        byte[] toSend = ArrayUtils.addAll(ArrayUtils.addAll(Ints.toByteArray(1),
                ArrayUtils.addAll(Ints.toByteArray(expected.getBytes().length), expected.getBytes())), new byte[]{0});
        var response = ResponseList.fromBytes(toSend);
        assertTrue(response.isValid());
        var view = response.toFileViews();
        assertEquals(1, view.size());
        assertEquals(expected, view.get(0).getFileName());
        assertFalse(view.get(0).isDirectory());
    }
}