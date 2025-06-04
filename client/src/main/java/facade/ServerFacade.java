package facade;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import exception.ResponseException;
import model.*;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class ServerFacade {

    private final String serverUrl;
    private final Gson gson = new Gson();

    public ServerFacade(String serverUrl) {
        this.serverUrl = serverUrl;
    }


    // API Methods

    public RegisterResponse register(RegisterRequest req) throws ResponseException {
        return makeRequest("POST", "/user", req, RegisterResponse.class, null);
    }


    public LoginResponse login(LoginRequest req) throws ResponseException {
        return makeRequest("POST", "/session", req, LoginResponse.class, null);
    }


    public void logout(String authToken) throws ResponseException {
        // pass responseClass=null since we expect no JSON on success
        makeRequest("DELETE", "/session", null, null, authToken);
    }


    public CreateGameResponse createGame(String gameName, String authToken) throws ResponseException {
        CreateGameRequest body = new CreateGameRequest(gameName);
        return makeRequest("POST", "/game", body, CreateGameResponse.class, authToken);
    }


    public ListGamesResponse listGames(String authToken) throws ResponseException {
        return makeRequest("GET", "/game", null, ListGamesResponse.class, authToken);
    }


    public JoinGameResponse joinGame(int gameID, String playerColor, String authToken) throws ResponseException {
        JoinGameRequest body = new JoinGameRequest(gameID, playerColor);
        return makeRequest("PUT", "/game", body, JoinGameResponse.class, null);
    }


    public void clear() throws ResponseException {
        makeRequest("POST", "/clear", null, null, null);
    }

    // helper
    private <T> T makeRequest(
            String method,
            String path,
            Object requestObj,
            Class<T> responseClass,
            String authToken
    ) throws ResponseException {
        HttpURLConnection connection = null;
        try {
            // create and open connection
            URL url = new URI(serverUrl + path).toURL();
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(method);

            // add the authoriztion header if we have an authToken
            if (authToken != null) {
                connection.setRequestProperty("Authorization", authToken);
            }

            // if we have a request object, then we serialize it into a JSON
            if (requestObj != null) {
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

                String json = gson.toJson(requestObj);
                byte[]   bytes = json.getBytes(StandardCharsets.UTF_8);

                try (OutputStream out = connection.getOutputStream()) {
                    out.write(bytes);
                }
            }

            int status = connection.getResponseCode();

            InputStream stream = (status >= 200 && status < 300)
                    ? connection.getInputStream() //true
                    : connection.getErrorStream(); //false

            // read response as a string
            StringBuilder text = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    text.append(line);
                }
            }

            // for fail code
            if (status < 200 || status >= 300) {
                throw new ResponseException(status, text.toString());
            }


            if (responseClass != null) {
                try {
                    return gson.fromJson(text.toString(), responseClass);
                } catch (JsonSyntaxException e) {
                    throw new ResponseException(500, "Unreadable JSON structure: " + e.getMessage());
                }
            } else {
                // for success
                return null;
            }

        } catch (IOException e) {
            throw new ResponseException(500, e.getMessage());
        } catch (ResponseException e) {
            throw e;
        } catch (Exception e) {
            // any other checked exception (e.g. URISyntaxException)
            throw new ResponseException(500, e.getMessage());
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }


}
