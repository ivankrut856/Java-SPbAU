package fr.ladybug.team;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class Query {

    private int taskName;
    private String message;

    public Query(int taskName, String message) {
        this.taskName = taskName;
        this.message = message;
    }

    public int getTaskName() {
        return taskName;
    }

    public String getMessage() {
        return message;
    }

    public void goTo(OutputStream outputStream) throws IOException {
        int packageSize = 0;

        packageSize += Integer.BYTES;
        var bytes = message.getBytes();
        packageSize += bytes.length;

        var stream = new DataOutputStream(outputStream);
        stream.writeInt(packageSize);
        stream.writeInt(taskName);
        stream.write(bytes, 0, bytes.length);
        stream.flush();
        System.out.println("sent something");
    }
}