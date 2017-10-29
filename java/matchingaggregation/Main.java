package matchingaggregation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Joost on 27/10/2017.
 */
public class Main {
    public static void main(String[] args) throws IOException{
        ReadInput inputReader = new ReadInput();
//        String[] testGrid = new String[] {
//                "1   333333333333            ",
//                " 1 333333 33 333 33         ",
//                "   3332332 133323   3  33 22",
//                "2    333 33 2 33 33    3   1",
//                "222     333     333   3 2 11",
//                "22222        3   3   3   111",
//                "222      1   1     1    3  1"
//        };
//        Grid grid = new Grid(testGrid);

        Grid grid = getRandomGrid(256, 256, 0.5, 3);
//        Grid grid = new Grid(new String[] {
//                "1                       1",
//                " 2                     2 ",
//                "  3                   3  ",
//                "                         ",
//                "                         ",
//                "                         ",
//                "                         ",
//                "  3                   3  ",
//                " 2                     2 ",
//                "1                       1",
//        });


//        Grid g = inputReader.loadJSON("C:\\Users\\Joost\\IdeaProjects\\GeoData\\src\\", "eindhoven");

        grid.printGrid(false);





        BipartiteMatchingBottomUp matching = new BipartiteMatchingBottomUp();
//        BipartiteMatchingTopDown matching = new BipartiteMatchingTopDown();

        long startTime = System.nanoTime();
        Grid aggregratedGrid = matching.performAggregation(grid);
        long timeTaken = System.nanoTime()-startTime;

        // Mapping can be called via:
        HashMap<Node,  ArrayList<Node>> finalMapping = matching.getFinalMapping();



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
