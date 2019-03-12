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
                result.append("return (new " + returnTypeName + "[1])[0];");
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
        if (clazz.isInterface()) {
        }
        else {
            result.append("class ");
        }
        result.append(clazz.getSimpleName());
        result.append(genericVariablesFormat(clazz));

        result.append(" { }");


        return result.toString();
    }

    public static void printStructure(Class<?> someClass, String filename) {
        try (FileOutputStream fileOutput = new FileOutputStream(filename); PrintStream out = new PrintStream(fileOutput)) {
            printStructureToPrintStream(someClass, out);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void printStructureToPrintStream(Class<?> someClass, PrintStream out) {
        freshNumber = 0;
        out.print("public class SomeClass");
        out.print(genericVariablesFormat(someClass));
        out.println(" {");

        Field[] fields = someClass.getDeclaredFields();
        for (var field : fields) {
            out.println(fieldRepresentation(field));
        }
        out.println();

        Method[] methods = someClass.getDeclaredMethods();
        for (var method : methods) {
            out.println(methodRepresentation(method));
        }
        out.println();

        Class<?>[] classes = someClass.getDeclaredClasses();
        for (var clazz : classes) {
            out.println(classRepresentation(clazz));
        }
        out.println("}");
    }

    public static void diffClasses(Class<?> aClass, Class<?> bClass) {
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

//        aList = new ArrayList<>(Arrays.asList("4"));
//        bList = new ArrayList<>(Arrays.asList("4"));
        ArrayList<Integer> indices = longestCommonSequence(aList, bList);

        int aPointer = 0;
        System.out.println("First class in relation to second:");
        for (int i = 0; i < aList.size(); i++) {
            if (aPointer < indices.size() && indices.get(aPointer) == i) {
                aPointer++;
                continue;
            }
            System.out.print("I  > ");
            System.out.print(i + ": ");
            System.out.println(aList.get(i));


            System.out.print("II > ");
            System.out.print(i + ": ");
            System.out.println(i < bList.size() ? bList.get(i) : "<Empty line>");
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

        //noinspection unchecked
//        ArrayList<Integer>[] result = (ArrayList<Integer>[])(new ArrayList[2]);
        var result = new ArrayList<Integer>();
//        result[0] = new ArrayList<>();
//        result[1] = new ArrayList<>();
        int aPointer = aSize;
        int bPointer = bSize;
        while (aPointer > 0 && bPointer > 0) {
            if (par[aPointer][bPointer] == 1) {
                result.add(aPointer - 1);
//                result[0].add(aPointer - 1);
//                result[1].add(bPointer - 1);
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
//        Collections.reverse(result[0]);
//        Collections.reverse(result[1]);

        return result;
    }

}
