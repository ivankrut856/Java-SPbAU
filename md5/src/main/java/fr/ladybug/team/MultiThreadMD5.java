package fr.ladybug.team;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

public class MultiThreadMD5 implements MD5Evaluator {
    @Override
    public String evaluate(String filepath) throws IOException, NoSuchAlgorithmException {
        return new ForkJoinPool().invoke(new MD5Task(filepath));
    }


    private static class MD5Task extends RecursiveTask<String> {
        private final String filepath;

        private MD5Task(String filepath) {
            this.filepath = filepath;
        }

        @Override
        protected String compute() {
            File currentFile = new File(filepath);
            if (!currentFile.isDirectory()) {
                String digest = null;
                try (FileInputStream inputStream = new FileInputStream(currentFile);
                     DigestInputStream digestInputStream = new DigestInputStream(inputStream, MessageDigest.getInstance("MD5"))) {

                    digestInputStream.on(true);
                    int status;
                    while ((status = digestInputStream.read()) != -1);
                    digestInputStream.close();
                    digest = MD5.getHexStringFromMD5(digestInputStream.getMessageDigest());
                } catch (NoSuchAlgorithmException e) {
                    throw new RuntimeException("No MD5 hash algorithm");
                } catch (FileNotFoundException e) {
                    throw new RuntimeException("File not found. Unchecked");
                } catch (IOException e) {
                    throw new RuntimeException("Cannot read file properly. Unchecked");
                }
                return digest;
            }

            File[] files = currentFile.listFiles();
            if (files == null) {
                throw new RuntimeException("Cannot read file properly. Unchecked");
            }

            List<MD5Task> tasks = new ArrayList<>();
            for (File file : files) {
                MD5Task task = new MD5Task(file.getPath());
                task.fork();
                tasks.add(task);
            }

            StringBuilder dataBuilder = new StringBuilder();
            for (var task : tasks) {
                dataBuilder.append(task.join());
            }
            String data = dataBuilder.toString();

            MessageDigest digestObject = null;
            try {
                digestObject = MessageDigest.getInstance("MD5");
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException("No MD5 hash algorithm");
            }

            digestObject.update(data.getBytes(StandardCharsets.UTF_8));
            return MD5.getHexStringFromMD5(digestObject);
        }
    }
}
