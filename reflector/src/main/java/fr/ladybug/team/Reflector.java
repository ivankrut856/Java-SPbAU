package fr.ladybug.team;

import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.stream.Collectors;

/** Class presents utils for partial source code restoration from Class instance */
public class Reflector {

    private static final String INDENT = "    ";
    private static int freshNumber = 0;

    /** Service only method, delivers next numbers */
    private static int getFreshNumber() {
        return freshNumber++;
    }

    /** Formats GenericDeclaration instance to fancy-looking compilable string */
    private static @NotNull String genericVariablesFormat(@NotNull GenericDeclaration genericDeclaration) {
        if (genericDeclaration.getTypeParameters().length == 0) {
            return "";
        }
        else {
            return Arrays.stream(genericDeclaration.getTypeParameters()).map(x -> x.getName() +
                    Arrays.stream(x.getBounds()).map(Type::getTypeName).collect(Collectors.joining(" & ", " extends ", "")))
                    .collect(Collectors.joining(" ", "<", ">"));
        }
    }

    /** Formats Field instace to fancy-looking compilable string */
    private static @NotNull String fieldRepresentation(@NotNull Field field) {
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

    /** Formats Method instance to fancy-looking compilable string */
    private static @NotNull String methodRepresentation(@NotNull Method method) {
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

    /** Format Class instance to fancy-looking compilable string */
    private static @NotNull String classRepresentation(@NotNull Class<?> clazz) {
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

    /**
     * Writes compilable Class instance's source code to the given file
     * @param someClass the Class instance whose source code is to be written to file
     * @param filename the filename of the file to which class's source code is to be written
     * @throws IOException in case of bad filename, when file cannot be properly open for writing
     */
    public static void printStructure(@NotNull Class<?> someClass, @NotNull String filename) throws IOException {
        try (FileOutputStream fileOutput = new FileOutputStream(filename); PrintStream out = new PrintStream(fileOutput)) {
            printStructureToPrintStream(someClass, out);
        }
    }

    /**
     * Writes compilable Class instance's source code to the given PrintStream
     * @param someClass the Class instance whose source code is to be written to PrintStream
     * @param out the PrintStream to which class's source code is to be written
     */
    public static void printStructureToPrintStream(@NotNull Class<?> someClass, @NotNull PrintStream out) {
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

    /**
     * Prints line-by-line differences between two classes to console
     * Not different parts of classes are to be found as longest common subsequence of two sets of lines
     * @param aClass the master class which is to be compared with slave one
     * @param bClass the slave class which is to be compared with master one
     */
    public static void diffClasses(@NotNull Class<?> aClass, @NotNull Class<?> bClass) {
        diffClassesToPrintStream(aClass, bClass, System.out);
    }

    /**
     * Writes line-by-line differences between two classes to the given PrintStream
     * Not different parts of classes are to be found as longest common subsequence of two sets of lines
     * @param aClass the master class which is to be compared with slave one
     * @param bClass the slave class which is to be compared with master one
     * @param out the PrintStream to which classes' differences are to be written
     */
    public static void diffClassesToPrintStream(@NotNull Class<?> aClass, @NotNull Class<?> bClass, @NotNull PrintStream out) {

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

    /** Finds longest commong subsequence in to lists of string
     * First list is considered as master list from which will be extracted answer lines numbers
     * Second list is just additional lines provider, no lines' number will be taken from one
     * @param aList the master list from which will be extracted answer lines' numbers
     * @param bList the list which is additional lines provider
     * @return list of indices (lines numbers) from master list which have been found in the second one
     */
    private static @NotNull ArrayList<Integer> longestCommonSequence(
            @NotNull ArrayList<String> aList,
            @NotNull ArrayList<String> bList) {
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