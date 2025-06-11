package ui;

import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPiece;
import chess.ChessPosition;
import exception.ResponseException;
import websocket.WebSocketFacade;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;
import websocket.messages.ServerMessage;

import static ui.EscapeSequences.*;

import java.io.IOException;
import java.util.Collection;
import java.util.Scanner;

public class GameplayClient {
    private final String baseUrl;
    private String authToken;
    private ChessGame game;
    private final String perspective;
    private WebSocketFacade ws;
    private final int gameID;

    public GameplayClient(String baseUrl, String authToken, int gameID, String perspective) throws ResponseException {
        this.baseUrl     = baseUrl;
        this.authToken   = authToken;
        this.gameID      = gameID;
        this.game        = new ChessGame();
        this.perspective = perspective;
        this.ws = new WebSocketFacade(baseUrl, this::handleServerMessage);
        ws.sendConnect(authToken, gameID);
        this.ws.sendConnect(authToken, gameID);
    }


    public void run() throws IOException {
        try {
            Scanner scanner = new Scanner(System.in);
            redrawBoard(game);
            displayHelp();
            while (true) {
                System.out.print(SET_TEXT_COLOR_CYAN + "Game >>> " + RESET_TEXT_COLOR);
                String line = scanner.nextLine().trim();
                if (line.isEmpty()) continue;

                //parse the commands
                String[] parts = line.split("\\s+");
                String cmd = parts[0].toLowerCase();

                switch (cmd) {
                    case "help" -> displayHelp();
                    case "redraw" -> redrawBoard(game);
                    case "move" -> {
                        if (parts.length < 3) {
                            System.out.println("Usage: move <from> <to>");
                            break;
                        }
                        try {
                            ChessPosition from = parsePosition(parts[1]);
                            ChessPosition to = parsePosition(parts[2]);
                            ws.sendMakeMove(authToken, gameID, new ChessMove(from, to, null));
                        } catch (Exception e) {
                            System.out.println(SET_TEXT_COLOR_YELLOW + "Error: illegal move" + RESET_TEXT_COLOR);
                        }
                    }
                    case "highlight" -> {
                        if (parts.length < 2) {
                            System.out.println("Usage: highlight <position>");
                            break;
                        }
                        try {
                            ChessPosition pos = parsePosition(parts[1]);
                            highlightLegalMoves(game, pos);
                        } catch (Exception e) {
                            System.out.println(SET_TEXT_COLOR_YELLOW + "Error: invalid position" + RESET_TEXT_COLOR);
                        }
                    }
                    case "resign" -> {
                        ws.sendResign(authToken, gameID);
                        System.out.println(SET_TEXT_COLOR_YELLOW + "You resigned." + RESET_TEXT_COLOR);
                        return;
                    }
                    case "leave" -> {
                        ws.sendLeave(authToken, gameID);
                        System.out.println(SET_TEXT_COLOR_YELLOW + "Leaving game." + RESET_TEXT_COLOR);
                        return;
                    }
                    default -> System.out.println(SET_TEXT_COLOR_YELLOW + "Unknown command" + RESET_TEXT_COLOR);
                }
            }
        } finally {
            ws.close();
        }
    }

    private ChessPosition parsePosition(String input) {
        String s = input.toLowerCase();
        if (s.length() != 2) throw new IllegalArgumentException("Invalid position: " + input);
        int col = s.charAt(0) - 'a' + 1;
        int row = Character.getNumericValue(s.charAt(1));
        return new ChessPosition(row, col);
    }


    private void displayHelp() {
        System.out.println(SET_TEXT_COLOR_CYAN + "Available commands:" + RESET_TEXT_COLOR);
        System.out.println("  help      - show this help text");
        System.out.println("  redraw    - redraw the board");
        System.out.println("  move      - make a move (e.g. move e2 e4)");
        System.out.println("  highlight - highlight legal moves for a piece (e.g. highlight e2)");
        System.out.println("  resign    - resign the game");
        System.out.println("  leave     - leave the game");
    }


    private void redrawBoard(ChessGame game) {
        String[][] board = buildBoardMatrix(game);
        printBoard(board);
    }


    private void highlightLegalMoves(ChessGame game, ChessPosition pos) {
        String[][] board = buildBoardMatrix(game);
        Collection<ChessMove> moves = game.validMoves(pos);

        int sr = pos.getRow();
        int sc = pos.getColumn();
        board[sr][sc] = SET_BG_COLOR_YELLOW + board[sr][sc] + RESET_BG_COLOR;

        for (ChessMove m : moves) {
            ChessPosition end = m.getEndPosition();
            int er = end.getRow();
            int ec = end.getColumn();
            board[er][ec] = SET_BG_COLOR_GREEN + board[er][ec] + RESET_BG_COLOR;
        }

        printBoard(board);
    }

    private String[][] buildBoardMatrix(ChessGame game) {
        String[][] board = new String[9][9];
        for (int r = 1; r <= 8; r++) {
            for (int c = 1; c <= 8; c++) {
                ChessPiece piece = game.getBoard().getPiece(new ChessPosition(r, c));
                if (piece == null) {
                    board[r][c] = " ";
                } else {
                    String symbol;
                    switch (piece.getPieceType()) {
                        case KING   -> symbol = piece.getTeamColor() == ChessGame.TeamColor.WHITE ? WHITE_KING   : BLACK_KING;
                        case QUEEN  -> symbol = piece.getTeamColor() == ChessGame.TeamColor.WHITE ? WHITE_QUEEN  : BLACK_QUEEN;
                        case ROOK   -> symbol = piece.getTeamColor() == ChessGame.TeamColor.WHITE ? WHITE_ROOK   : BLACK_ROOK;
                        case BISHOP -> symbol = piece.getTeamColor() == ChessGame.TeamColor.WHITE ? WHITE_BISHOP : BLACK_BISHOP;
                        case KNIGHT -> symbol = piece.getTeamColor() == ChessGame.TeamColor.WHITE ? WHITE_KNIGHT : BLACK_KNIGHT;
                        case PAWN   -> symbol = piece.getTeamColor() == ChessGame.TeamColor.WHITE ? WHITE_PAWN   : BLACK_PAWN;
                        default     -> symbol = "  ";
                    }
                    String colored = piece.getTeamColor() == ChessGame.TeamColor.WHITE
                            ? SET_TEXT_COLOR_WHITE + symbol + RESET_TEXT_COLOR
                            : SET_TEXT_COLOR_BLACK + symbol + RESET_TEXT_COLOR;
                    board[r][c] = colored;
                }
            }
        }
        return board;
    }

    private void printBoard(String[][] board) {
        boolean flip = "BLACK".equalsIgnoreCase(perspective);
        printColumnLabels(flip);
        for (int rank = 8; rank >= 1; rank--) {
            printRank(board, rank, flip);
        }
        printColumnLabels(flip);
    }

    private void printColumnLabels(boolean flip) {
        String labels = flip
                ? "    h  g  f  e  d  c  b  a    "
                : "    a  b  c  d  e  f  g  h    ";
        System.out.println(SET_BG_COLOR_BLUE + SET_TEXT_COLOR_BLACK + SET_TEXT_BOLD
                + labels
                + RESET_TEXT_COLOR + RESET_BG_COLOR + RESET_TEXT_BOLD_FAINT);
    }

    private void printRank(String[][] board, int rank, boolean flip) {
        int display = flip ? 9 - rank : rank;
        StringBuilder line = new StringBuilder();
        line.append(SET_BG_COLOR_BLUE).append(SET_TEXT_COLOR_BLACK).append(SET_TEXT_BOLD)
                .append(" ").append(display).append(" ")
                .append(RESET_TEXT_COLOR).append(RESET_BG_COLOR).append(RESET_TEXT_BOLD_FAINT);
        for (int file = 1; file <= 8; file++) {
            int c = flip ? 9 - file : file;
            int r = flip ? 9 - rank : rank;
            boolean dark = ((r + c) % 2 == 0);
            String bg = dark ? SET_BG_COLOR_DARK_GREY : SET_BG_COLOR_LIGHT_GREY;
            line.append(bg).append(" ").append(board[r][c]).append(" ")
                    .append(RESET_TEXT_COLOR).append(RESET_BG_COLOR);
        }
        line.append(SET_BG_COLOR_BLUE).append(SET_TEXT_COLOR_BLACK).append(SET_TEXT_BOLD)
                .append(" ").append(display).append(" ")
                .append(RESET_TEXT_COLOR).append(RESET_BG_COLOR).append(RESET_TEXT_BOLD_FAINT);
        System.out.println(line.toString());
    }

    private void handleServerMessage(ServerMessage msg) {
        switch (msg.getServerMessageType()) {
            case LOAD_GAME -> {
                var load = (LoadGameMessage) msg;
                this.game = load.getGame().game();
                redrawBoard(game);
            }
            case NOTIFICATION -> {
                var note = (NotificationMessage) msg;
                System.out.println(SET_TEXT_COLOR_YELLOW + note.message + RESET_TEXT_COLOR);
            }
            case ERROR -> {
                var err = (ErrorMessage) msg;
                System.out.println(SET_TEXT_COLOR_RED + err.errorMessage + RESET_TEXT_COLOR);
            }
        }
    }
}
