/*
Implementation of the game Othello / Reversi with an ai using minimax and alpha-beta pruning.
*/

import java.util.Scanner;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class Game {
    public static int[][] game = new int[8][8];
    private static Scanner scanner = new Scanner(System.in);
    private static int DEPTH = 8;
    public static ArrayList<int[][]> movesMade = new ArrayList<int[][]>();

    public static void main(String[] args) {

        // Initialize starting game sate
        game[4][4] = 2;
        game[3][3] = 2;
        game[3][4] = 1;
        game[4][3] = 1;

        gameLoop(game);

        // Write moces made to a moves log
        try {
            writeMoveLog(movesMade, "movesLog.txt");
        } catch (IOException e) {
            System.out.println("Something went wrong...");
        }
        
        scanner.close();
    }

    private static void gameLoop(int[][] game) {
        int[] input = new int[] {-3,0};
        int player = 1;
        int opponent = 2;
        int[] bestMove = null;

        int[][] newGame = null;
        
        while (true) {
            opponent = player == 1 ? 2 : 1;
            newGame = null;
            input = new int[] {-3,0};

            // Keep going until both player's dont have any valid moves
            if (Othello.getValidMoves(game, player).isEmpty()) {
                if (Othello.getValidMoves(game, opponent).isEmpty()) {
                    break;
                } else {
                    // Pass move to opponent is player has no valid moves
                    player = opponent;
                    continue;
                }
            }

            // Keep asking for valid input until it is given
            while (input[0] == -3) {
                displayOptions();
                Othello.printGameState(game, player);
                input = getInput(player);
            }
            
            switch (input[0]) {  
                case -2:
                    // ai makes the move
                    bestMove = Othello.getBestMove(Othello.getGameCopy(game), player, DEPTH);
                    newGame = Othello.makeMove(game, player, bestMove[0], bestMove[1]);
                    System.out.println("AI went: " + bestMove[1] + " " + bestMove[0]);
                    
                    if (newGame != null) {
                        movesMade.add(new int[][] {{player}, bestMove});
                        game = newGame;
                        player = opponent;
                    }
                    break;
                case -1:
                    // toggle pruning
                    Othello.PRUNING = input[1] == 1 ? true : false;
                    System.out.println("Pruning is now " + (Othello.PRUNING ? "on" : "off"));
                    break;
                case -4:
                    // toggle debug
                    Othello.DEBUG = input[1] == 1 ? true : false;
                    System.out.println("Debug is now " + (Othello.DEBUG ? "on" : "off"));
                    break;
                case -6:
                    if (input[1] > 0 && input[1] < 10) {
                        DEPTH = input[1];
                        System.out.println("Depth is now: " + DEPTH);
                    } else {
                        System.out.println("Invalid input. For depth, enter an integer in the range [1-9]");
                    }
                    break;
                case -5:
                    // exit game
                    System.out.println(getFinalStatement());
                    return;
                default:
                    // continue with player moves
                    newGame = Othello.makeMove(game, player, input[0], input[1]);
                    if (newGame != null) {
                        movesMade.add(new int[][] {{player}, input});
                        game = newGame;
                        player = opponent;
                    }
                    break;
            }
        }

        // Print winner
        System.out.println(getFinalStatement());
    }


    public static int[] getInput(int player) {
        int[] output = new int[] {-3, 0};


        // Prompt player and get input
        System.out.print("Player " + player + " Input: ");

        String[] input = scanner.nextLine().trim().split(" ");

        // Check all possible input cases
        switch (input[0].toLowerCase()) {
            case "ai":
                output[0] = -2;
                break;
            case "depth":
                if (input.length < 2) {
                    System.out.println("depth here , input length: " + input.length);
                    System.out.println("Invalid input\n");
                    break;
                }
                output[0] = -6;
                try {
                    output[1] = Integer.parseInt(input[1]);
                } catch (Exception e) {
                    output[1] = -1;
                }
                
                break;
            case "exit":
                output[0] = -5;
                break;
            default:
                if (input.length < 2) {
                    System.out.println("Invalid input\n");
                    break;
                }
                if (input[0].matches("\\d") && input[1].matches("\\d")) {
                    output[1] = Integer.parseInt(input[0]);
                    output[0] = Integer.parseInt(input[1]);
                } else {
                    System.out.println("Invalid input\n");
                }
                break;
        }
        System.out.println();

        return output;
    }

    public static void displayOptions() {
        System.out.println("Enter a column then row (ex. '4 5') to make a move, 'ai' to have the ai make the move for you, or 'exit' to stop the game. To change depth, enter 'depth' followed by an integer in the range [1-9]. ex. 'depth 4'");
    }

    public static void writeMoveLog(ArrayList<int[][]> movesMade, String fileName) throws IOException{
        BufferedWriter outFile = new BufferedWriter(new FileWriter(fileName));

        //Write all moves to file
        for (int[][] move : movesMade) {
            outFile.write("Player " + move[0][0] + ": " + move[1][1] + " " + move[1][0] + "\n");
        }

        //Write final scores and winner
        outFile.write(getFinalStatement());

        outFile.close();
    }

    public static String getFinalStatement() {
        int p1Score = Othello.getScore(game, 1);
        int p2Score = Othello.getScore(game, 2);

        String winner = "";

        // get winner
        if (p1Score > p2Score) {
            winner = "Player 1";
        } else if (p2Score > p1Score) {
            winner = "Player 2";
        } else {
            winner = "Nobody (tie)";
        }

        return "\nPlayer 1 Score: " + p1Score + "\nPlayer 2 Score: " + p2Score + "\n\nWinner: " + winner;
    }
}
