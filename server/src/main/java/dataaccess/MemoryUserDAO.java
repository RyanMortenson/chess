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
    public UserData getUser(String username) {
        return storage.get(username);
    }

    @Override
    public void createUser(String username, String password, String email) {
        storage.put(username, new UserData(username, password, email));
    }

    @Override
    public boolean authenticateUser(String username, String password) {
        UserData user = storage.get(username);
        return user != null && user.password().equals(password);
    }

    @Override
    public void clear() {
        storage.clear();
    }

}
