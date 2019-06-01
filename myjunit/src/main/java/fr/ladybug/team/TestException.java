package fr.ladybug.team;

/**
 * An exception for handling exceptional situations during Tester constructor or test()ing
 */
public class TestException extends Exception {
    public TestException(String message) {
        super(message);
    }
}
