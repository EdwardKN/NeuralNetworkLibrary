
import java.util.Arrays;

public class NetworkAdapter {
    private double[] pack;

    private static final int inputAmount = 4;

    public NetworkAdapter() {
        pack = new double[inputAmount];
        Arrays.fill(pack, 0);
    }

    public void setPos(double pos){
        pack[0] = pos / Pendulum.getWIDTH();
    }

    public void setBallX(double x){
        pack[1] = x;
    }

    public void setBallY(double y){
        pack[2] = y;
    }

    public void setAngularVelocity(double angularVelocity){
        pack[3] = angularVelocity;
    }
    public double[] getPack() {
        return pack;
    }

    public static int getInputAmount() {
        return inputAmount;
    }
}
