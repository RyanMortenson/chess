package ui;

import static ui.EscapeSequences.*;

public class GameplayClient {

    private final int gameID;
    private final String perspective; // WHITE, BLACK, or OBSERVER


    public GameplayClient(int gameID, String perspective) {
        this.gameID = gameID;
        this.perspective = perspective;
    }

    public void drawInitialBoard() {
        String[][] board = new String[9][9]; // index 1..8 used

        // Row 8: black special pieces
        board[8][1] = BLACK_ROOK;
        board[8][2] = BLACK_KNIGHT;
        board[8][3] = BLACK_BISHOP;
        board[8][4] = BLACK_QUEEN;
        board[8][5] = BLACK_KING;
        board[8][6] = BLACK_BISHOP;
        board[8][7] = BLACK_KNIGHT;
        board[8][8] = BLACK_ROOK;
        // Row 7: black pawns
        for (int c = 1; c <= 8; c++) {
            board[7][c] = BLACK_PAWN;
        }
        // Row 6,5,4,3: empty
        for (int r = 3; r <= 6; r++) {
            for (int c = 1; c <= 8; c++) {
                board[r][c] = " ";
            }
        }
        // Row 2: white pawns
        for (int f = 1; f <= 8; f++) {
            board[2][f] = WHITE_PAWN;
        }
        // Row 1: white special pieces
        board[1][1] = WHITE_ROOK;
        board[1][2] = WHITE_KNIGHT;
        board[1][3] = WHITE_BISHOP;
        board[1][4] = WHITE_QUEEN;
        board[1][5] = WHITE_KING;
        board[1][6] = WHITE_BISHOP;
        board[1][7] = WHITE_KNIGHT;
        board[1][8] = WHITE_ROOK;

        boolean flip = perspective.equalsIgnoreCase("BLACK");

        // Column labels
        if (!flip) {
            System.out.println("    a   b   c   d   e   f   g   h");
        } else {
            System.out.println("    h   g   f   e   d   c   b   a");
        }

        // Draw rows
        for (int rank = 8; rank >= 1; rank--) {
            int displayRank = flip
                    ? (9 - rank) // true
                    : rank;      // false
            // Each row: row number then 8 squares then rank number
            StringBuilder row = new StringBuilder();
            row.append(displayRank).append("  ");
            for (int file = 1; file <= 8; file++) {
                int f = flip ? (9 - file) : file;
                int r = flip ? (9 - rank) : rank;

                // Determine if square is light or dark
                boolean isDarkSquare = ((f + r) % 2 == 0);
                String bgColor = isDarkSquare
                        ? EscapeSequences.SET_BG_COLOR_DARK_GREY
                        : EscapeSequences.SET_BG_COLOR_LIGHT_GREY;

                // Determine piece and its color
                String piece = board[r][f];
                String fgColoredPiece;
                if (piece.equals(" ")) {
                    // Empty, just show two spaces prefixed by a space
                    fgColoredPiece = "  ";
                } else {
                    if (piece.equals(WHITE_PAWN) || piece.equals(WHITE_ROOK) ||
                            piece.equals(WHITE_KNIGHT) || piece.equals(WHITE_BISHOP) ||
                            piece.equals(WHITE_QUEEN) || piece.equals(WHITE_KING)) {
                        fgColoredPiece = EscapeSequences.SET_TEXT_COLOR_WHITE + piece + EscapeSequences.SET_TEXT_COLOR_WHITE;
                    } else {
                        fgColoredPiece = EscapeSequences.SET_TEXT_COLOR_BLACK + piece + EscapeSequences.SET_TEXT_COLOR_BLACK;
                    }

                    fgColoredPiece = "   " + fgColoredPiece;
                }

                row.append(bgColor)
                        .append(fgColoredPiece)
                        .append(" ")
                        .append(EscapeSequences.RESET_TEXT_COLOR)
                        .append(EscapeSequences.RESET_BG_COLOR);
            }
            row.append("  ").append(displayRank);
            System.out.println(row.toString());
        }

        // Column labels again
        if (!flip) {
            System.out.println("    a   b   c   d   e   f   g   h");
        } else {
            System.out.println("    h   g   f   e   d   c   b   a");
        }

        System.out.println(EscapeSequences.SET_TEXT_COLOR_CYAN
                + "\n(Initial position of game " + gameID + ")"
                + EscapeSequences.RESET_TEXT_COLOR);
    }
}
