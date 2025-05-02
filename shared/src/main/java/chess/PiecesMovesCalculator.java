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
            while (onBoard(r,c)) {
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
            while (onBoard(r,c)) {
                ChessPosition target = new ChessPosition(r, c);
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
        int[][] directions = {{1, 0}, {1, 1}, {0, 1}, {-1, 1}, {-1, 0}, {-1, -1}, {0, -1}, {1, -1}};
        for (int[] dir : directions) {
            int r = position.getRow() + dir[0];
            int c = position.getColumn() + dir[1];
            while (onBoard(r,c)) {
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
        int[][] steps = {{1, 0}, {1, 1}, {0, 1}, {-1, 1}, {-1, 0}, {-1, -1}, {0, -1}, {1, -1}};
        for (int[] step : steps) {
            int r = position.getRow() + step[0];
            int c = position.getColumn() + step[1];
            if (onBoard(r,c)) {
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

    private static java.util.Collection<ChessMove> knightMoves(ChessBoard board,
                                                               ChessPosition position,
                                                               ChessPiece knight) {
        java.util.List<ChessMove> moves = new java.util.ArrayList<>();
        int[][] jumps = {{2, 1}, {1, 2},
                {-1, 2}, {-2, 1},
                {-1, -2}, {-2, -1},
                {1, -2}, {2, -1}};
        for (int[] jump : jumps) {
            int r = position.getRow() + jump[0];
            int c = position.getColumn() + jump[1];
            if (onBoard(r,c)) {
                ChessPosition target = new ChessPosition(r, c);
                ChessPiece occupant = board.getPiece(target);
                if (occupant == null) {
                    moves.add(new ChessMove(position, target, null));
                } else {
                    if (occupant.getTeamColor() != knight.getTeamColor()) {
                        moves.add(new ChessMove(position, target, null));
                    }
                }
            }
        }
        return moves;
    }

    private static java.util.Collection<ChessMove> pawnMoves(ChessBoard board,
                                                             ChessPosition position,
                                                             ChessPiece pawn) {
        java.util.List<ChessMove> moves = new java.util.ArrayList<>();
        int old_r = position.getRow(), r, r2 = 0;
        int c = position.getColumn(), c_left = position.getColumn() - 1, c_right = position.getColumn() + 1;
        boolean isWhite = (pawn.getTeamColor() == ChessGame.TeamColor.WHITE);
        ChessPosition target_left, target_right;
        ChessPiece occupant_left, occupant_right;

        // std_move (white)
        if (isWhite) {
            if (old_r == 2) {
                r2 = old_r + 2;
            }
            r = old_r + 1;
        }
        else // std_move (black)
        {
            if (old_r == 7) {
                r2 = old_r - 2;
            }
            r = old_r - 1;
        }

        if (onBoard(r,c)) {
            //Single move
            ChessPosition target = new ChessPosition(r, c);
            ChessPiece occupant = board.getPiece(target);

            //attack left
            if (c_left >= 1) {
                target_left = new ChessPosition(r, c_left);
                occupant_left = board.getPiece(target_left);
                if (occupant_left != null && occupant_left.getTeamColor() != pawn.getTeamColor()) {
                    if (r == 1 || r == 8) {
                        moves.add(new ChessMove(position, target_left, ChessPiece.PieceType.QUEEN));
                        moves.add(new ChessMove(position, target_left, ChessPiece.PieceType.BISHOP));
                        moves.add(new ChessMove(position, target_left, ChessPiece.PieceType.KNIGHT));
                        moves.add(new ChessMove(position, target_left, ChessPiece.PieceType.ROOK));
                    } else {
                        moves.add(new ChessMove(position, target_left, null));
                    }
                }

            }

            //attack right
            if (c_right <= 8) {
                target_right = new ChessPosition(r, c_right);
                occupant_right = board.getPiece(target_right);
                if (occupant_right != null && occupant_right.getTeamColor() != pawn.getTeamColor()) {
                    if (r == 1 || r == 8) {
                        moves.add(new ChessMove(position, target_right, ChessPiece.PieceType.QUEEN));
                        moves.add(new ChessMove(position, target_right, ChessPiece.PieceType.BISHOP));
                        moves.add(new ChessMove(position, target_right, ChessPiece.PieceType.KNIGHT));
                        moves.add(new ChessMove(position, target_right, ChessPiece.PieceType.ROOK));
                    } else {
                        moves.add(new ChessMove(position, target_right, null));
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
