package fr.ladybug.team;

import fr.ladybug.team.client.Client;
import fr.ladybug.team.client.Query;
import fr.ladybug.team.client.ResponseGet;
import fr.ladybug.team.client.ResponseList;
import fr.ladybug.team.server.Server;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class ServerClientInteractionTest {
    private static final int PORT = 8179;
    private static final String ADDRESS = "localhost";
    private static Server server;

    @BeforeAll
    static void setUp() throws IOException {
        server = new Server(ADDRESS, PORT);
    }

    @Test
    void testSimpleGet() throws IOException {
        var client = new Client(ADDRESS, PORT);
        var query = client.makeQuery(new Query(Query.QueryType.GET, "src/test/resources/file"));
        var result = ResponseGet.fromBytes(query);
        assertTrue(result.isValid());
        assertEquals("File content", new String(result.getFileContent()));
    }

    @Test
    void testSimpleList() throws IOException {
        var client = new Client(ADDRESS, PORT);
        var query = client.makeQuery(new Query(Query.QueryType.LIST, "src/test/resources/dir"));
        var result = ResponseList.fromBytes(query);
        assertTrue(result.isValid());
        var files = result.toFileViews();
        assertEquals(1, files.size());
        assertEquals("file", files.get(0).getFileName());
        assertFalse(files.get(0).isDirectory());
    }

    @Test
    void testBadGet() throws IOException {
        var client = new Client(ADDRESS, PORT);
        var query = client.makeQuery(new Query(Query.QueryType.GET, "src/test/resources/notFile"));
        var result = ResponseGet.fromBytes(query);
        assertFalse(result.isValid());
        assertEquals("File does not exist.", result.getError());
    }

    @Test
    void testBadList() throws IOException {
        var client = new Client(ADDRESS, PORT);
        var query = client.makeQuery(new Query(Query.QueryType.LIST, "src/test/resources/file"));
        var result = ResponseList.fromBytes(query);
        assertFalse(result.isValid());
        assertEquals("Directory does not exist.", result.getError());
    }

    @Test
    void testSeveralQueries() throws IOException {
        var client = new Client(ADDRESS, PORT);
        var firstQuery = client.makeQuery(new Query(Query.QueryType.LIST, "src/test/resources/file"));
        var firstResult = ResponseList.fromBytes(firstQuery);
        assertFalse(firstResult.isValid());
        assertEquals("Directory does not exist.", firstResult.getError());

        var secondQuery = client.makeQuery(new Query(Query.QueryType.GET, "src/test/resources/dir/file"));
        var secondResult = ResponseGet.fromBytes(secondQuery);
        assertTrue(secondResult.isValid());
        assertEquals("This is a file", new String(secondResult.getFileContent()));
    }

    @Test
    void testSeveralClients() throws InterruptedException {
        var threads = new Thread[10];
        for (int i = 0; i < 10; i++) {
            threads[i] = new Thread(() -> {
                try {
                    var client = new Client(ADDRESS, PORT);
                    var query = client.makeQuery(new Query(Query.QueryType.GET, "src/test/resources/notFile"));
                    var result = ResponseGet.fromBytes(query);
                    assertFalse(result.isValid());
                    assertEquals("File does not exist.", result.getError());
                } catch (IOException ignored) {
                }
            });
        }
        for (int i = 0; i < 10; i++) {
            threads[i].start();
        }
        for (int i = 0; i < 10; i++) {
            threads[i].join();
        }
    }


    @AfterAll
    static void tearDown() throws InterruptedException {
        server.shutdown();
    }
}