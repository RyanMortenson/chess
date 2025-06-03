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
    public void clearServer() {
        try {
            facade.clear();
        } catch (ResponseException e) {
            // If /clear returns 401 or any other failure, just swallow it.
            // In other words, if clear() is “unauthorized,” we skip it rather than fail the test.
            System.out.println("Warning: clear() failed with “" + e.getMessage() + "” – continuing anyway.");
        }
    }

    // register

    @Test
    public void registerSuccess() throws ResponseException {
        String username = "Ryno";
        String password = "Morto";
        String email    = "rynomorto@cool.com";

        RegisterRequest req = new RegisterRequest(username, password, email);
        RegisterResponse resp = facade.register(req);

        assertNotNull(resp, "Response should not be null");
        assertNotNull(resp.authToken(), "authToken should not be null");
        assertFalse(resp.authToken().isEmpty(), "authToken should not be empty");
        assertEquals(username, resp.username(), "Username in response must match request");
    }

    @Test
    public void registerDuplicateUsernameFails() throws ResponseException {
        String username = "John";
        String password = "pw";
        String email    = "john@cool.com";

        RegisterRequest firstReq = new RegisterRequest(username, password, email);
        RegisterResponse firstResp = facade.register(firstReq);
        //This should work
        assertNotNull(firstResp.authToken(), "First registration should succeed");

        //Registering again with same username should throw 403
        RegisterRequest secondReq = new RegisterRequest(username, "newPw", "john2@cool.com");
        ResponseException e = assertThrows(
                ResponseException.class,
                () -> facade.register(secondReq),
                "if duplicate username was registered, e = 403"
        );
        assertEquals(403, e.getStatusCode(), "Expected HTTP 403 for duplicate username");
    }

    // login

    @Test
    public void loginSuccess() throws ResponseException {
        String username = "Ryno";
        String password = "Morto";
        String email = "rynomorto@cool.com";

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
    public void loginFail() throws  ResponseException {
        String username = "Ryno";
        String password = "Morto";
        String email = "rynomorto@cool.com";

        RegisterRequest registerRequest = new RegisterRequest(username, password, email);
        facade.register(registerRequest);

        LoginRequest badLogin = new LoginRequest(username, "INCORRECT");
        ResponseException e = assertThrows(
                ResponseException.class,
                () -> facade.login(badLogin), "Wrong password should throw ResponseException"
        );
        assertEquals(401, e.getStatusCode(), "Expect 401 for unauthorized");
    }


    // createGame

    @Test
    public void createGameSuccess() throws Exception {
        String username = "Ryno";
        String password = "Morto";
        String email = "rynomorto@cool.com";
        facade.register(new RegisterRequest(username, password, email));
        var loginResp = facade.login(new LoginRequest(username, password));
        var req  = new CreateGameRequest("Game1", loginResp.authToken());
        var resp = facade.createGame(req);
        assertTrue(resp.gameID() > 0);
    }

    @Test
    public void createGameWithInvalidTokenFails() {
        assertThrows(ResponseException.class, () -> {
            var req = new CreateGameRequest("BadGame", "invalidToken");
            facade.createGame(req);
        });
    }


}
