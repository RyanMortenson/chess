package dataaccess;

import model.GameData;
import java.util.List;

public interface GameDAO {

    int createGame(GameData game);
    GameData getGame(int gameID) throws DataAccessException;
    List<GameData> listGames();
    void updateGame(int gameID, GameData updated) throws DataAccessException;
    void clear();
}
