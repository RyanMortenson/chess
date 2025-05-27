package dataaccess;

public class DatabaseInitializer {
    private static final String USER_DDL = """
      CREATE TABLE IF NOT EXISTS user (
        username      VARCHAR(50)   PRIMARY KEY,
        email         VARCHAR(100)  NOT NULL  UNIQUE,
        password_hash VARCHAR(60)   NOT NULL
      );
      """;

    private static final String AUTH_DDL = """
      CREATE TABLE IF NOT EXISTS auth (
        authToken CHAR(36)    PRIMARY KEY,
        username  VARCHAR(50) NOT NULL,
        FOREIGN KEY(username) REFERENCES user(username) ON DELETE CASCADE
      );
      """;

    private static final String GAME_DDL = """
      CREATE TABLE IF NOT EXISTS game (
        gameID        INT           AUTO_INCREMENT PRIMARY KEY,
        whiteUsername VARCHAR(50),
        blackUsername VARCHAR(50),
        gameName      VARCHAR(100)  NOT NULL,
        gameState     TEXT          NOT NULL,
        FOREIGN KEY(whiteUsername) REFERENCES user(username) ON DELETE SET NULL,
        FOREIGN KEY(blackUsername) REFERENCES user(username) ON DELETE SET NULL
      );
      """;

    /** Run all the DDLs in sequence. */
    public static void initialize() throws Exception {
        DatabaseManager.createDatabase();
        try (var conn = DatabaseManager.getConnection();
             var stmt = conn.createStatement()) {

            stmt.executeUpdate(USER_DDL);
            stmt.executeUpdate(AUTH_DDL);
            stmt.executeUpdate(GAME_DDL);
        }
    }
}
