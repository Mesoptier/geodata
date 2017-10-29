package matchingaggregation;

import java.util.HashMap;

/**
 * Created by Joost on 27/10/2017.
 */
public class Grid {
    int [][] grid;
    int height, width;
    int nrOfClasses = -1;
    int nrOfPointsToAggregate;

    Grid(int [][] classesGrid)
    {
        this.grid = classesGrid;
        this.height = classesGrid.length;
        this.width = classesGrid[0].length;
        calculateNumberOfClasses();
    }

    Grid(String[] stringGrid) {
        this.height = stringGrid.length;
        this.width = stringGrid[0].toCharArray().length;
        grid = new int[height][width];
        for (int y = 0; y < height; y ++) {
            for (int x = 0; x < width; x ++) {
                char nextChar = stringGrid[y].toCharArray()[x];
                int dotClass = (nextChar == ' ') ? 0 : Character.getNumericValue(nextChar);
                grid[y][x] = dotClass;
            }
        }
        calculateNumberOfClasses();
    }

    Grid(int height, int width) {
        this.grid = new int[height][width];
        this.height = height;
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    @Override
    public Grid clone() {

        return new Grid(grid);
    }

    protected void calculateNumberOfClasses () {
        HashMap<Integer, Integer> classCount = new HashMap<>();

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int c = grid[i][j];
                if (c != 0) {
                    int nrCount = classCount.getOrDefault(c, -1);
                    if (nrCount == -1) {
                        classCount.put(c, 1);
                    }
                    else {
                        classCount.put(c, nrCount + 1);
                    }
                }

            }
        }

        nrOfClasses = classCount.keySet().size();

        for (int classType : classCount.keySet()) {
            nrOfPointsToAggregate += classCount.get(classType) / 4;
        }

    }

    public Grid padOddDimensions() {
        // Extra padding in case of odd dimensions.
        int heightPadding = height % 2;
        int widthPadding = width % 2;

        int [][] paddedClasses = new int[height + heightPadding][width + widthPadding];

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                paddedClasses[i][j] = grid[i][j];
            }
        }

        return new Grid(paddedClasses);

    }

    public void printGrid(boolean showEmptyClass) {
        for (int [] rowClasses : grid) {
            for (int classValue : rowClasses) {
                if (! showEmptyClass && classValue == 0) {
                    System.out.print(" ");
                }
                else {
                    System.out.print(classValue+ "");
                }
            }
            System.out.println();
        }
    }

    public int getXY(int x, int y) {
        return grid[y][x];
    }

    public int getYX(int y, int x) {
        return grid[y][x];
    }

    public void setXY(int x, int y, int c) {
        grid[y][x] = c;
    }

    public void setYX(int y, int x, int c) {
        grid[y][x] = c;
    }

    public int getNumberOfValues() {
        int count = 0;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                count += (grid[i][j] == 0) ? 0 : 1;
            }
        }
        return count;
    }

    public int getNumberOfClasses() {
        if (nrOfClasses == -1) {
            calculateNumberOfClasses();
        }
        return nrOfClasses;
    }

    public int getNumberOfTimesToAggregate() {
        if (nrOfClasses == -1) {
            calculateNumberOfClasses();
        }
        return nrOfPointsToAggregate;
    }
}
