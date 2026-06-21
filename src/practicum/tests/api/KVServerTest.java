package practicum.tests.api;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import practicum.api.KVServer;
import practicum.api.KVTaskClient;

import java.io.IOException;
import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class KVServerTest {
    private static KVServer server;
    private KVTaskClient client;

    @BeforeAll
    public static void startServer() throws IOException {
        server = new KVServer();
        server.start();
    }

    @AfterAll
    public static void stopServer() {
        server.stop();
    }

    @BeforeEach
    public void setUp() throws Exception {
        client = new KVTaskClient(new URI("http://localhost:" + KVServer.PORT + "/"));
        client.clear();
    }

    @Test
    public void shouldSaveAndLoadValue() throws IOException, InterruptedException {
        client.put("1", "{\"name\":\"Task\"}");

        assertEquals("{\"name\":\"Task\"}", client.load("1"));
    }

    @Test
    public void shouldReturnNullForUnknownKey() throws IOException, InterruptedException {
        assertEquals("null", client.load("does-not-exist"));
    }

    @Test
    public void shouldClearStoredValues() throws IOException, InterruptedException {
        client.put("1", "value");
        client.clear();

        assertEquals("null", client.load("1"));
    }

    @Test
    public void shouldResetIdsKeyAfterClear() throws IOException, InterruptedException {
        client.put("ids", "1_practicum.model.Task |");
        client.clear();

        assertEquals("|", client.load("ids"));
    }
}
