import java.util.ArrayList;

/**
 * Represents a single tile on the grid.
 * Each tile has a position (column, row), a type (e.g., grass, sand, obstacle),
 * a passability feature, and a list of adjacent neighbor tiles.
 */
public class Tile {

    // DATA FIELDS
    private int column;
    private int row;
    private int type;
    private ArrayList<Tile> adjacentTiles; // store neighbor tiles
    private boolean isPassable; // determine is it obstacle

    // CONSTRUCTOR
    /**
     * Constructs a new Tile object with specified position and type.
     * Automatically sets the passability tentatively 2.
     *
     * @param column Column index of the tile.
     * @param row Row index of the tile.
     * @param type Type of the tile (0: grass, 1: sand, 2: obstacle).
     */
    public Tile(int column, int row, int type){
        this.column = column;
        this.row = row;
        this.type = type;
        this.adjacentTiles = new ArrayList<>();
        if (type == 2){ // is it obstacle?
            this.isPassable = false;
        } else {
            this.isPassable = true;
        }
    }

    // GETTERS
    /**
     * Gets the column index of the tile.
     *
     * @return Column index.
     */
    public int getColumn() {return column;}
    /**
     * Gets the row index of the tile.
     *
     * @return Row index.
     */
    public int getRow() {return row;}
    /**
     * Gets the type of the tile.
     *
     * @return Type of the tile (0, 1, or 2).
     */
    public int getType() {return type;}
    /**
     * Checks whether the tile is passable (not an obstacle).
     *
     * @return true if passable, false if an obstacle.
     */
    public boolean getIsPassable() {return isPassable;}


    // OTHER METHODS

    /**
     * Adds a neighboring tile to the adjacent list if it's not already added.
     *
     * @param tile The tile to be added as a neighbor.
     */
    public void addAdjacentTile(Tile tile) {
        if (!adjacentTiles.contains(tile)) {
            adjacentTiles.add(tile);
        }
    }

}
