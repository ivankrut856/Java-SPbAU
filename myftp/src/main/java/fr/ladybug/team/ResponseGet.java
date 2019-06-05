package fr.ladybug.team;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

import static com.google.common.base.Preconditions.checkArgument;

/** Class for storing and processing the server's response to a get query. */
public class ResponseGet {
    private int fileSize;
    private @Nullable byte[] fileContent;

    private boolean valid = true;
    private @Nullable String errorMessage = null;

    private ResponseGet() {}

    /** Constructs a ResponseGet from the server's response. */
    public static @NotNull ResponseGet fromBytes(@NotNull byte[] response) {
        var stream = new DataInputStream(new ByteArrayInputStream(response));
        var responseGet = new ResponseGet();
        try {
            responseGet.fileSize = stream.readInt();
            if (responseGet.fileSize < -1) {
                return errorResponse("Query execution failed.");
            } else if (responseGet.fileSize == -1) {
                return errorResponse("Directory does not exist.");
            }
            responseGet.fileContent = stream.readNBytes(responseGet.fileSize);
            return responseGet;
        } catch (IOException e) {
            return errorResponse("Response was corrupted.");
        }
    }

    /** Constructs an error response with the given message. */
    private static @NotNull ResponseGet errorResponse(@NotNull String errorMessage) {
        var instance = new ResponseGet();
        instance.valid = false;
        instance.errorMessage = errorMessage;
        return instance;
    }

    public @NotNull byte[] getFileContent() {
        checkArgument(fileContent != null); // should only be called from valid ResponseGets.
        return fileContent;
    }

    /** Returns true if the response is not erroneous, false otherwise. */
    public boolean isValid() {
        return valid;
    }

    /** Returns the error string associated with the response, or null if the response was correct. */
    public @NotNull String getError() {
        checkArgument(errorMessage != null); // should only be called from invalid ResponseGets.
        return errorMessage;
    }
}
