package websocket.commands;

import chess.ChessMove;


public class MakeMoveCommand extends UserGameCommand {


    public MakeMoveCommand(String authToken, Integer gameID, ChessMove move) {
        super(CommandType.MAKE_MOVE, authToken, gameID, move);
    }

}
