package se.test.neuralNetwork.src.visual.utils;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FontLoader {
    private static final Logger LOGGER = Logger.getLogger(FontLoader.class.getName());

    public void loadFont(String fontname) {
        try {
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            Font customFont = Font.createFont(Font.TRUETYPE_FONT, new File("resources/fonts/" + fontname));
            ge.registerFont(customFont);
        } catch (IOException | FontFormatException e) {
            LOGGER.log(Level.SEVERE, "Failed to load font: " + fontname);
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.log(Level.FINE, "Exception details:", e);
            }
        }
    }

}
