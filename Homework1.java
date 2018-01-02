import java.io.*;
import java.util.*;

public class Homework1 {
    public static void main(String[] args) {
        try {
            File filename = new File("input.txt");
            BufferedReader br = new BufferedReader(new FileReader(filename));
            String line = "";
            line = br.readLine();
            String algo = line.trim();
            line = br.readLine();
            int n = Integer.parseInt(line.trim());
            int[][] input = new int[n][n];
            line = br.readLine();
            int p = Integer.parseInt(line.trim());
            int row = 0;
            while (true) {
                line = br.readLine();
                if (line != null) {
                    for (int j = 0; j < line.trim().length(); j++) {
                        input[row][j] = line.charAt(j) - '0';
                    }
                    row++;
                } else {
                    break;
                }
            }
            br.close();

            List<List<Integer>> res = new ArrayList<>();
            boolean find = false;
            if (algo.equals("DFS")) {
                 res = arrangeLizardDFS(input, n, p);
            } else if (algo.equals("BFS")) {
                res = arrangeLizardBFS(input, n, p);
            } else {
                find = simulated(input, n, p);
            }

            if (algo.equals("SA")) {
                if (find) {
                    File writename = new File("output.txt");
                    if (!writename.exists()) {
                        writename.createNewFile();
                    }
                    BufferedWriter out = new BufferedWriter(new FileWriter(writename));
                    out.write("OK\n");
                    for (int i = 0; i < n; i++) {
                        StringBuilder sb = new StringBuilder();
                        for (int j = 0; j < n; j++) {
                            sb.append(input[i][j]);
                        }
                        sb.append("\n");
                        out.write(sb.toString());
                    }
                    out.flush();
                    out.close();
                } else {
                    File writename = new File("output.txt");
                    if (!writename.exists()) {
                        writename.createNewFile();
                    }
                    BufferedWriter out = new BufferedWriter(new FileWriter(writename));
                    out.write("FAIL");
                    out.flush();
                    out.close();
                }
            } else {
                if (res.size() == 0) {
                    File writename = new File("output.txt");
                    if (!writename.exists()) {
                        writename.createNewFile();
                    }
                    BufferedWriter out = new BufferedWriter(new FileWriter(writename));
                    out.write("FAIL");
                    out.flush();
                    out.close();
                } else {
                    for (int i = 0; i < n; i++) {
                        List<Integer> cols = res.get(i);
                        for (Integer col : cols) {
                            input[i][col] = 1;
                        }
                    }
                    File writename = new File("output.txt");
                    if (!writename.exists()) {
                        writename.createNewFile();
                    }
                    BufferedWriter out = new BufferedWriter(new FileWriter(writename));
                    out.write("OK\n");
                    for (int i = 0; i < n; i++) {
                        StringBuilder sb = new StringBuilder();
                        for (int j = 0; j < n; j++) {
                            sb.append(input[i][j]);
                        }
                        sb.append("\n");
                        out.write(sb.toString());
                    }
                    out.flush();
                    out.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static List<List<Integer>> arrangeLizardDFS(int[][] input, int n, int p) {
        // Use a list of list of integers to record the result.
        // Each inner list represent the columns to put lizard on this row.
        List<List<Integer>> res = new ArrayList<>();
        List<List<Integer>> cur = new ArrayList<>(n);
        if (n <= 0) {
            return res;// FAIL!
        }
        // In order to be convenient in DFS, first we record the position of every tree.
        // Each inner list records the rows of trees on this column. If there is no trees on this column, record -1.
        List<List<Integer>> treesRowToCol = new ArrayList<>();
        int[] maxLizardByRow = new int[n]; // just consider the collision on each row, the maximum number of lizards that can be put
        for (int i = 0; i <= n - 1; i++) { // each row
            treesRowToCol.add(new ArrayList<>()); // Assume that there is always a tree on column - 1.
            treesRowToCol.get(i).add(-1);
            for (int j = 0; j <= n - 1; j++) { // each col
                if (input[i][j] == 2) {
                    treesRowToCol.get(i).add(j); // record col
                }
            }
            treesRowToCol.get(i).add(n); // Assume that there is always a tree on column n.
            maxLizardByRow[i] = treesRowToCol.get(i).size() - 1;
        }
        for (int i = n - 2; i >= 0; i--) {
            maxLizardByRow[i] = maxLizardByRow[i] + maxLizardByRow[i + 1];
        }
        if (p > maxLizardByRow[0]) {
            return new ArrayList<>();
        }
        // Used in DFS to record if a solution has existed
        boolean[] flag = new boolean[] {false};
        // dfs helper function
        arrangeLizard(input, res, cur, treesRowToCol, maxLizardByRow, n, p, 0, 0, 1, flag);
        if (flag[0]) {
            return res;
        } else {
            return new ArrayList<>(); // Fail!
        }
    }
    private static void arrangeLizard(int[][] input, List<List<Integer>> res, List<List<Integer>> cur, List<List<Integer>> treesRowToCol,
                               int[] maxLizardByRow,int n, int p, int level, int searchStart,
                               int searchEnd, boolean[] flag) {
        if(flag[0]) {
            return;
        }
        if (p == 0) {
            for (int i = 0; i <= cur.size() - 1; i++) {
                res.add(new ArrayList<>(cur.get(i)));
            }
            while (res.size() < n) {
                res.add(new ArrayList<>());
            }
            flag[0] = true;
            return;
        }
        if (level == n || searchEnd == treesRowToCol.get(level).size()) { // If flag has been true, we will not do any DFS afterwards
            return;
        }
        // Initialize the inner list in the list of list. Don't forget!
        if (cur.size() < level + 1) {
            cur.add(new ArrayList<>());
        }
        if (p > maxLizardByRow[level] - cur.get(level).size()) { // prune
            return;
        }
        for (int i = treesRowToCol.get(level).get(searchStart) + 1; i <= treesRowToCol.get(level).get(searchEnd) - 1; i++) {
            if (checkValid(input, cur, level, i)) {
                cur.get(level).add(i);
                if (treesRowToCol.get(level).get(searchEnd) != n) {
                    // Then search on the same level
                    arrangeLizard(input, res, cur, treesRowToCol, maxLizardByRow, n, p - 1, level, searchStart + 1, searchEnd + 1, flag);
                } else {
                    // Then search on the next level
                    arrangeLizard(input, res, cur, treesRowToCol, maxLizardByRow, n, p - 1, level + 1, 0, 1, flag);
                }
                // don't add on this position
                cur.get(level).remove(cur.get(level).size() - 1);
            }
        }
        // Don't add any lizards on this section
        if (treesRowToCol.get(level).get(searchEnd) != n) {
            // Then search on the same level
            arrangeLizard(input, res, cur, treesRowToCol, maxLizardByRow, n, p, level, searchStart + 1, searchEnd + 1, flag);
        } else {
            // Then search on the next level
            arrangeLizard(input, res, cur, treesRowToCol, maxLizardByRow, n, p, level + 1, 0, 1, flag);
        }
    }
    // The checkValid function actually controls the validity on columns and diagonals.
    // We have controlled the validity of rows in the last funtion.
    private static boolean checkValid(int[][] input, List<List<Integer>> cur, int curRow, int curCol) {
        // 1. There is some lizards on the current column, and we don't have a tree in the middle
        // 2. There is some lizards on the current diagonal, and we don't have a tree in the middle
        // Note that in each inner list of treesColToRow, the numbers are in increasing order
        for (int i = 0; i <= curRow - 1; i++) {
            for (Integer j : cur.get(i)) {
                // Situation 1
                if (j == curCol) {
                    boolean hasTree = false;
                    int tempRow = i + 1;
                    while (tempRow < curRow) {
                        if (input[tempRow][j] == 2) {
                            hasTree = true;
                            break;
                        }
                        tempRow++;
                    }
                    if (!hasTree) {
                        return false;
                    }
                }
                // Situation 2.1: on the LHS diagonal
                if (curRow - i == curCol - j) {
                    boolean hasTree = false;
                    int tempRow = i + 1;
                    int tempCol = j + 1;
                    while (tempRow < curRow && tempCol < curCol) {
                        if (input[tempRow][tempCol] == 2) {
                            hasTree = true;
                            break;
                        }
                        tempRow++;
                        tempCol++;
                    }
                    if (!hasTree) {
                        return false;
                    }
                }
                // Situation 2.2: on the RHS diagonal
                if (curRow - i == j - curCol) {
                    boolean hasTree = false;
                    int tempRow = i + 1;
                    int tempCol = j - 1;
                    while (tempRow < curRow && tempCol > curCol) {
                        if (input[tempRow][tempCol] == 2) {
                            hasTree = true;
                            break;
                        }
                        tempRow++;
                        tempCol--;
                    }
                    if (!hasTree) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public static List<List<Integer>> arrangeLizardBFS(int[][] input, int n, int p) {
        if (n <= 0) {
            return new ArrayList<>();// FAIL!
        }
        List<List<Integer>> treesRowToCol = new ArrayList<>();
        List<List<Integer>> start = new ArrayList<>();
        int[] maxLizardByRow = new int[n]; // just consider the collision on each row, the maximum number of lizards that can be put
        for (int i = 0; i <= n - 1; i++) { // each row
            start.add(new ArrayList<>());
            treesRowToCol.add(new ArrayList<>()); // Assume that there is always a tree on column - 1.
            treesRowToCol.get(i).add(-1);
            for (int j = 0; j <= n - 1; j++) { // each col
                if (input[i][j] == 2) {
                    treesRowToCol.get(i).add(j); // record col
                }
            }
            treesRowToCol.get(i).add(n); // Assume that there is always a tree on column n.
            maxLizardByRow[i] = treesRowToCol.get(i).size() - 1;
        }
        if (p == 0) {
            return start;
        }
        for (int i = n - 2; i >= 0; i--) {
            maxLizardByRow[i] = maxLizardByRow[i] + maxLizardByRow[i + 1];
        }
        if (p > maxLizardByRow[0]) {
            return new ArrayList<>();
        }
        // Now each inner list in cur is NULL.
        Queue<List<List<Integer>>> queue = new LinkedList<>();
        //Queue<Integer> remaining = new LinkedList<>(); // Stores the remaining p for the corresponding current state
        queue.offer(start);
        //remaining.offer(p);
        int curRow = 0;
        int searchStart = 0;
        int searchEnd = 1;
        /*
            For each List<List<Integer>> we get by queue.poll(), we start from 1st col to find any position that we can
            put a lizard on each row. Once we have such a position, then we put a lizard and put this partial solution back to the queue.
         */
        while (!queue.isEmpty()) {
            int size = queue.size();
            for (int i = 0; i <= size - 1; i++) {
                List<List<Integer>> cur = queue.poll();
                int remainingP = p - numHasPutIn(cur, curRow);
                if (remainingP > maxLizardByRow[curRow] - cur.get(curRow).size()) {
                    continue;
                }
                List<List<Integer>> emptyCur = new ArrayList<>(cur); // We can also don't add anything on this row.
                for (int j = treesRowToCol.get(curRow).get(searchStart) + 1; j <= treesRowToCol.get(curRow).get(searchEnd) - 1; j++) { // The columns we are focusing on on this row
                    if (checkValid(input, cur, curRow, j)) {
                        cur.get(curRow).add(j);
                        if (remainingP == 1) {
                            return cur;
                        }
                        List<List<Integer>> temp = new ArrayList<>();
                        for (int k = 0; k <= cur.size() - 1; k++) {
                            temp.add(new ArrayList<>(cur.get(k)));
                        }
                        queue.offer(temp);
                        cur.get(curRow).remove(cur.get(curRow).size() - 1);
                    }
                }
                queue.offer(emptyCur);
            }
            if (searchEnd == treesRowToCol.get(curRow).size() - 1) {
                curRow++;
                if (curRow >= n) {
                    break;
                }
                searchStart = 0;
                searchEnd = 1;
            } else {
                searchStart++;
                searchEnd++;
            }
        }
        return new ArrayList<>();
    }

    private static int numHasPutIn(List<List<Integer>> cur, int curRow) {
        int res = 0;
        for (int i = 0; i <= curRow; i++) {
            res += cur.get(i).size();
        }
        return res;
    }

    // SA
    public static boolean simulated(int[][] input, int n, int p) {
        if (n <= 0 || input.length != n) {
            return false;
        }

        double temperature = 100.0;
        Random rand = new Random();

        List<List<Integer>> treesRowToCol = new ArrayList<>();
        for (int i = 0; i <= n - 1; i++) { // each row
            treesRowToCol.add(new ArrayList<>()); // Assume that there is always a tree on column - 1.
            treesRowToCol.get(i).add(-1);
            for (int j = 0; j <= n - 1; j++) { // each col
                if (input[i][j] == 2) {
                    treesRowToCol.get(i).add(j); // record col
                }
            }
            treesRowToCol.get(i).add(n); // Assume that there is always a tree on column n.
        }

        List<List<Integer>> curState = new ArrayList<>();
        int hasPut = 0;
        // initialize the state. Put lizard and only consider the collision in the same section
        for (int i = 0; i <= n - 1; i++) {
            List<Integer> treeThisRow = treesRowToCol.get(i);
            curState.add(new ArrayList<>());
            int searchStart = 0;
            int searchEnd = 1;
            while (searchEnd <= treeThisRow.size() - 1 && hasPut < p) {
                if (treeThisRow.get(searchEnd) - treeThisRow.get(searchStart) - 1 > 0) {
                    int randCol = treeThisRow.get(searchStart) + 1 + rand.nextInt(treeThisRow.get(searchEnd) - treeThisRow.get(searchStart) - 1);
                    input[i][randCol] = 1;
                    curState.get(i).add(randCol);
                    hasPut++;
                }
                searchStart++;
                searchEnd++;
            }
        }

        if (hasPut < p) {
            return false;
        }

        int curScore = findCollision(input, n);
        if (curScore == 0) {
//            for (int i = 0; i <= n - 1; i++) {
//                for (int j = 0; j <= n - 1; j++) {
//                    System.out.print(input[i][j] + " ");
//                }
//                System.out.println();
//            }
            return true;
        }

        while (temperature > 0.000001) {

            // 1. 随机从所有蜥蜴中抽出一个，定为我们要移动的蜥蜴
            int rowSelected = -1; // 最后选中要被移动的蜥蜴的row
            int colSelected = -1; // 最后选中要被移动的蜥蜴的col
            int count = 0; // 遍历中已经遇见多少个蜥蜴
            for (int i = 0; i <= curState.size() - 1; i++) {
                for (int j = 0; j <= curState.get(i).size() - 1; j++) {
                    count++;
                    if (rand.nextInt(count) == 0) {
                        rowSelected = i;
                        colSelected = curState.get(i).get(j);
                    }
                }
            }

            // 2. 随机选取一个下一状态
            boolean better = false;
            int next;
            int rowNext;
            int colNext;

            while (true) {
                next = rand.nextInt(n * n);
                rowNext = next / n;
                colNext = next % n;
                if (input[rowNext][colNext] != 1 && input[rowNext][colNext] != 2) {
                    break;
                }
            }

            // 3. 计算移动后的collision数量
            input[rowSelected][colSelected] = 0;
            input[rowNext][colNext] = 1;
            int nextScore = findCollision(input, n);

            // 结束条件:
            if (nextScore == 0) {
//                for (int i = 0; i <= n - 1; i++) {
//                    for (int j = 0; j <= n - 1; j++) {
//                        System.out.print(input[i][j] + " ");
//                    }
//                    System.out.println();
//                }
                return true;
            }

            int E = nextScore - curScore;

            if (E < 0) {
                better = true;
            } else if (Math.exp((-1) * E / temperature) > ((double)(rand.nextDouble() % 1000) / 1000)) {
                better = true;
            }

            // 如果决定移动当前蜥蜴，那么将curScore更新，curState这个list也更新
            if (better) {
                curScore = nextScore;
                curState.get(rowSelected).remove((Integer) colSelected);
                curState.get(rowNext).add(colNext); // There is no need to sort the inner list after we add a new col.
            } else {
                // 如果决定不移动当前蜥蜴，则将input复原
                input[rowSelected][colSelected] = 1;
                input[rowNext][colNext] = 0;
            }

            temperature *= 0.99999;
        }
        return false;
    }

    private static int findCollision(int[][] matrix, int n) {
        // Count the number of collisions on the current state
        int count = 0;
        for (int i = 0; i <= n - 1; i++) {
            for (int j = 0; j <= n - 1; j++) {
                if (matrix[i][j] == 1) {
                    // left row
                    for (int k = j - 1; k >= 0; k--) {
                        if (matrix[i][k] == 2) {
                            break;
                        }
                        if (matrix[i][k] == 1) {
                            count++;
                        }
                    }
                    // right row
                    for (int k = j + 1; k <= n - 1; k++) {
                        if (matrix[i][k] == 2) {
                            break;
                        }
                        if (matrix[i][k] == 1) {
                            count++;
                        }
                    }
                    // up col
                    for (int k = i - 1; k >= 0; k--) {
                        if (matrix[k][j] == 2) {
                            break;
                        }
                        if (matrix[k][j] == 1) {
                            count++;
                        }
                    }
                    // down col
                    for (int k = i + 1; k <= n - 1; k++) {
                        if (matrix[k][j] == 2) {
                            break;
                        }
                        if (matrix[k][j] == 1) {
                            count++;
                        }
                    }
                    // up left
                    for (int k = i - 1, l = j - 1; k >= 0 && l >= 0; k--, l--) {
                        if (matrix[k][l] == 2) {
                            break;
                        }
                        if (matrix[k][l] == 1) {
                            count++;
                        }
                    }
                    // down left
                    for (int k = i + 1, l = j - 1; k <= n - 1 && l >= 0; k++, l--) {
                        if (matrix[k][l] == 2) {
                            break;
                        }
                        if (matrix[k][l] == 1) {
                            count++;
                        }
                    }
                    // up right
                    for (int k = i - 1, l = j + 1; k >= 0 && l <= n - 1; k--, l++) {
                        if (matrix[k][l] == 2) {
                            break;
                        }
                        if (matrix[k][l] == 1) {
                            count++;
                        }
                    }
                    // down right
                    for (int k = i + 1, l = j + 1; k <= n - 1 && l <= n - 1; k++, l++) {
                        if (matrix[k][l] == 2) {
                            break;
                        }
                        if (matrix[k][l] == 1) {
                            count++;
                        }
                    }
                }
            }
        }
        return count / 2;
    }
}
