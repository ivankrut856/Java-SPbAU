package fr.ladybug.team;

import org.junit.jupiter.api.Test;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Paths;
import java.sql.Ref;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

class ReflectorTest {

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
        assertEquals("First class in relation to second:\n", new String(diffResultBytes.toByteArray()));
    }

    @Test
    void testIndevDifferences() {
        var diffResult = new ByteArrayOutputStream();
        var diffResultStream = new PrintStream(diffResult);
        Reflector.diffClassesToPrintStream(IndevTestClass.class, IndevTestClass2.class, diffResultStream);
        assertEquals("First class in relation to second:\n" +
                "I  > 1:     private static java.lang.String data;\n" +
                "II > 1:     public double name;\n" +
                "I  > 2:     public int name;\n" +
                "II > 2: \n" +
                "I  > 3:     protected T what;\n" +
                "II > 3: \n" +
                "I  > 5:     public static  void Main(java.lang.String[] v0, java.util.List<? super java.lang.String> v1) {\n" +
                "II > 5: }\n" +
                "I  > 6:         return;\n" +
                "II > 6: <Empty line>\n" +
                "I  > 7:     }\n" +
                "II > 7: <Empty line>\n" +
                "I  > 8:     public static <E extends java.lang.Object> java.lang.String anotherMethod(E v2) {\n" +
                "II > 8: <Empty line>\n" +
                "I  > 9:         return null;\n" +
                "II > 9: <Empty line>\n" +
                "I  > 10:     }\n" +
                "II > 10: <Empty line>\n" +
                "I  > 11:       void justMethod(int v3, java.lang.String v4, T v5) {\n" +
                "II > 11: <Empty line>\n" +
                "I  > 12:         return;\n" +
                "II > 12: <Empty line>\n" +
                "I  > 13:     }\n" +
                "II > 13: <Empty line>\n" +
                "I  > 14:     public <T extends java.lang.Object> int whatStuffAreYou(T v6) {\n" +
                "II > 14: <Empty line>\n" +
                "I  > 15:         return (new int[1])[0];\n" +
                "II > 15: <Empty line>\n" +
                "I  > 16:     }\n" +
                "II > 16: <Empty line>\n" +
                "I  > 18:     class InnerOne { }\n" +
                "II > 18: <Empty line>\n" +
                "I  > 19:     static class NestedOne { }\n" +
                "II > 19: <Empty line>\n" +
                "I  > 21:     abstract static interface wierdOne<F extends java.lang.Object> { }\n" +
                "II > 21: <Empty line>\n",
                new String(diffResult.toByteArray()));
    }

    @Test
    void testSimpleRepresentation() {
        var reflectionResult = new ByteArrayOutputStream();
        var reflectionResultStream = new PrintStream(reflectionResult);
        Reflector.printStructureToPrintStream(Simple.class, reflectionResultStream);
        assertEquals("public class SomeClass {\n" +
                "\n" +
                "\n" +
                "}\n",
                new String(reflectionResult.toByteArray()));
    }
}