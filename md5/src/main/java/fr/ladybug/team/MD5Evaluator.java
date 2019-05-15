package fr.ladybug.team;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

public interface MD5Evaluator {
    String evaluate(String filepath) throws IOException, NoSuchAlgorithmException;
}
