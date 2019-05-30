package fr.ladybug.team;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

/**
 * Interface representing MD5 hash evaluator
 */
public interface MD5Evaluator {
    /**
     * Main acting function. Calculates digest of the file or directory (recursively) and turns it into hex string
     * @param filepath the path to the target file or directory
     * @return the result hex string
     * @throws IOException the exception is thrown when there is a problem with reading the files
     * @throws NoSuchAlgorithmException the exception is normally never thrown, but if there is no md5 hash algorithm in Java then it is
     */
    String evaluate(String filepath) throws IOException, NoSuchAlgorithmException;
}
