package server;

import com.google.gson.Gson;
import dataaccess.UserDAO;
import dataaccess.AuthDAO;
import dataaccess.GameDAO;
import dataaccess.MemoryUserDAO;
import dataaccess.MemoryAuthDAO;
import dataaccess.MemoryGameDAO;
import service.AuthService;
import service.UserService;
import service.GameService;
import service.DBService;
import server.UserHandler;
import server.AuthHandler;
import server.GameHandler;
import spark.Spark;

import static spark.Spark.*;

public class Server {
    private final Gson gson;
    private final UserDAO userDao;
    private final AuthDAO authDao;
    private final GameDAO gameDao;
    private final AuthService authService;
    private final UserService userService;
    private final GameService gameService;
    private final DBService databaseService;

    public Server() {
        this.gson             = new Gson();
        this.userDao          = new MemoryUserDAO();
        this.authDao          = new MemoryAuthDAO();
        this.gameDao          = new MemoryGameDAO();
        this.authService      = new AuthService(authDao);
        this.userService      = new UserService(userDao, authDao);
        this.gameService      = new GameService(gameDao, authDao);
        this.databaseService  = new DBService(userDao, authDao, gameDao);
    }


    public int run(int desiredPort) {
        port(desiredPort);
        staticFiles.location("web");

        new UserHandler(userService, gson).registerRoutes();
        new AuthHandler(userService, authService, gson).registerRoutes();
        new GameHandler(gameService, gson).registerRoutes();


        delete("/db", (req, res) -> {
            databaseService.clear();
            res.status(200);
            return "{}";
        });

        awaitInitialization();
        return port();
    }

    public void stop() {
        Spark.stop();
    }
}
