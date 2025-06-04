package ui;

import exception.ResponseException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.JsonObject;
import com.google.gson.Gson;

public class ClientUtils {

    public static void printError(ResponseException e) {
        System.out.println(
                EscapeSequences.SET_TEXT_COLOR_RED
                        + extractErrorMessage(e)
                        + EscapeSequences.RESET_TEXT_COLOR
        );
    }


    private static String extractErrorMessage(ResponseException e) {
        try {
            JsonObject obj = new Gson()
                    .fromJson(e.getMessage(), JsonObject.class);
            if (obj.has("message")) {
                return obj.get("message").getAsString();
            }
        } catch (JsonSyntaxException ex) {
            // Not valid JSON → fall through
        }
        return e.getMessage();
    }
}
