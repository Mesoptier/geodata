package geographic;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import javax.imageio.ImageIO;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Loads CBS data
 * 
 * Dependency:
 * http://www.java2s.com/Code/Jar/j/Downloadjavajsonjar.htm
 *
 * @author Brouwer M.R.
 */
public class Data {
    
    /**
     * Loads CBS images into one image with 1x1px dots, given coordinates in the 
     * CBS dot map and a size. Images are loaded from zoom level 17 (max) using
     * http://research.cbs.nl/colordotmap/tiles_herkomst_light/17/{x}/{y}.png
     * and written to the given file in JSON format
     * 
     * @param x x-coordinate of first CBS image
     * @param y y-coordinate of first CBS image
     * @param size number of images to write from CBS in both x and y direction
     * @param file file to which JSON is stored
     * @throws IOException if an error occurred while writing JSON to file
     * @throws JSONException if an error occurred while converting image to JSON
     */
    public static void loadJSON(int x, int y, int size, File file) 
            throws IOException, JSONException{
        BufferedImage cbsImage = loadImage(x, y, size);
        JSONObject json = toJSON(cbsImage);
        
        try (FileWriter writer = new FileWriter(file)) {
            writer.append(json.toString());
        }
    }
    
    /**
     * Converts an image to JSON, only including points with a colour
     * 
     * @param image image to convert
     * @return JSONObject with width, height and points ([x, y, c]) 
     * @throws JSONException if an error occurred while created JSON
     */
    private static JSONObject toJSON(BufferedImage image) throws JSONException {
        JSONObject json = new JSONObject();
        json.put("width", image.getWidth());
        json.put("height", image.getHeight());
        
        JSONArray points = new JSONArray();
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                switch (image.getRGB(x, y)) {
                    case -34146: // roze
                        points.put(new JSONArray("[" + x + "," + y + "," + 1 + "]"));
                        break;
                    case -10894848: // groen
                        points.put(new JSONArray("[" + x + "," + y + "," + 2 + "]"));
                        break;
                    case -16730113: // blauw
                        points.put(new JSONArray("[" + x + "," + y + "," + 3 + "]"));
                        break;
                }
            }
        }
        
        json.put("points", points);
        return json;
    }
    
    /**
     * Loads CBS images into one image with 1x1px dots, given coordinates in the 
     * CBS dot map and a size. Images are loaded from zoom level 17 (max) using
     * http://research.cbs.nl/colordotmap/tiles_herkomst_light/17/{x}/{y}.png
     * 
     * @param x x-coordinate of first CBS image
     * @param y y-coordinate of first CBS image
     * @param size number of images to write from CBS in both x and y direction
     * @return image with 1x1px dots with (32*size)x(32*size) pixels in total
     */
    private static BufferedImage loadImage(int x, int y, int size) {
        BufferedImage result = new BufferedImage(256/8 * size, 256/8 * size, BufferedImage.TYPE_INT_ARGB);
        
        BufferedImage img, tmp;
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                
                try {
                    img = ImageIO.read(new URL("http://research.cbs.nl/colordotmap/tiles_herkomst_light/17/" + (x + i) + "/" + (y + j) + ".png"));
                    tmp = new BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB);
                    tmp.getGraphics().drawImage(img, 0, 0, null);
                    
                    img = new BufferedImage(256 / 8, 256 / 8, BufferedImage.TYPE_INT_ARGB);

                    Graphics2D g = img.createGraphics();
                    g.drawImage(tmp, 0, 0, img.getWidth(), img.getHeight(), null);
                    g.dispose();

                } catch (IOException e) {
                    img = new BufferedImage(256 / 8, 256 / 8, BufferedImage.TYPE_INT_ARGB);
                }
                
                result.setRGB(i * 256/8, j * 256/8, 256/8, 256/8, ((DataBufferInt) img.getRaster().getDataBuffer()).getData(), 0, 256/8);
            }
        }
        
        return result;
    }
    
}
