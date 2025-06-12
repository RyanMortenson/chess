package server.websocket;

import chess.ChessGame;
import chess.ChessPiece;
import chess.ChessPosition;
import com.google.gson.Gson;
import dataaccess.*;
import model.GameData;
import websocket.commands.*;
import websocket.messages.*;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;


import java.io.IOException;

@WebSocket
public class WebSocketHandler {

    private static final ConnectionManager CONNECTIONS = new ConnectionManager();
    private static final GameDAO GAME_DAO = new MySqlGameDAO();
    private static final AuthDAO AUTH_DAO = new MySqlAuthDAO();
    private static final Gson GSON = new Gson();

    private Session session;

    @OnWebSocketConnect
    public void onConnect(Session session) {
        this.session = session;
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String text) throws IOException {
        try {
            UserGameCommand baseCommand = GSON.fromJson(text, UserGameCommand.class);

            switch (baseCommand.getCommandType()) {
                case CONNECT ->    handleConnect(session, GSON.fromJson(text, ConnectCommand.class));
                case MAKE_MOVE ->  handleMakeMove(session, GSON.fromJson(text, MakeMoveCommand.class));
                case LEAVE ->      handleLeave(session, GSON.fromJson(text, LeaveCommand.class));
                case RESIGN ->     handleResign(session, GSON.fromJson(text, ResignCommand.class));
                default -> throw new IllegalStateException("Unknown commandType: " + baseCommand.getCommandType());
            }
        }  catch (DataAccessException dae) {
            sendError(session, "Error: " + dae.getMessage());
        }
    }


    @OnWebSocketClose
    public void onClose(Session session, int status, String reason) {
        CONNECTIONS.removeSession(session);
    }



    private void handleConnect(Session session, ConnectCommand cmd)
            throws IOException, DataAccessException {
        var auth     = AUTH_DAO.getAuth(cmd.getAuthToken());
        String user  = auth != null ? auth.username() : null;
        var gameData = GAME_DAO.getGame(cmd.getGameID());
        if (user == null || gameData == null) {
            sendError(session, "Error: invalid auth or game ID");
            return;
        }

        CONNECTIONS.add(cmd.getGameID(), user, session);

        CONNECTIONS.sendTo(user, new LoadGameMessage(gameData));


        boolean isPlayer = user.equals(gameData.whiteUsername())
                || user.equals(gameData.blackUsername());
        String role = isPlayer
                ? (user.equals(gameData.whiteUsername()) ? "white player" : "black player")
                : "observer";


        CONNECTIONS.broadcast(cmd.getGameID(), user,
                new NotificationMessage(user + " connected as " + role)
        );
    }


    private void handleMakeMove(Session session, MakeMoveCommand cmd)
            throws IOException, DataAccessException {

        var auth = AUTH_DAO.getAuth(cmd.getAuthToken());
        String user = auth != null ? auth.username() : null;
        var gameData = GAME_DAO.getGame(cmd.getGameID());
        if (user == null || gameData == null) {
            sendError(session, "Error: invalid auth or game ID");
            return;
        }
        ChessGame game = gameData.game();

        ChessPosition start = cmd.getMove().getStartPosition();
        ChessPosition end   = cmd.getMove().getEndPosition();
        ChessPiece piece    = game.getBoard().getPiece(start);


        if (game.isGameOver()) {
            sendError(session, "Error: game is already over");
            return;
        }

        if (piece == null) {
            sendError(session, "Error: no piece at start square");
            return;
        }
        ChessGame.TeamColor userColor = user.equals(gameData.whiteUsername())
                ? ChessGame.TeamColor.WHITE
                : ChessGame.TeamColor.BLACK;
        if (piece.getTeamColor() != userColor) {
            sendError(session, "Error: cannot move opponent's piece");
            return;
        }

        if (game.getTeamTurn() != userColor) {
            sendError(session, "Error: not your turn");
            return;
        }

        try {
            game.makeMove(cmd.getMove());
        } catch (Exception ex) {
            sendError(session, "Error: illegal move");
            return;
        }

        GAME_DAO.updateGame(cmd.getGameID(), gameData);

        String moveMessage = String.format("%s moved %s from %s to %s", user, piece, start, end);

        CONNECTIONS.broadcastAll(cmd.getGameID(), new LoadGameMessage(gameData));
        CONNECTIONS.broadcast(cmd.getGameID(), user,
                new NotificationMessage(moveMessage));


        ChessGame.TeamColor opponent = (userColor == ChessGame.TeamColor.WHITE)
                ? ChessGame.TeamColor.BLACK : ChessGame.TeamColor.WHITE;
        String finalNotify = null;
        if (game.isInCheckmate(opponent)) {
            game.setGameOver(true);
            finalNotify = user + " checkmated opponent";
        } else if (game.isInStalemate(opponent)) {
            game.setGameOver(true);
            finalNotify = "Stalemate";
        } else if (game.isInCheck(opponent)) {
            finalNotify = user + " put opponent in check";
        }
        if (finalNotify != null) {
            CONNECTIONS.broadcast(cmd.getGameID(), null,
                    new NotificationMessage(finalNotify));
        }
    }


    private void handleLeave(Session session, LeaveCommand cmd) throws IOException, DataAccessException {
        var auth = AUTH_DAO.getAuth(cmd.getAuthToken());
        String user = auth != null ? auth.username() : null;
        var gameData = GAME_DAO.getGame(cmd.getGameID());
        if (user == null || gameData == null) {
            sendError(session, "Error: invalid auth or game ID");
            return;
        }

        boolean wasPlayer = false;
        if (user.equals(gameData.whiteUsername())) {
            gameData = new GameData(
                    gameData.gameID(),
                    null,
                    gameData.blackUsername(),
                    gameData.gameName(),
                    gameData.game()
            );
            wasPlayer = true;
        } else if (user.equals(gameData.blackUsername())) {
            gameData = new GameData(
                    gameData.gameID(),
                    gameData.whiteUsername(),
                    null,
                    gameData.gameName(),
                    gameData.game()
            );
            wasPlayer = true;
        }

        if (wasPlayer) {
            GAME_DAO.updateGame(cmd.getGameID(), gameData);
        }


        CONNECTIONS.broadcast(cmd.getGameID(), user,
                new NotificationMessage(user + " left the game"));

        CONNECTIONS.remove(cmd.getGameID(), user);
    }

    private void handleResign(Session session, ResignCommand cmd) throws IOException, DataAccessException {
        var auth = AUTH_DAO.getAuth(cmd.getAuthToken());
        String user = auth != null ? auth.username() : null;
        var gameData = GAME_DAO.getGame(cmd.getGameID());
        if (user == null || gameData == null) {
            sendError(session, "Error: invalid auth or game ID");
            return;
        }

        if (!user.equals(gameData.whiteUsername()) && !user.equals(gameData.blackUsername())) {
            sendError(session, "Error: observers cannot resign");
            return;
        }

        ChessGame game = gameData.game();

        if (game.isGameOver()) {
            sendError(session, "Error: game is already over");
            return;
        }

        game.setGameOver(true);
        GAME_DAO.updateGame(cmd.getGameID(), gameData);

        CONNECTIONS.broadcastAll(cmd.getGameID(), new NotificationMessage(user + " resigned"));
    }


    private void sendError(Session session, String msg) throws IOException {
        session.getRemote().sendString(GSON.toJson(new ErrorMessage(msg)));
    }
}
