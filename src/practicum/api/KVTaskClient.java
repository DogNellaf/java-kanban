package practicum.api;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class KVTaskClient {
    private final String apiToken;
    private final HttpClient client;
    private final URI serverURI;

    public KVTaskClient(URI uri) throws IOException, InterruptedException {
        serverURI = uri;
        client = HttpClient.newHttpClient();
        apiToken = sendRequest("register", "");
    }

    public void put(String key, String json) throws IOException, InterruptedException {
        sendRequest("save/" + key + "?API_TOKEN=" + apiToken, json);
    }

    public String load(String key) throws IOException, InterruptedException {
        return sendRequest("load/" + key + "?API_TOKEN=" + apiToken, "");
    }

    public void clear() throws IOException, InterruptedException {
        var uri = serverURI.resolve("clear/?API_TOKEN=" + apiToken);
        var request = HttpRequest.newBuilder()
                .DELETE()
                .uri(uri)
                .build();
        client.send(request, HttpResponse.BodyHandlers.ofString());
    }


    private String sendRequest(String address, String body) throws IOException, InterruptedException {
        var requestBuilder = HttpRequest.newBuilder();

        var uri = serverURI.resolve(address);

        HttpRequest request;
        if (body.equals("")) {
            request = requestBuilder
                    .GET()
                    .uri(uri)
                    .build();
        } else {
            request = requestBuilder
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .uri(uri)
                    .build();
        }

        var handler = HttpResponse.BodyHandlers.ofString();
        return client.send(request, handler).body();
    }
}
