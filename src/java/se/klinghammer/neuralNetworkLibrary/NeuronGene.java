package se.klinghammer.neuralNetworkLibrary;

import java.io.Serializable;

public class NeuronGene implements Serializable {
    private final int id;
    private double bias;
    private Activation activation;

    public NeuronGene(int id, double mutationSpeed) {
        this.id = id;
        this.activation = Activation.None;
        initializeBias(mutationSpeed);
    }

    public NeuronGene(int id, double bias, Activation activation) {
        this.id = id;
        this.bias = bias;
        this.activation = activation;
    }

    public void initializeBias(double mutationSpeed) {
        bias = mutationSpeed * (RandomUtil.random.nextDouble() * 2 - 1);
    }

    public double getBias() {
        return bias;
    }

    public Activation getActivation() {
        return activation;
    }

    public double activate(double input) {
        return activation.activate(input);
    }

    public void setActivation(Activation activation) {
        this.activation = activation;
    }

    public int getId() {
        return id;
    }

    public void setBias(double bias) {
        this.bias = bias;
    }

    // Faster hashing in HashMap
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        return id == ((NeuronGene) o).id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}
