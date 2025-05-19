package dataaccess;

import model.GameData;

import java.util.*;

public class MemoryGameDAO implements GameDAO{
    private final Map<Integer, GameData> storage;
    private int nextID;

    public MemoryGameDAO() {
        this.storage = new HashMap<>();
        this.nextID = 1;
    }

    @Override
    public int createGame(GameData game) {
        int id = nextID++;
        GameData withID = new GameData(id,
                game.whiteUsername(),
                game.blackUsername(),
                game.gameName(),
                game.game());
        storage.put(id, withID);
        return id;
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        GameData game = storage.get(gameID);
        if (game == null) {
            throw new DataAccessException("Game does not exist: " + gameID);
        }
        return game;
    }

    @Override
    public List<GameData> listGames() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public void updateGame(int gameID, GameData updated) throws DataAccessException {
        if (!storage.containsKey(gameID)) {
            throw new DataAccessException("Game "+gameID+" not found.");
        }
        storage.put(gameID, new GameData(gameID,
                updated.whiteUsername(),
                updated.blackUsername(),
                updated.gameName(),
                updated.game()));
    }

    @Override
    public void clear() {
        storage.clear();
        nextID=1;
    }
}
