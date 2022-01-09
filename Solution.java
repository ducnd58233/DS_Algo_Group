import java.io.*;
import java.util.Scanner;

public class Solution {
    public static void main(String[] args) {
        File inFile = null;
        int rows, cols;
        String[][] matrix;

        // Check if the input missing file name
        if (args.length <= 0) {
            System.err.println("Missing arguments input file name!");
            System.exit(1);
        }

        // Check if the file name is exist in directory
        try {
            inFile = new File(args[0]);
            Scanner sc = new Scanner(inFile);
            rows = sc.nextInt();
            cols = sc.nextInt();
            sc.nextLine();

            // Check if rows is in range 1 to 27
            if ((rows <= 0 || rows > 27) && (cols <= 0 || cols > 27)) {
                System.err.println("Rows and Columns need no less than 1 and no more than 27");
                System.exit(1);
            }

            matrix = new String[rows][cols];

            // Read the input of matrix
            for (int r = 0; r < rows; r++) {
                String line = sc.nextLine();
                String[] parts = line.split(" ");
                for (int c = 0; c < cols; c++) {
                    matrix[r][c] = parts[c];

                    // Check if gold of matrix is more than 0 or Invalid input
                    if (!matrix[r][c].equals("x") && !matrix[r][c].equals("X")
                    && !matrix[r][c].equals("."))
                    {
                        try
                        {
                            if (Long.parseLong(matrix[r][c]) <= 0) 
                            {
                                System.err.println("GOLD must be Z+");
                                System.exit(1);
                            }
                        }
                        catch (Exception e) 
                        {
                            System.err.println("Invalid input data (Character must be ('X', 'x', '.' or a number)");
                            System.exit(1);
                        }
                    }         
                }
            }

            // Convert String array to Numeric array for calculation
            // Create 2 different numeric array to compare 2 different algorithms
            long[][] matrixInt1 = convertStringMatrixToNumericMatrix(matrix, rows, cols);
            long[][] matrixInt2 = convertStringMatrixToNumericMatrix(matrix, rows, cols);

            // Backtracking algorithm
            System.out.println("Exhaustive Search Algorithm (Back tracking):");
            long startTime1 = System.currentTimeMillis();
            BackTracking bt = new BackTracking(matrixInt1, rows, cols);
            bt.findPath(matrixInt1, rows, cols, 0, 0, matrixInt1[0][0]);
            long endTime1 = System.currentTimeMillis();
            long diff1 = (endTime1 - startTime1);
            bt.printResult();
            System.out.printf("Exhaustive Search Algorithm (Back tracking) takes: %d milli-seconds\n", diff1);

            // Dynamic programming algorithm
            System.out.println("Dynamic Programming Algorithm :");
            long startTime2 = System.currentTimeMillis();
            DynamicProgramming dp = new DynamicProgramming(matrixInt2, rows, cols);
            dp.findPath(matrixInt2, rows, cols);
            long endTime2 = System.currentTimeMillis();
            long diff2 = (endTime2 - startTime2);
            dp.printResult();
            System.out.printf("Dynamic Programming Algorithm takes: %d milli-seconds\n", diff2);

            sc.close();

        }

        catch (Exception e) 
        {
            System.out.println("Error reading file: " + e);
        }
    }

    // Convert String Matrix to Numeric Matrix
    public static long[][] convertStringMatrixToNumericMatrix(String[][] matrix, int rows, int cols) {
        long[][] goldTable = new long[rows][cols];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (matrix[i][j].equals("x") || matrix[i][j].equals("X"))
                    goldTable[i][j] = -1;
                else if (matrix[i][j].equals("."))
                    goldTable[i][j] = 0;
                else
                    goldTable[i][j] = Long.parseLong(matrix[i][j]);
            }
        }

        return goldTable;
    }
}

// Backtracking (exhaustive search algorithms) algorithm
// Time complexity: O(2 ^(m x n))
// Space complexity: O(m x n)
class BackTracking {
    long[][] matrix;
    int rows, cols;
    long max_gold = 0;
    String cur_path = "";
    String best_path = "";

    public BackTracking(long[][] matrix, int rows, int cols) {
        this.matrix = matrix;
        this.rows = rows;
        this.cols = cols;
    }

    // Check if the current position is still in matrix length and able to move
    public boolean canMove(int x, int y) {
        return (x >= 0 && x <= rows - 1) && (y >= 0 && y <= cols - 1) && matrix[x][y] >= 0;
    }

    // The main method of Backtracking algorithm
    public void findPath(long[][] matrix, int m, int n, int cur_row, int cur_col, long cur_gold) {
        // Check if the current path has max gold and min path
        if (max_gold == cur_gold && cur_path.length() < best_path.length()) {
            best_path = cur_path;
        } else if (max_gold < cur_gold) {
            max_gold = cur_gold;
            best_path = cur_path;
        }

        // Go down the matrix
        if (canMove(cur_row + 1, cur_col)) {
            cur_path += "D";
            findPath(matrix, m, n, cur_row + 1, cur_col, cur_gold + matrix[cur_row + 1][cur_col]);
            cur_path = cur_path.substring(0, cur_path.length() - 1);
        }

        // Go right the matrix
        if (canMove(cur_row, cur_col + 1)) {
            cur_path += "R";
            findPath(matrix, m, n, cur_row, cur_col + 1, cur_gold + matrix[cur_row][cur_col + 1]);
            cur_path = cur_path.substring(0, cur_path.length() - 1);
        }

    }

    // Print the final result
    public void printResult() {
        System.out.printf("Steps: %d, Gold: %d, Path: %s\n", best_path.length(), max_gold, best_path);
    }
}

// Dynamic algorithm
// Time complexity: O(m x n)
// Space complexity: O(m x n)
class DynamicProgramming {
    long[][] matrix;
    int rows, cols;
    long max_gold = 0;
    int min_stop = 0;
    int min_row = 0;
    int min_col = 0;
    String path = "";

    public DynamicProgramming(long[][] matrix, int rows, int cols) {
        this.matrix = matrix;
        this.cols = cols;
        this.rows = rows;
    }

    // Check if the current position is still in matrix length and able to move
    public boolean canMove(int x, int y) {
        return (x >= 0 && x <= rows - 1) && (y >= 0 && y <= cols - 1) && matrix[x][y] >= 0;
    }

    // The main method of Dynamic algorithm
    public void findPath(long[][] matrix, int m, int n) {
        long[][] dp = new long[m][n]; // matrix to check the path that we go have the max gold
        char[][] trace = new char[m][n]; // matrix tracking the best path to go

        // Create the matrix to check the path that we go have the max gold
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                dp[i][j] = Integer.MIN_VALUE;
            }
        }

        dp[0][0] = matrix[0][0];

        // Calculate the gold in matrix dp and path in matrix trace
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                if (matrix[i][j] == -1) // This cell is rock cannot move
                {
                    continue;
                }
                if (canMove(i + 1, j) && dp[i][j] + matrix[i + 1][j] > dp[i + 1][j]) // Check the bottom cell
                {
                    dp[i + 1][j] = dp[i][j] + matrix[i + 1][j];
                    trace[i + 1][j] = 'D';
                }
                if (canMove(i, j + 1) && dp[i][j] + matrix[i][j + 1] > dp[i][j + 1]) // Check the right cell
                {
                    dp[i][j + 1] = dp[i][j] + matrix[i][j + 1];
                    trace[i][j + 1] = 'R';
                }
            }
        }

        // Loop back to dp matrix finout the max gold with min step
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                if (max_gold < dp[i][j] || (max_gold == dp[i][j] && i + j < min_stop)) {
                    min_stop = i + j;
                    min_row = i;
                    min_col = j;
                    max_gold = dp[i][j];
                }
            }
        }

        int cur_row = min_row;
        int cur_col = min_col;

        // Loop back to create the path way
        while (cur_row > 0 || cur_col > 0) {
            if (trace[cur_row][cur_col] == 'R') {
                path = "R" + path;
                --cur_col;
            } else if (trace[cur_row][cur_col] == 'D') {
                path = "D" + path;
                --cur_row;
            }
        }
    }

    // Print the final result
    public void printResult() {
        System.out.printf("Steps: %d, Gold: %d, Path: %s\n", min_stop, max_gold, path);
    }
}
