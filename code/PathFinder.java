import java.util.ArrayList;

/**
 * The class provides functionality to compute the shortest path between
 * tiles on a grid using Dijkstra's algorithm, considering individual travel costs.
 */
public class PathFinder {

    // DATA FIELDS
    private static final int[] DX = {0, 1, 0, -1};
    private static final int[] DY = {-1, 0, 1, 0};
    private ArrayList<ArrayList<Tile>> grid;
    private ArrayList<ArrayList<ArrayList<Double>>> travelCosts;
    private int cols;
    private int rows;

    // CONSTRUCTOR
    /**
     * Constructs a PathFinder for a given grid with associated travel costs.
     *
     * @param grid         2D grid of {@code Tile} objects.
     * @param travelCosts  3D list containing movement costs in each direction for each tile.
     * @param cols         Number of columns in the grid.
     * @param rows         Number of rows in the grid.
     */
    public PathFinder(ArrayList<ArrayList<Tile>> grid, ArrayList<ArrayList<ArrayList<Double>>> travelCosts, int cols, int rows) {
        this.grid = grid;
        this.travelCosts = travelCosts;
        this.cols = cols;
        this.rows = rows;
    }


    // OTHER METHODS

    /**
     * Finds the shortest path from a source tile to a target tile using Dijkstra's algorithm.
     *
     * @param sourceRow  Row index of the source tile.
     * @param sourceCol  Column index of the source tile.
     * @param targetCol  Column index of the target tile.
     * @param targetRow  Row index of the target tile.
     * @return A list of {@code Tile} objects representing the path, or {@code null} if no path exists.
     */
    public ArrayList<Tile> findShortestPath(int sourceRow, int sourceCol, int targetCol, int targetRow) {
        // check if source or target is out of bounds or impassable
        if (!isValidTile(sourceRow, sourceCol) || !isValidTile(targetRow, targetCol)) {
            return null;
        }

        Tile sourceTile = grid.get(sourceRow).get(sourceCol);
        Tile targetTile = grid.get(targetRow).get(targetCol);

        // check if source or target tile is impassable
        if (!sourceTile.getIsPassable() || !targetTile.getIsPassable()) {
            return null;
        }

        // create distance and visited arrays
        double[][] distances = new double[rows][cols];
        boolean[][] visited = new boolean[rows][cols];
        Tile[][] previous = new Tile[rows][cols];

        // initialize distances with infinity
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                distances[i][j] = Double.POSITIVE_INFINITY;
                visited[i][j] = false;
                previous[i][j] = null;
            }
        }

        // distance to source is 0
        distances[sourceRow][sourceCol] = 0.0;

        // main Dijkstra loop
        for (int count = 0; count < rows * cols; count++) {
            // find the tile with minimum distance among unvisited tiles
            int minRow = -1;
            int minCol = -1;
            double minDist = Double.POSITIVE_INFINITY;

            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    if (!visited[i][j] && distances[i][j] < minDist) {
                        minDist = distances[i][j];
                        minRow = i;
                        minCol = j;
                    }
                }
            }

            // path doesn't exist
            if (minRow == -1) {
                break;
            }

            // mark as visited
            visited[minRow][minCol] = true;

            // if target is reached and return the path
            if (minRow == targetRow && minCol == targetCol) {
                return reconstructPath(previous, targetTile);
            }

            // check all adjacent tiles
            for (int i = 0; i < 4; i++) {
                int newRow = minRow + DY[i];
                int newCol = minCol + DX[i];

                // skip invalid tiles
                if (!isValidTile(newRow, newCol)) {
                    continue;
                }

                Tile adjacent = grid.get(newRow).get(newCol);

                // skip impassable tiles
                if (!adjacent.getIsPassable()) {
                    continue;
                }

                // skip visited tiles
                if (visited[newRow][newCol]) {
                    continue;
                }

                // get travel cost to adjacent tile
                double cost = travelCosts.get(minRow).get(minCol).get(i);

                // calculate tentative distance
                double tentativeDistance = distances[minRow][minCol] + cost;

                // if a better path is found, update distance
                if (tentativeDistance < distances[newRow][newCol]) {
                    distances[newRow][newCol] = tentativeDistance;
                    previous[newRow][newCol] = grid.get(minRow).get(minCol);
                }
            }
        }

        // no path found
        return null;
    }


    /**
     * Checks whether a tile at the given position is within grid bounds.
     *
     * @param row  Row index to check.
     * @param col  Column index to check.
     * @return {@code true} if the tile is within bounds, otherwise {@code false}.
     */
    private boolean isValidTile(int row, int col) {
        return row >= 0 && row < rows && col >= 0 && col < cols;
    }


    /**
     * Reconstructs the path from the source to the target using the recorded previous tiles.
     *
     * @param previous  2D array storing the previous tile for each grid position.
     * @param target    The target {@code Tile} to reconstruct the path to.
     * @return An {@code ArrayList} of tiles representing the shortest path.
     */
    private ArrayList<Tile> reconstructPath(Tile[][] previous, Tile target) {
        ArrayList<Tile> path = new ArrayList<>();

        // start from target to source
        int row = target.getRow();
        int col = target.getColumn();

        // add target to path
        path.add(0, grid.get(row).get(col));

        // traverse back to source
        Tile current = previous[row][col];
        while (current != null) {
            path.add(0, current);
            row = current.getRow();
            col = current.getColumn();
            current = previous[row][col];
        }

        return path;
    }

}
