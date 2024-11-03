package se.klinghammer.neuralNetworkLibrary;

import java.io.Serializable;
import java.util.*;

public class Genome implements Serializable {
    // Must have
    private final int amountOfInputs;
    private final int amountOfOutputs;
    private final List<NeuronGene> neurons = new ArrayList<>();
    private final List<LinkGene> links = new ArrayList<>();
    // Performance
    private final HashMap<Integer, NeuronGene> idToNeuron = new HashMap<>();
    private final HashMap<Integer, List<Integer>> adjacencyList = new HashMap<>();

    public Genome(int amountOfInputs, int amountOfOutputs) {
        if (amountOfInputs <= 0 || amountOfOutputs <= 0) {
            throw new IllegalArgumentException("There has to be at least one input- and one output neuron");
        }

        this.amountOfInputs = amountOfInputs;
        this.amountOfOutputs = amountOfOutputs;

        // Input: 0, ..., amountOfInputs - 1
        // Output: amountOfInputs, ..., amountOfInputs + amountOfOutputs - 1
        for (int i = 0; i < amountOfInputs + amountOfOutputs; i++) {
            addNeuron(new NeuronGene(i));
        }
    }

    public double[] propagate(double[] inputs) {
        HashMap<Integer, Double> currentValue = new HashMap<>(neurons.size());
        BitSet activatedNeurons = new BitSet(neurons.size());

        for (NeuronGene neuron : neurons) {
            currentValue.put(neuron.getId(), neuron.getBias());
        }

        for (int i = 0; i < amountOfInputs; i++) {
            currentValue.put(i, inputs[i]);
            activatedNeurons.set(i);
        }

        for (LinkGene link : links) {
            if (!link.isEnabled()) {
                continue;
            }

            int inputId = link.getInputId();
            int outputId = link.getOutputId();

            // Apply activation function
            if (!activatedNeurons.get(inputId)) {
                activatedNeurons.set(inputId);
                //se.klinghammer.neuralNetworkLibrary.NeuronGene inputNeuron = idToNeuron.get(inputId);
                //currentValue.put(inputId, inputNeuron.activate(currentValue.get(inputId)));

                currentValue.put(inputId, Activation.ReLU.activate(currentValue.get(inputId)));
            }

            // Calculate input
            double propagatedValue = currentValue.get(outputId) + currentValue.get(inputId) * link.getWeight();
            currentValue.put(outputId, propagatedValue);
        }

        // Limit outputs between 0 and 1
        double[] outputs = new double[amountOfOutputs];

        for (int i = 0; i < amountOfOutputs; i++) {
            //outputs[i] = se.klinghammer.neuralNetworkLibrary.Activation.Sigmoid.activate(currentValue.get(amountOfInputs + i));
            outputs[i] = Activation.Tanh.activate(currentValue.get(amountOfInputs + i));
        }

        return outputs;
    }

    public double specialPropagate(double[] inputs, int outputIndex) {
        Set<Integer> involvedIds = new HashSet<>();
        List<LinkGene> linksToProcess = new ArrayList<>();
        involvedIds.add(amountOfInputs + outputIndex);

        for (int i = links.size() - 1; i >= 0; i--) {
            LinkGene link = links.get(i);

            if (link.isEnabled() && involvedIds.contains(link.getOutputId())) {
                involvedIds.add(link.getInputId());
                linksToProcess.add(link);
            }
        }

        HashMap<Integer, Double> neuronValues = new HashMap<>(involvedIds.size());
        BitSet activatedNeurons = new BitSet(involvedIds.size());

        for (int id : involvedIds) {
            neuronValues.put(id, idToNeuron.get(id).getBias());
        }

        for (int i = 0; i < amountOfInputs; i++) {
            neuronValues.put(i, inputs[i]);
            activatedNeurons.set(i);
        }

        for (int i = linksToProcess.size() - 1; i >= 0; i--) {
            LinkGene link = linksToProcess.get(i);
            int inputId = link.getInputId();
            int outputId = link.getOutputId();

            // Apply activation function
            if (!activatedNeurons.get(inputId)) {
                activatedNeurons.set(inputId);
                NeuronGene inputNeuron = idToNeuron.get(inputId);
                neuronValues.put(inputId, inputNeuron.activate(neuronValues.get(inputId)));
            }

            // Calculate input
            double propagatedValue = neuronValues.get(outputId) + neuronValues.get(inputId) * link.getWeight();
            neuronValues.put(outputId, propagatedValue);
        }

        /*double value = se.klinghammer.neuralNetworkLibrary.Activation.Sigmoid.activate(neuronValues.get(amountOfInputs + outputIndex));
        double[] outputs = propagate(inputs);
        if (value != outputs[outputIndex]) {
            System.out.println("Något är lurt");
        }*/

        return Activation.Sigmoid.activate(neuronValues.get(amountOfInputs + outputIndex));
    }

    public boolean createsCycle(int inputId, int outputId) {
        if (neurons.size() > Population.getConfig().getInt("cycleDetectionNeuronThreshold") || links.size() > Population.getConfig().getInt("cycleDetectionLinkThreshold")) {
            return hasPathBFS(inputId, outputId);
        } else {
            return hasPathDFS(inputId, outputId);
        }
    }

    private boolean hasPathBFS(int inputId, int outputId) {
        Set<Integer> visited = new HashSet<>();
        Queue<Integer> queue = new LinkedList<>();
        queue.add(outputId);

        while (!queue.isEmpty()) {
            int current = queue.poll();
            if (current == inputId) return true;

            if (visited.contains(current)) {
                continue;
            }

            visited.add(current);

            for (int neighbor : adjacencyList.get(current)) {
                if (!visited.contains(neighbor)) {
                    queue.add(neighbor);
                }
            }
        }

        return false;
    }

    private boolean hasPathDFS(int inputId, int outputId) {
        Set<Integer> visited = new HashSet<>();
        Deque<Integer> stack = new ArrayDeque<>();
        stack.add(outputId);

        while (!stack.isEmpty()) {
            int current = stack.pop();
            if (current == inputId) return true;

            if (visited.contains(current)) {
                continue;
            }

            visited.add(current);

            for (int id : adjacencyList.get(current)) {
                if (!visited.contains(id)) {
                    stack.add(id);
                }
            }
        }

        return false;
    }

    public LinkGene findLink(int inputId, int outputId) {
        for (LinkGene link : links) {
            if (link.getInputId() == inputId
                    && link.getOutputId() == outputId) {
                return link;
            }
        }

        return null;
    }

    public NeuronGene getNeuronFromId(int id) {
        return idToNeuron.get(id);
    }

    public void addNeuron(NeuronGene neuron) {
        neurons.addLast(neuron);
        idToNeuron.put(neuron.getId(), neuron);
        adjacencyList.put(neuron.getId(), new ArrayList<>());
    }

    public void addLink(LinkGene link) {
        adjacencyList.get(link.getInputId()).add(link.getOutputId());

        for (int i = links.size() - 1; i >= 0; i--) {
            if (links.get(i).getOutputId() == link.getInputId()) {
                links.add(i + 1, link);
                return;
            }
        }

        links.addFirst(link);
    }

    // Uses Xavier initialization
    public void reinitializeLinkWithXavier(LinkGene link) {
        int inputSize = 0;
        int outputSize = adjacencyList.get(link.getOutputId()).size();

        for (LinkGene compareLink : links) {
            if (compareLink.getOutputId() == link.getInputId()) {
                inputSize++;
            }
        }

        link.initializeWeight(inputSize, outputSize);
    }

    public void addLinkWithXavier(int inputId, int outputId) {
        LinkGene link = new LinkGene(inputId, outputId);
        reinitializeLinkWithXavier(link);
        addLink(link);
    }

    public int getAmountOfInputs() {
        return amountOfInputs;
    }

    public List<NeuronGene> getInputNeurons() {
        return new ArrayList<>(neurons.subList(0, amountOfInputs));
    }

    public int getAmountOfOutputs() {
        return amountOfOutputs;
    }

    public NeuronGene getRandomHidden() {
        List<NeuronGene> hidden = getHiddenNeurons();

        if (hidden.isEmpty()) {
            return null;
        }

        return hidden.get(RandomUtil.random.nextInt(hidden.size()));
    }

    public int getRandomInputOrHiddenId() {
        List<NeuronGene> choose = getInputNeurons();
        choose.addAll(neurons.subList(amountOfInputs + amountOfOutputs, neurons.size()));

        return choose.get(RandomUtil.random.nextInt(choose.size())).getId();
    }

    public int getRandomHiddenOrOutputId(int exceptionId) {
        List<Integer> ids = new ArrayList<>();

        for (int i = amountOfInputs; i < neurons.size(); i++) {
            int neuronId = neurons.get(i).getId();

            if (neuronId != exceptionId) {
                ids.add(neuronId);
            }
        }

        return ids.get(RandomUtil.random.nextInt(ids.size()));
    }

    public NeuronGene getRandomHiddenOrOutput() {
        return neurons.get(RandomUtil.random.nextInt(amountOfInputs, neurons.size()));
    }

    public List<NeuronGene> getNeurons() {
        return neurons;
    }

    public int getNeuronsSize() {
        return neurons.size();
    }

    public List<NeuronGene> getHiddenNeurons() {
        return neurons.subList(amountOfInputs + amountOfOutputs, neurons.size());
    }

    public List<LinkGene> getLinks() {
        return links;
    }

    public LinkGene getRandomLink() {
        List<LinkGene> enabledLinks = links.stream()
                .filter(LinkGene::isEnabled)
                .toList();

        if (enabledLinks.isEmpty()) {
            return null;
        }

        return enabledLinks.get(RandomUtil.random.nextInt(enabledLinks.size()));
    }

    public static Genome createCompletelyConnectedGenome(int amountOfInputs, int amountOfOutputs) {
        Genome genome = new Genome(amountOfInputs, amountOfOutputs);

        for (int i = 0; i < amountOfInputs; i++) {
            for (int h = amountOfInputs; h < amountOfInputs + amountOfOutputs; h++) {
                genome.addLinkWithXavier(i, h);
            }
        }

        return genome;
    }

    public int getAmountOfExcessNeurons(Genome compareTo) {
        int excess = 0;
        int maxId = 0;

        for (NeuronGene neuron : compareTo.neurons) {
            if (neuron.getId() > maxId) {
                maxId = neuron.getId();
            }
        }

        for (NeuronGene neuron : neurons) {
            if (neuron.getId() > maxId) {
                excess++;
            }
        }

        return excess;
    }

    public int getAmountOfDisjointNeurons(Genome compareTo) {
        int disjoint = 0;
        int maxIdCurrent = 0;
        int maxIdCompareTo = 0;

        for (NeuronGene neuron : neurons) {
            if (neuron.getId() > maxIdCurrent) {
                maxIdCurrent = neuron.getId();
            }
        }

        for (NeuronGene neuron : compareTo.neurons) {
            if (neuron.getId() > maxIdCompareTo) {
                maxIdCompareTo = neuron.getId();
            }
        }

        // Count disjoint neurons in the first genome
        for (NeuronGene neuron : neurons) {
            if (neuron.getId() <= maxIdCompareTo
                    && compareTo.getNeuronFromId(neuron.getId()) == null) {
                disjoint++;
            }
        }

        // Count disjoint neurons in the second genome
        for (NeuronGene neuron : compareTo.neurons) {
            if (neuron.getId() <= maxIdCurrent
                    && compareTo.getNeuronFromId(neuron.getId()) == null) {
                disjoint++;
            }
        }

        return disjoint;
    }

    public double getAverageWeightDifference(Genome compareTo) {
        int N = 0;
        double averageDifference = 0;

        for (LinkGene link : links) {
            LinkGene compareLink = compareTo.findLink(link.getInputId(), link.getOutputId());

            if (compareLink != null) {
                averageDifference += Math.abs(link.getWeight() - compareLink.getWeight());
                N++;
            }
        }

        if (N == 0) {
            return 0;
        }

        return averageDifference / N;
    }

    public int getComplexity() {
        return neurons.size() + (int) links.stream().filter(LinkGene::isEnabled).count();
    }

}
