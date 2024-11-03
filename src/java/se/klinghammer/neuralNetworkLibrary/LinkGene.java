package se.klinghammer.neuralNetworkLibrary;

import java.io.Serializable;

public class LinkGene implements Serializable {
    private final int inputId;
    private final int outputId;
    private double weight;
    private boolean enabled;

    public LinkGene(int inputId, int outputId) {
        this.inputId = inputId;
        this.outputId = outputId;
        this.enabled = true;
        this.weight = 1;
    }

    public LinkGene(int inputId, int outputId, int inputSize, int outputSize) {
        this.inputId = inputId;
        this.outputId = outputId;
        this.enabled = true;
        initializeWeight(inputSize, outputSize);
    }

    public LinkGene(int inputId, int outputId, double weight) {
        this.inputId = inputId;
        this.outputId = outputId;
        this.weight = weight;
        this.enabled = true;
    }

    public LinkGene(int inputId, int outputId, double weight, boolean enabled) {
        this.inputId = inputId;
        this.outputId = outputId;
        this.enabled = enabled;
        this.weight = weight;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        LinkGene cloned = (LinkGene) super.clone();
        // Deep copy any mutable fields if necessary
        return cloned;
    }

    public void initializeWeight(int inputSize, int outputSize) {
        double k = (inputSize + outputSize == 0) ? 0.1 : Math.sqrt(6.0 / (inputSize + outputSize));
        this.weight = k * (RandomUtil.random.nextDouble() * 2 - 1);
    }

    public void enable() {
        this.enabled = true;
    }

    public void disable() {
        this.enabled = false;
    }

    public int getOutputId() {
        return outputId;
    }

    public int getInputId() {
        return inputId;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public boolean isEnabled() {
        return enabled;
    }

    // Faster hashing in HashMap
    public long convertToLong() {
        return convertToLong(this);
    }

    public static long convertToLong(LinkGene link) {
        return convertToLong(link.inputId, link.outputId);
    }

    public static long convertToLong(int id1, int id2) {
        return (((long) id1) << 32) | (id2 & 0xffffffffL);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LinkGene link = (LinkGene) o;
        return inputId == link.getInputId() && outputId == link.getOutputId();
    }

    @Override
    public int hashCode() {
        return Long.hashCode(convertToLong(inputId, outputId));
    }
}
