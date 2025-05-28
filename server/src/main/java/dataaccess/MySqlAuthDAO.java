package dataaccess;

import model.AuthData;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


public class MySqlAuthDAO implements AuthDAO {
    public MySqlAuthDAO() {
    }


    @Override
    public void clear() throws DataAccessException {
        final String sql = "DELETE FROM auth";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error clearing auths", e);
        }
    }


    @Override
    public void addAuth(AuthData auth) throws DataAccessException {
        final String sql = "INSERT INTO auth (authToken, username) VALUES (?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, auth.authToken());
            stmt.setString(2, auth.username());
            stmt.executeUpdate();
            System.out.println("Inserted auth token for user: " + auth.username());
        } catch (SQLException e) {
            throw new DataAccessException("Error adding auth token", e);
        }
    }


    @Override
    public AuthData getAuth(String authToken) throws DataAccessException {
        final String sql = "SELECT authToken, username FROM auth WHERE authToken = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, authToken);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return new AuthData(
                        rs.getString("authToken"),
                        rs.getString("username")
                );
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error retrieving auth token", e);
        }
    }


    @Override
    public void removeAuth(String authToken) throws DataAccessException {
        final String sql = "DELETE FROM auth WHERE authToken = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, authToken);
            int count = stmt.executeUpdate();
            if (count == 0) {
                throw new DataAccessException("Error: unauthorized");
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error removing auth token", e);
        }
    }
}