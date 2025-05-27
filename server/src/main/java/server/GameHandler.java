package server;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import dataaccess.DataAccessException;
import service.GameService;
import service.exceptions.AlreadyTakenException;
import service.exceptions.NotFoundException;
import service.exceptions.UnauthorizedException;
import service.requests.CreateGameRequest;
import service.requests.JoinGameRequest;
import service.results.CreateGameResult;
import service.results.ListGamesResult;
import spark.Request;
import spark.Response;

import java.util.Map;

import static spark.Spark.get;
import static spark.Spark.post;
import static spark.Spark.put;

public class GameHandler {
    private final GameService gameService;
    private final Gson gson;

    public GameHandler(GameService gameService, Gson gson) {
        this.gameService = gameService;
        this.gson = gson;
    }

    public void registerRoutes() {
        post("/game", this::handleCreateGame);
        get("/game", this::handleListGames);
        put("/game", this::handleJoinGame);
    }

    private Object handleCreateGame(Request req, Response res) {
        String token = req.headers("authorization");
        if (token == null || token.isBlank()) {
            res.status(401);
            return errorBody("unauthorized");
        }

        CreateGameRequest createReq;
        try {
            createReq = gson.fromJson(req.body(), CreateGameRequest.class);
        } catch (JsonSyntaxException e) {
            res.status(400);
            return errorBody("bad request");
        }
        if (createReq == null || createReq.gameName() == null) {
            res.status(400);
            return errorBody("bad request");
        }

        try {
            CreateGameResult result = gameService.createGame(createReq, token);
            res.status(200);
            return gson.toJson(result);

        } catch (UnauthorizedException e) {
            res.status(401);
            return errorBody("unauthorized");

        } catch (DataAccessException e) {
            return mapDataAccessError(res, e);

        } catch (Exception e) {
            res.status(500);
            return errorBody("internal error");
        }
    }

    private Object handleListGames(Request req, Response res) {
        String token = req.headers("authorization");
        if (token == null || token.isBlank()) {
            res.status(401);
            return errorBody("unauthorized");
        }

        try {
            ListGamesResult result = gameService.listGames(token);
            res.status(200);
            return gson.toJson(result);

        } catch (DataAccessException e) {
            return mapDataAccessError(res, e);

        } catch (Exception e) {
            res.status(500);
            return errorBody("internal error");
        }
    }

    private Object handleJoinGame(Request req, Response res) {
        String token = req.headers("authorization");
        if (token == null || token.isBlank()) {
            res.status(401);
            return errorBody("unauthorized");
        }

        JoinGameRequest joinReq;
        try {
            joinReq = gson.fromJson(req.body(), JoinGameRequest.class);
        } catch (JsonSyntaxException e) {
            res.status(400);
            return errorBody("bad request");
        }
        if (joinReq == null || joinReq.gameID() == null || joinReq.playerColor() == null) {
            res.status(400);
            return errorBody("authToken, gameID and teamColor required");
        }

        try {
            gameService.joinGame(joinReq, token);
            res.status(200);
            return "{}";

        } catch (IllegalArgumentException e) {
            res.status(400);
            return errorBody("bad request");

        } catch (AlreadyTakenException e) {
            res.status(403);
            return errorBody("already taken");

        } catch (NotFoundException e) {
            res.status(404);
            return errorBody("game not found");

        } catch (DataAccessException e) {
            return mapDataAccessError(res, e);

        } catch (Exception e) {
            res.status(500);
            return errorBody("internal error");
        }
    }

    private Object mapDataAccessError(Response res, DataAccessException e) {
        String msg = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
        if (msg.contains("unauthorized") || msg.contains("invalid token")) {
            res.status(401);
            return errorBody("unauthorized");
        } else {
            res.status(500);
            return errorBody("internal server error");
        }
    }

    private String errorBody(String msg) {
        return gson.toJson(Map.of("message", "Error: " + msg));
    }
}