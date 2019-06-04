package fr.ladybug.team;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.charset.Charset;

public class ResponseList {

    private int directorySize;
    String[] filenames;

    private ResponseList() {}

    public static ResponseList fromBytes(byte[] response) throws IOException {
        var stream = new DataInputStream(new ByteArrayInputStream(response));
        var instance = new ResponseList();
        try {
            instance.directorySize = stream.readInt();
            instance.filenames = new String[instance.directorySize];
            for (int i = 0; i < instance.directorySize; i++) {
                int stringSize = stream.readInt();
                String s = new String(stream.readNBytes(stringSize), Charset.forName("UTF-8"));
                instance.filenames[i] = s;
            }
            return instance;
        } catch (IOException e) {
            var exception = new IOException("Message format corrupted");
            exception.addSuppressed(e);
            throw exception;
        }
    }
}
