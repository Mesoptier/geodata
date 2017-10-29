package matchingaggregation;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by MSchalken on 29/10/2017.
 */
public class WriteOutput {

    public static void writeJSON(File file, Grid g) throws IOException, JSONException {
        JSONObject json = toJSON(g);

        try (FileWriter writer = new FileWriter(file)) {
            writer.append(json.toString());
        }

    }

    private static JSONObject toJSON(Grid g) throws JSONException {
        JSONObject json = new JSONObject();
        json.put("width", g.getWidth());
        json.put("height", g.getHeight());

        JSONArray points = new JSONArray();
        for (int x = 0; x < g.getWidth(); x++) {
            for (int y = 0; y < g.getHeight(); y++) {
                if (g.getXY(x,y) > 0) {
                    points.put(new JSONArray("[" + x + "," + "y" + "," + g.getXY(x,y) + "]"));
                }
            }
        }
        json.put("points", points);
        return json;
    }

}
