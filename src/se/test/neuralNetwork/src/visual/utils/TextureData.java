package se.test.neuralNetwork.src.visual.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Hashtable;
import java.util.Map;

public class TextureData {
    private BufferedImage image;

    private final Map<String, ImageData> imageData = new Hashtable<>();

    private BufferedImage loadImage(String imagePath) {
        try {
            InputStream imageStream = getClass().getResourceAsStream(imagePath);
            if (imageStream == null) {
                throw new IOException("Resource not found: " + imagePath);
            }
            return ImageIO.read(imageStream);
        } catch (IOException e) {
            System.err.println("Error loading image: " + e.getMessage());
            return null;
        }
    }

    public void loadImageData(String path) {
        String imagePath = path + "/texture.png";
        String imageDataPath = path + "/texture.json";

        image = loadImage(imagePath);

        InputStream jsonStream = getClass().getResourceAsStream(imageDataPath);
        if (jsonStream == null) {
            throw new RuntimeException("Error reading JSON file: " + imageDataPath);
        }

        JsonArray imageDataArray = convert(jsonStream);

        for (int i = 0; i < imageDataArray.size(); i++) {
            ImageData data = new ImageData(imageDataArray.get(i).getAsJsonObject());
            imageData.put(data.getName(), data);
        }
    }

    public JsonArray convert(InputStream inputStream) {
        StringBuilder jsonString = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                jsonString.append(line);
            }
        } catch (Exception e) {
            System.err.println("Kunde inte konvertera Inputstream till JsonArray");
        }

        return JsonParser.parseString(jsonString.toString()).getAsJsonObject().getAsJsonArray("frames");
    }

    public BufferedImage getImage() {
        return image;
    }

    public ImageData getImageData(String key) {
        return imageData.get(key);
    }
}
