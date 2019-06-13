package fr.ladybug.team;

import fr.ladybug.team.testdata.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TesterTest {

    @Test
    public void testNoTests() {
        var results = assertDoesNotThrow(() -> Tester.fromClass(NoTests.class).test());
        assertEquals(0, results.size());
    }

    @Test
    public void testNoPublicTests() {
        var results = assertDoesNotThrow(() -> Tester.fromClass(NoPublicTests.class).test());
        assertEquals(0, results.size());
    }

    @Test
    public void testExceptiveInstance() {
        var exception = assertThrows(TestException.class, () -> Tester.fromClass(ExceptiveInstance.class));
        assertEquals(1, exception.getSuppressed().length);
        assertEquals("Such an exceptive constructor", exception.getSuppressed()[0].getMessage());
    }

    @Test
    public void testNoNullaryConstructor() {
        var exception = assertThrows(TestException.class, () -> Tester.fromClass(NoNullaryConstructor.class));
        assertEquals("The test class must regular class with public nullary constructor", exception.getMessage());
    }

    @Test
    public void testInvalidTestSignature() {
        var exception = assertThrows(TestException.class, () -> Tester.fromClass(InvalidTestSignature.class));
        assertEquals("All @Test methods must have no argument to invoke", exception.getMessage());
    }

    @Test
    public void testInvalidBeforeSignature() {
        var exception = assertThrows(TestException.class, () -> Tester.fromClass(InvalidBeforeSignature.class));
        assertEquals("All @Before methods must have no argument to invoke", exception.getMessage());
    }

    @Test
    public void testInvalidAfterSignature() {
        var exception = assertThrows(TestException.class, () -> Tester.fromClass(InvalidAfterSignature.class));
        assertEquals("All @After methods must have no argument to invoke", exception.getMessage());
    }

    @Test
    public void testInvalidBeforeClassSignature() {
        var exception = assertThrows(TestException.class, () -> Tester.fromClass(InvalidBeforeClassSignature.class));
        assertEquals("All @BeforeClass methods must have no argument to invoke", exception.getMessage());
    }

    @Test
    public void testInvalidAfterClassSignature() {
        var exception = assertThrows(TestException.class, () -> Tester.fromClass(InvalidAfterClassSignature.class));
        assertEquals("All @AfterClass methods must have no argument to invoke", exception.getMessage());
    }

    @Test
    public void testSingleTestSuccess() {
        var results = assertDoesNotThrow(() -> Tester.fromClass(SingleSuccessTest.class).test());
        assertEquals(1, results.size());

        assertEquals(Tester.TestResult.TestResultState.SUCCESS, results.get(0).getState());
    }

    @Test
    public void testBeforeAfterDoesNotThrowIfNoTests() {
        assertDoesNotThrow(() -> Tester.fromClass(BeforeAfterDoesNotThrowIfNoTests.class).test());
    }

    @Test
    public void testComplexTest() {
        var results = assertDoesNotThrow(() -> Tester.fromClass(ComplexTest.class).test());
        assertEquals(5, results.size());


        assertEquals(Tester.TestResult.TestResultState.DISABLED, results.get(0).getState());
        assertEquals(Tester.TestResult.TestResultState.FAILED, results.get(1).getState());
        assertEquals(Tester.TestResult.TestResultState.SUCCESS, results.get(2).getState());
        assertEquals(Tester.TestResult.TestResultState.FAILED, results.get(3).getState());
        assertEquals(Tester.TestResult.TestResultState.SUCCESS, results.get(4).getState());
    }

}