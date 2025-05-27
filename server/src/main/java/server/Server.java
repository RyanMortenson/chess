package server;

import com.google.gson.Gson;
import dataaccess.*;
import dataaccess.AuthDAO;
import dataaccess.GameDAO;
import service.AuthService;
import service.UserService;
import service.GameService;
import service.DBService;
import service.exceptions.UnauthorizedException;
import spark.Spark;


import java.util.Map;

import static spark.Spark.*;

public class Server {
    private final Gson gson;
    private final AuthService authService;
    private final UserService userService;
    private final GameService gameService;
    private final DBService databaseService;

    public Server() {
        this.gson             = new Gson();
        UserDAO userDao = new MySqlUserDAO();
        AuthDAO authDao = new MySqlAuthDAO();
        GameDAO gameDao = new MySqlGameDAO();
        this.authService      = new AuthService(authDao);
        this.userService      = new UserService(userDao, authDao);
        this.gameService      = new GameService(gameDao, authDao);
        this.databaseService  = new DBService(userDao, authDao, gameDao);
    }


    public int run(int desiredPort) {
        try {
            dataaccess.DatabaseInitializer.initialize();
        } catch (Exception e) {
            System.err.println("Failed to initialize schema: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }

        port(desiredPort);
        staticFiles.location("web");


        // 1) global 500‐handler for any real DataAccessException
        exception(DataAccessException.class, (e, req, res) -> {
            res.type("application/json");
            res.status(500);
            res.body(gson.toJson(Map.of("message","Error: "+e.getMessage())));
        });

// 2) auth filter
        before((req, res) -> {
            String p = req.pathInfo();
            String m = req.requestMethod();

            // --- open endpoints:
            if ("/db".equals(p))            return;  // clear
            if ("POST".equals(m) && "/user".equals(p))    return;  // register
            if ("/session".equals(p))       return;  // both login (POST) and logout (DELETE)

            // --- everything else MUST have a token
            String token = req.headers("Authorization");
            if (token == null) {
                halt(401, gson.toJson(Map.of("message","Error: missing token")));
            }
            // if the DB is down, this next line will throw DataAccessException,
            // and that will bubble straight to your 500‐handler above.
            authService.validateToken(token);
        });

// 3) now register your routes
        new UserHandler(  userService, gson).registerRoutes();   // POST /user
        new AuthHandler(  userService, authService, gson).registerRoutes();
        new GameHandler( gameService,  gson).registerRoutes();   // /game…

// 4) finally the clear‐DB route
        delete("/db", (req, res) -> {
            databaseService.clear();   // if this throws DataAccessException, the exception‐handler above will catch it
            res.status(200);
            return "{}";
        });





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