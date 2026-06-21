package practicum.api;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;

public class KVServer {
    public static int PORT = 8078;
    public static final InetSocketAddress serverAddress = new InetSocketAddress("localhost", PORT);
    public final String apiToken;
    private final HttpServer server;
    private final Map<String, String> data = new HashMap<>();

    public KVServer() throws IOException {
        data.put("ids", "|");

        apiToken = generateApiToken();
        server = HttpServer.create(serverAddress, 0);
        server.createContext("/register", this::register);
        server.createContext("/save", this::save);
        server.createContext("/load", this::load);
        server.createContext("/clear", this::clear);
    }

    private void load(HttpExchange h) throws IOException {
        String key = h.getRequestURI().getPath().substring("/load/".length());

        if (!hasAuth(h)) {
            //System.out.print("Запрос неавторизован, нужен параметр в query API_TOKEN со значением апи-ключа");
            h.sendResponseHeaders(403, 0);
            return;
        }

        if (!data.containsKey(key)) {
            //System.out.println("Ключ " + key + " не представлен");
            sendText(h, "null");
            return;
        }

        String value = data.get(key);
        //System.out.println("Значение " + value + " соотвествует ключу " + key);

        sendText(h, value);
    }

    private void save(HttpExchange h) throws IOException {
        try (h) {
            //System.out.println("\n/save");
            if (!hasAuth(h)) {
                //System.out.println("Запрос неавторизован, нужен параметр в query API_TOKEN со значением апи-ключа");
                h.sendResponseHeaders(403, 0);
                return;
            }
            if ("POST".equals(h.getRequestMethod())) {
                String key = h.getRequestURI().getPath().substring("/save/".length());
                if (key.isEmpty()) {
                    //System.out.println("Key для сохранения пустой. key указывается в пути: /save/{key}");
                    h.sendResponseHeaders(400, 0);
                    return;
                }
                String value = readText(h);
                if (value.isEmpty()) {
                    //System.out.println("Value для сохранения пустой. value указывается в теле запроса");
                    h.sendResponseHeaders(400, 0);
                    return;
                }
                data.put(key, value);
                //System.out.println("Значение для ключа " + key + " успешно обновлено!");
                h.sendResponseHeaders(200, 0);
            } else {
                //System.out.println("/save ждёт POST-запрос, а получил: " + h.getRequestMethod());
                h.sendResponseHeaders(405, 0);
            }
        }
    }

    private void clear(HttpExchange h) throws IOException {
        try (h) {
            if (!hasAuth(h)) {
                h.sendResponseHeaders(403, 0);
                return;
            }

            if ("DELETE".equals(h.getRequestMethod())) {
                data.clear();
                data.put("ids", "|");
                h.sendResponseHeaders(200, 0);
            } else {
                h.sendResponseHeaders(405, 0);
            }
        }
    }

    private void register(HttpExchange h) throws IOException {
        try (h) {
            //System.out.println("\n/register");
            if ("GET".equals(h.getRequestMethod())) {
                sendText(h, apiToken);
            } else {
                //System.out.println("/register ждёт GET-запрос, а получил " + h.getRequestMethod());
                h.sendResponseHeaders(405, 0);
            }
        }
    }

    public void start() {
        System.out.println("Запускаем сервер на порту " + PORT);
        System.out.println("Открой в браузере http://localhost:" + PORT + "/");
        System.out.println("API_TOKEN: " + apiToken);
        server.start();
    }

    public void stop() {
        server.stop(0);
        System.out.println("Сервер остановлен " + PORT);
    }

    private String generateApiToken() {
        return "" + System.currentTimeMillis();
    }

    protected boolean hasAuth(HttpExchange h) {
        String rawQuery = h.getRequestURI().getRawQuery();
        return rawQuery != null && (rawQuery.contains("API_TOKEN=" + apiToken) || rawQuery.contains("API_TOKEN=DEBUG"));
    }

    protected String readText(HttpExchange h) throws IOException {
        return new String(h.getRequestBody().readAllBytes(), UTF_8);
    }

    protected void sendText(HttpExchange h, String text) throws IOException {
        h.sendResponseHeaders(200, 0);
        var os = h.getResponseBody();
        os.write(text.getBytes());
        os.close();
    }
}
