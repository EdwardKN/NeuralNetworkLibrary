import se.klinghammer.neuralNetworkLibrary.Genome;
import se.klinghammer.neuralNetworkLibrary.Individual;

public class Propagater {
    private final Individual individual;

    private final Genome genome;

    private final NetworkAdapter networkAdapter;

    private static final int outputAmount = 81;

    private double[] propagation = new double[outputAmount];


    public Propagater(Individual individual, NetworkAdapter networkAdapter) {
        this.individual = individual;
        this.genome = individual.getNetwork();
        this.networkAdapter = networkAdapter;
    }

    public void propagate() {
        propagation = genome.propagate(networkAdapter.getPack());
    }

    public double[] getPropagation() {
        return propagation;
    }
    public void superSpecialShit(int index) {
        propagation = genome.superSpecialPropagate(networkAdapter.getPack(), index);
    }


    public void addFitness(double fitness) {
        individual.setFitness(individual.getFitness() + fitness);
    }

    public static int getOutputAmount() {
        return outputAmount;
    }
}
