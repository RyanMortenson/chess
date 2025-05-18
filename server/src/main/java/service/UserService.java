package service;

import dataaccess.DataAccessException;
import model.UserData;
import service.requests.LoginRequest;
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

    public LoginResult login(LoginRequest request) throws DataAccessException {
        boolean correctPassword = userDao.authenticateUser(request.username(), request.password());
        if (!correctPassword) {
            throw new DataAccessException("Error: unauthorized");
        }

        String authToken = generateNewAuthToken();
        authDao.addAuth(new AuthData(authToken, request.username()));
        return new LoginResult(request.username(), authToken);
    }


    private void validateRegistration(RegisterRequest request) {
        if (request.username() == null || request.password() == null || request.email() == null) {
            throw new IllegalArgumentException("All fields required");
        }
    }

    private String generateNewAuthToken() {
        return UUID.randomUUID().toString();
    }
}
