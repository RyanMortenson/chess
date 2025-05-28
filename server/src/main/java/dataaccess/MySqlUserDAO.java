package dataaccess;

import model.UserData;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;


public class MySqlUserDAO implements UserDAO {
    public MySqlUserDAO() {
    }


    @Override
    public void clear() throws DataAccessException {
        final String sql = "DELETE FROM user";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error clearing users", e);
        }
    }


    @Override
    public void createUser(UserData user) throws DataAccessException {
        final String sql = "INSERT INTO `user` (username, email, password_hash) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, user.username());
            stmt.setString(2, user.email());
            stmt.setString(3, user.password());
            stmt.executeUpdate();
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new DataAccessException("Error: username already taken", e);
        } catch (SQLException e) {
            throw new DataAccessException("Error creating user", e);
        }
    }



    @Override
    public UserData getUser(String username) throws DataAccessException {
        final String sql = "SELECT username, password_hash, email FROM `user` WHERE username = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                String u = rs.getString("username");
                String pwdHash = rs.getString("password_hash");
                String email = rs.getString("email");
                return new UserData(u, pwdHash, email);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error retrieving user", e);
        }
    }
}