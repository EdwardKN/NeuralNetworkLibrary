import se.klinghammer.neuralNetworkLibrary.Genome;
import se.klinghammer.neuralNetworkLibrary.Individual;

import java.util.HashMap;

public class Propagater {
    private final Individual individual;

    private final Genome genome;

    private final NetworkAdapter networkAdapter;

    private static final int outputAmount = 81;

    private double[] propagation = new double[outputAmount];

    private HashMap<Integer, Double> savedData = new HashMap<>();

    public Propagater(Individual individual, NetworkAdapter networkAdapter) {
        this.individual = individual;
        this.genome = individual.getNetwork();
        this.networkAdapter = networkAdapter;
    }

    public void propagate() {
        Genome.SpecialPropagateResponse response = genome.propagate(networkAdapter.getPack());
        propagation = response.getPropagation();
        savedData = response.getSavedValues();
    }

    public double[] getPropagation() {
        return propagation;
    }
    public void superSpecialShit(int index) {
        Genome.SpecialPropagateResponse response = genome.superSpecialPropagate(savedData, networkAdapter.getPack(), index);
        savedData = response.getSavedValues();
        propagation = response.getPropagation();
    }


    public void addFitness(double fitness) {
        individual.setFitness(individual.getFitness() + fitness);
    }

    public static int getOutputAmount() {
        return outputAmount;
    }
}
