package websocket.messages;

public class ErrorMessage extends ServerMessage {

    public final String errorMessage;

    public ErrorMessage(String errorMessage) {
        super(ServerMessageType.ERROR);
        this.errorMessage = errorMessage;
    }
}
