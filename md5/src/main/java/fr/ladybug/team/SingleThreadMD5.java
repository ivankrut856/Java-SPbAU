package fr.ladybug.team;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class SingleThreadMD5 implements MD5Evaluator {
    @Override
    /** {@inheritDoc} */
    public String evaluate(String filepath) throws IOException, NoSuchAlgorithmException {
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
            }
            return digest;
        }

        File[] files = currentFile.listFiles();
        StringBuilder dataBuilder = new StringBuilder();
        for (File file : files) {
            dataBuilder.append(evaluate(file.getPath()));
        }
        String data = dataBuilder.toString();
        var digestObject = MessageDigest.getInstance("MD5");
        digestObject.update(data.getBytes(StandardCharsets.UTF_8));
        return MD5.getHexStringFromMD5(digestObject);
    }
}
