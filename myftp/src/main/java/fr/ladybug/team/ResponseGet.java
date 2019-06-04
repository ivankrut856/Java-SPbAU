package fr.ladybug.team;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.charset.Charset;

public class ResponseGet {

    private int fileSize;
    private byte[] fileContent;

    private ResponseGet() {}

    public static ResponseGet fromBytes(byte[] response) throws IOException {
        var stream = new DataInputStream(new ByteArrayInputStream(response));
        var instance = new ResponseGet();
        try {
            instance.fileSize = stream.readInt();
            instance.fileContent = stream.readNBytes(instance.fileSize);
            return instance;
        } catch (IOException e) {
            var exception = new IOException("Message format corrupted");
            exception.addSuppressed(e);
            throw exception;
        }
    }

    public byte[] getFileContent() {
        return fileContent;
    }
}
