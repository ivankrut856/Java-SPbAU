package fr.ladybug.team;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class Query {
    private int taskName;
    private String[] args;

    public Query(int taskName, String[] args) {
        this.taskName = taskName;
        this.args = args;
    }

    public int getTaskName() {
        return taskName;
    }

    public String[] getArgs() {
        return args;
    }

    public void goTo(OutputStream outputStream) throws IOException {
        int packageSize = 0;

        packageSize += Integer.BYTES;
        for (var arg : args) {
            var bytes = arg.getBytes(StandardCharsets.UTF_8);
            packageSize += bytes.length + Short.BYTES;
        }

        var stream = new DataOutputStream(outputStream);
        stream.writeInt(packageSize);
        stream.writeInt(taskName);
        for (var arg : args) {
            var bytes = arg.getBytes(StandardCharsets.UTF_8);
            stream.writeInt(bytes.length);
            stream.write(bytes, 0, bytes.length);
        }
        stream.flush();
    }
}