package se.klinghammer.neuralNetworkLibrary;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfigLoader {
    private Properties properties = new Properties();

    public ConfigLoader(String configFilePath) {
        try (FileInputStream input = new FileInputStream(configFilePath)) {
            properties.load(input);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getInt(String key) {
        try {
            return Integer.parseInt(properties.getProperty(key));
        } catch (NumberFormatException e) {
            System.out.println(key);
            return 0;
        }
    }

    public double getDouble(String key) {
        try {
            return Double.parseDouble(properties.getProperty(key));
        } catch (NumberFormatException e) {
            System.out.println(key);
            return 0;
        }
    }

    public String getSring(String key) {
        try {
            return properties.getProperty(key);
        } catch (NumberFormatException e) {
            System.out.println(key);
            return "";
        }
    }
}