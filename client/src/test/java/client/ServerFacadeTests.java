package client;

import exception.ResponseException;
import model.*;
import org.junit.jupiter.api.*;
import server.Server;
import facade.ServerFacade;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ServerFacadeTests {

    private static Server server;
    private static ServerFacade facade;

    // Global variables so I have less boilerplate
    private String username = "Ryno";
    private String password = "Morto";
    private String email = "rynomorto@cool.com";

    @BeforeAll
    public void init() {
        // 1) Start the embedded test server on a random port
        server = new Server();
        int port = server.run(0);
        System.out.println("Started test HTTP server on port " + port);

        // 2) Initialize the static facade that all tests will use
        String baseUrl = "http://localhost:" + port;
        facade = new ServerFacade(baseUrl);
    }

    @AfterAll
    public void stopServer() {
        server.stop();
    }

    @BeforeEach
    public void clearServer() throws ResponseException {
        facade.clear();
    }

    // register ----------------------------------------------------------------------------

    @Test
    public void registerSuccess() throws ResponseException {

        RegisterRequest req = new RegisterRequest(username, password, email);
        RegisterResponse resp = facade.register(req);

        assertNotNull(resp, "Response should not be null");
        assertNotNull(resp.authToken(), "authToken should not be null");
        assertFalse(resp.authToken().isEmpty(), "authToken should not be empty");
        assertEquals(username, resp.username(), "Username in response must match request");
    }

    @Test
    public void registerDuplicateUsernameFails() throws ResponseException {

        RegisterRequest firstReq = new RegisterRequest(username, password, email);
        RegisterResponse firstResp = facade.register(firstReq);
        //This should work
        assertNotNull(firstResp.authToken(), "First registration should succeed");

        //Registering again with same username should throw 403
        RegisterRequest secondReq = new RegisterRequest(username, "newPw", "newEmail@cool.com");
        ResponseException e = assertThrows(
                ResponseException.class,
                () -> facade.register(secondReq),
                "if duplicate username was registered, e = 403"
        );
        assertEquals(403, e.getStatusCode(), "Expected HTTP 403 for duplicate username");
    }

    // login ----------------------------------------------------------------------------

    @Test
    public void loginSuccess() throws ResponseException {

        RegisterRequest registerRequest = new RegisterRequest(username, password, email);
        facade.register(registerRequest);

        LoginRequest loginRequest = new LoginRequest(username, password);
        LoginResponse loginResponse = facade.login(loginRequest);

        assertNotNull(loginResponse, "Login response should not be null");
        assertNotNull(loginResponse.authToken(), "authToken should not be null");
        assertFalse(loginResponse.authToken().isEmpty(), "authToken should not be empty");
        assertEquals(username, loginResponse.username(), "usernames should match");
    }

    @Test
    public void loginFail() throws ResponseException {

        RegisterRequest registerRequest = new RegisterRequest(username, password, email);
        facade.register(registerRequest);

        LoginRequest badLogin = new LoginRequest(username, "INCORRECT");
        ResponseException e = assertThrows(
                ResponseException.class,
                () -> facade.login(badLogin), "Wrong password should throw ResponseException"
        );
        assertEquals(401, e.getStatusCode(), "Expect 401 for unauthorized");
    }

    // logout ----------------------------------------------------------------------------

    @Test
    public void logoutSuccess() throws ResponseException {
        String token = registerHelper();
        assertDoesNotThrow(() -> facade.logout(token));
    }

    @Test
    public void logoutFail() {
        ResponseException e = assertThrows(
                ResponseException.class,
                () -> facade.logout("fakeAuthToken")
        );
        assertEquals(401, e.getStatusCode(), "Should be 401 for invalid token");
    }


    // createGame ----------------------------------------------------------------------------

    @Test
    public void createGameSuccess() throws ResponseException {
        String token = registerHelper();
        CreateGameResponse resp = facade.createGame("Game1", token);
        assertTrue(resp.gameID() > 0, "Created gameID should be positive");

    }

    @Test
    public void createGameFail() {
        // Passing an invalid token produces a 401
        ResponseException e = assertThrows(
                ResponseException.class,
                () -> facade.createGame("BadGame", "invalidToken"),
                "Invalid token should throw ResponseException"
        );
        assertEquals(401, e.getStatusCode(), "Expect HTTP 401 for invalid token");
    }


    // listGame ----------------------------------------------------------------------------

    @Test
    public void listGameSuccess() throws ResponseException {
        String token = registerHelper();

        ListGamesResponse listResp = facade.listGames(token);
        assertNotNull(listResp, "List games should not be null");
        assertNotNull(listResp.games(), "Games list should not be null");
        assertTrue(listResp.games().isEmpty(), "there should not be any games yet");
    }

    @Test
    public void listGamesFail() throws ResponseException {
        String token = registerHelper();

        //create a game
        facade.createGame("testGame", token);

        ListGamesResponse listResp = facade.listGames(token);
        assertNotNull(listResp.games(), "Games list should not be null");
        assertEquals(1, listResp.games().size(), "There should be 1 game exactly");
        GameData data = listResp.games().getFirst();
        assertEquals("testGame", data.gameName(), "Game name should match");
    }


    // joinGame ----------------------------------------------------------------------------

    @Test
    public void joinGameSuccess() throws ResponseException {
        String token = registerHelper();

        CreateGameResponse created = facade.createGame("joinTest", token);
        JoinGameResponse joinResp = facade.joinGame(created.gameID(), "WHITE", token);

        assertNotNull(joinResp, "joinGameResponse shouldn't be null");
    }

    @Test
    public void joinGameFail() throws ResponseException {
        String token = registerHelper();

        CreateGameResponse createdGame = facade.createGame("joinTest", token);

        ResponseException e = assertThrows(
                ResponseException.class,
                () -> facade.joinGame(createdGame.gameID(), "BLACK", "badToken")
        );

        assertEquals(401, e.getStatusCode());
    }



    //Helper for registering returns token
    private String registerHelper() throws ResponseException {
        facade.register(new RegisterRequest(username, password, email));
        LoginResponse login = facade.login(new LoginRequest(username, password));
        return login.authToken();
    }
}


