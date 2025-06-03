package facade;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import exception.ResponseException;
import model.*;



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


    public CreateGameResponse createGame(CreateGameRequest req) throws ResponseException {
        return makeRequest("POST", "/game", req, CreateGameResponse.class, null);
    }


    public ListGamesResponse listGames(String authToken) throws ResponseException {
        return makeRequest("GET", "/game", null, ListGamesResponse.class, authToken);
    }


    public JoinGameResponse joinGame(JoinGameRequest req) throws ResponseException {
        return makeRequest("PUT", "/game", req, JoinGameResponse.class, null);
    }

    // helpers
    private <T> T makeRequest(
            String method,
            String path,
            Object requestObj,
            Class<T> responseClass,
            String authToken
    ) {}
}
