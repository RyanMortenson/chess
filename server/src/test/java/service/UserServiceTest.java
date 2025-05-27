package service;

import dataaccess.MemoryUserDAO;
import dataaccess.MemoryAuthDAO;
import dataaccess.DataAccessException;
import model.UserData;
import service.exceptions.UnauthorizedException;
import service.requests.RegisterRequest;
import service.requests.LoginRequest;
import service.requests.LogoutRequest;
import service.results.RegisterResult;
import service.results.LoginResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;

import static org.junit.jupiter.api.Assertions.*;

public class UserServiceTest {
    private MemoryUserDAO userDao;
    private MemoryAuthDAO authDao;
    private UserService userService;

    @BeforeEach
    public void setUp() {
        userDao = new MemoryUserDAO();
        authDao = new MemoryAuthDAO();
        userService = new UserService(userDao, authDao);
    }

    // register tests

    @Test
    public void registerSuccess() throws DataAccessException {
        var req = new RegisterRequest("john","cena","john@cena.com");
        RegisterResult res = userService.register(req);

        assertEquals("john", res.username());
        assertNotNull(res.authToken());

        UserData stored = userDao.getUser("john");
        assertEquals("john",    stored.username());
        assertEquals("john@cena.com", stored.email());

        String hash = stored.password();
        assertNotEquals("cena", hash,        "raw password must not be stored");
        assertTrue(hash.startsWith("$2a$"), "should be a BCrypt hash");
        assertTrue(BCrypt.checkpw("cena", hash), "BCrypt.checkpw must succeed");
    }

    @Test
    public void registerDuplicateFails() throws DataAccessException {
        var req = new RegisterRequest("jake","from","state@farm.com");
        userService.register(req);

        DataAccessException ex = assertThrows(
                DataAccessException.class,
                () -> userService.register(req)
        );
        String msg = ex.getMessage().toLowerCase();
        assertTrue(msg.contains("taken") || msg.contains("exists"));
    }

    // login tests

    @Test
    public void loginSuccess() throws Exception {
        userService.register(new RegisterRequest("john","cena","john@cena.com"));
        var loginRes = userService.login(new LoginRequest("john","cena"));

        assertEquals("john", loginRes.username());
        assertNotNull(loginRes.authToken());
    }

    @Test
    public void loginBadPasswordFails() throws DataAccessException {
        userService.register(new RegisterRequest("john","cena","john@cena.com"));
        assertThrows(
                UnauthorizedException.class,
                () -> userService.login(new LoginRequest("john","theBeloved"))
        );
    }

    @Test
    public void loginUnknownUserFails() {
        assertThrows(
                DataAccessException.class,
                () -> userService.login(new LoginRequest("nobodyKnows","yeah"))
        );
    }

    // logout tests

    @Test
    public void logoutSuccess() throws Exception {
        RegisterResult reg = userService.register(
                new RegisterRequest("eve","pw","e@x.com")
        );

        // should not throw
        assertDoesNotThrow(() ->
                userService.logout(new LogoutRequest(reg.authToken()))
        );
    }

    @Test
    public void logoutUnauthorized() throws Exception {
        // register & log out once
        RegisterResult reg = userService.register(
                new RegisterRequest("jake","from","state@farm.com")
        );
        userService.logout(new LogoutRequest(reg.authToken()));

        // second logout on same token should now throw
        assertThrows(
                DataAccessException.class,
                () -> userService.logout(new LogoutRequest(reg.authToken()))
        );

        // and logging out with a random invalid token also throws
        assertThrows(
                DataAccessException.class,
                () -> userService.logout(new LogoutRequest("non-existent token"))
        );
    }
}
