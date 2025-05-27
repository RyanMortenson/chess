package service;

import service.exceptions.AlreadyTakenException;
import service.exceptions.NotFoundException;
import service.exceptions.UnauthorizedException;
import service.requests.*;
import service.results.*;
import dataaccess.GameDAO;
import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import model.GameData;
import model.AuthData;

import java.util.*;

public class GameService {
    private final GameDAO gameDao;
    private final AuthDAO authDao;

    public GameService(GameDAO gameDao,
                       AuthDAO authDao) {
        this.gameDao = gameDao;
        this.authDao = authDao;
    }

    public CreateGameResult createGame(CreateGameRequest request,
                                       String authToken)
            throws UnauthorizedException, DataAccessException {
        AuthData auth = authDao.getAuth(authToken);
        if (auth == null) {throw new UnauthorizedException("invalid token");}

        if (request.gameName() == null) {
            throw new DataAccessException("Game name is required");
        }

        GameData template = new GameData(0,
                null,
                null,
                request.gameName(),
                null);

        int id = gameDao.createGame(template);
        return new CreateGameResult(id);
    }


    public ListGamesResult listGames(String authToken) throws DataAccessException {
        authDao.getAuth(authToken);
        List<GameData> allGames = gameDao.listGames();
        return new ListGamesResult(allGames);
    }


    public void joinGame(JoinGameRequest request, String authToken)
            throws UnauthorizedException,
            AlreadyTakenException,
            NotFoundException,
            DataAccessException
    {
        AuthData auth;
        try {
            auth = authDao.getAuth(authToken);
        } catch (DataAccessException e) {
            throw new UnauthorizedException("invalid token");
        }
        if (auth == null) {
            throw new UnauthorizedException("invalid token");
        }

        GameData game;
        try {
            game = gameDao.getGame(request.gameID());
        } catch (DataAccessException dae) {
            throw new NotFoundException("Game " + request.gameID() + " not found");
        }
        if (game == null) {
            throw new NotFoundException("Game " + request.gameID() + " not found");
        }


        GameData updated;
        String user = auth.username();
        if ("WHITE".equals(request.playerColor())) {
            if (game.whiteUsername() != null) {
                throw new AlreadyTakenException();
            }

            updated = new GameData(
                    game.gameID(),
                    user,
                    game.blackUsername(),
                    game.gameName(),
                    game.game()
            );

        } else if ("BLACK".equals(request.playerColor())) {
            if (game.blackUsername() != null) {
                throw new AlreadyTakenException();
            }
            updated = new GameData(
                    game.gameID(),
                    game.whiteUsername(),
                    user,
                    game.gameName(),
                    game.game()
            );

        } else {
            throw new IllegalArgumentException("Invalid teamColor");
        }

        gameDao.updateGame(game.gameID(), updated);
    }
}