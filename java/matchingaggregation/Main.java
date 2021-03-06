package matchingaggregation;

import org.json.JSONException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Joost on 27/10/2017.
 */
public class Main {
    public static void main(String[] args) throws IOException{
        ReadInput inputReader = new ReadInput();

        Grid testGrid = getRandomGrid(256, 256, 0.5, 3);

        Grid grid = testGrid;
        try {
            grid = inputReader.loadJSON("C:\\Users\\Martijn\\Documents\\GeoData\\data\\", "eindhoven");
        } catch (JSONException e) {
            e.printStackTrace();
        }


        BipartiteMatchingBottomUp matching = new BipartiteMatchingBottomUp();
//        BipartiteMatchingTopDown matching = new BipartiteMatchingTopDown();

        long startTime = System.nanoTime();
        Grid aggregratedGrid = matching.performAggregation(grid);
        long timeTaken = System.nanoTime()-startTime;

        // Mapping can be called via:
//        HashMap<Node,  ArrayList<Node>> finalMapping = matching.getFinalMapping();



        System.out.println();
        System.out.println("Woo! Aggregation");
        aggregratedGrid.printGrid(false);
        System.out.println();
        System.out.println("Total time taken = " + timeTaken/1000000 + " ms");
        System.out.println("Number of new nodes: " + aggregratedGrid.getNumberOfValues());
        System.out.println("Final Score (SE): " + matching.getFinalScore());
        System.out.println("Final Score (MSE): " + matching.getFinalScore() / aggregratedGrid.getNumberOfValues());
        System.out.println("Final Score (RMSE): " +
                Math.sqrt(matching.getFinalScore() / aggregratedGrid.getNumberOfValues()));
                
        File file = new File("C:\\Users\\Martijn\\Documents\\GeoData\\data\\" + "eindhovenAggregated1.json");
        if (file.exists()) {
            file.delete();
        }
        file.createNewFile();
        try {
            WriteOutput.writeJSON(file, aggregratedGrid);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }


    public static Grid getRandomGrid(int height, int width, double densityClasses, int nrOfClasses) {
        int [][] grid = new int[height][width];

        for (int i = 0; i < grid.length; i++) {
            for (int j = 0; j < grid[0].length; j++) {
                grid[i][j] = (Math.random() < densityClasses) ? (int) Math.ceil(Math.random() * nrOfClasses) : 0;
            }
        }

        Grid myGrid = new Grid(grid);

        return myGrid;

    }
}
