package service;

import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import model.AuthData;
import service.exceptions.UnauthorizedException;

import java.util.UUID;

public class AuthService {
    private final AuthDAO authDao;

    public AuthService(AuthDAO authDao) {
        this.authDao = authDao;
    }

    public AuthData issueToken(String username) throws DataAccessException {
        String token = UUID.randomUUID().toString();
        AuthData auth = new AuthData(token, username);
        authDao.addAuth(auth);
        return auth;
    }

    public AuthData validateToken(String token) throws DataAccessException, UnauthorizedException {
        if (token == null) {
            throw new UnauthorizedException("missing token");
        }
        try {
            // this will throw DataAccessException if the token isn't in the DAO
            return authDao.getAuth(token);
        } catch (DataAccessException e) {
            // convert a missing/invalid‐token DAO error into UnauthorizedException
            throw new UnauthorizedException("invalid token");
        }
    }

    public void revokeToken(String authToken) throws DataAccessException {
        if (authToken == null) {
            throw new DataAccessException("Error: unauthorized");
        }
        authDao.removeAuth(authToken);
    }
}