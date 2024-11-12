import java.util.Arrays;

public class NetworkAdapter {
    private double[] pack;

    private static final int inputAmount = 81;

    public NetworkAdapter() {
        pack = new double[inputAmount];
        Arrays.fill(pack, 0);
    }

    public void setCellValue(int index, double value) {
        pack[index] = value;
    }

    public double[] getPack() {
        return pack;
    }

    public static int getInputAmount() {
        return inputAmount;
    }
}
