package transportationproblem;

import java.util.*;

public class Main {
    public static void main(String[] args) {
        // process input from the user
//        Scanner scanner = new Scanner(System.in);
//        int[] supply = Arrays.stream(scanner.nextLine().split(" ")).mapToInt(Integer::parseInt).toArray();
//        int[][] costs = new int[supply.length][];
//        for (int i = 0; i < supply.length; i++) {
//            costs[i] = Arrays.stream(scanner.nextLine().split(" ")).mapToInt(Integer::parseInt).toArray();
//        }
//        int[] demand = Arrays.stream(scanner.nextLine().split(" ")).mapToInt(Integer::parseInt).toArray();
//        ProblemConfiguration config = new ProblemConfiguration(supply, costs, demand);
//        runSolver(config);

        // hard coded input for testing
        ProblemConfiguration test1 = new ProblemConfiguration(
                new int[]{20, 30, 25},
                new int[][]{
                        {2, 3, 1, 4},
                        {3, 3, 2, 2},
                        {4, 2, 5, 9}
                },
                new int[]{10, 15, 20, 30}
        );
        System.out.println();
        System.out.println("=== Test 1 ===");
        System.out.println();
        runSolver(test1);

        ProblemConfiguration test2 = new ProblemConfiguration(
                new int[]{20, 30, 25},
                new int[][]{
                        {2, 3, 1, 4},
                        {3, 3, 2, 2},
                        {4, 2, 5, -9}
                },
                new int[]{10, 15, 20, 30}
        );
        System.out.println();
        System.out.println("=== Test 2 ===");
        System.out.println();
        runSolver(test2);

        ProblemConfiguration test3 = new ProblemConfiguration(
                new int[]{20, 30, 25},
                new int[][]{
                        {2, 3, 1, 4},
                        {3, 3, 2, 2},
                        {4, 2, 5, 9}
                },
                new int[]{10, 15, 10, 10}
        );
        System.out.println();
        System.out.println("=== Test 3 ===");
        System.out.println();
        runSolver(test3);

        ProblemConfiguration test4 = new ProblemConfiguration(
                new int[]{160, 140, 170},
                new int[][]{
                        {7, 8, 1, 2},
                        {4, 5, 9, 8},
                        {9, 2, 3, 6}
                },
                new int[]{120, 50, 190, 110}
        );
        System.out.println();
        System.out.println("=== Test 4 ===");
        System.out.println();
        runSolver(test4);
    }

    public static void runSolver(ProblemConfiguration config) {
        System.out.println("Input parameter table:");
        config.printParameterTable();

        // check if the problem is balanced
        if (!config.checkBalanced()) {
            System.out.println("The problem is not balanced!");
            return;
        }

        // check if the method is applicable
        if (!config.checkMethodApplicable()) {
            System.out.println("The method is not applicable!");
            return;
        }

        // solve using North-West corner method
        int[][] result = NorthWestCornerMethod.solve(config);
        printVector("Initial basic feasible solution using North-West corner method:", result);
        System.out.println();

        // solve using Vogel's approximation method
        result = VogelApproximationMethod.solve(config);
        printVector("Initial basic feasible solution using Vogel's approximation method:", result);
        System.out.println();

        // solve using Russell's approximation method
        result = RussellApproximationMethod.solve(config);
        printVector("Initial basic feasible solution using Russell's approximation method:", result);
    }

    public static void printVector(String title, int[][] vector) {
        System.out.println(title);
        for (int[] row : vector) {
            System.out.println(Arrays.toString(row));
        }
    }
}

class ProblemConfiguration {
    public int[] supply;
    public int[][] costs;
    public int[] demand;

    public ProblemConfiguration(int[] supply, int[][] costs, int[] demand) {
        this.supply = supply;
        this.costs = costs;
        this.demand = demand;
    }

    public void printTable(int[][] costs) {
        int cellWidth = 7; // content width without delimiter and whitespace
        String verticalDelimiter = " | ";

        // print header
        System.out.println();
        printRow(List.of("", "D1", "D2", "D3", "D4", "Supply"), cellWidth);
        printHorizontalDelimiter(cellWidth, 6);

        // print rows
        for (int i = 0; i < supply.length; i++) {
            List<String> cells = new ArrayList<>();
            cells.add("S" + (i + 1));
            cells.addAll(Arrays.stream(costs[i])
                    .mapToObj(String::valueOf)
                    .toList());
            cells.add(String.valueOf(supply[i]));
            printRow(cells, cellWidth);
            if (i < supply.length - 1) {
                printHorizontalDelimiter(cellWidth, 6);
            }
        }

        // print last row
        List<String> lastRow = new ArrayList<>();
        lastRow.add("Demand");
        for (int i = 0; i < demand.length; i++) {
            lastRow.add(String.valueOf(demand[i]));
        }
        lastRow.add("");
        printHorizontalDelimiter(cellWidth, 6);
        printRow(lastRow, cellWidth);
        System.out.println();
    }

    public void printParameterTable() {
        printTable(costs);
    }

    private void printRow(List<String> cells, int cellWidth) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < cells.size(); i++) {
            String cellContent = cells.get(i);
            int padding = cellWidth - cellContent.length();
            if (i == 0) {
                padding += 1;
            }
            result.append(" ".repeat(padding)).append(cellContent);
            if (i < cells.size() - 1) {
                result.append(" | ");
            }
        }
        System.out.println(result);
    }

    private void printHorizontalDelimiter(int cellWidth, int cellCount) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < cellCount; i++) {
            result.append("-".repeat(cellWidth + 2));
            if (i < cellCount - 1) {
                result.append("+");
            }
        }
        System.out.println(result);
    }

    public boolean checkBalanced() {
        return Arrays.stream(supply).sum() == Arrays.stream(demand).sum();
    }

    public boolean checkMethodApplicable() {
        for (int[] row : costs) {
            for (int value : row) {
                if (value < 0) {
                    return false;
                }
            }
        }
        for (int s : supply) {
            if (s <= 0) {
                return false;
            }
        }
        for (int d : demand) {
            if (d <= 0) {
                return false;
            }
        }
        return true;
    }
}

class NorthWestCornerMethod {
    public static int[][] solve(ProblemConfiguration config) {
        int[] supply = Arrays.copyOf(config.supply, config.supply.length);
        int[] demand = Arrays.copyOf(config.demand, config.demand.length);
        int[][] costs = new int[config.supply.length][config.demand.length];
        for (int i = 0; i < config.supply.length; i++) {
            costs[i] = Arrays.copyOf(config.costs[i], config.costs[i].length);
        }
        int[][] result = new int[supply.length][demand.length];
        int i = 0, j = 0;
        while (i < supply.length && j < demand.length) {
            int quantity = Math.min(supply[i], demand[j]);
            result[i][j] = quantity;
            supply[i] -= quantity;
            demand[j] -= quantity;
            if (supply[i] == 0) {
                i++;
            }
            if (demand[j] == 0) {
                j++;
            }
        }
        return result;
    }
}

class VogelApproximationMethod {
    public static int[][] solve(ProblemConfiguration config) {
        int[][] allocation = new int[config.supply.length][config.demand.length];

        int[] supplyCopy = config.supply.clone();
        int[] demandCopy = config.demand.clone();
        int[][] costCopy = new int[config.costs.length][];
        for (int i = 0; i < config.costs.length; i++) {
            costCopy[i] = config.costs[i].clone();
        }

        while (hasPositive(supplyCopy) && hasPositive(demandCopy)) {
            List<Float> rowPenalties = new ArrayList<>();
            List<Float> colPenalties = new ArrayList<>();

            for (int i = 0; i < supplyCopy.length; i++) {
                if (supplyCopy[i] > 0) {
                    List<Integer> row = new ArrayList<>();
                    for (int j = 0; j < demandCopy.length; j++) {
                        if (demandCopy[j] > 0) {
                            row.add(costCopy[i][j]);
                        }
                    }
                    if (row.size() >= 2) {
                        Collections.sort(row);
                        rowPenalties.add((float)(row.get(1) - row.get(0)));
                    } else {
                        rowPenalties.add(Float.POSITIVE_INFINITY);
                    }
                } else {
                    rowPenalties.add(Float.NEGATIVE_INFINITY);
                }
            }

            for (int j = 0; j < demandCopy.length; j++) {
                if (demandCopy[j] > 0) {
                    List<Integer> col = new ArrayList<>();
                    for (int i = 0; i < supplyCopy.length; i++) {
                        if (supplyCopy[i] > 0) {
                            col.add(costCopy[i][j]);
                        }
                    }
                    if (col.size() >= 2) {
                        Collections.sort(col);
                        colPenalties.add((float)(col.get(1) - col.get(0)));
                    } else {
                        colPenalties.add(Float.POSITIVE_INFINITY);
                    }
                } else {
                    colPenalties.add(Float.NEGATIVE_INFINITY);
                }
            }

            int rowIdx = maxIndex(rowPenalties);
            int colIdx = maxIndex(colPenalties);
            if (rowPenalties.get(rowIdx) >= colPenalties.get(colIdx)) {
                int minCost = Integer.MAX_VALUE;
                int minCostIndex = -1;
                for (int j = 0; j < demandCopy.length; j++) {
                    if (demandCopy[j] > 0 && costCopy[rowIdx][j] < minCost) {
                        minCost = costCopy[rowIdx][j];
                        minCostIndex = j;
                    }
                }
                int allocationAmount = Math.min(supplyCopy[rowIdx], demandCopy[minCostIndex]);
                allocation[rowIdx][minCostIndex] = allocationAmount;
                supplyCopy[rowIdx] -= allocationAmount;
                demandCopy[minCostIndex] -= allocationAmount;
            } else {
                int minCost = Integer.MAX_VALUE;
                int minCostIndex = -1;
                for (int i = 0; i < supplyCopy.length; i++) {
                    if (supplyCopy[i] > 0 && costCopy[i][colIdx] < minCost) {
                        minCost = costCopy[i][colIdx];
                        minCostIndex = i;
                    }
                }
                int allocationAmount = Math.min(supplyCopy[minCostIndex], demandCopy[colIdx]);
                allocation[minCostIndex][colIdx] = allocationAmount;
                supplyCopy[minCostIndex] -= allocationAmount;
                demandCopy[colIdx] -= allocationAmount;
            }

            for (int i = 0; i < supplyCopy.length; i++) {
                if (supplyCopy[i] == 0) {
                    for (int j = 0; j < demandCopy.length; j++) {
                        costCopy[i][j] = Integer.MAX_VALUE;
                    }
                }
            }
            for (int j = 0; j < demandCopy.length; j++) {
                if (demandCopy[j] == 0) {
                    for (int i = 0; i < supplyCopy.length; i++) {
                        costCopy[i][j] = Integer.MAX_VALUE;
                    }
                }
            }
        }
        return allocation;
    }

    private static boolean hasPositive(int[] array) {
        for (int value : array) {
            if (value > 0) {
                return true;
            }
        }
        return false;
    }

    private static int maxIndex(List<Float> list) {
        float maxVal = Float.NEGATIVE_INFINITY;
        int maxIdx = -1;
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i) > maxVal) {
                maxVal = list.get(i);
                maxIdx = i;
            }
        }
        return maxIdx;
    }
}

class RussellApproximationMethod {
    public static int[][] solve(ProblemConfiguration config) {
        int[] supply = Arrays.copyOf(config.supply, config.supply.length);
        int[] demand = Arrays.copyOf(config.demand, config.demand.length);
        int[][] cost = new int[config.supply.length][config.demand.length];
        for (int i = 0; i < config.supply.length; i++) {
            cost[i] = Arrays.copyOf(config.costs[i], config.costs[i].length);
        }
        int[][] allocation = new int[supply.length][demand.length];

        while (Arrays.stream(supply).sum() > 0 && Arrays.stream(demand).sum() > 0) {
            int[] rowMaxCosts = new int[supply.length];
            int[] colMaxCosts = new int[demand.length];

            for (int i = 0; i < supply.length; i++) {
                if (supply[i] > 0) {
                    rowMaxCosts[i] = Arrays.stream(cost[i])
                            .filter(c -> c != Integer.MAX_VALUE)
                            .max()
                            .orElse(Integer.MIN_VALUE);
                }
            }

            for (int j = 0; j < demand.length; j++) {
                if (demand[j] > 0) {
                    int[] col = new int[supply.length];
                    for (int i = 0; i < supply.length; i++) {
                        col[i] = cost[i][j];
                    }
                    colMaxCosts[j] = Arrays.stream(col)
                            .filter(c -> c != Integer.MAX_VALUE)
                            .max()
                            .orElse(Integer.MIN_VALUE);
                }
            }

            float minDelta = Float.POSITIVE_INFINITY;
            int minI = -1, minJ = -1;
            for (int i = 0; i < supply.length; i++) {
                for (int j = 0; j < demand.length; j++) {
                    if (supply[i] > 0 && demand[j] > 0) {
                        float delta = cost[i][j] - (rowMaxCosts[i] + colMaxCosts[j]);
                        if (delta < minDelta) {
                            minDelta = delta;
                            minI = i;
                            minJ = j;
                        }
                    }
                }
            }

            int allocationAmount = Math.min(supply[minI], demand[minJ]);
            allocation[minI][minJ] = allocationAmount;
            supply[minI] -= allocationAmount;
            demand[minJ] -= allocationAmount;

            if (supply[minI] == 0) {
                Arrays.fill(cost[minI], Integer.MAX_VALUE);
            }
            if (demand[minJ] == 0) {
                for (int[] row : cost) {
                    row[minJ] = Integer.MAX_VALUE;
                }
            }
        }
        return allocation;
    }
}