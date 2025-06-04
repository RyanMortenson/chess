package ui;

import static ui.EscapeSequences.*;

public class GameplayClient {

    private final String perspective;

    public GameplayClient(int gameID, String perspective) {
        this.perspective = perspective;
    }

    public void drawInitialBoard() {
        String[][] board = initBoard();
        boolean flip = perspective.equalsIgnoreCase("BLACK");

        printColumnLabels(flip);
        for (int rank = 8; rank >= 1; rank--) {
            printRank(board, rank, flip);
        }
        printColumnLabels(flip);

        System.out.println(SET_TEXT_COLOR_CYAN
                + "\n(Initial position of game)\n"
                + RESET_TEXT_COLOR);
    }

    private String[][] initBoard() {
        String[][] board = new String[9][9];
        board[8][1] = BLACK_ROOK;
        board[8][2] = BLACK_KNIGHT;
        board[8][3] = BLACK_BISHOP;
        board[8][4] = BLACK_QUEEN;
        board[8][5] = BLACK_KING;
        board[8][6] = BLACK_BISHOP;
        board[8][7] = BLACK_KNIGHT;
        board[8][8] = BLACK_ROOK;
        for (int c = 1; c <= 8; c++) {
            board[7][c] = BLACK_PAWN;
        }
        for (int r = 3; r <= 6; r++) {
            for (int c = 1; c <= 8; c++) {
                board[r][c] = EMPTY;
            }
        }
        for (int f = 1; f <= 8; f++) {
            board[2][f] = WHITE_PAWN;
        }
        board[1][1] = WHITE_ROOK;
        board[1][2] = WHITE_KNIGHT;
        board[1][3] = WHITE_BISHOP;
        board[1][4] = WHITE_QUEEN;
        board[1][5] = WHITE_KING;
        board[1][6] = WHITE_BISHOP;
        board[1][7] = WHITE_KNIGHT;
        board[1][8] = WHITE_ROOK;
        return board;
    }

    private void printColumnLabels(boolean flip) {
        if (!flip) {
            System.out.println(SET_BG_COLOR_BLUE + SET_TEXT_COLOR_BLACK + SET_TEXT_BOLD
                    + "    a  b  c  d  e  f  g  h    "
                    + RESET_TEXT_COLOR + RESET_BG_COLOR + RESET_TEXT_BOLD_FAINT);
        } else {
            System.out.println(SET_BG_COLOR_BLUE + SET_TEXT_COLOR_BLACK + SET_TEXT_BOLD
                    + "    h  g  f  e  d  c  b  a    "
                    + RESET_TEXT_COLOR + RESET_BG_COLOR + RESET_TEXT_BOLD_FAINT);
        }
    }

    private void printRank(String[][] board, int rank, boolean flip) {
        int displayRank = flip ? (9 - rank) : rank;
        StringBuilder row = new StringBuilder();
        row.append(SET_BG_COLOR_BLUE + SET_TEXT_COLOR_BLACK + SET_TEXT_BOLD).append(" ")
                .append(displayRank).append(" ").append(RESET_TEXT_COLOR)
                .append(RESET_BG_COLOR).append(RESET_TEXT_BOLD_FAINT);
        for (int file = 1; file <= 8; file++) {
            int f = flip ? (9 - file) : file;
            int r = flip ? (9 - rank) : rank;
            boolean isDarkSquare = ((f + r) % 2 == 0);
            String bgColor = isDarkSquare ? SET_BG_COLOR_DARK_GREY : SET_BG_COLOR_LIGHT_GREY;
            String piece = board[r][f];
            String fgColoredPiece;
            if (piece.equals(EMPTY)) {
                fgColoredPiece = "  ";
            } else {
                if (piece.equals(WHITE_PAWN) || piece.equals(WHITE_ROOK) ||
                        piece.equals(WHITE_KNIGHT) || piece.equals(WHITE_BISHOP) ||
                        piece.equals(WHITE_QUEEN) || piece.equals(WHITE_KING)) {
                    fgColoredPiece = SET_TEXT_COLOR_WHITE + piece + SET_TEXT_COLOR_WHITE;
                } else {
                    fgColoredPiece = SET_TEXT_COLOR_BLACK + piece + SET_TEXT_COLOR_BLACK;
                }
                fgColoredPiece = " " + fgColoredPiece;
            }
            row.append(bgColor)
                    .append(fgColoredPiece)
                    .append(" ")
                    .append(RESET_TEXT_COLOR)
                    .append(RESET_BG_COLOR);
        }
        row.append(SET_BG_COLOR_BLUE + SET_TEXT_COLOR_BLACK + SET_TEXT_BOLD).append(" ")
                .append(displayRank).append(" ").append(RESET_TEXT_COLOR)
                .append(RESET_BG_COLOR).append(RESET_TEXT_BOLD_FAINT);
        System.out.println(row.toString());
    }
}
