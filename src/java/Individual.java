import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class Individual {
    private final Genome network;
    private double fitness;
    private final int id;

    private int addedLinks = 0;
    private int addedNeurons = 0;

    private int removedLinks = 0;

    public Individual(int amountOfInputs, int amountOfOutputs, int id) {
        this.network = new Genome(amountOfInputs, amountOfOutputs);
        this.fitness = 0;
        this.id = id;
    }

    public Individual(Genome genome, int id) {
        this.network = genome;
        this.fitness = 0;
        this.id = id;
    }

    public void mutateAddLink() {
        int inputId = network.getRandomInputOrHiddenId();
        int outputId = network.getRandomHiddenOrOutputId(inputId);

        LinkGene link = network.findLink(inputId, outputId);
        if (link != null) {
            if (link.isEnabled()) {
                mutate(1, true);
            } else {
                link.enable();
            }
            return;
        }

        if (network.createsCycle(inputId, outputId) || addedLinks > Population.getConfig().getInt("maxEnabledLinksPerMutation")) {
            mutate(1, true);
            return;
        }

        network.addLinkWithXavier(inputId, outputId);

        addedLinks++;
    }

    public void mutateDisableLink() {
        LinkGene link = network.getRandomLink();

        if (link == null || removedLinks > Population.getConfig().getInt("maxEnabledLinksPerMutation")) {
            mutate(1, true);
            return;
        }

        link.disable();
        removedLinks++;
    }

    // Complete global innovation number
    public void mutateAddNeuron() {
        List<LinkGene> possibleLinksToSplit = new ArrayList<>();
        List<Integer> possibleNewIds = new ArrayList<>();

        for (LinkGene link : network.getLinks()) {
            if (!link.isEnabled()) {
                continue;
            }

            int id = Population.getLinkId(link);

            if (network.getNeuronFromId(id) == null) {
                possibleLinksToSplit.add(link);
                possibleNewIds.add(id);
            }
        }

        if (possibleLinksToSplit.isEmpty() || addedNeurons > Population.getConfig().getInt("maxNeuronsPerMutation")) {
            mutate(1, true);
            return;
        }

        int index = RandomUtil.random.nextInt(possibleLinksToSplit.size());
        LinkGene linkToSplit = possibleLinksToSplit.get(index);

        int id = possibleNewIds.get(index);
        linkToSplit.disable();

        // Global innovation number
        if (id == Integer.MIN_VALUE) {
            id = Population.getGlobalInnovationId();
            Population.updateGlobalInnovationId();
            Population.updateLinkToId(linkToSplit, id);
        }

        NeuronGene neuron = new NeuronGene(id);
        network.addNeuron(neuron);

        network.addLinkWithXavier(linkToSplit.getInputId(), neuron.getId());
        network.addLink(new LinkGene(neuron.getId(), linkToSplit.getOutputId(), linkToSplit.getWeight()));
        addedNeurons++;
    }

    public void mutateChangeWeight() {
        LinkGene link = network.getRandomLink();
        if (link == null) {
            mutate(1, true);
            return;
        }

        if (RandomUtil.random.nextDouble() < Population.getConfig().getDouble("extremeMutationChance")) {
            network.reinitializeLinkWithXavier(link);
        } else {
            double value = RandomUtil.random.nextDouble() * 2 - 1;
            link.setWeight(link.getWeight() + value * Population.getConfig().getDouble("mutationSpeed"));
        }
    }

    public void mutateChangeBias() {
        if (network.getNeuronsSize() == 0) {
            mutate(1, true);
            return;
        }

        NeuronGene neuron = network.getRandomHiddenOrOutput();

        if (RandomUtil.random.nextDouble() < Population.getConfig().getDouble("extremeMutationChance")) {
            neuron.initializeBias();
        } else {
            double value = RandomUtil.random.nextDouble() * 2 - 1;
            neuron.setBias(neuron.getBias() + value * Population.getConfig().getDouble("mutationSpeed"));
        }
    }

    public void mutateChangeActivation() {
        NeuronGene neuronGene = network.getRandomHidden();

        if (neuronGene == null) {
            mutate(1, true);
            return;
        }

        List<Activation> availableActivations = Arrays.stream(Activation.values())
                .filter(a -> a != neuronGene.getActivation()).toList();

        Activation newActivation = availableActivations.get(RandomUtil.random.nextInt(availableActivations.size()));
        neuronGene.setActivation(newActivation);
    }


    public void mutate(int rolls, boolean repeat) {
        if (!repeat) {
            addedNeurons = 0;
            addedLinks = 0;
            removedLinks = 0;
        }

        Runnable[] mutationFunctions = new Runnable[]{
                this::mutateChangeWeight,
                this::mutateChangeBias,
                this::mutateAddNeuron,
                this::mutateAddLink,
                this::mutateChangeActivation
        };

        double[] mutationProbabilities = new double[]{
                Population.getConfig().getDouble("weightMutationProb"),
                Population.getConfig().getDouble("biasMutationProb"),
                Population.getConfig().getDouble("addNeuronMutationProb"),
                Population.getConfig().getDouble("addLinkMutationProb"),
                Population.getConfig().getDouble("activationMutationProb"),
        };

        double sum = Arrays.stream(mutationProbabilities).sum();
        double[] normalizedProbabilities = Arrays.stream(mutationProbabilities).map(k
                -> sum == 0 ? 1.0 / mutationProbabilities.length : k / sum).toArray();

        for (int i = 0; i < rolls; i++) {
            double random = Math.random();
            double cumulative = 0;

            for (int j = 0; j < mutationFunctions.length; j++) {
                cumulative += normalizedProbabilities[j];

                if (random < cumulative) {
                    mutationFunctions[j].run();
                    break;
                }
            }
        }
    }


    public Genome getNetwork() {
        return network;
    }

    public double getFitness() {
        return fitness;
    }

    public void setFitness(double fitness) {
        this.fitness = fitness;
    }

    public int getId() {
        return id;
    }
}