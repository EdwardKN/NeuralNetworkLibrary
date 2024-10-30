package se.test.neuralNetwork.src.visual.utils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.FileReader;
import java.io.IOException;

public class JSONUtil {
    public JsonObject readJson(String jsonFilePath) {
        try {
            FileReader reader = new FileReader(jsonFilePath);

            Gson gson = new Gson();

            JsonObject jsonObject = gson.fromJson(reader, JsonObject.class);

            reader.close();

            return jsonObject;
        } catch (IOException e) {
            System.err.println("Error reading JSON file: " + e.getMessage());
            return null;
        }
    }
}
