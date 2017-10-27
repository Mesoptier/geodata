package geographic;

import java.awt.Point;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Quality Measures
 * 
 * Dependency:
 * http://www.java2s.com/Code/Jar/j/Downloadjavajsonjar.htm
 *
 * @author Brouwer M.R.
 */
public class Measures {
    
    /**
     * Loads JSON dot map into an easy to measure format
     * 
     * @param path directory of JSON file
     * @param filename filename of JSON file
     * @return Mapping of colours to associated points
     * @throws IOException if an error occurred while reading the JSON file
     * @throws JSONException if an error occurred while parsing JSON
     */
    public static Map<Integer, List<Point>> loadJSON(String path, String filename)
            throws IOException, JSONException {
        if (!path.endsWith("/") && !path.endsWith("\\")) {
            path += "/";
        }
        
        if (!filename.endsWith(".json")) {
            filename += ".json";
        }
        
        Map<Integer, List<Point>> data = new HashMap<>();
        
        File file = new File(path + filename);
        List<String> lines = Files.readAllLines(file.toPath());
        
        JSONArray json = (new JSONObject(String.join("\n", lines))).getJSONArray("points");
        JSONArray array;
        int x, y, c;
        for (int i = 0; i < json.length(); i++) {
            array = json.getJSONArray(i);
            
            x = array.getInt(0);
            y = array.getInt(1);
            c = array.getInt(2);
            
            if (!data.containsKey(c)) {
                data.put(c, new ArrayList());
            }
            
            data.get(c).add(new Point(x, y));
            
        }

        return data;
    }
    
    /**
     * Measures the distribution of oolours between original data and aggregated
     * data
     * 
     * @param original original data
     * @param aggregated aggregated data
     * @return distribution of colours
     */
    public static double measureDistribution(Map<Integer, List<Point>> original,
            Map<Integer, List<Point>> aggregated) {
        int n = original.size();
        int m = aggregated.size();
        
        Set<Integer> colours = original.keySet();
        
        double sum = 0;
        for (Integer colour : colours) {
            sum += Math.abs((original.get(colour).size() / (double) n) 
                    - (aggregated.getOrDefault(colour, new ArrayList()).size() / (double) m));
        }
        
        return sum / colours.size();
    }
    
    
}
