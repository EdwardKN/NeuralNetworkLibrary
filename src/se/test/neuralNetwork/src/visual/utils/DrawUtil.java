package se.test.neuralNetwork.src.visual.utils;

import se.test.neuralNetwork.src.visual.Content;
import se.test.neuralNetwork.src.visual.Window;

import javax.swing.*;
import java.awt.*;

public class DrawUtil {
    private TextureData textureData;
    private Graphics2D g;
    private se.test.neuralNetwork.src.visual.Window window;
    private final FontLoader fontLoader = new FontLoader();
    private int width;
    private int height;

    public DrawUtil() {
        fontLoader.loadFont("verdanai.ttf");
        fontLoader.loadFont("verdanaz.ttf");
    }

    public DrawUtil(TextureData textureData, se.test.neuralNetwork.src.visual.Window window, Content game) {
        this.textureData = textureData;

        this.window = window;

        g = window.getGraphics2D();

        width = window.getPanelWidth();
        height = window.getPanelHeight();

        fontLoader.loadFont("verdanai.ttf");
        fontLoader.loadFont("verdanaz.ttf");
    }

    public void drawImage(String key, int x, int y, int i, int max) {
        drawImage(g, key, x, y, i, max);
    }

    public void drawImage(Graphics2D g, String key, int x, int y, int i, int max) {

        ImageData imageData = textureData.getImageData(key);

        int w = imageData.getW() / max;

        int startX = (w * Math.min(i, max - 1));

        g.drawImage(textureData.getImage(), x, y, x + w, y + imageData.getH(), imageData.getX() + startX, imageData.getY(), imageData.getX() + startX + w, imageData.getY() + imageData.getH(), null);
    }

    public void drawText(Graphics2D graphics, String text, int x, int y, Color color, int fontSize, int font, String align) {
        Font fontObj = new Font(font == 0 ? "Verdana Italic" : "Verdana Bold Italic", Font.PLAIN, fontSize);
        FontMetrics fm = graphics.getFontMetrics(fontObj);
        int textWidth = fm.stringWidth(text);

        switch (align.toLowerCase()) {
            case "left":
                break;
            case "right":
                x -= textWidth;
                break;
            case "center":
                x -= textWidth / 2;
                break;
            default:
                throw new IllegalArgumentException("Invalid alignment: " + align);
        }

        graphics.setFont(fontObj);
        graphics.setColor(Color.gray);
        graphics.drawString(text, x, y);
        graphics.setColor(color);
        graphics.drawString(text, x - 1, y - 1);
    }

    public void drawText(String text, int x, int y, Color color, int fontSize, int font) {
        drawText(g, text, x, y, color, fontSize, font);
    }

    public void drawText(String text, int x, int y, Color color, int fontSize, int font, String align) {
        drawText(g, text, x, y, color, fontSize, font, align);
    }

    public void drawText(Graphics2D graphics, String text, int x, int y, Color color, int fontSize, int font) {
        graphics.setFont(new Font(font == 0 ? "Verdana Italic" : "Verdana Bold Italic", Font.PLAIN, fontSize));
        graphics.setColor(Color.gray);
        graphics.drawString(text, x, y);
        graphics.setColor(color);
        graphics.drawString(text, x - 1, y - 1);

        drawText(graphics, text, x, y, color, fontSize, font, "left");
    }

    public void drawRectangle(int x, int y, int w, int h, Color color) {
        g.setColor(color);
        g.fillRect(x, y, w, h);
    }

    public int getWidthOfImage(String key) {
        return textureData.getImageData(key).getW();
    }

    public int getHeightOfImage(String key) {
        return textureData.getImageData(key).getH();
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public static int splitPoints(int amount, int totalW, int w, int i) {
        return (totalW / amount - w) / 2 + totalW / amount * i;
    }

    public static int[] to_screen_coordinate(int x, int y) {
        return new int[]{(int) (x * 0.5 + y * -0.5), (int) (x * 0.25 + y * 0.25)};
    }

    public static double[] invert_matrix(double a, double b, double c, double d) {
        double DET = (1 / (a * d - b * c));

        return new double[]{DET * d, DET * -b, DET * -c, DET * a};
    }

    public static int[] to_grid_coordinate(int x, int y) {
        double A = 1 * 0.5;
        double B = -1 * 0.5;
        double C = 0.5 * 0.5;
        double D = 0.5 * 0.5;

        double[] INV = invert_matrix(A, B, C, D);

        return new int[]{(int) (x * INV[0] + y * INV[1]), (int) (x * INV[2] + y * INV[3])};
    }

    public boolean detectCollisionWithImage(String key, int x, int y, int max) {
        ImageData imageData = textureData.getImageData(key);

        int w = imageData.getW() / max;

        return detectCollisionWithRectangle(x, y, w, imageData.getH());
    }

    public boolean detectCollisionWithRectangle(int x, int y, int w, int h) {
        return CollisionHandler.detectCollision(x, y, w, h, getMousePos()[0], getMousePos()[1], 1, 1);
    }

    public int[] getMousePos() {
        return window.getMousePos();
    }


    public JPanel getDrawPanel() {
        return window.getDrawPanel();
    }

    public void addClickListener(Window.ClickListener clickListener) {
        window.addClickListener(clickListener);
    }

    public void setFullscreen(boolean state) {

    }

}
