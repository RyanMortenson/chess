package chess;

public class PiecesMovesCalculator {

    public static final int BOARD_MIN = 1, BOARD_MAX = 8;

    private static Boolean onBoard(int row, int col) {
        return row >= BOARD_MIN && row <= BOARD_MAX &&
                col >= BOARD_MIN && col <= BOARD_MAX;
    }

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
            case KNIGHT:
                return knightMoves(board, position, piece);
            case PAWN:
                return pawnMoves(board, position, piece);
            default:
                return java.util.Collections.emptyList();
        }
    }

    private static java.util.Collection<ChessMove> slidingPiece(ChessBoard board,
                                                                ChessPosition position,
                                                                ChessPiece piece,
                                                                int[][] directions) {
        java.util.List<ChessMove> moves = new java.util.ArrayList<>();
        for (int[] dir : directions) {
            int r = position.getRow() + dir[0];
            int c = position.getColumn() + dir[1];
            while (onBoard(r,c)) {
                ChessPosition target = new ChessPosition(r,c);
                ChessPiece occupant = board.getPiece(target);
                if (occupant == null) {
                    moves.add(new ChessMove(position, target, null));
                } else {
                    if (occupant.getTeamColor() != piece.getTeamColor()) {
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

    private static java.util.Collection<ChessMove> jumpingPiece(ChessBoard board,
                                                                ChessPosition position,
                                                                ChessPiece piece,
                                                                int[][] jumps) {
        java.util.List<ChessMove> moves = new java.util.ArrayList<>();
        for (int[] jump : jumps) {
            int r = position.getRow() + jump[0];
            int c = position.getColumn() + jump[1];
            if (onBoard(r,c)) {
                ChessPosition target = new ChessPosition(r,c);
                ChessPiece occupant = board.getPiece(target);
                if (occupant == null || occupant.getTeamColor() != piece.getTeamColor()) {
                    moves.add(new ChessMove(position, target, null));
                }
            }
        }
        return moves;
    }

    private static java.util.Collection<ChessMove> bishopMoves(ChessBoard board,
                                                               ChessPosition position,
                                                               ChessPiece bishop) {
        int[][] directions = {
                {1, 1}, {1, -1}, {-1, 1}, {-1, -1}
        };
        return slidingPiece(board,position, bishop, directions);
    }

    private static java.util.Collection<ChessMove> rookMoves(ChessBoard board,
                                                             ChessPosition position,
                                                             ChessPiece rook) {
        int[][] directions = {
                {1, 0}, {-1, 0}, {0, 1}, {0, -1}
        };
        return slidingPiece(board, position, rook, directions);
    }

    private static java.util.Collection<ChessMove> queenMoves(ChessBoard board,
                                                              ChessPosition position,
                                                              ChessPiece queen) {
        int[][] directions = {{1, 0}, {1, 1}, {0, 1}, {-1, 1}, {-1, 0}, {-1, -1}, {0, -1}, {1, -1}};
        return slidingPiece(board, position, queen, directions);
    }

    private static java.util.Collection<ChessMove> kingMoves(ChessBoard board,
                                                             ChessPosition position,
                                                             ChessPiece king) {
        int[][] jumps = {{1, 0}, {1, 1}, {0, 1}, {-1, 1}, {-1, 0}, {-1, -1}, {0, -1}, {1, -1}};
        return jumpingPiece(board, position, king, jumps);
    }

    private static java.util.Collection<ChessMove> knightMoves(ChessBoard board,
                                                               ChessPosition position,
                                                               ChessPiece knight) {
        int[][] jumps = {{2, 1}, {1, 2},
                {-1, 2}, {-2, 1},
                {-1, -2}, {-2, -1},
                {1, -2}, {2, -1}};
        return jumpingPiece(board, position, knight, jumps);
    }

    private static java.util.Collection<ChessMove> pawnMoves(ChessBoard board,
                                                             ChessPosition position,
                                                             ChessPiece pawn) {
        java.util.List<ChessMove> moves = new java.util.ArrayList<>();
        int oldR = position.getRow(), r, r2 = 0;
        int c = position.getColumn(), cLeft = position.getColumn() - 1, cRight = position.getColumn() + 1;
        boolean isWhite = (pawn.getTeamColor() == ChessGame.TeamColor.WHITE);
        ChessPosition targetLeft, targetRight;
        ChessPiece occupantLeft, occupantRight;

        // std_move (white)
        if (isWhite) {
            if (oldR == 2) {
                r2 = oldR + 2;
            }
            r = oldR + 1;
        }
        else // std_move (black)
        {
            if (oldR == 7) {
                r2 = oldR - 2;
            }
            r = oldR - 1;
        }

        if (onBoard(r,c)) {
            //Single move
            ChessPosition target = new ChessPosition(r, c);
            ChessPiece occupant = board.getPiece(target);

            //attack left
            if (cLeft >= 1) {
                targetLeft = new ChessPosition(r, cLeft);
                occupantLeft = board.getPiece(targetLeft);
                if (occupantLeft != null && occupantLeft.getTeamColor() != pawn.getTeamColor()) {
                    if (r == 1 || r == 8) {
                        moves.add(new ChessMove(position, targetLeft, ChessPiece.PieceType.QUEEN));
                        moves.add(new ChessMove(position, targetLeft, ChessPiece.PieceType.BISHOP));
                        moves.add(new ChessMove(position, targetLeft, ChessPiece.PieceType.KNIGHT));
                        moves.add(new ChessMove(position, targetLeft, ChessPiece.PieceType.ROOK));
                    } else {
                        moves.add(new ChessMove(position, targetLeft, null));
                    }
                }

            }

            //attack right
            if (cRight <= 8) {
                targetRight = new ChessPosition(r, cRight);
                occupantRight = board.getPiece(targetRight);
                if (occupantRight != null && occupantRight.getTeamColor() != pawn.getTeamColor()) {
                    if (r == 1 || r == 8) {
                        moves.add(new ChessMove(position, targetRight, ChessPiece.PieceType.QUEEN));
                        moves.add(new ChessMove(position, targetRight, ChessPiece.PieceType.BISHOP));
                        moves.add(new ChessMove(position, targetRight, ChessPiece.PieceType.KNIGHT));
                        moves.add(new ChessMove(position, targetRight, ChessPiece.PieceType.ROOK));
                    } else {
                        moves.add(new ChessMove(position, targetRight, null));
                    }
                }
            }


            //Double move from start
            if (r2 != 0 && occupant == null) {
                ChessPosition target2 = new ChessPosition(r2, c);
                ChessPiece occupant2 = board.getPiece(target2);
                if (occupant2 == null) {
                    moves.add(new ChessMove(position, target2, null));
                }
            }
            if (occupant == null) {
                if (r == 8 || r == 1) {
                    moves.add(new ChessMove(position, target, ChessPiece.PieceType.QUEEN));
                    moves.add(new ChessMove(position, target, ChessPiece.PieceType.BISHOP));
                    moves.add(new ChessMove(position, target, ChessPiece.PieceType.KNIGHT));
                    moves.add(new ChessMove(position, target, ChessPiece.PieceType.ROOK));
                } else {
                    moves.add(new ChessMove(position, target, null));
                }
            }
        }
        return moves;
    }
}
