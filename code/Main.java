/**
 * Program provides shortest paths visualization (optional)
 * author Melih Efe Sonmez
 * since Date: 10.05.2025
 */
public class Main {

    /**
     * Main method of the program.
     * Parses input files, builds map and cost structures, finds shortest paths
     * to each objective, and outputs results to a file and optionally a drawing.
     *
     * @param args Command-line arguments: [-draw] <mapFile> <costFile> <objectivesFile>
     */
    public static void main(String[] args) {

        boolean draw = false;
        String mapDataFile = "";
        String travelCostsFile = "";
        String objectivesFile = "";

        // parse command line arguments
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-draw")) {
                draw = true;
            } else if (mapDataFile.isEmpty()) {
                mapDataFile = args[i];
            } else if (travelCostsFile.isEmpty()) {
                travelCostsFile = args[i];
            } else if (objectivesFile.isEmpty()) {
                objectivesFile = args[i];
            }
        }

        try {
            ArrayList<ArrayList<Tile>> grid = new ArrayList<>();
            int cols;
            int rows;

            // read map data
            Scanner mapScanner = new Scanner(new File(mapDataFile));

            // read dimensions
            String[] dimensions = mapScanner.nextLine().split("\\s+");
            cols = Integer.parseInt(dimensions[0]);
            rows = Integer.parseInt(dimensions[1]);

            // initialize the grid with default grass tiles
            for (int i = 0; i < rows; i++) {
                ArrayList<Tile> row = new ArrayList<>();
                for (int j = 0; j < cols; j++) {
                    row.add(new Tile(j, i, 0)); // add as type 0
                }
                grid.add(row);
            }

            // read tile data from map data file
            while (mapScanner.hasNextLine()) {
                String line = mapScanner.nextLine();
                String[] parts = line.split("\\s+");

                int column = Integer.parseInt(parts[0]);
                int row = Integer.parseInt(parts[1]);
                int type = Integer.parseInt(parts[2]);

                grid.get(row).set(column, new Tile(column, row, type));
            }

            mapScanner.close();

            // set adjacent tiles
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    Tile currentTile = grid.get(i).get(j);

                    // add adjacent tiles (up, right, down, left)
                    if (i > 0) {
                        currentTile.addAdjacentTile(grid.get(i-1).get(j));
                    }
                    if (j < cols-1) {
                        currentTile.addAdjacentTile(grid.get(i).get(j+1));
                    }
                    if (i < rows-1) {
                        currentTile.addAdjacentTile(grid.get(i+1).get(j));
                    }
                    if (j > 0) {
                        currentTile.addAdjacentTile(grid.get(i).get(j-1));
                    }
                }
            }

            // initialize travel costs
            ArrayList<ArrayList<ArrayList<Double>>> travelCosts = new ArrayList<>();
            for (int i = 0; i < rows; i++) {
                ArrayList<ArrayList<Double>> rowCosts = new ArrayList<>();
                for (int j = 0; j < cols; j++) {
                    ArrayList<Double> cellCosts = new ArrayList<>();
                    for (int k = 0; k < 4; k++) {
                        cellCosts.add(Double.POSITIVE_INFINITY);
                    }
                    rowCosts.add(cellCosts);
                }
                travelCosts.add(rowCosts);
            }

            // read travel costs
            Scanner costScanner = new Scanner(new File(travelCostsFile));
            while (costScanner.hasNextLine()) {
                String line = costScanner.nextLine();
                String[] parts = line.split("\\s+");

                int x1 = Integer.parseInt(parts[0]);
                int y1 = Integer.parseInt(parts[1]);
                int x2 = Integer.parseInt(parts[2]);
                int y2 = Integer.parseInt(parts[3]);
                double cost = Double.parseDouble(parts[4]);

                // determine direction (0,1,2,3: up,right,down,left)
                int dir1 = -1; // ->
                int dir2 = -1; // <-

                if (x1 == x2) {
                    if (y1 == y2 - 1) {
                        dir1 = 2; dir2 = 0; // down, up
                    }
                    else if (y1 == y2 + 1) {
                        dir1 = 0; dir2 = 2; // up, down
                    }
                } else if (y1 == y2) {
                    if (x1 == x2 - 1) {
                        dir1 = 1; dir2 = 3; // right, left
                    }
                    else if (x1 == x2 + 1) {
                        dir1 = 3; dir2 = 1; // left, right
                    }
                }

                // apply both direction
                if (dir1 != -1) {
                    if (y1 < rows && x1 < cols) {
                        travelCosts.get(y1).get(x1).set(dir1, cost);
                    }
                    if (y2 < rows && x2 < cols) {
                        travelCosts.get(y2).get(x2).set(dir2, cost);
                    }
                }

            }
            costScanner.close();

            // read objectives
            ArrayList<int[]> objectives = new ArrayList<>();

            Scanner objScanner = new Scanner(new File(objectivesFile));

            while (objScanner.hasNextLine()) {
                String line = objScanner.nextLine();
                if (line.isEmpty()) continue;
                String[] parts = line.split("\\s+");

                int column = Integer.parseInt(parts[0]);
                int row = Integer.parseInt(parts[1]);
                int activationNum = (objectives.isEmpty()) ? 0 : 1;
                objectives.add(new int[]{column, row, activationNum});
            }

            objScanner.close();

            // create output directory if it doesn't exist
            File outputDir = new File("out");
            if (!outputDir.exists()) {
                outputDir.mkdirs();
            }

            // open output file
            PrintWriter output = new PrintWriter(new FileWriter("out/output.txt"));

            // initialize StdDraw if draw flag is called
            if (draw) {
                StdDraw.setCanvasSize(cols*40, rows*40);
                StdDraw.setXscale(0, cols*40);
                StdDraw.setYscale(0, rows*40);
                StdDraw.enableDoubleBuffering();

                // draw the grid
                drawGrid(grid, rows, cols);

                // draw all coins
                for (int[] objective : objectives) {
                    if (objective[2] == 1) {
                        drawCoin(objective[0], objective[1], cols, rows);
                    }
                }

                StdDraw.show();
            }

            // initialize path finder object
            PathFinder pathFinder = new PathFinder(grid, travelCosts, cols, rows);

            // initialize knight's position
            int[] currentPos = objectives.get(0);
            int currentCol = currentPos[0];
            int currentRow = currentPos[1];

            int totalSteps = 0;
            double totalCost = 0.0;

            // store all visited path
            ArrayList<Tile> allVisitedTiles = new ArrayList<>();

            // draw knight at starting position
            if (draw) {
                drawKnight(currentCol, currentRow, cols, rows);
                StdDraw.show();
                StdDraw.pause(550);

                // add starting position to visited path
                allVisitedTiles.add(grid.get(currentRow).get(currentCol));
            }

            // process each objective
            for (int i = 1; i < objectives.size(); i++) {
                int[] objective = objectives.get(i);
                int targetCol = objective[0];
                int targetRow = objective[1];

                // find path to objective
                ArrayList<Tile> path = pathFinder.findShortestPath(currentRow, currentCol, targetCol, targetRow);

                if (path == null || path.isEmpty()) {
                    output.println("Objective " + i + " cannot be reached!");
                    continue;
                }

                output.println("Starting position: (" + currentCol + ", " + currentRow + ")");

                // counters for step and cost
                int stepCount = 0;
                double objectiveCost = 0.0;

                // skip the first tile as it's starting point
                for (int j = 1; j < path.size(); j++) {
                    Tile prevTile = path.get(j - 1);
                    Tile currentTile = path.get(j);

                    int direction = -1;

                    // determine direction of movement
                    if (prevTile.getColumn() == currentTile.getColumn()) {
                        if (prevTile.getRow() == currentTile.getRow() - 1) direction = 2; // down
                        else if (prevTile.getRow() == currentTile.getRow() + 1) direction = 0; // up
                    } else if (prevTile.getRow() == currentTile.getRow()) {
                        if (prevTile.getColumn() == currentTile.getColumn() - 1) direction = 1; // right
                        else if (prevTile.getColumn() == currentTile.getColumn() + 1) direction = 3; // left
                    }

                    double stepCost = travelCosts.get(prevTile.getRow()).get(prevTile.getColumn()).get(direction);
                    objectiveCost += stepCost;
                    totalCost += stepCost;
                    stepCount++;

                    // update knight's position
                    if (draw) {
                        drawGrid(grid, rows, cols);

                        // draw coins
                        for (int[] obj : objectives) {
                            if (obj[2] == 1) { // if coin is activated
                                drawCoin(obj[0], obj[1], cols, rows);
                            }
                        }

                        // add previous tile to visited path and draw all visited path
                        allVisitedTiles.add(prevTile);
                        drawVisitedPath(allVisitedTiles, cols, rows, currentTile.getColumn(), currentTile.getRow());

                        // draw knight at new position
                        drawKnight(currentTile.getColumn(), currentTile.getRow(), cols, rows);
                        StdDraw.show();
                        StdDraw.pause(150);
                    }

                    output.println("Step Count: " + stepCount + ", move to (" + currentTile.getColumn() + ", "
                            + currentTile.getRow() + "). Total Cost: " + String.format("%.2f", objectiveCost) + ".");
                }

                totalSteps += stepCount;
                output.println("Objective " + i + " reached!");

                allVisitedTiles.clear(); // clear red dots

                // mark objective as collected (deactivated coin)
                objectives.get(i)[2] = 0;

                // update current position
                currentRow = targetRow;
                currentCol = targetCol;

                if (draw) {
                    // redraw everything at the end
                    drawGrid(grid, rows, cols);

                    // draw remaining coins
                    for (int[] obj : objectives) {
                        if (obj[2] == 1) {
                            drawCoin(obj[0], obj[1], cols, rows);
                        }
                    }

                    // draw visited path
                    drawVisitedPath(allVisitedTiles, cols, rows, currentCol, currentRow);

                    // draw knight at new position
                    drawKnight(currentCol, currentRow, cols, rows);
                    StdDraw.show();
                    StdDraw.pause(150);
                }

            }

            // write total steps and cost
            output.print("Total Step: " + totalSteps + ", Total Cost: " + String.format("%.2f", totalCost));
            output.close();


        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }

    }


    /**
     * Draws the entire grid.
     *
     * @param grid The 2D grid of tiles.
     * @param rows Number of rows.
     * @param cols Number of columns.
     */
    private static void drawGrid(ArrayList<ArrayList<Tile>> grid, int rows, int cols) {
        StdDraw.clear();

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                drawTile(grid.get(i).get(j), cols, rows);
            }
        }
    }

    /**
     * Draws a single tile.
     *
     * @param tile The tile to be drawn.
     * @param cols Total number of columns (for scaling).
     * @param rows Total number of rows (for scaling).
     */
    private static void drawTile(Tile tile, int cols, int rows) {

        int col = tile.getColumn();
        int row = tile.getRow();

        // draw based on tile type
        switch (tile.getType()) {
            case 0: // grass
                StdDraw.picture(col*40+20, rows*40-(row*40+20), "misc/grassTile.jpeg", 40, 40);
                break;
            case 1: // sand
                StdDraw.picture(col*40+20, rows*40-(row*40+20), "misc/sandTile.png", 40, 40);
                break;
            case 2: // obstacle
                StdDraw.picture(col*40+20, rows*40-(row*40+20), "misc/impassableTile.jpeg", 40, 40);
                break;
        }

    }

    /**
     * Draws the knight image.
     *
     * @param col Column index of the tile.
     * @param row Row index of the tile.
     * @param cols Total number of columns (for scaling).
     * @param rows Total number of rows (for scaling).
     */
    private static void drawKnight(int col, int row, int cols, int rows) {
        StdDraw.picture(col*40+20, rows*40-(row*40+20), "misc/knight.png", 38, 38);
    }

    /**
     * Draws a coin image.
     *
     * @param col Column index of the tile.
     * @param row Row index of the tile.
     * @param cols Total number of columns (for scaling).
     * @param rows Total number of rows (for scaling).
     */
    private static void drawCoin(int col, int row, int cols, int rows) {
        StdDraw.picture(col*40+20, rows*40-(row*40+20), "misc/coin.png", 25, 25);
    }


    /**
     * Draws red dots on visited tiles.
     *
     * @param visitedTiles List of visited tiles.
     * @param cols Total number of columns (for scaling).
     * @param rows Total number of rows (for scaling).
     * @param currentCol Column index of the current tile (knight's position).
     * @param currentRow Row index of the current tile (knight's position).
     */
    private static void drawVisitedPath(ArrayList<Tile> visitedTiles, int cols, int rows, int currentCol, int currentRow) {
        StdDraw.setPenColor(StdDraw.RED);
        for (Tile tile : visitedTiles) {
            if (tile.getColumn() != currentCol || tile.getRow() != currentRow) {
                StdDraw.filledCircle(
                        tile.getColumn() * 40 + 20,
                        rows * 40 - (tile.getRow() * 40 + 20),
                        4
                );
            }
        }
    }

}
