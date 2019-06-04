package fr.ladybug.team;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static com.google.common.base.Preconditions.checkArgument;

public class ResponseList {

    private int directorySize;
    String[] filenames;
    boolean[] isDirectory;

    private ResponseList() {}

    public static ResponseList fromBytes(byte[] response) throws IOException {
        var stream = new DataInputStream(new ByteArrayInputStream(response));
        var instance = new ResponseList();
        try {
            instance.directorySize = stream.readInt();
            checkArgument(instance.directorySize >= 0);
            instance.filenames = new String[instance.directorySize];
            instance.isDirectory = new boolean[instance.directorySize];
            for (int i = 0; i < instance.directorySize; i++) {
                int stringSize = stream.readInt();
                String s = new String(stream.readNBytes(stringSize));
                instance.isDirectory[i] = stream.readNBytes(1)[0] == 1;
                instance.filenames[i] = s;
            }
            return instance;
        } catch (IOException | IllegalArgumentException e) {
            var exception = new IOException("Message format corrupted");
            exception.addSuppressed(e);
            throw exception;
        }
    }
}
