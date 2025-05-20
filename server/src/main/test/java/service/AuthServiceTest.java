package service;

import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.MemoryAuthDAO;
import model.AuthData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class AuthServiceTest {
    private MemoryAuthDAO authDao;
    private AuthService authService;

    @BeforeEach
    public void setUp() {
        authDao = new MemoryAuthDAO();
        authService = new AuthService(authDao);
    }

    // issueToken tests

    @Test
    public void issueToken_success() throws DataAccessException {
        AuthData auth = authService.issueToken("bob");
        assertNotNull(auth.authToken(), "Token should not be null");
        assertEquals("bob", auth.username(), "Username should round-trip");
        AuthData fromDao = authDao.getAuth(auth.authToken());
        assertEquals(auth, fromDao, "DAO should return the same AuthData");
    }

    @Test
    public void issueToken_daoFailure() {
        // stub DAO that always fails on addAuth
        AuthDAO failingDao = new MemoryAuthDAO() {
            @Override
            public void addAuth(AuthData auth) throws DataAccessException {
                throw new DataAccessException("boom");
            }
            // unused:
            @Override public AuthData getAuth(String token) throws DataAccessException { return null; }
            @Override public void removeAuth(String token) throws DataAccessException { }
        };
        AuthService svc = new AuthService(failingDao);
        DataAccessException ex = assertThrows(
                DataAccessException.class,
                () -> svc.issueToken("x")
        );
        assertTrue(ex.getMessage().contains("boom"));
    }

    // validateToken tests

    @Test
    public void validateToken_success() throws DataAccessException {
        String t = UUID.randomUUID().toString();
        authDao.addAuth(new AuthData(t, "carol"));
        AuthData auth = authService.validateToken(t);
        assertEquals("carol", auth.username());
        assertEquals(t, auth.authToken());
    }

    @Test
    public void validateToken_nullOrMissing() {
        // for null token
        assertThrows(
                DataAccessException.class,
                () -> authService.validateToken(null),
                "null should be unauthorized"
        );
        // for missing token
        assertThrows(
                DataAccessException.class,
                () -> authService.validateToken("invalid"),
                "unknown token should fail"
        );
    }

    // revokeToken tests

    @Test
    public void revokeToken_success() throws DataAccessException {
        String t = UUID.randomUUID().toString();
        authDao.addAuth(new AuthData(t, "dave"));
        authService.revokeToken(t);
        // now DAO.getAuth should throw
        assertThrows(
                DataAccessException.class,
                () -> authDao.getAuth(t),
                "token should be removed"
        );
    }

    @Test
    public void revokeToken_nullOrMissing() {
        // null token
        assertThrows(
                DataAccessException.class,
                () -> authService.revokeToken(null)
        );
        // missing token
        assertThrows(
                DataAccessException.class,
                () -> authService.revokeToken("nope")
        );
    }
}
