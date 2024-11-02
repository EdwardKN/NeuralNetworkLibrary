package se.test.neuralNetwork.examples.pendulum;

import se.test.neuralNetwork.src.Genome;
import se.test.neuralNetwork.src.Individual;

public class Propagater {
    private final Individual individual;

    private final Genome genome;

    private final NetworkAdapter networkAdapter;

    private static final int outputAmount = 1;

    private double[] propagation = new double[outputAmount];


    public Propagater(Individual individual, NetworkAdapter networkAdapter) {
        this.individual = individual;
        this.genome = individual.getNetwork();
        this.networkAdapter = networkAdapter;
    }

    public void propagate() {
        propagation = genome.propagate(networkAdapter.getPack());
    }

    public double getCartAcc() {
        return propagation[0];
    }
}
