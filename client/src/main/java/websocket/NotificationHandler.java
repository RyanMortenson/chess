package websocket;

import websocket.messages.NotificationMessage;
import websocket.messages.ErrorMessage;

public class NotificationHandler {

    // Print a normal notification
    public void handleNotification(NotificationMessage msg) {
        System.out.println("[Notification] " + msg.message);
    }

    // Print an error
    public void handleError(ErrorMessage err) {
        System.err.println("Error: " + err.errorMessage);
    }
}
