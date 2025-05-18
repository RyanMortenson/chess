package dataaccess;

import model.AuthData;

import java.util.HashMap;
import java.util.Map;

public class MemoryAuthDAO implements AuthDAO{
    private final Map<String, AuthData> storage;

    MemoryAuthDAO() {
        this.storage = new HashMap<>();
    }

    @Override
    public void addAuth(AuthData authData) throws DataAccessException{
        String token = authData.authToken();
        if (storage.containsKey(token)) {
            throw new DataAccessException("Auth token exists.");
        }
        storage.put(token, authData);
    }

    @Override
    public AuthData getAuth(String authToken) throws DataAccessException {
        AuthData auth = storage.get(authToken);
        if (auth == null) {
            throw new DataAccessException("Invalid auth token.");
        }
        return auth;
    }

    @Override
    public void removeAuth(String authToken) throws DataAccessException {
        if (storage.remove(authToken) == null) {
            throw new DataAccessException("Auth token not found.");
        }
    }

    @Override
    public void clear() {
        storage.clear();
    }

}
