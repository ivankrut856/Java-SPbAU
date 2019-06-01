package fr.ladybug.team;

import fr.ladybug.team.annotations.*;

import java.util.List;

/** Console application Main class */
public class Main {

    /** Console application entry point */
    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Please provide only filepath to test class file");
            return;
        }
        Class<?> testClass = null;
        try {
            testClass = Class.forName(args[0]);
        } catch (ClassNotFoundException e) {
            System.err.println("No such class");
            return;
        }

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

        if (results.size() == 0) {
            System.out.println("Class doesn't contain any @Test methods");
        }
        for (var result: results) {
            String runningTime = result.getRunningTime() != 0 ? String.format("%.3fs", result.getRunningTime() / 1000) : "was not correctly defined";
            System.out.println(String.format("Test №%d status: %s\nComment: %s\nRunning time: %s",
                    result.getTestNumber(), result.getState(), result.getMessage(), runningTime));

        }
    }
}

