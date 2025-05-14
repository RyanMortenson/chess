package chess;

import java.util.*;

/**
 * For a class that can manage a chess game, making moves on a board
 *
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {

    private TeamColor turn;
    private ChessBoard board;

    public ChessGame() {
        board = new ChessBoard();
        board.resetBoard();
        setTeamTurn(TeamColor.WHITE);
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return turn;
    }

    /**
     * Sets which team's turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        turn = team;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK;
    }

    /**
     * Gets valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     *         startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        ChessPiece piece = board.getPiece(startPosition);
        if (piece == null) {
            return null;
        }
        Collection<ChessMove> raw = piece.pieceMoves(board, startPosition);
        Set<ChessMove> validMoves = new HashSet<>(raw.size());
        for (ChessMove move : raw) {
            ChessPiece temp = board.getPiece(move.getEndPosition());
            board.addPiece(startPosition, null);
            board.addPiece(move.getEndPosition(), piece);
            if (!isInCheck(piece.getTeamColor())) {
                validMoves.add(move);
            }
            board.addPiece(move.getEndPosition(), temp);
            board.addPiece(startPosition, piece);
        }
        return validMoves;
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        ChessPiece currPiece = board.getPiece(move.getStartPosition());
        if (currPiece == null) {
            throw new InvalidMoveException("No valid moves.");
        }
        Collection<ChessMove> moves = validMoves(move.getStartPosition());
        boolean valid = moves.contains(move);
        boolean turnOfTeam = currPiece.getTeamColor() == turn;

        if (valid && turnOfTeam) {
            ChessPiece pieceToMove = currPiece;
            if (move.getPromotionPiece() != null) {
                pieceToMove = new ChessPiece(pieceToMove.getTeamColor(), move.getPromotionPiece());
            }
            board.addPiece(move.getStartPosition(), null);
            board.addPiece(move.getEndPosition(), pieceToMove);
            setTeamTurn(turn == TeamColor.WHITE ? TeamColor.BLACK : TeamColor.WHITE);
        } else {
            throw new InvalidMoveException("Invalid move: " + move);
        }
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        ChessPosition kingPosition = null;

        // Find the king
        for (int r = 1; r <= 8 && kingPosition == null; r++) {
            for (int c = 1; c <= 8 && kingPosition == null; c++) {
                ChessPosition pos = new ChessPosition(r, c);
                ChessPiece p = board.getPiece(pos);
                if (p != null && p.getTeamColor() == teamColor && p.getPieceType() == ChessPiece.PieceType.KING) {
                    kingPosition = new ChessPosition(r, c);
                }
            }
        }
        if (kingPosition == null) {
            return false;
        }
        // see if any enemy piece can attack the king
        for (int r = 1; r <= 8; r++) {
            for (int c = 1; c <= 8; c++) {
                ChessPosition pos = new ChessPosition(r, c);
                ChessPiece p = board.getPiece(pos);
                if (p == null || p.getTeamColor() == teamColor)
                    continue;
                for (ChessMove m : p.pieceMoves(board, pos)) {
                    if (m.getEndPosition().equals(kingPosition)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Determines if the given team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        return isInCheck(teamColor) && hasNoValidMoves(teamColor);
    }

    /**
     * Determines if the given team is in stalemate (no valid moves and not in check)
     */
    public boolean isInStalemate(TeamColor teamColor) {
        if (isInCheck(teamColor)) {
            return false;
        }
        return hasNoValidMoves(teamColor);
    }

    /**
     * Helper to check if a team has no legal moves
     */
    private boolean hasNoValidMoves(TeamColor teamColor) {
        for (int r = 1; r <= 8; r++) {
            for (int c = 1; c <= 8; c++) {
                ChessPosition pos = new ChessPosition(r, c);
                ChessPiece p = board.getPiece(pos);
                if (p != null && p.getTeamColor() == teamColor) {
                    Collection<ChessMove> moves = validMoves(pos);
                    if (moves != null && !moves.isEmpty()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * Sets this game's chessboard with a given board
     */
    public void setBoard(ChessBoard board) {
        this.board = board;
    }

    /**
     * Gets the current chessboard
     */
    public ChessBoard getBoard() {
        return board;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessGame chessGame = (ChessGame) o;
        return turn == chessGame.turn && Objects.equals(board, chessGame.board);
    }

    @Override
    public int hashCode() {
        return Objects.hash(turn, board);
    }

    @Override
    public String toString() {
        return "ChessGame{" +
                "teamTurn=" + turn +
                ", board=" + board +
                '}';
    }
}
