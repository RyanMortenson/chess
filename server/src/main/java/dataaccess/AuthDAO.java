package dataaccess;

import model.AuthData;

public interface AuthDAO {
    void addAuth(AuthData authData) throws  DataAccessException;
    AuthData getAuth(String authToken) throws DataAccessException;
    void removeAuth(String authToken) throws DataAccessException;
    void clear();
}
