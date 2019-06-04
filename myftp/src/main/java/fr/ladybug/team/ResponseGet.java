package fr.ladybug.team;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.charset.Charset;

public class ResponseGet {

    private int fileSize;
    private byte[] fileContent;

    private boolean valid = true;
    private String errorMessage = null;

    private ResponseGet() {}

    public static ResponseGet fromBytes(byte[] response) {
        var stream = new DataInputStream(new ByteArrayInputStream(response));
        var instance = new ResponseGet();
        try {
            instance.fileSize = stream.readInt();
            instance.fileContent = stream.readNBytes(instance.fileSize);
            return instance;
        } catch (IOException e) {
            return errorResponse("Message corrupted");
        }
    }

    private static ResponseGet errorResponse(String errorMessage) {
        var instance = new ResponseGet();
        instance.valid = false;
        instance.errorMessage = errorMessage;
        return instance;
    }

    public byte[] getFileContent() {
        return fileContent;
    }

    public boolean isValid() {
        return valid;
    }
}
