package fr.ladybug.team;

import fr.ladybug.team.annotations.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Main JUnit class
 *  Using to perform construction from the Class instance and to provide results after test() invocation
 */
public class Tester {

    private List<Method> beforeAll = new ArrayList<>();
    private List<Method> afterAll = new ArrayList<>();

    private List<Method> beforeEach = new ArrayList<>();
    private List<Method> afterEach = new ArrayList<>();

    private List<Method> testMethods = new ArrayList<>();

    private Object testClassInstance;

    private Tester() {
    }

    /** Factory constructs an instance of Tester from Class instance
     * The Class instance should meet some requirements:
     * 1. Class must have public nullary constructor
     * 2, The constructor must not throw any throwable
     * 3. Every annotated method must not require arguments to invoke
     * 4. Every annotated method must be public (otherwise it will be ignored)
     * @param testClass the class which is to test
     * @return Tester instance which is ready to test() invocation
     * @throws TestException the exception is thrown if the Class instance doesn't meet the requirements
     */
    public static Tester fromClass(Class<?> testClass) throws TestException {
        Tester instance = new Tester();

        try {
            instance.testClassInstance = testClass.getConstructor().newInstance();
        } catch (InstantiationException | NoSuchMethodException | IllegalAccessException e) {
            throw new TestException("The test class must regular class with public nullary constructor");
        }
        catch (InvocationTargetException e) {
            var testException = new TestException("An exception occurred during test class instantiation");
            testException.addSuppressed(e.getCause());
            throw testException;
        }


        List<Method> methods = Arrays.asList(testClass.getMethods());
        for (var method : methods) {
            if (method.isAnnotationPresent(Test.class)) {
                if (method.getParameterCount() != 0) {
                    throw new TestException("All @Test methods must have no argument to invoke");
                }
                instance.testMethods.add(method);
            }
            if (method.isAnnotationPresent(AfterClass.class)) {
                if (method.getParameterCount() != 0) {
                    throw new TestException("All @AfterClass methods must have no argument to invoke");
                }
                instance.afterAll.add(method);
            }
            if (method.isAnnotationPresent(BeforeClass.class)) {
                if (method.getParameterCount() != 0) {
                    throw new TestException("All @BeforeClass methods must have no argument to invoke");
                }
                instance.beforeAll.add(method);
            }
            if (method.isAnnotationPresent(After.class)) {
                if (method.getParameterCount() != 0) {
                    throw new TestException("All @After methods must have no argument to invoke");
                }
                instance.afterEach.add(method);
            }
            if (method.isAnnotationPresent(Before.class)) {
                if (method.getParameterCount() != 0) {
                    throw new TestException("All @Before methods must have no argument to invoke");
                }
                instance.beforeEach.add(method);
            }
        }

        return instance;
    }

    /**
     * Provides test results
     * If the Tester was correctly instantiated and every annotated methods throws no exception then this methods guaranteed not to throw any exception too
     * @return the set of test results
     * @throws TestException the exception is thrown only if some annotated method throws an exception during invocation
     */
    public List<TestResult> test() throws TestException {
        List<TestResult> results = new ArrayList<>();

        for (var method : beforeAll) {
            try {
                method.invoke(testClassInstance);
            } catch (IllegalAccessException e) {
                System.err.println(String.format("Skipped @BeforeClass %s invocation due to the internal error: %s", method.getName(), e.getMessage()));
            }
            catch (InvocationTargetException e) {
                var testException = new TestException("An exception occurred in @BeforeClass method");
                testException.addSuppressed(e.getCause());
                throw testException;
            }
        }

        int testNumber = 0;
        for (var method: testMethods) {
            testNumber++;
            var testAnnotation = method.getAnnotation(Test.class);
            if (testAnnotation.ignore().length > 0) {
                results.add(new TestResult(testNumber, TestResult.TestResultState.DISABLED, testAnnotation.ignore()[0]));
                continue;
            }

            for (var beforeMethod : beforeEach) {
                try {
                    beforeMethod.invoke(testClassInstance);
                } catch (IllegalAccessException e) {
                    System.err.println(String.format("Skipped @Before %s invocation due to the internal error: %s", beforeMethod.getName(), e.getMessage()));
                }
                catch (InvocationTargetException e) {
                    var testException = new TestException("An exception occurred in @Before method");
                    testException.addSuppressed(e.getCause());
                    throw testException;
                }
            }

            List<Class<?>> expectedExceptions = Arrays.asList(testAnnotation.expected());
            boolean thrown = false;
            long elapsedTime = 0;
            try {
                long startTime = System.nanoTime();
                method.invoke(testClassInstance);
                elapsedTime = System.nanoTime() - startTime;
            }
            catch (IllegalAccessException e) {
                System.err.println(String.format("Skipped @Test %s invocation due to the internal error: %s", method.getName(), e.getMessage()));
                thrown = true;
            }
            catch (InvocationTargetException e) {
                thrown = true;
                if (expectedExceptions.contains(e.getCause().getClass())) {
                    results.add(new TestResult(testNumber, TestResult.TestResultState.SUCCESS, String.format("Expected exception %s happened", e.getCause().getClass().getName())));
                }
                else {
                    results.add(new TestResult(testNumber, TestResult.TestResultState.FAILED, "Unexpected exception:\n" + e.getMessage()));
                }
            }

            if (!thrown) {
                if (testAnnotation.expected().length > 0) {
                    results.add(new TestResult(testNumber, TestResult.TestResultState.FAILED, "No exception was thrown but expected", elapsedTime));
                } else {
                    results.add(new TestResult(testNumber, TestResult.TestResultState.SUCCESS, "All correct", elapsedTime));
                }
            }

            for (var afterMethod : afterEach) {
                try {
                    afterMethod.invoke(testClassInstance);
                } catch (IllegalAccessException e) {
                    System.err.println(String.format("Skipped @After %s invocation due to the internal error: %s", afterMethod.getName(), e.getMessage()));
                }
                catch (InvocationTargetException e) {
                    var testException = new TestException("An exception occurred in @After method");
                    testException.addSuppressed(e.getCause());
                    throw testException;
                }
            }
        }


        for (var method : afterAll) {
            try {
                method.invoke(testClassInstance);
            } catch (IllegalAccessException e) {
                System.err.println(String.format("Skipped @AfterClass %s invocation due to the internal error: %s", method.getName(), e.getMessage()));
            }
            catch (InvocationTargetException e) {
                var testException = new TestException("An exception occurred in @AfterClass method");
                testException.addSuppressed(e.getCause());
                throw testException;
            }
        }

        return results;
    }


    /**
     * Class represents single test's result
     */
    public static class TestResult {
        private int testNumber;
        private TestResultState state;
        private String message;

        private double runningTime = 0;

        /** Field-wise constructor without running time provided */
        public TestResult(int testNumber, TestResultState state, String message) {
            this.testNumber = testNumber;
            this.state = state;
            this.message = message;
        }

        /** Full field-wise constructor */
        public TestResult(int testNumber, TestResultState state, String message, long runningTimeInNanos) {
            this.testNumber = testNumber;
            this.state = state;
            this.message = message;
            this.runningTime = (double)runningTimeInNanos / 1000000;
        }

        public int getTestNumber() {
            return testNumber;
        }

        public TestResultState getState() {
            return state;
        }

        public String getMessage() {
            return message;
        }

        public double getRunningTime() {
            return runningTime;
        }

        /**
         * Enum represents every state which can be achieved during testing for single test
         */
        public enum TestResultState {
            SUCCESS,
            FAILED,
            DISABLED
        }
    }
}
