package dataaccess;

import com.google.gson.Gson;
import chess.ChessGame;
import model.GameData;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * MySQL-backed implementation of the GameDAO interface.
 */
public class MySqlGameDAO implements GameDAO {
    private final Gson gson = new Gson();

    public MySqlGameDAO() {
        // no-arg constructor
    }

    /**
     * Clears all games from the 'game' table.
     */
    @Override
    public void clear() throws DataAccessException {
        final String sql = "TRUNCATE TABLE `game`";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error clearing games", e);
        }
    }

    /**
     * Inserts a new game record and returns the generated gameID.
     */
    @Override
    public int createGame(GameData game) throws DataAccessException {
        final String sql = "INSERT INTO game (whiteUsername, blackUsername, gameName, gameState) VALUES (?,?,?,?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, game.whiteUsername());
            stmt.setString(2, game.blackUsername());
            stmt.setString(3, game.gameName());
            stmt.setString(4, gson.toJson(game.game()));
            stmt.executeUpdate();
            System.out.println("Inserted game: " + game.gameName());
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                } else {
                    throw new DataAccessException("Unable to retrieve generated gameID");
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error creating game", e);
        }
    }

    /**
     * Retrieves a GameData by gameID, or null if not found.
     */
    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        final String sql = "SELECT whiteUsername, blackUsername, gameName, gameState FROM game WHERE gameID = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, gameID);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                String white = rs.getString("whiteUsername");
                String black = rs.getString("blackUsername");
                String name  = rs.getString("gameName");
                String state = rs.getString("gameState");
                ChessGame chessGame = gson.fromJson(state, ChessGame.class);
                return new GameData(gameID, white, black, name, chessGame);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error retrieving game", e);
        }
    }

    /**
     * Returns a list of all games.
     */
    @Override
    public List<GameData> listGames() throws DataAccessException {
        final String sql = "SELECT gameID, whiteUsername, blackUsername, gameName, gameState FROM game";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            List<GameData> games = new ArrayList<>();
            while (rs.next()) {
                int id = rs.getInt("gameID");
                String white = rs.getString("whiteUsername");
                String black = rs.getString("blackUsername");
                String name  = rs.getString("gameName");
                String state = rs.getString("gameState");
                ChessGame chessGame = gson.fromJson(state, ChessGame.class);
                games.add(new GameData(id, white, black, name, chessGame));
            }
            return games;
        } catch (SQLException e) {
            throw new DataAccessException("Error listing games", e);
        }
    }

    /**
     * Updates an existing game row; throws if no row was updated.
     */
    @Override
    public void updateGame(int gameID, GameData game) throws DataAccessException {
        final String sql = "UPDATE game SET whiteUsername=?, blackUsername=?, gameState=? WHERE gameID=?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, game.whiteUsername());
            stmt.setString(2, game.blackUsername());
            stmt.setString(3, gson.toJson(game.game()));
            stmt.setInt(4, gameID);
            int changed = stmt.executeUpdate();
            if (changed == 0) {
                throw new DataAccessException("Error: game not found");
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error updating game", e);
        }
    }
}