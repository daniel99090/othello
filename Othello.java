/*
Implementation of the game Othello / Reversi with an ai using minimax and alpha-beta pruning.
*/

import java.util.ArrayList;

public class Othello {
    public static final String[] COLORS = {"\u001B[0m", "\u001B[34m", "\u001B[32m", "\u001B[31m"};
    public static boolean PRUNING = true;
    public static boolean DEBUG = false;
    public static int MOVES_CONSIDERED = 0;

    public static void printGameState(int[][] game, int player) {
        // get game showing valid moves
        int newGame[][] = showValidMoves(game, getValidMoves(game, player));
        String color = "";

        for (int i=0; i<newGame.length; i++) {
            // Show row numbers
            System.out.print(i + " ");

            // Color of element is dependant on its value
            for (int element : newGame[i]) {
                color = COLORS[element];
                System.out.print(color +( element == 0 ? "-" : element) + COLORS[0] +" ");
            }
            System.out.println();
        }
        // SHow column numbers
        System.out.println("  0 1 2 3 4 5 6 7");
    }

    public static boolean isValidMove(int[][] game, int player, int row, int column, ArrayList<int[]> poitionsToUpdate) {
        boolean isValid = false;

        // Values for defining the direction to search in
        int[][] inputs = {{1,1},{-1,-1},{1,0},{0,1},{1,-1},{-1,1},{-1,0},{0,-1}};

        // Make sure position is on the board
        if (game[row][column] != 0 || row > 7 || row < 0 || column > 7 || column < 0) {
            return false;
        }

        // Check each direction for possible pieces to flip
        for (int[] input : inputs) {
            if (checkLine(game, player, input[0], input[1], row, column, poitionsToUpdate)) {
                isValid = true;
            }
        }

        return isValid;
    }

    private static boolean checkLine(int[][] game, int player, int rowIncrement, int columnIncrement, int row, int column, ArrayList<int[]> poitionsToUpdate) {
        int opponentPiece = player == 1 ? 2 : 1;

        // Potential positions to update
        ArrayList<int[]> newPositions = new ArrayList<>();

        row += rowIncrement;
        column += columnIncrement;

        // Check inital position and value of next piece
        if (row >= 8 || column >= 8 || row < 0 || column < 0) {
            return false;
        }

        int currPiece = game[row][column];

        if (currPiece == 0 || currPiece == player) {
            return false;
        }

        newPositions.add(new int[] {row,column});

        // Check positions until position is no longer on the board
        while (((row += rowIncrement) < 8) && ((column += columnIncrement) < 8) && row>=0 && column >=0) {
            currPiece = game[row][column];
            newPositions.add(new int[] {row,column});

            if (currPiece == opponentPiece) {
                continue;
            }

            if (currPiece == 0) {
                return false;
            }

            // Getting past previous conditions indicates we reached our own piece
            if (poitionsToUpdate != null) {
                poitionsToUpdate.addAll(newPositions); 
            }

            return true;
        }

        return false;
    }

    public static void updatePositions(int[][] game, int player, ArrayList<int[]> poitionsToUpdate) {
        // Flip pices to the current player
        for (int[] position : poitionsToUpdate) {
            game[position[0]][position[1]] = player;
        }
    }
    

    public static int[][] makeMove(int[][] game, int player, int row, int column) {
        ArrayList<int[]> positionsToUpdate = new ArrayList<int[]>();

        // update game positions if it is a valid move
        if (isValidMove(game, player, row, column, positionsToUpdate)) {
            game[row][column] = player;
            updatePositions(game, player, positionsToUpdate);
        } else {
            System.out.println("Invalid move.");
            game = null;
        }

        return game;
    }

    public static ArrayList<int[]> getValidMoves(int[][]game, int player) {
        ArrayList<int[]> validMoves = new ArrayList<>();

        // Test every position on board
        for (int i=0; i<8; i++) {
            for (int j=0; j<8; j++) {
                if (isValidMove(game, player, i, j, null)) {
                    validMoves.add(new int[] {i,j});
                }
            }
        }
        return validMoves;
    }

    public static int[][] showValidMoves(int[][] game, ArrayList<int[]> validMoves) {
        int[][] newGame = getGameCopy(game);

        // Update all potential moves 
        for (int[] position : validMoves) {
            newGame[position[0]][position[1]] = 3;
        }

        return newGame;
    }

    public static int getScoreEval(int[][] game, int player) {
        int opponent = player == 1 ? 2 : 1;
        return getScore(getGameCopy(game), player) - getScore(getGameCopy(game), opponent);
    }

    public static int getScore(int[][] game, int player) {
        int score = 0;

        // Each plyer piece is 1 point
        for (int i=0;i<8;i++) {
            for (int j=0;j<8;j++) {
                if (game[i][j] == player) {
                    score += 1;
                }
            }
        }

        return score;
    }

    public static boolean isTerminalNode(int[][] game, int player) {
        return (getValidMoves(game, 1).isEmpty() && getValidMoves(game, 2).isEmpty());
    }

    public static int[][] getGameCopy(int[][] game) {
        int[][] newGame = new int[8][8];

        for (int i=0;i<8;i++) {
            for (int j=0;j<8;j++) {
                newGame[i][j] = game[i][j];
            }
        }

        return newGame;
    }

    public static int minimax(int[][] game, int depth, int currDepth, int alpha, int beta, boolean maximizer, int player, StringBuilder debugTree, int[] prevMove) {
        //Minimax call means you're considering another move
        MOVES_CONSIDERED++;

        // Return current score is this is a leaf node
        if (depth == 0 || isTerminalNode(game, player)) {
            int score = getScoreEval(game, player);

            if (DEBUG) {
                debugTree.insert(0,"\n" +  "  ".repeat(currDepth) + "Player " + player + " Score: " + score + " Move: " + prevMove[0] + " " + prevMove[1] + "\n");
            }

            return score;
        }

        ArrayList<int[]> validMoves = getValidMoves(game, player);
        int opponent = player == 1 ? 2 : 1;
        int score = 0;

        // If current player has no moves it will pass to the other player
        if (validMoves.isEmpty()) {
            return minimax(game, depth-1, currDepth+1, alpha, beta, !maximizer, opponent, debugTree, prevMove);
        }

        // Define starting eval
        int eval = maximizer ? Integer.MIN_VALUE : Integer.MAX_VALUE;

        for (int[] move : validMoves) {
            // Make next move on duplicate board
            int[][] gameCopy = getGameCopy(game);
            makeMove(gameCopy, player, move[0], move[1]);

            // Recursive call to get eval to compare to
            score = minimax(gameCopy, depth-1, currDepth+1, alpha, beta, !maximizer, opponent, debugTree, move);

            // Re evaluate own eval
            eval = maximizer ? Math.max(eval, score) : Math.min(eval, score);

            // Update alpha/beta
            if (maximizer) {
                alpha = Math.max(eval, alpha);
            } else {
                beta = Math.min(eval, beta);
            }

            // Check for pruning condition
            if (PRUNING && (alpha >= beta)) {
                break;
            }
        }
        if (DEBUG) {
            debugTree.insert(0,"\n" +  "  ".repeat(currDepth) + "Player: " + player + " Score: " + eval + " Move: " + prevMove[0] + " " + prevMove[1] + "\n");
        }
        return eval;
    }

    public static int[] getBestMove(int[][] game, int player, int depth) {
        ArrayList<int[]> validMoves = getValidMoves(game, player);
        int[] bestMove = null;
        int bestScore = Integer.MIN_VALUE;
        int currentScore = 0;
        int opponent = player == 1 ? 2 : 1;
        StringBuilder debugTree = new StringBuilder();

        // Iterate over all valid moves to best best score and move
        for (int[] move : validMoves) {
            int[][] gameCopy = makeMove(getGameCopy(game), player, move[0], move[1]);

            currentScore = minimax(gameCopy, depth-1, 0, Integer.MIN_VALUE, Integer.MAX_VALUE, false, opponent, debugTree, move);

            if (currentScore > bestScore) {
                bestScore = currentScore;
                bestMove = move;
            }
        }
        
        if (DEBUG) {
            System.out.println(debugTree.toString());
            System.out.println("Debug moves displayed as (row, column)");
        }

        System.out.println("Number of moves considered: " + MOVES_CONSIDERED);
        MOVES_CONSIDERED = 0;

        return bestMove;
    }
    
}