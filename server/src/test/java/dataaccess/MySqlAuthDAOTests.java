package dataaccess;

import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

public class MySqlAuthDAOTests {
    private MySqlAuthDAO authDao;
    private MySqlUserDAO userDao;

    @BeforeAll
    static void initSchema() throws Exception {
        DatabaseInitializer.initialize();
    }

    @BeforeEach
    void setUp() throws DataAccessException {
        userDao = new MySqlUserDAO();
        userDao.clear();

        authDao = new MySqlAuthDAO();
        authDao.clear();

        userDao.createUser(new UserData("u1", "pw1", "u1@x.com"));
        userDao.createUser(new UserData("u2", "pw2", "u2@x.com"));
    }

    // clear
    @Test
    void clearRemovesAllAuths() throws DataAccessException {
        authDao.addAuth(new AuthData("t1", "u1"));
        assertNotNull(authDao.getAuth("t1"));
        authDao.clear();
        assertNull(authDao.getAuth("t1"));
    }

    // addAuth
    @Test
    void addAuthSuccess() throws DataAccessException {
        // just ensure it doesn't throw
        authDao.addAuth(new AuthData("tok1", "u1"));
    }

    @Test
    void addAuthDuplicateTokenFail() throws DataAccessException {
        authDao.addAuth(new AuthData("dup", "u1"));
        DataAccessException ex = assertThrows(
                DataAccessException.class,
                () -> authDao.addAuth(new AuthData("dup", "u1"))
        );
        assertTrue(ex.getMessage().toLowerCase().contains("error"));
    }

    // getAuth(...)
    @Test
    void getAuthSuccess() throws DataAccessException {
        AuthData a = new AuthData("t2", "u2");
        authDao.addAuth(a);
        AuthData fetched = authDao.getAuth("t2");
        assertEquals(a, fetched);
    }

    @Test
    void getAuthFail() throws DataAccessException {
        assertNull(authDao.getAuth("no-such-token"));
    }

    // removeAuth
    @Test
    void removeAuthSuccess() throws DataAccessException {
        authDao.addAuth(new AuthData("t3", "u2"));
        authDao.removeAuth("t3");
        assertNull(authDao.getAuth("t3"));
    }

    @Test
    void removeAuthFail() {
        DataAccessException ex = assertThrows(
                DataAccessException.class,
                () -> authDao.removeAuth("missing")
        );
        assertTrue(ex.getMessage().toLowerCase().contains("unauthorized"));
    }
}
