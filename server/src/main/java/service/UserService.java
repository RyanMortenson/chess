package service;

import dataaccess.DataAccessException;
import dataaccess.MemoryUserDAO;
import model.UserData;
import service.exceptions.UnauthorizedException;
import service.requests.LoginRequest;
import service.requests.LogoutRequest;
import service.requests.RegisterRequest;
import service.results.LoginResult;
import service.results.RegisterResult;
import dataaccess.UserDAO;
import dataaccess.AuthDAO;
import model.AuthData;

import java.util.UUID;

public class UserService {
    private final UserDAO userDao;
    private final AuthDAO authDao;

    public UserService(UserDAO userDao, AuthDAO authDao) {
        this.userDao = userDao;
        this.authDao = authDao;
    }

    public RegisterResult register(RegisterRequest request) throws DataAccessException {
        userDao.createUser(new UserData(request.username(),
                request.password(),
                request.email()));

        String authToken = generateNewAuthToken();

        authDao.addAuth(new AuthData(authToken, request.username()));

        return new RegisterResult(request.username(), authToken);
    }

    public LoginResult login(LoginRequest req) throws UnauthorizedException, DataAccessException {
        UserData stored = userDao.getUser(req.username());

        if (!stored.password().equals(req.password())) {
            throw new UnauthorizedException("invalid credentials");
        }

        String token = UUID.randomUUID().toString();
        AuthData auth = new AuthData(token, req.username());
        authDao.addAuth(auth);

        return new LoginResult(req.username(), token);
    }


    public void logout(LogoutRequest request) throws DataAccessException {
        if (request.authToken() == null) {
            throw new DataAccessException("Error: unauthorized");
        }
        authDao.removeAuth(request.authToken());
    }

    private String generateNewAuthToken() {
        return UUID.randomUUID().toString();
    }
}
