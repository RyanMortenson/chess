package dataaccess;

import chess.ChessGame;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class MySqlGameDAOTests {
    private MySqlGameDAO gameDao;
    private MySqlUserDAO userDao;

    @BeforeAll
    static void initSchema() throws Exception {
        DatabaseInitializer.initialize();
    }

    @BeforeEach
    void setUp() throws DataAccessException {
        userDao = new MySqlUserDAO();
        userDao.clear();
        gameDao = new MySqlGameDAO();
        gameDao.clear();

        userDao.createUser(new UserData("JohnCena","pw","JohnCena@x.com"));
        userDao.createUser(new UserData("chickenJoe",  "pw","chickenJoe@x.com"));
    }

    // clear
    @Test
    void clearRemovesAllGames() throws DataAccessException {
        int id = gameDao.createGame(new GameData(0,null,null,"G1", new ChessGame()));
        assertFalse(gameDao.listGames().isEmpty());
        gameDao.clear();
        assertTrue(gameDao.listGames().isEmpty());
    }

    // createGame
    @Test
    void createGameSuccess() throws DataAccessException {
        int id = gameDao.createGame(new GameData(0,null,null,"MyGame", new ChessGame()));
        assertTrue(id > 0);
        GameData g = gameDao.getGame(id);
        assertEquals("MyGame", g.gameName());
    }

    @Test
    void createGameNullNameFail() {
        assertThrows(
                DataAccessException.class,
                () -> gameDao.createGame(new GameData(0,null,null,null, new ChessGame()))
        );
    }

    // getGame
    @Test
    void getGameExistingReturnsGame() throws DataAccessException {
        int id = gameDao.createGame(new GameData(0,null,null,"Exists", new ChessGame()));
        GameData g = gameDao.getGame(id);
        assertNotNull(g);
        assertEquals("Exists", g.gameName());
    }

    @Test
    void getGameNonexistentReturnsNull() throws DataAccessException {
        assertNull(gameDao.getGame(9999));
    }

    // listGames
    @Test
    void listGamesReturnsNonEmptyList() throws DataAccessException {
        gameDao.createGame(new GameData(0,null,null,"L1", new ChessGame()));
        List<GameData> all = gameDao.listGames();
        assertEquals(1, all.size());
        assertEquals("L1", all.get(0).gameName());
    }

    @Test
    void listGamesReturnsEmptyList() throws DataAccessException {
        List<GameData> all = gameDao.listGames();
        assertNotNull(all);
        assertTrue(all.isEmpty());
    }

    // updateGame
    @Test
    void updateGameSuccess() throws DataAccessException {
        int id = gameDao.createGame(new GameData(0,null,null,"U1", new ChessGame()));
        
        ChessGame state = new ChessGame();
        GameData updated = new GameData(id, "JohnCena", "chickenJoe", "U1", state);
        gameDao.updateGame(id, updated);

        GameData g2 = gameDao.getGame(id);
        assertEquals("JohnCena", g2.whiteUsername());
        assertEquals("chickenJoe",   g2.blackUsername());
    }

    @Test
    void updateGameNonexistentFail() {
        ChessGame state = new ChessGame();
        GameData updated = new GameData(12345, "JohnCena", "chickenJoe", "Nope", state);
        assertThrows(
                DataAccessException.class,
                () -> gameDao.updateGame(12345, updated)
        );
    }
}
