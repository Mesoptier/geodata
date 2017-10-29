package geographic;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author Brouwer M.R.
 */
public class Algorithm2 {
    
    private static Map<Point, List<Point>> mapping;
    
    /**
     * Algorithm 2
     * Idea: We place a grid over the input, for each grid-square we approximate an optimal solution.
     * 
     * 1.Place a grid style overlay over the map to divide the problem in small sets
     * 2.For each overlay-cell, find all points in this square.
     * 3.Calculate the number of points we can place for each class.
     *  • Divide the number of points for each class over k.
     * 4.Determine where we can place the points.
     *  • Greedily place the points at their optimal place for each colour in order
     *  • If we have overlap then place in the nearest available place
     */
    
    public static void main(String[] args) throws Exception {
        String filename = "denhaag";
        String path = "D:\\marcbrouwer\\Documents\\NetBeansProjects\\Geographic\\data\\";
        
        Map<Integer, List<Point>> data = loadJSON(path, filename);
        BufferedImage image = imageFromJSON(data, 1024, 1024);
        
        int scale = 1;
        Panel p = new Panel(scaleImage(setColours(image), scale));
        
        int zoomLevel = 4;
        int gridSize = 2;
        
        mapping = new HashMap<>();
        
        ImageMeasure imageMeasure = compute(image, zoomLevel, 2 * gridSize);
        System.out.println("qDist: " + imageMeasure.getQDist());
        System.out.println("qLoc: " + imageMeasure.getQLoc());

        Panel p2 = new Panel(scaleImage(setColours(imageMeasure.getImage()), scale *  2 * zoomLevel));
    }
    
    public static Map<Point, List<Point>> runAlg2(String path, String filename,
            int width, int height, int zoomLevel, File outputFile) 
            throws JSONException, IOException {
        Map<Integer, List<Point>> data = loadJSON(path, filename);
        BufferedImage image = imageFromJSON(data, width, height);
        mapping = new HashMap<>();
        
        ImageMeasure imageMeasure = compute(image, zoomLevel, 2 * 2);
        saveAsJSON(imageMeasure, outputFile);
        
        return mapping;
    }
    
    private static void saveAsJSON(ImageMeasure imageMeasure, File file) 
            throws JSONException, IOException {
        JSONObject json = toJSON(imageMeasure.getImage());
        json.put("qDist", imageMeasure.getQDist());
        json.put("qLoc", imageMeasure.getQLoc());
        
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
    public static JSONObject toJSON(BufferedImage image) throws JSONException {
        JSONObject json = new JSONObject();
        json.put("width", image.getWidth());
        json.put("height", image.getHeight());
        
        JSONArray points = new JSONArray();
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                switch (image.getRGB(x, y)) {
                    case 0:
                        break;
                    default:
                        points.put(new JSONArray(
                                "[" + x + "," + y + "," + image.getRGB(x, y) + "]"));
                        break;
                }
            }
        }
        
        json.put("points", points);
        return json;
    }
    
    private static BufferedImage imageFromJSON(Map<Integer, List<Point>> json, int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        
        json.forEach((c, ps) -> {
            ps.forEach(p -> {
                image.setRGB(p.x, p.y, c);
            });
        });

        return image;
    }
    
    private static BufferedImage scaleImage(BufferedImage input, double scale) {
        BufferedImage scaled = new BufferedImage((int) (input.getWidth() * scale), 
                (int) (input.getHeight() * scale), input.getType());
        
        Graphics2D g2 = scaled.createGraphics();
        g2.drawImage(input, 0, 0, scaled.getWidth(), scaled.getHeight(), null);
        
        return scaled;
    }
    
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
    
    private static BufferedImage setColours(BufferedImage input) {
        BufferedImage image = new BufferedImage(input.getWidth(), input.getHeight(), 
                input.getType());
        
        for (int x = 0; x < input.getWidth(); x++) {
            for (int y = 0; y < input.getHeight(); y++) {
                switch (input.getRGB(x, y)) {
                    case 1: // roze
                        image.setRGB(x, y, -34146);
                        break;
                    case 2: // groen
                        image.setRGB(x, y, -10894848);
                        break;
                    case 3: // blauw
                        image.setRGB(x, y, -16730113);
                        break;
                    default:
                        image.setRGB(x, y, 0);
                        break;
                }
            }
        }

        return image;
    }
    
    private static ImageMeasure compute(BufferedImage input, int level, int gridSize) {
        BufferedImage output = new BufferedImage(input.getWidth() / (2 * level), 
                input.getHeight() / (2 * level), input.getType());
        
        for (int x = 0; x < output.getWidth(); x++) {
            for (int y = 0; y < output.getHeight(); y++) {
                output.setRGB(x, y, -1);
            }
        }
        
        List<Double> qDists = new ArrayList<>();
        List<Double> qLocs = new ArrayList<>();
        
        ImageMeasure gridMeasure;
        BufferedImage subImage, gridImage;
        for (int x = 0; x < output.getWidth(); x += gridSize) {
            for (int y = 0; y < output.getHeight(); y += gridSize) {
                subImage = input.getSubimage(
                        x * 2 * level,
                        y * 2 * level,
                        gridSize * 2 * level,
                        gridSize * 2 * level);
                gridImage = output.getSubimage(x, y, gridSize, gridSize);

                gridMeasure = computeGrid(subImage, gridImage, level, gridSize, x, y);
                qDists.add(gridMeasure.getQDist());
                qLocs.add(gridMeasure.getQLoc());

                gridMeasure.getImage().createGraphics().drawImage(output, x, y, null);
            }
        }

        return new ImageMeasure(
                output, 
                qDists.stream().mapToDouble(v -> v).average().getAsDouble(), 
                qLocs.stream().mapToDouble(v -> v).average().getAsDouble()
        );
    }
    
    private static ImageMeasure computeGrid(BufferedImage subImage, 
            BufferedImage gridImage, int level, int gridSize, int xOffset, int yOffset) {
        Map<Integer, Integer> colours = new HashMap<>();
        
        Map<Integer, Integer>[][] colourMapping = new Map[gridImage.getWidth()][gridImage.getHeight()];
        
        int colour;
        for (int x = 0; x < gridImage.getWidth(); x++) {
            for (int y = 0; y < gridImage.getHeight(); y++) {
                colourMapping[x][y] = new HashMap<>();
                
                for (int xi = 0; xi < 2 * level; xi++) {
                    for (int yi = 0; yi < 2 * level; yi++) {
                        colour = subImage.getRGB(x + xi, y + yi);
                        colours.putIfAbsent(colour, 0);
                        colours.put(colour, colours.get(colour) + 1);
                        
                        colourMapping[x][y].putIfAbsent(colour, 0);
                        colourMapping[x][y].put(colour, colourMapping[x][y].get(colour) + 1);
                    }
                }
            }
        }
        
        if (colours.size() == 1) {
            colour = colours.keySet().stream().findAny().get();
            
            for (int x = 0; x < gridImage.getWidth(); x++) {
                for (int y = 0; y < gridImage.getHeight(); y++) {
                    gridImage.setRGB(x, y, colour);
                }
            }
            
            return new ImageMeasure(gridImage, 0, 0);
        }
        
        int fullMergeCount = 2 * level * 2 * level;
        List<Map.Entry<Integer, Integer>> orderedColours = new ArrayList<>();
        colours.entrySet().stream()//.filter(c -> c.getKey() != 0)
                .forEach(c -> orderedColours.add(
                        new AbstractMap.SimpleEntry<>(c.getKey(), c.getValue())));
        int subtract = orderedColours.stream().mapToInt(i -> i.getValue()).sum() / (gridSize * gridSize);
        
        double qLoc = 0, qLocTemp;
        
        int count, maxDif, maxX, maxY, dif;
        while (!orderedColours.isEmpty()) {
            Collections.sort(orderedColours, (a, b) -> Integer.compare(b.getValue(), a.getValue()));
            
            colour = orderedColours.get(0).getKey();
            count = orderedColours.get(0).getValue();
            maxDif = Integer.MIN_VALUE;
            maxX = -1;
            maxY = -1;
            qLocTemp = 0;
           
            outer:
            for (int x = 0; x < gridImage.getWidth(); x++) {
                for (int y = 0; y < gridImage.getHeight(); y++) {
                    dif = colourMapping[x][y].getOrDefault(colour, Integer.MIN_VALUE);

                    if (dif > maxDif && gridImage.getRGB(x, y) == -1) {
                        maxX = x;
                        maxY = y;
                        maxDif = dif;

                        if (maxDif >= subtract) {
                            qLocTemp = 0;
                        }
                    }
                }
            }
            
            if (maxDif == Integer.MIN_VALUE) {
                break;
            }
            
            orderedColours.get(0).setValue(orderedColours.get(0).getValue() 
                    - (subtract ));
            colourMapping[maxX][maxY].put(colour, 
                    colourMapping[maxX][maxY].getOrDefault(colour, 
                            (subtract )) 
                            - (subtract ));
            
            Point pA = new Point(maxX + xOffset, maxY + yOffset);
            mapping.put(pA, new ArrayList<>());
            
            if (maxDif < subtract) {
                dif = subtract - maxDif;
                int tempDif;
                int e = 1;
                
                int d0 = maxDif;
                for (int xO = 0; xO < 2 * level && d0 > 0; xO++) {
                    for (int yO = 0; yO < 2 * level && d0 > 0; yO++) {
                        mapping.get(pA).add(new Point(xOffset + maxX + xO, yOffset + maxY + yO));
                        d0--;
                    }
                }
                
                while (dif > 0 && e <= gridSize) {
                    for (int ex = -e; ex <= e && dif > 0; ex += 2 * e) {
                        if (maxX + ex < 0 || gridImage.getWidth() <= maxX + ex ) {
                            continue;
                        }
                        
                        for (int ey = -e; ey <= e && dif > 0; ey += 2 * e) {
                            if (maxY + ey < 0 || gridImage.getHeight() <= maxY + ey) {
                                continue;
                            }

                            if (colourMapping[maxX + ex][maxY + ey].getOrDefault(colour, 0) > 0) {
                                if (colourMapping[maxX + ex][maxY + ey].get(colour) <= dif) {
                                    qLocTemp += colourMapping[maxX + ex][maxY + ey].get(colour) *
                                            Math.sqrt((ex * 2 * level)
                                            * (ex * 2 * level)
                                            + (ey * 2 * level)
                                            * (ey * 2 * level));
                                    tempDif = colourMapping[maxX + ex][maxY + ey].get(colour);
                                } else {
                                    qLocTemp += dif *
                                            Math.sqrt((ex * 2 * level)
                                            * (ex * 2 * level)
                                            + (ey * 2 * level)
                                            * (ey * 2 * level));
                                    tempDif = dif;
                                }
                                
                                d0 = tempDif;
                                for (int xO = 0; xO < 2 * level && d0 > 0; xO++) {
                                    for (int yO = 0; yO < 2 * level && d0 > 0; yO++) {
                                        mapping.get(pA).add(new Point(
                                                xOffset + maxX + ex + xO, 
                                                yOffset + maxY + ey + yO));
                                        d0--;
                                    }
                                }
                                
                                colourMapping[maxX + ex][maxY + ey].put(colour, 
                                        colourMapping[maxX + ex][maxY + ey].get(colour) - dif);
                                if (colourMapping[maxX + ex][maxY + ey].get(colour) <= 0) {
                                    colourMapping[maxX + ex][maxY + ey].remove(colour);
                                }
                                
                                dif -= tempDif;
                            }
                        }
                    }
                    e++;
                }
            } else {
                for (int xO = 0; xO < 2 * level; xO++) {
                    for (int yO = 0; yO < 2 * level; yO++) {
                        mapping.get(pA).add(new Point(xOffset + maxX + xO, yOffset + maxY + yO));
                    }
                }
            }

            if (orderedColours.get(0).getValue() <= 0) {
                orderedColours.remove(0);
            }

            if (colourMapping[maxX][maxY].get(colour) <= 0) {
                colourMapping[maxX][maxY].remove(colour);
            }
            
            gridImage.setRGB(maxX, maxY, colour);
            
            qLoc += qLocTemp;
        }
        
        Map<Integer, Integer> gridColours = new HashMap<>();
        for (int x = 0; x < gridImage.getWidth(); x++) {
            for (int y = 0; y < gridImage.getHeight(); y++) {
                colour = subImage.getRGB(x, y);
                gridColours.putIfAbsent(colour, 0);
                gridColours.put(colour, gridColours.get(colour) + 1);
            }
        }
        
        int n = colours.values().stream().mapToInt(i -> i).sum();
        int m = gridColours.values().stream().mapToInt(i -> i).sum();
        
        double sum = 0;
        for (Integer c : colours.keySet()) {
            if (gridColours.getOrDefault(c, 0) == 0) {
                sum += (colours.get(c) / (double) n);
            } else {
                sum += Math.abs((colours.get(c) / (double) n) 
                        - (gridColours.getOrDefault(c, 0) / (double) m));
            }
        }
        
        double qDist = sum / colours.keySet().size();
        
        return new ImageMeasure(gridImage, qDist, qLoc);
    }
    
    private static class ImageMeasure {
        
        private final BufferedImage image;
        private final double qDist;
        private final double qLoc;

        public ImageMeasure(BufferedImage image, double qDist, double qLoc) {
            this.image = image;
            this.qDist = qDist;
            this.qLoc = qLoc;
        }
        
        public BufferedImage getImage() {
            return image;
        }
        
        public double getQDist() {
            return qDist;
        }
        
        public double getQLoc() {
            return qLoc;
        }
        
    }
    
}
