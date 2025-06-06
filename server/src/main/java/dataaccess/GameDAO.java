package dataaccess;

import model.GameData;
import java.util.List;

public interface GameDAO {

    int createGame(GameData game) throws DataAccessException;
    GameData getGame(int gameID) throws DataAccessException;
    List<GameData> listGames() throws DataAccessException;
    void updateGame(int gameID, GameData updated) throws DataAccessException;
    void clear() throws DataAccessException;
}