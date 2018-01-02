import java.io.*;
import java.util.*;

public class Homework2 {

    static char[][] board;
    static double totalTime;
    static int[][] direction = new int[][] {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
    static long startTime;

    static class Coordinate {
        int x;
        int y;
        public Coordinate(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Coordinate)) {
                return false;
            }
            Coordinate another = (Coordinate) obj;
            return this.x == another.x && this.y == another.y;
        }

        @Override
        public int hashCode() {
            return 101 * x + y;
        }
    }

    public static void main(String[] args) {
        try {
            File fileName = new File("input.txt");
            BufferedReader br = new BufferedReader(new FileReader(fileName));
            String line = "";
            line = br.readLine();
            int n = Integer.parseInt(line.trim());
            line = br.readLine();
            int p  = Integer.parseInt(line.trim());
            line = br.readLine();
            totalTime = Double.parseDouble(line.trim());

            board = new char[n][n];
            int row = 0;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                for (int j = 0; j < line.length(); j++) {
                    board[row][j] = line.charAt(j);
                }
                row++;
            }
            br.close();

            // Start to choose a move. Here may change lookAhead according to the time limit.
            startTime = System.currentTimeMillis();
//            int[] res = move(2);
            int[] res;
            if (totalTime > 10) {
                res = move(3);
            } else {
                res = move(2);
            }

            Coordinate finalMove = new Coordinate(res[0], res[1]);
            List<Coordinate> eliminates = new ArrayList<>();
            Set<Coordinate> visited = new HashSet<>();
            dfs(board[finalMove.x][finalMove.y], finalMove.x, finalMove.y, eliminates, visited);
            executeMove(eliminates);
            gravity(eliminates);

            File writename = new File("output.txt");
            if (!writename.exists()) {
                writename.createNewFile();
            }
            BufferedWriter out = new BufferedWriter(new FileWriter(writename));
            char col = (char) ('A' + finalMove.y);
            String ro = String.valueOf(finalMove.x + 1);
            out.write("" + col + ro + "\n");
            for (int i = 0; i < n; i++) {
                StringBuilder sb = new StringBuilder();
                for (int j = 0; j < n; j++) {
                    sb.append(board[i][j]);
                }
                sb.append("\n");
                out.write(sb.toString());
            }
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static int[] move(int lookAhead) {
        int[] res = minMax("1", lookAhead, Integer.MIN_VALUE, Integer.MAX_VALUE);
        return new int[] {res[1], res[2]};
    }

    // turn = "1" means this is a max turn (my turn), -1 means this is min turn (opponent's turn)
    private static int[] minMax(String turn, int lookAhead, int alpha, int beta) {
        Map<Coordinate, List<Coordinate>> chain = new HashMap<>();
        List<Coordinate> moves = findMove(chain);

        // base case
        if (lookAhead == 0 || moves.size() == 0) {
            if (turn.equals("1")) {
                return new int[] {calculateCurState(), -1, -1};
            } else {
                return new int[] {-calculateCurState(), -1, -1};
            }
        }

        int resRow = -1;
        int resCol = -1;
        int score;

        for (Coordinate move : moves) {
            char cur = board[move.x][move.y];
            List<Coordinate> eliminate = chain.get(move);
            executeMove(eliminate);
            char[][] shift = gravity(eliminate);
            if (turn.equals("1")) {
                score = eliminate.size() * eliminate.size() + minMax("-1", lookAhead - 1, alpha, beta)[0];
                if (score > alpha) {
                    alpha = score;
                    resRow = move.x;
                    resCol = move.y;
                }
            } else {
                score = - eliminate.size() * eliminate.size() + minMax("1", lookAhead - 1, alpha, beta)[0];
                if (score < beta) {
                    beta = score;
                    resRow = move.x;
                    resCol = move.y;
                }
            }

            unGravity(shift);
            recover(cur, eliminate);

            if (alpha >= beta) {
                break;
            }
            long curTime = System.currentTimeMillis();
            if (curTime - startTime > totalTime * 1000 / 2) {
                if (lookAhead % 2 == 0) {
                    return new int[] {alpha, resRow, resCol};
                } else {
                    return new int[] {beta, resRow, resCol};
                }
            }
        }
        if (turn.equals("1")) {
            return new int[] {alpha, resRow, resCol};
        } else {
            return new int[] {beta, resRow, resCol};
        }
    }

    private static List<Coordinate> findMove(Map<Coordinate, List<Coordinate>> chain) {
        List<Coordinate> list = new ArrayList<>();
        Set<Coordinate> noNeedToMove = new HashSet<>();
        for (int i = 0; i <= board.length - 1; i++) {
            for (int j = 0; j <= board[0].length - 1; j++) {
                if (board[i][j] == '*' || noNeedToMove.contains(new Coordinate(i, j))) {
                    continue;
                } else {
                    Coordinate head = new Coordinate(i, j);
                    list.add(head);
                    chain.put(head, new ArrayList<>());
                    List<Coordinate> eliminates = chain.get(head);
                    dfs(board[i][j], i, j, eliminates, noNeedToMove);
                    noNeedToMove.remove(new Coordinate(i, j)); // Remove itself, which is also added to this set
                }
            }
        }
        return list;
    }

    private static void dfs(char target, int i, int j, List<Coordinate> eliminates, Set<Coordinate> noNeedToMove) {
        if (i < 0 || i >= board.length || j < 0 || j >= board[0].length || board[i][j] != target || noNeedToMove.contains(new Coordinate(i, j))) {
            return;
        }
        noNeedToMove.add(new Coordinate(i, j));
        for (int[] dir : direction) {
            dfs(target, i + dir[0], j + dir[1], eliminates, noNeedToMove);
        }
        eliminates.add(new Coordinate(i, j));
    }

    private static int dfs(char target, int i, int j, Set<Coordinate> visited) {
        if (i < 0 || i >= board.length || j < 0 || j >= board[0].length || board[i][j] != target || visited.contains(new Coordinate(i, j))) {
            return 0;
        }
        int res = 0;
        visited.add(new Coordinate(i, j));
        for (int[] dir : direction) {
            res += dfs(target, i + dir[0], j + dir[1], visited);
        }
        return res + 1;
    }

    private static int calculateCurState() {
        int res = 0;
        Set<Coordinate> visited = new HashSet<>();
        for (int i = 0; i <= board.length - 1; i++) {
            for (int j = 0; j <= board[0].length - 1; j++) {
                if (!visited.contains(new Coordinate(i, j)) && board[i][j] != '*') {
                    res = Math.max(res, dfs(board[i][j], i, j, visited));
                }
            }
        }
        return res;
    }

    private static void executeMove(List<Coordinate> list) {
        for (Coordinate co : list) {
            board[co.x][co.y] = '*';
        }
    }

    private static void recover(char cur, List<Coordinate> list) {
        for (Coordinate co : list) {
            board[co.x][co.y] = cur;
        }
    }

    private static char[][] gravity(List<Coordinate> list) {
        int[] cols = new int[board[0].length];
        for (Coordinate co : list) {
            if (cols[co.y] == 0) {
                cols[co.y] = 1;
            }
        }
        char[][] shift = new char[board.length][board[0].length]; // a row in shift is the original column in the board
        for (int i = 0; i <= cols.length - 1; i++) {
            if (cols[i] == 1) {
                shift[i] = columnGravity(i);
            }
        }
        return shift;
    }

    private static char[] columnGravity(int col) {
        char[] res = new char[board.length];
        int starBottom = -1;
        int numBottom = -1;
        for (int i = board.length -1 ; i >= 0; i--) {
            res[i] = board[i][col];
            if (res[i] == '*' && starBottom == -1) {
                starBottom = i;
            } else if (res[i] != '*' && starBottom != -1 && numBottom == -1) {
                numBottom = i;
            }
        }
        if (numBottom == - 1) {
            return res;
        }
        while (numBottom >= 0 && board[numBottom][col] != '*' && board[starBottom][col] == '*') {
            swap(numBottom--, starBottom--, col);
        }
        return res;
    }

    private static void unGravity(char[][] shift) {
        for (int i = 0; i <= shift.length - 1; i++) {
            if (shift[i][0] == '\u0000') {  // null
                continue;
            } else {
                for (int j = 0; j <= shift[0].length - 1; j++) {
                    board[j][i] = shift[i][j];
                }
            }
        }
    }

    private static void swap(int i, int j, int col) {
        char temp = board[i][col];
        board[i][col] = board[j][col];
        board[j][col] = temp;
    }
}
