package fr.ladybug.team;

import fr.ladybug.team.classes.*;
import org.junit.jupiter.api.Test;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

class ReflectorTest {

    private final String NEW_LINE = System.lineSeparator();

    @Test
    void testIndevCompilableAndRobust() throws IOException, ClassNotFoundException {
        Reflector.printStructure(IndevTestClass.class, "SomeClass.java");
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        compiler.run(null, null, null,
                new File("SomeClass.java").getPath());
        ClassLoader classLoader = URLClassLoader.newInstance(new URL[]{Paths.get("").toUri().toURL()});
        Class<?> loaded = Class.forName("SomeClass", false, classLoader);

        var diffResultBytes = new ByteArrayOutputStream();
        var diffResultStream = new PrintStream(diffResultBytes);
        Reflector.diffClassesToPrintStream(IndevTestClass.class, loaded, diffResultStream);
        assertEquals("First class in relation to second:" + NEW_LINE, new String(diffResultBytes.toByteArray()));
    }

    @Test
    void testIndevDifferences() {
        var diffResult = new ByteArrayOutputStream();
        var diffResultStream = new PrintStream(diffResult);
        Reflector.diffClassesToPrintStream(IndevTestClass.class, IndevTestClass2.class, diffResultStream);
        assertEquals("First class in relation to second:" + NEW_LINE +
                "I  > 1:     private static java.lang.String data;" + NEW_LINE +
                "II > 1:     public double name;" + NEW_LINE +
                "I  > 2:     public int name;" + NEW_LINE +
                "II > 2: " + NEW_LINE +
                "I  > 3:     protected T what;" + NEW_LINE +
                "II > 3: " + NEW_LINE +
                "I  > 5:     public static  void Main(java.lang.String[] v0, java.util.List<? super java.lang.String> v1) {" + NEW_LINE +
                "II > 5: }" + NEW_LINE +
                "I  > 6:         return;" + NEW_LINE +
                "II > 6: <Empty line>" + NEW_LINE +
                "I  > 7:     }" + NEW_LINE +
                "II > 7: <Empty line>" + NEW_LINE +
                "I  > 8:     public static <E extends java.lang.Object> java.lang.String anotherMethod(E v2) {" + NEW_LINE +
                "II > 8: <Empty line>" + NEW_LINE +
                "I  > 9:         return null;" + NEW_LINE +
                "II > 9: <Empty line>" + NEW_LINE +
                "I  > 10:     }" + NEW_LINE +
                "II > 10: <Empty line>" + NEW_LINE +
                "I  > 11:       void justMethod(int v3, java.lang.String v4, T v5) {" + NEW_LINE +
                "II > 11: <Empty line>" + NEW_LINE +
                "I  > 12:         return;" + NEW_LINE +
                "II > 12: <Empty line>" + NEW_LINE +
                "I  > 13:     }" + NEW_LINE +
                "II > 13: <Empty line>" + NEW_LINE +
                "I  > 14:     public <T extends java.lang.Object> int whatStuffAreYou(T v6) {" + NEW_LINE +
                "II > 14: <Empty line>" + NEW_LINE +
                "I  > 15:         return (new int[1])[0];" + NEW_LINE +
                "II > 15: <Empty line>" + NEW_LINE +
                "I  > 16:     }" + NEW_LINE +
                "II > 16: <Empty line>" + NEW_LINE +
                "I  > 18:     class InnerOne { }" + NEW_LINE +
                "II > 18: <Empty line>" + NEW_LINE +
                "I  > 19:     static class NestedOne { }" + NEW_LINE +
                "II > 19: <Empty line>" + NEW_LINE +
                "I  > 21:     abstract static interface wierdOne<F extends java.lang.Object> { }" + NEW_LINE +
                "II > 21: <Empty line>" + NEW_LINE,
                new String(diffResult.toByteArray()));
    }

    @Test
    void testSimpleRepresentation() {
        var reflectionResult = new ByteArrayOutputStream();
        var reflectionResultStream = new PrintStream(reflectionResult);
        Reflector.printStructureToPrintStream(Simple.class, reflectionResultStream);
        assertEquals("public class SomeClass {" + NEW_LINE +
                "" + NEW_LINE +
                "" + NEW_LINE +
                "}"+ NEW_LINE,
                new String(reflectionResult.toByteArray()));
    }

    @Test
    void testAbstractOneRepresentation() {
        var reflectionResult = new ByteArrayOutputStream();
        var reflectionResultStream = new PrintStream(reflectionResult);
        Reflector.printStructureToPrintStream(AbstractOne.class, reflectionResultStream);
        assertEquals("public abstract class SomeClass {" + NEW_LINE +
                "" + NEW_LINE +
                "    public  int getInt() {" + NEW_LINE +
                "        return (new int[1])[0];" + NEW_LINE +
                "    }" + NEW_LINE +
                "    public abstract  void toAbstract();" + NEW_LINE +
                "" + NEW_LINE +
                "}" +  NEW_LINE, new String(reflectionResult.toByteArray()));
    }

    @Test
    void testDoubleNested() {
        //No Hook class expected
        var reflectionResult = new ByteArrayOutputStream();
        var reflectionResultStream = new PrintStream(reflectionResult);
        Reflector.printStructureToPrintStream(DoubleNested.class, reflectionResultStream);
        assertEquals("public class SomeClass {" + NEW_LINE +
                "" + NEW_LINE +
                "" + NEW_LINE +
                "    protected abstract class AbstractNested { }" + NEW_LINE +
                "    class Nested { }" + NEW_LINE +
                "}" +  NEW_LINE, new String(reflectionResult.toByteArray()));
    }

    @Test
    void testAllAreCompilable() throws IOException {
        Class<?>[] toTest = { AbstractOne.class, IndevTestClass.class, IndevTestClass2.class, Simple.class,
                              DoubleNested.class };
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        for (var clazz : toTest) {
            Reflector.printStructure(clazz, "SomeClass.java");
            assertEquals(0, compiler.run(null, null, null, new File("SomeClass.java").getPath()));
        }
    }
}