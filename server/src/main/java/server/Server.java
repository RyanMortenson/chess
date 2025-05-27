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


        exception(DataAccessException.class, (e, req, res) -> {
            res.type("application/json");
            res.status(500);
            res.body(gson.toJson(Map.of("message","Error: "+e.getMessage())));
        });


        before((req, res) -> {
            String path   = req.pathInfo();
            String method = req.requestMethod();

            // always allow the database‐clear endpoint
            if ("DELETE".equals(method) && "/db".equals(path)) {
                return;
            }

            // allow registration
            if ("POST".equals(method) && "/user/register".equals(path)) {
                return;
            }

            // allow login
            if ("POST".equals(method) && "/user/login".equals(path)) {
                return;
            }

            // everywhere else *requires* a token
            String token = req.headers("Authorization");
            try {
                authService.validateToken(token);
            } catch (UnauthorizedException ue) {
                halt(401, gson.toJson(Map.of("message", "Error: " + ue.getMessage())));
            }
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