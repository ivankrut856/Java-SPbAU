package fr.ladybug.team;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import static org.junit.jupiter.api.Assertions.*;

class MD5Test {

    private File file = null;
    @AfterAll
    public void loadTestFile() {
        file = new File(this.getClass().getResource("/TestMD5File").getPath());
    }

    @Test
    public void checkSingleThreadCorrectness() throws IOException, NoSuchAlgorithmException {
        var hash = MD5.getEvaluator().evaluate(file.getPath());
        assertEquals("db89bb5ceab87f9c0fcc2ab36c189c2c", hash);
    }

    @Test
    public void checkMultiThreadCorrectness() throws IOException, NoSuchAlgorithmException {
        var hash = MD5.getEvaluatorMultiThread().evaluate(file.getPath());
        assertEquals("db89bb5ceab87f9c0fcc2ab36c189c2c", hash);
    }

}