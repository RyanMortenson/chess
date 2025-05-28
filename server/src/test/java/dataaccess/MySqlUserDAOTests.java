
package dataaccess;

import model.UserData;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

public class MySqlUserDAOTests {
    private MySqlUserDAO dao;

    @BeforeAll
    static void initSchema() throws Exception {
        DatabaseInitializer.initialize();
    }

    @BeforeEach
    void setUp() throws DataAccessException {
        dao = new MySqlUserDAO();
        dao.clear();
    }

    // Clear

    @Test
    void clearTest() throws DataAccessException {
        dao.createUser(new UserData("u1","pw1","e1@example.com"));
        assertNotNull(dao.getUser("u1"));
        dao.clear();
        assertNull(dao.getUser("u1"));
    }


    // createUser

    @Test
    void createUserSuccess() throws DataAccessException {
        var ud = new UserData("alice","secret","alice@x.com");
        dao.createUser(ud);
        UserData stored = dao.getUser("alice");
        assertNotNull(stored);
        assertEquals("alice", stored.username());
        assertEquals("secret", stored.password());
        assertEquals("alice@x.com", stored.email());
    }

    @Test
    void createUserDuplicateUsernameFail() {
        assertDoesNotThrow(() ->
                dao.createUser(new UserData("bob","pw","bob@x.com"))
        );
        DataAccessException ex = assertThrows(
                DataAccessException.class,
                () -> dao.createUser(new UserData("bob","pw2","bob2@x.com"))
        );
        assertTrue(ex.getMessage().toLowerCase().contains("taken"));
    }


    // getUser

    @Test
    void getUserNull() throws DataAccessException {
        assertNull(dao.getUser("noone"));
    }

    @Test
    void getUserSuccess() throws DataAccessException {
        dao.createUser(new UserData("carol","pw3","carol@x.com"));
        UserData u = dao.getUser("carol");
        assertNotNull(u);
        assertEquals("carol", u.username());
    }
}
