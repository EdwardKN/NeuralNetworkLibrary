
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.prefs.Preferences;

public class ScreenRefreshRateDetector {

    public int getScreenRefreshRate() {
        String os = System.getProperty("os.name").toLowerCase();
        int refreshRate = 0;

        if (os.contains("win")) {
            refreshRate = getWindowsRefreshRate();
        } else if (os.contains("mac")) {
            refreshRate = getMacRefreshRate();
        } else if (os.contains("nix") || os.contains("nux") || os.contains("aix")) {
            refreshRate = getLinuxRefreshRate();
        } else {
            System.out.println("Unsupported operating system: " + os);
        }

        return refreshRate;
    }

    private int getWindowsRefreshRate() {
        int refreshRate = 0;
        try {
            Preferences prefs = Preferences.userRoot().node("Software\\Microsoft\\Multimedia\\Video");

            refreshRate = prefs.getInt("PreferredDisplayModeRefreshRate", 60);
        } catch (Exception e) {
            System.err.println("Kunde inte få ut refresh rate");
        }
        return refreshRate;
    }

    private int getMacRefreshRate() {
        int refreshRate = 0;
        try {
            GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
            refreshRate = gd.getDisplayMode().getRefreshRate();
        } catch (Exception e) {
            System.err.println("Kunde inte få ut refresh rate");
        }
        return refreshRate;
    }

    private int getLinuxRefreshRate() {
        int refreshRate = 0;
        try {
            Process process = Runtime.getRuntime().exec("xrandr --verbose");
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("connected primary")) {
                    String[] tokens = line.split(" ");
                    for (int i = 0; i < tokens.length; i++) {
                        if (tokens[i].equals("Rate:")) {
                            refreshRate = Integer.parseInt(tokens[i + 1]);
                            break;
                        }
                    }
                    break;
                }
            }

            reader.close();
        } catch (IOException | NumberFormatException e) {
            System.err.println("Kunde inte få ut refresh rate");
        }
        return refreshRate;
    }

    public static void main(String[] args) {
        ScreenRefreshRateDetector detector = new ScreenRefreshRateDetector();
        int refreshRate = detector.getScreenRefreshRate();
        System.out.println("Screen Refresh Rate: " + refreshRate + " Hz");
    }
}
