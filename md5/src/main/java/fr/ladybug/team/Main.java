package fr.ladybug.team;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

public class Main {
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Not enough arguments");
            return;
        }
        String path = args[0];

        var beforeAll = System.nanoTime();

        String hash1 = null;
        try {
            hash1 = MD5.getEvaluator().evaluate(path);
        } catch (IOException e) {
            System.out.println("Cannot read the files properly");
            return;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("No hash for MD5");
        }
        var afterFirst = System.nanoTime();

        String hash2 = null;
        try {
            hash2 = MD5.getEvaluatorMultiThread().evaluate(path);
        } catch (IOException e) {
            System.out.println("Cannot read the files properly");
            return;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("No hash for MD5");
        }
        var afterSecond = System.nanoTime();

        System.out.println(String.format("%f ms. for single thread version MD5", (double)(afterFirst - beforeAll) / 1000000));
        System.out.println(String.format("%f ms. for multi thread version MD5", (double)(afterSecond - afterFirst) / 1000000));

        System.out.println(String.format("Single thread version says:\n %s", hash1));
        System.out.println(String.format("Multi thread version says:\n %s", hash2));
    }
}
