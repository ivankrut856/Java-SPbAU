package fr.ladybug.team;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class Query {

    private QueryType taskName;
    private String message;

    public Query(QueryType taskName, String message) {
        this.taskName = taskName;
        this.message = message;
    }

    public QueryType getTaskName() {
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
        stream.writeInt(taskName.value());
        stream.write(bytes, 0, bytes.length);
        stream.flush();
        System.out.println("sent something");
    }

    public enum QueryType {
        LIST(1),
        GET(2);

        private int value;
        QueryType(int value) {
            this.value = value;
        }

        public int value() {
            return value;
        }
    }
}