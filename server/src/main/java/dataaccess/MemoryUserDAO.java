package dataaccess;

import model.UserData;

import java.util.HashMap;
import java.util.Map;

public class MemoryUserDAO implements UserDAO{
    private final Map<String, UserData> storage;

    MemoryUserDAO() {
        this.storage = new HashMap<>();
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        UserData user = storage.get(username);
        if (user == null){
            throw new DataAccessException("User not found.");
        }
        return user;
    }

    @Override
    public void createUser(UserData userData) throws DataAccessException {
        if (storage.containsKey(userData.username())) {
            throw new DataAccessException("Username already taken.");
        }
        storage.put(userData.username(), userData);
    }

    @Override
    public boolean authenticateUser(String username, String password) {
        UserData user = storage.get(username);
        return user != null && password.equals(user.password());
    }

    @Override
    public void clear() {
        storage.clear();
    }

}
