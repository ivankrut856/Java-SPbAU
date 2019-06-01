package fr.ladybug.team;

import fr.ladybug.team.annotations.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Tester {

    private List<Method> beforeAll = new ArrayList<>();
    private List<Method> afterAll = new ArrayList<>();

    private List<Method> beforeEach = new ArrayList<>();
    private List<Method> afterEach = new ArrayList<>();

    private List<Method> testMethods = new ArrayList<>();

    private Object testClassInstance;

    private void Tester() {
    }

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
                    throw new TestException("All @TestClass methods must have no argument to invoke");
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

    public List<TestResult> test() throws TestException {
        List<TestResult> results = new ArrayList<>();

        for (var method : beforeAll) {
            try {
                method.invoke(testClassInstance);
            } catch (IllegalAccessException e) {
                throw new RuntimeException("@BeforeClass invocation error. Shouldn't normally happen");
            }
            catch (InvocationTargetException e) {
                var testException = new TestException("An exception occurred in @BeforeClass method");
                testException.addSuppressed(e.getCause());
                throw testException;
            }
        }

        for (var method: testMethods) {
            var testAnnotation = method.getAnnotation(Test.class);
            if (testAnnotation.ignore().length > 0) {
                results.add(new TestResult(TestResult.TestResultState.DISABLED, testAnnotation.ignore()[0]));
                continue;
            }

            for (var beforeMethod : beforeEach) {
                try {
                    beforeMethod.invoke(testClassInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("@Before invocation error. Shouldn't normally happen");
                }
                catch (InvocationTargetException e) {
                    var testException = new TestException("An exception occurred in @Before method");
                    testException.addSuppressed(e.getCause());
                    throw testException;
                }
            }

            List<Class<?>> expectedExceptions = Arrays.asList(testAnnotation.expected());
            try {
                method.invoke(testClassInstance);
                results.add(new TestResult(TestResult.TestResultState.SUCCESS, "All correct"));
            }
            catch (IllegalAccessException e) {
                throw new RuntimeException("@Test invocation error. Shouldn't normally happen");
            }
            catch (InvocationTargetException e) {
                if (expectedExceptions.contains(e.getCause().getClass())) {
                    results.add(new TestResult(TestResult.TestResultState.SUCCESS, String.format("Expected exception %s happened", e.getCause().getClass().getName())));
                }
                else {
                    results.add(new TestResult(TestResult.TestResultState.FAILED, "Unexpected exception:\n" + e.getMessage()));
                }
            }

            for (var afterMethod : afterEach) {
                try {
                    afterMethod.invoke(testClassInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("@After invocation error. Shouldn't normally happen");
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
                throw new RuntimeException("@AfterClass invocation error. Shouldn't normally happen");
            }
            catch (InvocationTargetException e) {
                var testException = new TestException("An exception occurred in @AfterClass method");
                testException.addSuppressed(e.getCause());
                throw testException;
            }
        }

        return results;
    }

    public static class TestResult {
        private TestResultState state;
        private String message;

        public TestResult(TestResultState state, String message) {
            this.state = state;
            this.message = message;
        }

        public TestResultState getState() {
            return state;
        }

        public String getMessage() {
            return message;
        }

        public enum TestResultState {
            SUCCESS,
            FAILED,
            DISABLED
        }
    }
}
