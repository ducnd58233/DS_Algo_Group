import java.io.*;
import java.util.Scanner;

public class Solution {
    public static void main(String[] args) {
        File inFile = null;
        int rows, cols;
        char[][] matrix;
        
        if (args.length <= 0) {
            System.err.println("Missing arguments input file name!");
            System.exit(1);
        }
        try {
            inFile = new File(args[0]);

            Scanner sc = new Scanner(inFile);
            rows = sc.nextInt();
            cols = sc.nextInt();
            sc.nextLine();
            matrix = new char[rows][cols];

            for (int r = 0; r < rows; r++) {
                String line = sc.nextLine();
                int c = 0;
                for(char ch: line.toCharArray()) {
                    matrix[r][c] = ch;
                    c++;
                }
            }

            int[][] matrixInt1 = convertMatrixToIntMatrix(matrix, rows, cols);
            int[][] matrixInt2 = convertMatrixToIntMatrix(matrix, rows, cols);

            // Backtracking algorithm
            long startTime1 = System.currentTimeMillis();
            BackTracking bt = new BackTracking(matrixInt1, rows, cols);
            bt.findPath(matrixInt1, rows, cols, 0, 0, matrixInt1[0][0]);
            long endTime1 = System.currentTimeMillis();
            long diff1 = (endTime1 - startTime1);
            bt.printResult();
            System.out.printf("Exhaustive Search Algorithm (Back tracking) takes: %d milli-seconds\n", diff1);

            // Dynamic programming algorithm
            long startTime2 = System.currentTimeMillis();
            DynamicProgramming dp = new DynamicProgramming(matrixInt2, rows, cols);
            dp.findPath(matrixInt2, rows, cols);
            long endTime2 = System.currentTimeMillis();
            long diff2 = (endTime2 - startTime2);
            dp.printResult();
            System.out.printf("Dynamic Programming Algorithm takes: %d milli-seconds\n", diff2);

            sc.close();

        } catch (Exception e) {
            System.out.println("Error reading file: " + e);
        }    
    }
 
    public static int[][] convertMatrixToIntMatrix(char[][] matrix, int rows, int cols) {
        int[][] goldTable = new int[rows][cols];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if(matrix[i][j] == 'x' || matrix[i][j] == 'X') goldTable[i][j] = -1;
                else if (matrix[i][j] == '.') goldTable[i][j] = 0;
                else goldTable[i][j] = Character.getNumericValue(matrix[i][j]);
            }
        }

        return goldTable;
    }
}

// Time complexity: O(2 ^(m x n))
// Space complexity: O(m x n)
class BackTracking {
    int[][] matrix;
    int rows, cols;
    int max_gold = 0;
    String cur_path = "";
    String best_path = "";

    public BackTracking(int[][] matrix, int rows, int cols) {
        this.matrix = matrix;
        this.rows = rows;
        this.cols = cols;
    }

    public boolean canMove(int x, int y) {
        return (x >= 0 && x <= rows - 1) && (y >= 0 && y <= cols - 1) && matrix[x][y] >= 0;
    }

    public void findPath(int[][] matrix, int m, int n, int cur_row, int cur_col, int cur_gold) {
        if (max_gold == cur_gold && cur_path.length() < best_path.length()) {
            best_path = cur_path;
        } else if (max_gold < cur_gold) {
            max_gold = cur_gold;
            best_path = cur_path;
        }

        if (canMove(cur_row + 1, cur_col)) {    
            cur_path += "D";      
            findPath(matrix, m, n, cur_row + 1, cur_col, cur_gold + matrix[cur_row + 1][cur_col]);
            cur_path = cur_path.substring(0, cur_path.length()-1);
        }
        

        if (canMove(cur_row, cur_col + 1)) {
            cur_path += "R";
            findPath(matrix, m, n, cur_row, cur_col + 1, cur_gold + matrix[cur_row][cur_col + 1]);
            cur_path = cur_path.substring(0, cur_path.length()-1);
        }
        
    }

    public void printResult() {
        System.out.printf("Steps: %d, Gold: %d, Path: %s\n", best_path.length(), max_gold, best_path);
    }
}

// Time complexity: O(m x n)
// Space complexity: O(m x n)
class DynamicProgramming {
    int[][] matrix;
    int rows, cols;
    int max_gold = 0;
    int min_stop = 0;
    int min_row = 0;
    int min_col = 0;
    int stop_row = 0;
    int stop_col = 0;
    String path = "";

    public DynamicProgramming(int[][] matrix, int rows, int cols) {
        this.matrix = matrix;
        this.cols = cols;
        this.rows = rows;
    }

    public boolean canMove(int x, int y) {
        return (x >= 0 && x <= rows - 1) && (y >= 0 && y <= cols - 1) && matrix[x][y] >= 0;
    }

    public void findPath(int[][] matrix, int m, int n) {
        int[][] dp = new int[m][n];
        char[][] trace = new char[m][n];

        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                dp[i][j] = Integer.MIN_VALUE;
            }
        }

        dp[0][0] = 0;
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                if (matrix[i][j] == -1) {
                    continue;
                }
                if (canMove(i + 1, j) && dp[i][j] + matrix[i + 1][j] > dp[i + 1][j]) {
                    dp[i + 1][j] = dp[i][j] + matrix[i + 1][j];
                    trace[i + 1][j] = 'D';
                }
                if (canMove(i, j + 1) && dp[i][j] + matrix[i][j + 1] > dp[i][j + 1]) {
                    dp[i][j + 1] = dp[i][j] + matrix[i][j + 1];
                    trace[i][j + 1] = 'R';
                } 
            }
        }

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

    public void printResult() {
        System.out.printf("Steps: %d, Gold: %d, Path: %s\n", min_stop, max_gold, path);
    }
}
