package matchingaggregation;
import org.json.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;


/**
 * Created by Joost on 27/10/2017.
 */
public class ReadInput {

    public Grid loadJSON(String path, String filename) throws IOException, JSONException {

        int [][] classes;


        if (!path.endsWith("/") && !path.endsWith("\\")) {
            path += "/";
        }

        if (!filename.endsWith(".json")) {
            filename += ".json";
        }

        File file = new File(path + filename);
        List<String> lines = Files.readAllLines(file.toPath());

        JSONObject dotObject = new JSONObject(String.join("\n", lines));

        int height = dotObject.getInt("height");
        int width = dotObject.getInt("width");

        System.out.println("Height: " + height);
        System.out.println("Width: " + width);

        classes = new int[height+2][width];

        JSONArray json = dotObject.getJSONArray("points");
        JSONArray array;

        int x, y, c;
        for (int i = 0; i < json.length(); i++) {
            array = json.getJSONArray(i);

            x = array.getInt(0);
            y = array.getInt(1);
            c = array.getInt(2);

            classes[y][x] = c;
        }


//        System.out.println(grid.length);
        Grid g = new Grid(classes);



        return g;
    }





}
