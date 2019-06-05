package fr.ladybug.team;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

import java.util.Arrays;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;

/** Class for storing and processing the server's response to a list query. */
public class ResponseList {
    private int directorySize;
    private @Nullable FileView[] fileViews;

    private boolean valid = true;
    private @Nullable String errorMessage;

    private ResponseList() {}

    /** Constructs a ResponseList from the server's response. */
    public static @NotNull ResponseList fromBytes(@NotNull byte[] response) {
        var stream = new DataInputStream(new ByteArrayInputStream(response));
        var responseList = new ResponseList();
        try {
            responseList.directorySize = stream.readInt();
            if (responseList.directorySize < -1) {
                return errorResponse("Query execution failed.");
            } else if (responseList.directorySize == -1) {
                return errorResponse("Directory does not exist.");
            }
            responseList.fileViews = new FileView[responseList.directorySize];
            for (int i = 0; i < responseList.directorySize; i++) {
                int stringSize = stream.readInt();
                String filename = new String(stream.readNBytes(stringSize));
                boolean isDirectory = stream.readNBytes(1)[0] == 1;
                responseList.fileViews[i] = new FileView(filename, isDirectory);
            }
            return responseList;
        } catch (IOException e) {
            return errorResponse("Response was corrupted.");
        }
    }

    /** Constructs an error response with the given message. */
    private static @NotNull ResponseList errorResponse(@NotNull String errorMessage) {
        var errorResponse = new ResponseList();
        errorResponse.valid = false;
        errorResponse.errorMessage = errorMessage;
        return errorResponse;
    }

    /** Returns a list of the response's FileViews. */
    public @NotNull List<FileView> toFileViews() {
        checkArgument(fileViews != null); // should only be called from valid ResponseLists.
        return Arrays.asList(fileViews);
    }

    /** Returns true if the response is not erroneous, false otherwise. */
    public boolean isValid() {
        return valid;
    }

    /** Returns the error string associated with the response, or null if the response was correct. */
    public @NotNull String getError() {
        checkArgument(errorMessage != null); // should only be called from invalid ResponseLists.
        return errorMessage;
    }
}
