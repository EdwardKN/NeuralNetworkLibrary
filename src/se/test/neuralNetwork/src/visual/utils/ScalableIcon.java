package se.test.neuralNetwork.src.visual.utils;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class ScalableIcon extends ImageIcon {
    private final String img;
    private Image customImage;
    private int width;
    private int height;

    private final int i;

    private final int max;
    private final DrawUtil drawUtil;

    public ScalableIcon(DrawUtil drawutil, String img, Image customImage, int i, int max, int width, int height) {
        this.drawUtil = drawutil;
        this.img = img;
        this.customImage = customImage;
        this.width = width;
        this.height = height;
        this.i = i;
        this.max = max;
    }

    public ScalableIcon(DrawUtil drawUtil, String img, Image customImage, int i, int max) {
        this.drawUtil = drawUtil;
        this.img = img;
        this.customImage = customImage;
        this.width = drawUtil.getWidthOfImage(img) / max;
        this.height = drawUtil.getHeightOfImage(img);
        this.i = i;
        this.max = max;
    }

    public ScalableIcon(DrawUtil drawutil, String img, int i, int max, int width, int height) {
        this.drawUtil = drawutil;
        this.img = img;
        this.customImage = null;
        this.width = width;
        this.height = height;
        this.i = i;
        this.max = max;
    }

    public ScalableIcon(DrawUtil drawUtil, String img, int i, int max) {
        this.drawUtil = drawUtil;
        this.img = img;
        this.customImage = null;
        this.width = drawUtil.getWidthOfImage(img) / max;
        this.height = drawUtil.getHeightOfImage(img);
        this.i = i;
        this.max = max;
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2d = (Graphics2D) g;

        BufferedImage image = new BufferedImage(drawUtil.getWidthOfImage(img) / max, drawUtil.getHeightOfImage(img), BufferedImage.TYPE_INT_ARGB);

        drawUtil.drawImage((Graphics2D) image.getGraphics(), img, 0, 0, i, max);
        if (customImage != null) {
            image.getGraphics().drawImage(customImage, 0, 0, null);
        }
        g2d.drawImage(image, 0, 0, width, height, null);
    }

    @Override
    public int getIconWidth() {
        return width;
    }

    @Override
    public int getIconHeight() {
        return height;
    }

    public void changeSize(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public void setCustomImage(Image customImage) {
        this.customImage = customImage;
    }
}