package practicum.api.handlers;

import com.sun.net.httpserver.HttpExchange;
import practicum.managers.Managers;

public class HistoryHandler extends Handler {

    @Override
    public void handle(HttpExchange exchange) {
        this.exchange = exchange;
        var method = exchange.getRequestMethod();
        if (method.equals("GET")) {
            sendHistoryToClient();
        } else {
            sendUnsupportedMethod();
        }
    }

    private void sendHistoryToClient() {
        var history = Managers.getDefaultHistory().getHistory();
        var json = gson.toJson(history);
        sendMessageToClient(json, 200);
    }
}
