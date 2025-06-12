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
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;

public class GameplayClient {
    private final String baseUrl;
    private String authToken;
    private ChessGame game;
    private final String perspective;
    private WebSocketFacade ws;
    private final int gameID;
    private boolean gameOver = false;

    // for highlight
    private ChessPosition selectedPosition = null;
    private Set<ChessPosition> highlightPositions = new HashSet<>();

    public GameplayClient(String baseUrl, String authToken, int gameID, String perspective) throws ResponseException {
        this.baseUrl     = baseUrl;
        this.authToken   = authToken;
        this.gameID      = gameID;
        this.game        = new ChessGame();
        this.perspective = perspective;
        this.ws = new WebSocketFacade(baseUrl, this::handleServerMessage);
    }

    public void run() throws IOException {
        try {
            Scanner scanner = new Scanner(System.in);
            displayHelp();
            ws.sendConnect(authToken, gameID);
            printPrompt();

            while (true) {
                if (gameOver) {
                    System.out.println("Game over. Returning to menu.");
                    break;
                }

                String line = scanner.nextLine().trim();
                if (line.isEmpty()) {
                    printPrompt();
                    continue;
                }

                String[] parts = line.split("\\s+");
                String cmd = parts[0].toLowerCase();

                switch (cmd) {
                    case "help"      -> { displayHelp(); }
                    case "redraw"    -> { redrawBoard(game); }
                    case "move"      -> handleMove(parts);
                    case "highlight" -> handleHighlight(parts);
                    case "resign"    -> {
                        ws.sendResign(authToken, gameID);
                        System.out.println(SET_TEXT_COLOR_YELLOW + "You resigned." + RESET_TEXT_COLOR);
                        return;
                    }
                    case "leave"     -> {
                        ws.sendLeave(authToken, gameID);
                        System.out.println(SET_TEXT_COLOR_YELLOW + "Leaving game." + RESET_TEXT_COLOR);
                        return;
                    }
                    default          -> System.out.println(SET_TEXT_COLOR_YELLOW + "Unknown command" + RESET_TEXT_COLOR);
                }

                printPrompt();
            }
        } finally {
            ws.close();
        }
    }


    private void handleMove(String[] parts) {
        if (parts.length < 3) {
            System.out.println("Usage: move <from> <to>");
            return;
        }
        try {
            ChessPosition from = parsePosition(parts[1]);
            ChessPosition to   = parsePosition(parts[2]);
            ws.sendMakeMove(authToken, gameID, new ChessMove(from, to, null));
        } catch (Exception e) {
            System.out.println(SET_TEXT_COLOR_YELLOW + "Error: illegal move" + RESET_TEXT_COLOR);
        }
    }


    private void handleHighlight(String[] parts) {
        if (parts.length < 2) {
            System.out.println("Usage: highlight <position>");
            return;
        }
        try {
            ChessPosition pos = parsePosition(parts[1]);
            selectedPosition = pos;
            highlightPositions = game.validMoves(pos).stream()
                    .map(ChessMove::getEndPosition)
                    .collect(Collectors.toSet());

            redrawBoard(game);
        } catch (Exception e) {
            System.out.println(SET_TEXT_COLOR_YELLOW + "Error: invalid position" + RESET_TEXT_COLOR);
        }
    }

    private ChessPosition parsePosition(String input) {
        String s = input.toLowerCase();
        if (s.length() != 2) {throw new IllegalArgumentException("Invalid position: " + input);}
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
        selectedPosition = null;
        highlightPositions.clear();

        String[][] board = buildBoardMatrix(game);
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
                    String symbol = switch (piece.getPieceType()) {
                        case KING   -> piece.getTeamColor() == ChessGame.TeamColor.WHITE ? WHITE_KING   : BLACK_KING;
                        case QUEEN  -> piece.getTeamColor() == ChessGame.TeamColor.WHITE ? WHITE_QUEEN  : BLACK_QUEEN;
                        case ROOK   -> piece.getTeamColor() == ChessGame.TeamColor.WHITE ? WHITE_ROOK   : BLACK_ROOK;
                        case BISHOP -> piece.getTeamColor() == ChessGame.TeamColor.WHITE ? WHITE_BISHOP : BLACK_BISHOP;
                        case KNIGHT -> piece.getTeamColor() == ChessGame.TeamColor.WHITE ? WHITE_KNIGHT : BLACK_KNIGHT;
                        case PAWN   -> piece.getTeamColor() == ChessGame.TeamColor.WHITE ? WHITE_PAWN   : BLACK_PAWN;
                        default     -> "  ";
                    };
                    board[r][c] = (piece.getTeamColor() == ChessGame.TeamColor.WHITE
                            ? SET_TEXT_COLOR_WHITE
                            : SET_TEXT_COLOR_BLACK)
                            + symbol
                            + RESET_TEXT_COLOR;
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
        var line = new StringBuilder();

        // left label
        line.append(SET_BG_COLOR_BLUE).append(SET_TEXT_COLOR_BLACK).append(SET_TEXT_BOLD)
                .append(" ").append(display).append(" ")
                .append(RESET_TEXT_COLOR).append(RESET_BG_COLOR).append(RESET_TEXT_BOLD_FAINT);

        for (int file = 1; file <= 8; file++) {
            int c = flip ? 9 - file : file;
            int r = flip ? 9 - rank : rank;

            boolean isSelected   = selectedPosition != null
                    && selectedPosition.getRow() == r
                    && selectedPosition.getColumn() == c;
            boolean isHighlighted = highlightPositions.contains(new ChessPosition(r, c));

            String bg;
            if (isSelected) {
                bg = SET_BG_COLOR_YELLOW;
            } else if (isHighlighted) {
                bg = SET_BG_COLOR_GREEN;
            } else {
                boolean dark = ((r + c) % 2 == 0);
                bg = dark ? SET_BG_COLOR_DARK_GREY : SET_BG_COLOR_LIGHT_GREY;
            }

            line.append(bg)
                    .append(" ")
                    .append(board[r][c])
                    .append(" ")
                    .append(RESET_TEXT_COLOR)
                    .append(RESET_BG_COLOR);
        }

        // right label
        line.append(SET_BG_COLOR_BLUE).append(SET_TEXT_COLOR_BLACK).append(SET_TEXT_BOLD)
                .append(" ").append(display).append(" ")
                .append(RESET_TEXT_COLOR).append(RESET_BG_COLOR).append(RESET_TEXT_BOLD_FAINT);

        System.out.println(line.toString());
    }

    private void handleServerMessage(ServerMessage msg) {
        System.out.println();

        switch (msg.getServerMessageType()) {
            case LOAD_GAME -> {
                var load = (LoadGameMessage) msg;
                this.game = load.getGame().game();
                redrawBoard(game);
                printPrompt();
            }
            case NOTIFICATION -> {
                var note = (NotificationMessage) msg;
                System.out.println(SET_TEXT_COLOR_YELLOW + note.message + RESET_TEXT_COLOR);

                String m = note.message.toLowerCase();
                if (m.contains("resigned") || m.contains("checkmated") || m.contains("stalemate")) {
                    gameOver = true;
                }

                printPrompt();
            }
            case ERROR -> {
                var err = (ErrorMessage) msg;
                System.out.println(SET_TEXT_COLOR_RED + err.errorMessage + RESET_TEXT_COLOR);

                if (err.errorMessage.toLowerCase().contains("already over")) {
                    gameOver = true;
                }

                printPrompt();
            }
        }
    }

    private void printPrompt() {
        System.out.print(SET_TEXT_COLOR_CYAN + "Game >>> " + RESET_TEXT_COLOR);
    }
}
