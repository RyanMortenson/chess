package websocket;

import chess.ChessMove;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import websocket.commands.*;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;
import websocket.messages.ServerMessage;
import javax.websocket.*;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.function.Consumer;

@ClientEndpoint
public class WebSocketFacade {
    private Session session;
    private final Consumer<ServerMessage> handler;
    private final Gson gson = new Gson();

    public WebSocketFacade(String baseUrl, Consumer<ServerMessage> handler) {
        this.handler = handler;
        try {
            String wsUrl = "ws://" + baseUrl + "/ws";
            WebSocketContainer c = ContainerProvider.getWebSocketContainer();
            c.connectToServer(this, new URI(wsUrl));
        } catch (URISyntaxException | DeploymentException | IOException e) {
            throw new UncheckedIOException(new IOException(e));
        }
    }

    @OnOpen
    public void onOpen(Session session) {
        this.session = session;
    }

    @OnMessage
    public void onMessage(String json) {

        JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
        String type = obj.get("serverMessageType").getAsString();
        ServerMessage msg;

        switch (ServerMessage.ServerMessageType.valueOf(type)) {
            case LOAD_GAME:
                msg = gson.fromJson(json, LoadGameMessage.class);
                break;
            case ERROR:
                msg = gson.fromJson(json, ErrorMessage.class);
                break;
            case NOTIFICATION:
                msg = gson.fromJson(json, NotificationMessage.class);
                break;
            default:
                msg = gson.fromJson(json, ServerMessage.class);
        }

        handler.accept(msg);
    }

    private void send(Object cmd) {
        String json = gson.toJson(cmd);
        session.getAsyncRemote().sendText(json);
    }

    public void sendConnect(String authToken, int gameID) {
        send(new ConnectCommand(authToken, gameID));
    }

    public void sendMakeMove(String authToken, int gameID, ChessMove move) {
        send(new MakeMoveCommand(authToken, gameID, move));
    }

    public void sendResign(String authToken, int gameID) {
        send(new ResignCommand(authToken, gameID));
    }

    public void sendLeave(String authToken, int gameID) {
        send(new LeaveCommand(authToken, gameID));
    }

    public void close() throws IOException {
        session.close();
    }
}
