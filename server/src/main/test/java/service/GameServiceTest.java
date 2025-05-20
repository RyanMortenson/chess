package service;

import dataaccess.MemoryGameDAO;
import dataaccess.MemoryAuthDAO;
import dataaccess.DataAccessException;
import model.AuthData;
import model.GameData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.exceptions.UnauthorizedException;
import service.requests.CreateGameRequest;
import service.requests.JoinGameRequest;
import service.results.CreateGameResult;
import service.results.ListGamesResult;

import static org.junit.jupiter.api.Assertions.*;

public class GameServiceTest {
    private MemoryGameDAO gameDao;
    private MemoryAuthDAO authDao;
    private GameService gameService;
    private final String validToken = "valid-token";

    @BeforeEach
    public void setUp() throws DataAccessException {
        gameDao = new MemoryGameDAO();
        authDao = new MemoryAuthDAO();
        authDao.addAuth(new AuthData(validToken, "alice"));
        gameService = new GameService(gameDao, authDao);
    }

    // createGame tests

    @Test
    public void createGame_success() throws Exception {
        CreateGameResult res = gameService.createGame(
                new CreateGameRequest("TestGame", validToken),
                validToken
        );
        assertTrue(res.gameID() > 0, "gameID should be positive");

        GameData saved = gameDao.getGame(res.gameID());
        assertEquals("TestGame", saved.gameName());
        assertNull(saved.whiteUsername());
        assertNull(saved.blackUsername());
    }

    @Test
    public void createGame_unauthorized() {
        assertThrows(
                DataAccessException.class,
                () -> gameService.createGame(
                        new CreateGameRequest("X", "bad-token"),
                        "bad-token"
                )
        );
    }

    // listGames tets

    @Test
    public void listGames_success() throws Exception {
        gameDao.createGame(new GameData(0, null, null, "G1", null));

        ListGamesResult list = gameService.listGames(validToken);
        assertEquals(1, list.games().size(), "should list the one game");
    }

    @Test
    public void listGames_unauthorized() {
        assertThrows(
                DataAccessException.class,
                () -> gameService.listGames("nope")
        );
    }

    // joinGame tests

    @Test
    public void joinGame_success() throws Exception {
        // create empty game
        int id = gameService.createGame(
                new CreateGameRequest("G2", validToken),
                validToken
        ).gameID();

        // join as WHITE
        gameService.joinGame(
                new JoinGameRequest(id, "WHITE", validToken),
                validToken
        );

        GameData updated = gameDao.getGame(id);
        assertEquals("alice", updated.whiteUsername(),
                "alice should occupy WHITE slot");
    }

    @Test
    public void joinGame_unauthorized() throws Exception {
        // create a game so the ID exists
        int id = gameService.createGame(
                new CreateGameRequest("Cool Game", validToken),
                validToken
        ).gameID();

        // attempt join with bad token
        assertThrows(
                UnauthorizedException.class,
                () -> gameService.joinGame(
                        new JoinGameRequest(id, "WHITE", "invalid-token"),
                        "invalid-token"
                )
        );
    }
}
