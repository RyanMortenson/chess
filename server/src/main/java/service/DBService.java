package service;

import dataaccess.DataAccessException;
import dataaccess.UserDAO;
import dataaccess.AuthDAO;
import dataaccess.GameDAO;

public class DBService {
    private final UserDAO userDao;
    private final AuthDAO authDao;
    private final GameDAO gameDao;

    public DBService(UserDAO u, AuthDAO a, GameDAO g) {
        this.userDao = u;
        this.authDao = a;
        this.gameDao = g;
    }

    public void clear() throws DataAccessException {
        userDao.clear();
        authDao.clear();
        gameDao.clear();
    }
}