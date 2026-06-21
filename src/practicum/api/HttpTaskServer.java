package practicum.api;

import com.sun.net.httpserver.HttpServer;
import practicum.api.handlers.*;

import java.io.IOException;
import java.net.InetSocketAddress;

public class HttpTaskServer {
    public static int PORT = 8080;
    private final HttpServer server;

    public HttpTaskServer() throws IOException {
        InetSocketAddress serverAddress = new InetSocketAddress("localhost", PORT);
        server = HttpServer.create(serverAddress, 0);

        server.createContext("/tasks/task", new TaskHandler());
        server.createContext("/tasks/subtask", new SubtaskHandler());
        server.createContext("/tasks/epic", new EpicHandler());
        server.createContext("/tasks/subtask/epic", new SubtaskEpicHandler());
        server.createContext("/tasks/history", new HistoryHandler());
        server.createContext("/tasks/", new TasksListHandler());
    }

    public void start() {
        server.start();
        System.out.println("HTTP-сервер успешно запущен по адресу http://localhost:" + PORT + "/");
    }

    public void stop() {
        server.stop(0);
        System.out.println("HTTP-сервер остановлен (порт " + PORT + ")");
    }
}
