package fr.ladybug.team;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;

public class ResponseList {

    private int directorySize;
    FileView[] fileViews;

    private ResponseList() {}

    public static ResponseList fromBytes(byte[] response) throws IOException {
        var stream = new DataInputStream(new ByteArrayInputStream(response));
        var instance = new ResponseList();
        try {
            instance.directorySize = stream.readInt();
            checkArgument(instance.directorySize >= 0);
            instance.fileViews = new FileView[instance.directorySize];
            for (int i = 0; i < instance.directorySize; i++) {
                int stringSize = stream.readInt();

                String filename = new String(stream.readNBytes(stringSize), StandardCharsets.UTF_8);
                boolean isDirectory = stream.readNBytes(1)[0] == 1;
                instance.fileViews[i] = new FileView(filename, isDirectory);
            }
            return instance;
        } catch (IOException | IllegalArgumentException e) {
            var exception = new IOException("Message format corrupted");
            exception.addSuppressed(e);
            throw exception;
        }
    }

    public List<FileView> toFileViews() {
        return Arrays.asList(fileViews);
    }
}
