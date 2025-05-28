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
        if (token == null || token.isBlank()) {
            throw new UnauthorizedException("missing token");
        }
        try {
            AuthData auth = authDao.getAuth(token);
            if (auth == null) {
                throw new UnauthorizedException("invalid token");
            }
            return auth;
        } catch (DataAccessException dae) {
            throw dae;
        }
    }

    public void revokeToken(String authToken) throws DataAccessException {
        if (authToken == null) {
            throw new DataAccessException("Error: unauthorized");
        }
        authDao.removeAuth(authToken);
    }
}