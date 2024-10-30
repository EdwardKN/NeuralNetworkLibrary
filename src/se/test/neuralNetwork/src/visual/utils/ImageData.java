package se.test.neuralNetwork.src.visual.utils;

import com.google.gson.JsonObject;

public class ImageData {
    private final String name;

    private final int x;
    private final int y;
    private final int w;
    private final int h;

    public ImageData(JsonObject data) {
        String filePath = data.get("filename").toString();
        int separatorIndex = filePath.lastIndexOf('/');
        int extensionIndex = filePath.lastIndexOf('.');

        name = filePath.substring(separatorIndex + 1, extensionIndex);

        x = data.get("frame").getAsJsonObject().get("x").getAsInt();
        y = data.get("frame").getAsJsonObject().get("y").getAsInt();
        w = data.get("frame").getAsJsonObject().get("w").getAsInt();
        h = data.get("frame").getAsJsonObject().get("h").getAsInt();
    }

    public String getName() {
        return name;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getW() {
        return w;
    }

    public int getH() {
        return h;
    }
}
