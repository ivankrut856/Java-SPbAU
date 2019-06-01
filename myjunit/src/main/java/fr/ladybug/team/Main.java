package fr.ladybug.team;

import fr.ladybug.team.annotations.*;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        Class<?> testClass = Main.class;

        Tester tester = null;
        try {
            tester = Tester.fromClass(testClass);
        } catch (TestException e) {
            System.err.println("Cannot parse test class correctly");
            return;
        }

        List<Tester.TestResult> results = null;
        try {
            results = tester.test();
        } catch (TestException e) {
            System.err.println("An exception occurred");
            e.getSuppressed()[0].printStackTrace(System.err);
            return;
        }

        for (var result: results) {
            System.out.println(String.format("Test status: %s\nComment: %s", result.getState(), result.getMessage()));
        }
    }
}
