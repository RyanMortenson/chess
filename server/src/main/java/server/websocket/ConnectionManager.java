package server.websocket;

import com.google.gson.Gson;
import org.eclipse.jetty.websocket.api.Session;
import websocket.messages.ServerMessage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionManager {

    private final ConcurrentHashMap<Integer, ConcurrentHashMap<String, Connection>> gameConnections = new ConcurrentHashMap<>();

    public void add(int gameID, String username, Session session) {
        var gameMap = gameConnections.computeIfAbsent(gameID, k -> new ConcurrentHashMap<>());
        gameMap.put(username, new Connection(username, session));
    }

    public void remove(int gameID, String username) {
        var gameMap = gameConnections.get(gameID);
        if (gameMap != null) {
            gameMap.remove(username);
        }
    }

    public void sendTo(String username, ServerMessage message) throws IOException {
        for (var gameMap : gameConnections.values()) {
            if (gameMap.containsKey(username)) {
                gameMap.get(username).send(new Gson().toJson(message));
                return;
            }
        }
    }

    public void broadcast(int gameID, String excludeUsername, ServerMessage message) throws IOException {
        var gameMap = gameConnections.get(gameID);
        if (gameMap == null) return;

        var removeList = new ArrayList<String>();
        for (var entry : gameMap.entrySet()) {
            var conn = entry.getValue();
            if (!conn.session.isOpen()) {
                removeList.add(entry.getKey());
                continue;
            }
            if (!entry.getKey().equals(excludeUsername)) {
                conn.send(new Gson().toJson(message));
            }
        }
        for (var user : removeList) {
            gameMap.remove(user);
        }
    }

    public void broadcastAll(int gameID, ServerMessage message) throws IOException {
        broadcast(gameID, null, message);
    }

    public void removeSession(Session session) {
        for (var gameMap : gameConnections.values()) {
            gameMap.entrySet().removeIf(entry -> entry.getValue().session.equals(session));
        }
    }
}
