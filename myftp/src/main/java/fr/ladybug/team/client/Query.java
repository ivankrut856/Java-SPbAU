package fr.ladybug.team.client;

import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.logging.Logger;

/** Representation of the query that the client sends to the server. */
public class Query {
    private @NotNull QueryType queryType;
    private @NotNull String queryBody;

    /** Constructor that creates Query with given type and text. */
    public Query(@NotNull QueryType queryType, @NotNull String queryBody) {
        this.queryType = queryType;
        this.queryBody = queryBody;
    }

    /** Getter for queryType field. */
    public @NotNull QueryType getQueryType() {
        return queryType;
    }

    /** Getter for queryBody field. */
    public @NotNull String getQueryBody() {
        return queryBody;
    }

    /** Writes the query to the given OutputStream. */
    public void printToStream(@NotNull OutputStream outputStream) throws IOException {
        int packageSize = 0;

        packageSize += Integer.BYTES;
        var bytes = queryBody.getBytes();
        packageSize += bytes.length;

        var stream = new DataOutputStream(outputStream);
        stream.writeInt(packageSize);
        stream.writeInt(queryType.value());
        stream.write(bytes, 0, bytes.length);
        stream.flush();
        Logger.getAnonymousLogger().info("Wrote query " + queryType + " " + queryBody);
    }

    /** Enum that stores the types of possible queries for the client to make. */
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