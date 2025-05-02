package chess;

public class PiecesMovesCalculator {

    public static java.util.Collection<ChessMove> calculateMoves(ChessBoard board, ChessPosition position) {
        ChessPiece piece = board.getPiece(position);
        if (piece == null) {
            return java.util.Collections.emptyList();
        }

        switch (piece.getPieceType()) {
            case BISHOP:
                return bishopMoves(board, position, piece);
            case ROOK:
                return rookMoves(board, position, piece);
            case QUEEN:
                return queenMoves(board, position, piece);
            case KING:
                return kingMoves(board, position, piece);
            /*
            case KNIGHT:
                return knightMoves(board, position, piece);
            case PAWN:
                return pawnMoves(board, position, piece);
                */

            default:
                return java.util.Collections.emptyList();
        }
    }

    private static java.util.Collection<ChessMove> bishopMoves(ChessBoard board,
                                                              ChessPosition position,
                                                              ChessPiece bishop) {
        java.util.List<ChessMove> moves = new java.util.ArrayList<>();
        int[][] directions = {
                {1, 1}, {1, -1}, {-1, 1}, {-1, -1}
        };

        for (int[] dir : directions) {
            int r = position.getRow() + dir[0];
            int c = position.getColumn() + dir[1];
            while (r >= 1 && r <= 8 && c >= 1 && c <=8 ) {
                ChessPosition target = new ChessPosition(r, c);
                ChessPiece occupant = board.getPiece(target);

                if (occupant == null) {
                    moves.add(new ChessMove(position, target, null));
                } else {
                    if (occupant.getTeamColor() != bishop.getTeamColor()) {
                        moves.add(new ChessMove(position, target, null));
                    }
                    break;
                }

                r += dir[0];
                c += dir[1];
            }
        }
        return moves;
    }

    private static java.util.Collection<ChessMove> rookMoves(ChessBoard board,
                                                            ChessPosition position,
                                                            ChessPiece rook) {
        java.util.List<ChessMove> moves = new java.util.ArrayList<>();
        int[][] directions = {
                {1, 0}, {-1, 0}, {0, 1}, {0, -1}
        };
        for (int[] dir : directions) {
            int r = position.getRow() + dir[0];
            int c = position.getColumn() + dir[1];
            while (r >= 1 && r <= 8 && c >= 1 && c <= 8) {
                ChessPosition target = new ChessPosition(r,c);
                ChessPiece occupant = board.getPiece(target);

                if (occupant == null) {
                    moves.add(new ChessMove(position, target, null));
                } else {
                    if (occupant.getTeamColor() != rook.getTeamColor()) {
                        moves.add(new ChessMove(position, target, null));
                    }
                    break;
                }
                r += dir[0];
                c += dir[1];
            }
        }
        return moves;
    }

    private static java.util.Collection<ChessMove> queenMoves(ChessBoard board,
                                                             ChessPosition position,
                                                             ChessPiece queen) {
        java.util.List<ChessMove> moves = new java.util.ArrayList<>();
        int[][] directions = {{1,0}, {1,1}, {0,1}, {-1,1}, {-1,0}, {-1,-1}, {0,-1}, {1,-1}};
        for (int[] dir : directions) {
            int r = position.getRow() + dir[0];
            int c = position.getColumn() + dir[1];
            while (r >= 1 && r <= 8 && c >= 1 && c <= 8) {
                ChessPosition target = new ChessPosition(r, c);
                ChessPiece occupant = board.getPiece(target);
                if (occupant == null) {
                    moves.add(new ChessMove(position, target, null));
                } else {
                    if (occupant.getTeamColor() != queen.getTeamColor()) {
                        moves.add(new ChessMove(position, target, null));
                    }
                    break;
                }
                r += dir[0];
                c += dir[1];
            }
        }
        return moves;
    }

    private static java.util.Collection<ChessMove> kingMoves(ChessBoard board,
                                                             ChessPosition position,
                                                             ChessPiece king) {
        java.util.List<ChessMove> moves = new java.util.ArrayList<>();
        int[][] steps = {{1,0}, {1,1}, {0,1}, {-1,1}, {-1,0}, {-1,-1}, {0,-1}, {1,-1}};
        for (int[] step : steps) {
            int r = position.getRow() + step[0];
            int c = position.getColumn() + step[1];
            if (r>=1 && r<=8 && c>= 1 && c<= 8) {
                ChessPosition target = new ChessPosition(r, c);
                ChessPiece occupant = board.getPiece(target);
                if (occupant == null) {
                    moves.add(new ChessMove(position, target, null));
                } else {
                    if (occupant.getTeamColor() != king.getTeamColor()) {
                        moves.add(new ChessMove(position, target, null));
                    }
                }
            }
        }
        return moves;
    }
}
