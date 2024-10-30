package se.test.neuralNetwork.src.visual.utils;

public class CollisionHandler {

    public static boolean detectCollision(int x, int y, int w, int h, int x2, int y2, int w2, int h2) {
        int[] convertedR1 = rectangleConverter(x, y, w, h);
        int[] convertedR2 = rectangleConverter(x2, y2, w2, h2);

        x = convertedR1[0];
        y = convertedR1[1];
        w = convertedR1[2];
        h = convertedR1[3];
        x2 = convertedR2[0];
        y2 = convertedR2[1];
        w2 = convertedR2[2];
        h2 = convertedR2[3];

        return (x + w > x2 && x < x2 + w2 && y + h > y2 && y < y2 + h2);
    }

    public static int[] rectangleConverter(int x, int y, int w, int h) {
        if (w < 0) {
            x += w;
            w = Math.abs(w);
        }
        if (h < 0) {
            y += h;
            h = Math.abs(h);
        }
        return new int[]{x, y, w, h};
    }
}
