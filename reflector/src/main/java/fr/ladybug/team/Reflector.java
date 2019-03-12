package fr.ladybug.team;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.stream.Collectors;

public class Reflector {

    static final String INDENT = "    ";

    static String fieldRepresentation(Field field) {
        var result = new StringBuilder();
        int modifiers = field.getModifiers();
        if (modifiers != 0) {
            result.append(Modifier.toString(modifiers));
            result.append(" ");
        }
        result.append(field.getGenericType().getTypeName());
        result.append(" ");
        result.append(field.getName());

        result.append(";");
        return result.toString();
    }

    static int freshNumber = 0;
    static int getFreshNumber() {
        return freshNumber++;
    }

    static String methodRepresentation(Method method) {
        var result = new StringBuilder();
        int modifiers = method.getModifiers();
        if (modifiers != 0) {
            result.append(Modifier.toString(modifiers));
            result.append(" ");
        }

        if (method.getTypeParameters().length > 0) {
            result.append(
                    Arrays.stream(method.getTypeParameters()).map(x -> x.getName() +
                            Arrays.stream(x.getBounds()).map(Type::getTypeName).collect(Collectors.joining("", " extends ", "")))
                            .collect(Collectors.joining(" ", "<", ">"))
            );
            result.append(" ");
        }

        result.append(method.getGenericReturnType().getTypeName());
        result.append(" ");
        result.append(method.getName());

        result.append(Arrays.stream(method.getGenericParameterTypes()).map(x -> x.getTypeName() + " v" + getFreshNumber()).collect(Collectors.joining(", ", "(", ")")));
        result.append(" {\n");
        result.append(INDENT);
        result.append(INDENT);

        if (!method.getReturnType().isPrimitive()) {
            result.append("return null;");
        }
        else {
            String returnTypeName = method.getGenericReturnType().getTypeName();
            if (returnTypeName.equals("void")) {
                result.append("return;");
            }
            else {
                result.append("return (new " + returnTypeName + "[1])[0];");
            }
        }

        result.append("\n");
        result.append(INDENT);
        result.append("}");
        return result.toString();
    }

    static void printStructure(Class<?> someClass) {
        freshNumber = 0;

        String filename = "Test.java";

        try (FileOutputStream fileOutput = new FileOutputStream(filename); PrintStream out = new PrintStream(fileOutput)) {

            out.print("public class SomeClass");
            if (someClass.getTypeParameters().length > 0) {
                    out.print(Arrays.stream(someClass.getTypeParameters()).
                            map(param -> param.getName() +
                                    Arrays.stream(param.getBounds())
                                            .map(Type::getTypeName)
                                            .collect(Collectors.joining(", ", " extends ", "")))
                            .collect(Collectors.joining(" ", "<", ">")) +
                    " {");
            }
            out.println();

            Field[] fields = someClass.getDeclaredFields();
            for (var field : fields) {
                out.print(INDENT);
                out.println(fieldRepresentation(field));
            }
            out.println();

            Method[] methods = someClass.getDeclaredMethods();
            for (var method : methods) {
                out.print(INDENT);
                out.println(methodRepresentation(method));
            }





            out.println("}");

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    static void diffClasses(Class<?> someClass) {

    }
}
