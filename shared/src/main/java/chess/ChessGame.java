package chess;

import java.util.*;


public class ChessGame {

    private TeamColor turn;
    private ChessBoard board;

    public ChessGame() {
        board = new ChessBoard();
        board.resetBoard();
        setTeamTurn(TeamColor.WHITE);
    }


    public TeamColor getTeamTurn() {
        return turn;
    }


    public void setTeamTurn(TeamColor team) {
        turn = team;
    }


    public enum TeamColor {
        WHITE,
        BLACK
    }


    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        ChessPiece piece = board.getPiece(startPosition);
        if (piece == null) {
            return null;
        }
        Collection<ChessMove> validMovesIfBoardEmpty = piece.pieceMoves(board, startPosition);
        Set<ChessMove> validMoves = new HashSet<>(validMovesIfBoardEmpty.size());
        for (ChessMove move : validMovesIfBoardEmpty) {
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


    public void makeMove(ChessMove move) throws InvalidMoveException {
        ChessPiece piece = board.getPiece(move.getStartPosition());
        if (piece == null) {
            throw new InvalidMoveException("No valid moves.");
        }
        Collection<ChessMove> moves = validMoves(move.getStartPosition());
        boolean valid = moves.contains(move);
        boolean turnOfTeam = piece.getTeamColor() == turn;

        if (valid && turnOfTeam) {
            ChessPiece pieceToMove = piece;
            if (move.getPromotionPiece() != null) {
                pieceToMove = new ChessPiece(pieceToMove.getTeamColor(), move.getPromotionPiece());
            }
            board.addPiece(move.getStartPosition(), null); //Get rid of piece in current position
            board.addPiece(move.getEndPosition(), pieceToMove);  //Place the piece in the new position
            setTeamTurn(turn == TeamColor.WHITE ? TeamColor.BLACK : TeamColor.WHITE);
        } else {
            throw new InvalidMoveException("Invalid move: " + move);
        }
    }


    public boolean isInCheck(TeamColor teamColor) {
        ChessPosition kingPosition = null;

        // Find the position of the king
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
        // Check if any of the pieces can attack the king
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


    public boolean isInCheckmate(TeamColor teamColor) {
        return isInCheck(teamColor) && hasNoValidMoves(teamColor);
    }


    public boolean isInStalemate(TeamColor teamColor) {
        if (isInCheck(teamColor)) {
            return false;
        }
        return hasNoValidMoves(teamColor);
    }


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


    public void setBoard(ChessBoard board) {
        this.board = board;
    }


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
