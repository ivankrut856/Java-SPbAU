package fr.ladybug.team;

import java.io.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.stream.Collectors;

public class Reflector {

    private static final String INDENT = "    ";
    private static int freshNumber = 0;

    private static int getFreshNumber() {
        return freshNumber++;
    }

    private static String genericVariablesFormat(GenericDeclaration genericDeclaration) {
        if (genericDeclaration.getTypeParameters().length == 0) {
            return "";
        }
        else {
            return Arrays.stream(genericDeclaration.getTypeParameters()).map(x -> x.getName() +
                    Arrays.stream(x.getBounds()).map(Type::getTypeName).collect(Collectors.joining(" & ", " extends ", "")))
                    .collect(Collectors.joining(" ", "<", ">"));
        }
    }

    private static String fieldRepresentation(Field field) {
        var result = new StringBuilder();
        result.append(INDENT);
        result.append(field.getModifiers() == 0 ? "" : Modifier.toString(field.getModifiers()));
        result.append(" ");
        result.append(field.getGenericType().getTypeName());
        result.append(" ");
        result.append(field.getName());

        result.append(";");
        return result.toString();
    }

    private static String methodRepresentation(Method method) {
        var result = new StringBuilder();
        result.append(INDENT);
        result.append(method.getModifiers() == 0 ? "" : Modifier.toString(method.getModifiers()));
        result.append(" ");
        result.append(genericVariablesFormat(method));
        result.append(" ");

        result.append(method.getGenericReturnType().getTypeName());
        result.append(" ");
        result.append(method.getName());

        result.append(Arrays.stream(method.getGenericParameterTypes()).map(x -> x.getTypeName() + " v" + getFreshNumber()).collect(Collectors.joining(", ", "(", ")")));
        result.append(" {\n");
        result.append(INDENT);
        result.append(INDENT);

        if (!method.getReturnType().isPrimitive()) {
            result.append("return null;");
        } else {
            String returnTypeName = method.getGenericReturnType().getTypeName();
            if (returnTypeName.equals("void")) {
                result.append("return;");
            } else {
                result.append("return (new ");
                result.append(returnTypeName);
                result.append("[1])[0];");
            }
        }

        result.append("\n");
        result.append(INDENT);
        result.append("}");
        return result.toString();
    }

    private static String classRepresentation(Class<?> clazz) {
        var result = new StringBuilder();
        result.append(INDENT);
        result.append(clazz.getModifiers() == 0 ? "" : Modifier.toString(clazz.getModifiers()) + " ");
        if (!clazz.isInterface()) {
            result.append("class ");
        }
        result.append(clazz.getSimpleName());
        result.append(genericVariablesFormat(clazz));

        result.append(" { }");


        return result.toString();
    }

    public static void printStructure(Class<?> someClass, String filename) throws IOException {
        try (FileOutputStream fileOutput = new FileOutputStream(filename); PrintStream out = new PrintStream(fileOutput)) {
            printStructureToPrintStream(someClass, out);
        }
    }

    public static void printStructureToPrintStream(Class<?> someClass, PrintStream out) {
        freshNumber = 0;

//        out.println("package pkg;");
//        out.println();

        out.print("public class SomeClass");
        out.print(genericVariablesFormat(someClass));
        out.println(" {");

        var fields = new ArrayList<Field>(Arrays.asList(someClass.getDeclaredFields()));
        fields.sort(Comparator.comparing(Field::getName, String::compareTo));
        for (var field : fields) {
            out.println(fieldRepresentation(field));
        }
        out.println();

        var methods = new ArrayList<Method>(Arrays.asList(someClass.getDeclaredMethods()));
        methods.sort(Comparator.comparing(Method::getName, String::compareTo));
        for (var method : methods) {
            out.println(methodRepresentation(method));
        }
        out.println();

        var classes = new ArrayList<Class<?>>(Arrays.asList(someClass.getDeclaredClasses()));
        classes.sort(Comparator.comparing(Class::getName, String::compareTo));
        for (var clazz : classes) {
            out.println(classRepresentation(clazz));
        }
        out.println("}");
    }

    public static void diffClasses(Class<?> aClass, Class<?> bClass) {
        diffClassesToPrintStream(aClass, bClass, System.out);
    }

    public static void diffClassesToPrintStream(Class<?> aClass, Class<?> bClass, PrintStream out) {

        var aStream = new ByteArrayOutputStream();
        var aPrintStream = new PrintStream(aStream);

        var bStream = new ByteArrayOutputStream();
        var bPrintStream = new PrintStream(bStream);

        printStructureToPrintStream(aClass, aPrintStream);
        aPrintStream.close();

        printStructureToPrintStream(bClass, bPrintStream);
        bPrintStream.close();

        Scanner aScanner = new Scanner(new ByteArrayInputStream(aStream.toByteArray()));
        Scanner bScanner = new Scanner(new ByteArrayInputStream(bStream.toByteArray()));

        var aList = new ArrayList<String>();
        while (aScanner.hasNextLine()) {
            aList.add(aScanner.nextLine());
        }

        var bList = new ArrayList<String>();
        while (bScanner.hasNextLine()) {
            bList.add(bScanner.nextLine());
        }

        ArrayList<Integer> indices = longestCommonSequence(aList, bList);

        int aPointer = 0;
        out.println("First class in relation to second:");
        for (int i = 0; i < aList.size(); i++) {
            if (aPointer < indices.size() && indices.get(aPointer) == i) {
                aPointer++;
                continue;
            }
            out.print("I  > ");
            out.print(i + ": ");
            out.println(aList.get(i));


            out.print("II > ");
            out.print(i + ": ");
            out.println(i < bList.size() ? bList.get(i) : "<Empty line>");
        }
    }

    private static ArrayList<Integer> longestCommonSequence(ArrayList<String> aList, ArrayList<String> bList) {
        int aSize = aList.size();
        int bSize = bList.size();
        int[][] dp = new int[aSize + 1][bSize + 1];
        int[][] par = new int[aSize + 1][bSize + 1];
        for (int i = 1; i <= aSize; i++) {
            for (int j = 1; j <= bSize; j++) {
                if (aList.get(i - 1) != null
                        && bList.get(j - 1) != null
                        && aList.get(i - 1).equals(bList.get(j - 1))) {
                    dp[i][j] = dp[i - 1][j - 1] + 1;
                    par[i][j] = 1;
                }
                int neighbourhoodMax = Math.max(dp[i - 1][j], dp[i][j - 1]);
                if (neighbourhoodMax > dp[i][j]) {
                    dp[i][j] = neighbourhoodMax;
                }
            }
        }

        var result = new ArrayList<Integer>();
        int aPointer = aSize;
        int bPointer = bSize;
        while (aPointer > 0 && bPointer > 0) {
            if (par[aPointer][bPointer] == 1) {
                result.add(aPointer - 1);
                aPointer--;
                bPointer--;
            }
            else if (dp[aPointer - 1][bPointer] > dp[aPointer][bPointer - 1]) {
                aPointer--;
            }
            else {
                bPointer--;
            }
        }
        Collections.reverse(result);

        return result;
    }

}
