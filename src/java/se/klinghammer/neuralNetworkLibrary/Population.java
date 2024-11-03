package se.klinghammer.neuralNetworkLibrary;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.lang3.SerializationUtils;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class Population {
    private static int globalInnovationId;
    private static final HashMap<Long, Integer> linkToId = new HashMap<>();
    private List<Individual> individuals = new ArrayList<>();

    private final int populationSize;

    private int generations = 0;

    private int untilGeneration;

    private final String fileName;

    private int currentSpeciesAmount = 0;
    private float largestDelta;

    private float highestDeltaBelowThreshold;

    private double highestFitness = 0;
    private double averageFitness = 0;

    private static ConfigLoader config;

    private FitnessComputer fitnessComputer;

    private static String configPath;

    public Population(int size, int amountOfInputs, int amountOfOutputs, String fileName, boolean initializeWithCompletelyConnected) {
        this.fileName = fileName;
        populationSize = size;
        globalInnovationId = amountOfInputs + amountOfOutputs;

        for (int i = 0; i < size; i++) {
            Individual individual = new Individual(Genome.createCompletelyConnectedGenome(amountOfInputs, amountOfOutputs), i);
            individual.mutate(config.getInt("amountOfMutationRolls"), false);
            individuals.add(individual);
        }


    }

    public Population(int size, int amountOfInputs, int amountOfOutputs, String fileName) {
        this.fileName = fileName;
        populationSize = size;
        globalInnovationId = amountOfInputs + amountOfOutputs;

        for (int i = 0; i < size; i++) {
            Individual individual = new Individual(amountOfInputs, amountOfOutputs, i);
            individual.mutate(config.getInt("amountOfMutationRolls"), false);
            individuals.add(individual);
        }
    }

    public static void setConfigPath(String configPath) {
        Population.configPath = configPath;
        config = new ConfigLoader(configPath + ".properties");
    }

    public void run(int untilGeneration) {
        this.untilGeneration = untilGeneration;
        if (generations < untilGeneration) {
            setConfigPath(configPath);
            computeFitness();
        } else {
            exportToJson(fileName);
        }
    }

    public void computeFitness() {
        if (fitnessComputer != null) {
            fitnessComputer.start();
        }
    }

    public void hasCalculatedFitness() {
        highestFitness = getBestSamplesSorted(1)[0].getFitness();
        averageFitness = individuals.stream()
                .mapToDouble(Individual::getFitness)
                .average()
                .orElse(0.0);
        generations++;
        if (generations >= untilGeneration) {
            exportToJson(fileName);
            return;
        }
        individuals = reproduce();
        run(untilGeneration);
    }

    private double calculateDelta(Individual representative, Individual individual) {
        if (representative == individual) {
            return 0;
        }

        Genome currentNetwork = individual.getNetwork();
        Genome representativeNetwork = representative.getNetwork();

        int N = Math.max(currentNetwork.getNeuronsSize(), representativeNetwork.getNeuronsSize());
        int excess = representativeNetwork.getAmountOfExcessNeurons(currentNetwork);
        int disjoint = representativeNetwork.getAmountOfDisjointNeurons(currentNetwork);
        double averageWeightDifference = representativeNetwork.getAverageWeightDifference(currentNetwork);

        return (config.getDouble("excessNeuronsConstant") * excess + config.getDouble("disjointNeuronsConstant") * disjoint) / N
                + config.getDouble("averageDeltaWeightConstant") * averageWeightDifference;
    }

    private List<List<Individual>> createSpecies() {
        largestDelta = 0;
        highestDeltaBelowThreshold = 0;

        List<List<Individual>> species = new ArrayList<>();

        individuals.sort(Comparator.comparing(Individual::getFitness).reversed());

        // λ = c1 * E / N + c2 * D / N + c3 * (W_)
        for (Individual individual : individuals) {
            boolean assignedToSpecies = false;

            for (List<Individual> currentSpecies : species) {
                double delta = calculateDelta(currentSpecies.getFirst(), individual);

                largestDelta = Math.max(largestDelta, (float) Math.floor(delta * 100) / 100);

                if (delta < config.getDouble("deltaThreshold")) {
                    highestDeltaBelowThreshold = Math.max(highestDeltaBelowThreshold, (float) Math.floor(delta * 100) / 100);
                    currentSpecies.add(individual);
                    assignedToSpecies = true;
                    break;
                }
            }

            if (!assignedToSpecies) {
                List<Individual> newSpecies = new ArrayList<>();
                newSpecies.add(individual);
                species.add(newSpecies);
            }
        }

        return species;
    }

    private int[] calculateSpeciesOffspring(List<List<Individual>> species) {
        double meanAdjustedFitness = 0;
        double[] adjustedFitnessSum = new double[species.size()];

        for (int i = 0; i < species.size(); i++) {
            List<Individual> currentSpecies = species.get(i);
            int speciesSize = currentSpecies.size();

            for (Individual individual : currentSpecies) {
                double adjustedFitness = individual.getFitness() / (1 +
                        Math.exp((config.getDouble("speciesSizePenaltySteepness") * config.getDouble("averageNumberOfSpecies") / speciesSize) * (speciesSize - populationSize / config.getDouble("averageNumberOfSpecies"))));

                adjustedFitnessSum[i] += adjustedFitness;
                meanAdjustedFitness += adjustedFitness;
            }
        }

        meanAdjustedFitness /= populationSize;

        // N'j = Summa(1, Nj, fij) / f_
        double[] fractionalOffspring = new double[species.size()];

        if (meanAdjustedFitness <= 0) {
            int[] amountOfOffspring = new int[species.size()];

            for (int i = 0; i < species.size(); i++) {
                amountOfOffspring[i] = species.get(i).size();
            }

            return amountOfOffspring;
        } else {
            for (int i = 0; i < species.size(); i++) {
                fractionalOffspring[i] = adjustedFitnessSum[i] / meanAdjustedFitness;
            }
        }

        int totalOffspring = 0;
        int[] currentOffspring = new int[species.size()];
        int[] potentialOffspring = new int[species.size()];

        for (int i = 0; i < fractionalOffspring.length; i++) {
            int maxSize = (int) ((1 + calculateCrossoverThreshold(species.get(i))) * species.get(i).size());

            if (maxSize <= fractionalOffspring[i]) {
                fractionalOffspring[i] = maxSize;
            }

            int lowerBound = (int) fractionalOffspring[i];
            double fractionalPart = fractionalOffspring[i] - lowerBound;

            if (RandomUtil.random.nextDouble() < fractionalPart) {
                currentOffspring[i] = lowerBound + 1;
            } else {
                currentOffspring[i] = lowerBound;
            }

            potentialOffspring[i] = maxSize - currentOffspring[i];
            totalOffspring += currentOffspring[i];
        }

        int difference = populationSize - totalOffspring;

        // Need to increase the total population
        while (difference > 0) {
            List<Integer> indexes = new ArrayList<>();
            int maxPotential = Integer.MIN_VALUE;

            for (int i = 0; i < currentOffspring.length; i++) {
                if (potentialOffspring[i] == 0 || potentialOffspring[i] < maxPotential) {
                    continue;
                }

                if (potentialOffspring[i] > maxPotential) {
                    indexes.clear();
                    maxPotential = potentialOffspring[i];
                }

                indexes.add(i);
            }

            int mostShrinkage = Integer.MIN_VALUE;
            int mostShrinkageIndex = indexes.getFirst();

            for (int index : indexes) {
                int shrinkage = species.get(index).size() - currentOffspring[index];

                if (shrinkage > mostShrinkage) {
                    mostShrinkageIndex = index;
                    mostShrinkage = shrinkage;
                }
            }

            // Increase the selected index
            currentOffspring[mostShrinkageIndex]++;
            potentialOffspring[mostShrinkageIndex]--;
            difference--;
        }

        // Need to decrease the total population
        while (difference < 0) {
            List<Integer> indexes = new ArrayList<>();
            int minPotential = Integer.MAX_VALUE;

            for (int i = 0; i < currentOffspring.length; i++) {
                if (currentOffspring[i] == 0 || potentialOffspring[i] > minPotential) {
                    continue;
                }

                if (potentialOffspring[i] < minPotential) {
                    minPotential = potentialOffspring[i];
                    indexes.clear();
                }

                indexes.add(i);
            }

            int mostGrowth = Integer.MIN_VALUE;
            int mostGrowthIndex = indexes.getFirst();

            for (int index : indexes) {
                int growth = currentOffspring[index] - species.get(index).size();

                if (growth > mostGrowth) {
                    mostGrowthIndex = index;
                    mostGrowth = growth;
                }
            }

            // Decrease the selected index
            currentOffspring[mostGrowthIndex]--;
            potentialOffspring[mostGrowthIndex]++;
            difference++;
        }

        return currentOffspring;
    }

    public double calculateFitnessVariance(List<Individual> species) {
        double meanFitness = species.stream()
                .mapToDouble(Individual::getFitness)
                .average()
                .orElse(0.0);

        return species.stream()
                .mapToDouble(individual -> Math.pow(individual.getFitness() - meanFitness, 2))
                .average()
                .orElse(0.0);

    }

    public double calculateCrossoverThreshold(List<Individual> species) {
        double fitnessVariance = calculateFitnessVariance(species);

        double normalizedVariance = Math.min(1.0, fitnessVariance / config.getDouble("maxFitnessVariance"));
        return config.getDouble("minThreshold") +
                (config.getDouble("maxThreshold") - config.getDouble("minThreshold")) * normalizedVariance;
    }

    public List<Individual> reproduce() {
        List<List<Individual>> species = createSpecies();
        int[] amountOfOffspring = calculateSpeciesOffspring(species);

        currentSpeciesAmount = species.size();

        //System.out.println(currentSpeciesAmount);

        //Keep best x% of each species
        List<Individual> newGeneration = new ArrayList<>();

        int currentIndividualId = 0;

        for (int index = 0; index < amountOfOffspring.length; index++) {
            List<Individual> currentSpecies = species.get(index);
            currentSpecies.sort(Comparator.comparing(Individual::getFitness).reversed());

            int crossoverCutoff = (int) (currentSpecies.size() * (1 - calculateCrossoverThreshold(currentSpecies)));

            int mutationCutoff = (int) (crossoverCutoff * config.getDouble("keepingPart"));

            //System.out.println("crossover " + crossoverCutoff);

            //System.out.println(currentSpecies.getFirst().getFitness());
            for (int i = 0; i < crossoverCutoff; i++) {
                Individual offspring;
                if (i < mutationCutoff) {
                    Genome copiedGenome = (Genome) SerializationUtils.clone(currentSpecies.get(i).getNetwork());
                    offspring = new Individual(copiedGenome, currentIndividualId++);
                } else {
                    Genome copiedGenome = (Genome) SerializationUtils.clone(currentSpecies.get(i - mutationCutoff).getNetwork());
                    offspring = new Individual(copiedGenome, currentIndividualId++);
                    offspring.mutate(config.getInt("amountOfMutationRolls"), false);
                }
                newGeneration.add(offspring);
            }

            if (amountOfOffspring[index] - crossoverCutoff == 1) {
                Genome copiedGenome = (Genome) SerializationUtils.clone(currentSpecies.getFirst().getNetwork());
                newGeneration.add(new Individual(copiedGenome, currentIndividualId++));
                continue;
            }

            for (int i = crossoverCutoff; i < amountOfOffspring[index]; i++) {
                Individual offspring;
                int index2;
                do {
                    index2 = RandomUtil.random.nextInt(amountOfOffspring[index] - crossoverCutoff);
                } while (i == index2);

                Individual parent1 = currentSpecies.get(i - crossoverCutoff);
                Individual parent2 = currentSpecies.get(index2);

                offspring = new Individual(crossover(parent1, parent2), currentIndividualId++);
                offspring.mutate(config.getInt("amountOfMutationRolls"), false);


                newGeneration.add(offspring);
            }


        }

        return newGeneration;
    }

    public void exportToJson(String filePath) {
        if (fitnessComputer != null) {
            fitnessComputer.stop();
            fitnessComputer = null;
        }
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (FileWriter writer = new FileWriter(filePath)) {
            gson.toJson(this, writer);
        } catch (IOException e) {
            System.err.println("Error vid export");
        }
    }

    public static Population importFromJson(String filePath) {
        setConfigPath(configPath);
        Gson gson = new Gson();
        Population filePopulation;
        try (FileReader reader = new FileReader(filePath)) {
            filePopulation = gson.fromJson(reader, Population.class);
        } catch (IOException e) {
            return null;
        }

        return filePopulation;
    }

    private Genome crossover(Individual parent1, Individual parent2) {
        //if (true) throw new UnsupportedOperationException("Check dominant and recessive disjoint and excess genes and links");

        Individual dominant, recessive;
        if (parent1.getFitness() > parent2.getFitness()) {
            dominant = parent1;
            recessive = parent2;
        } else if (parent1.getFitness() < parent2.getFitness()) {
            dominant = parent2;
            recessive = parent1;
        } else {
            int complexity1 = parent1.getNetwork().getComplexity();
            int complexity2 = parent2.getNetwork().getComplexity();

            if (complexity1 < complexity2) {
                dominant = parent1;
                recessive = parent2;
            } else if (complexity2 < complexity1) {
                dominant = parent2;
                recessive = parent1;
            } else if (RandomUtil.random.nextBoolean()) {
                dominant = parent1;
                recessive = parent2;
            } else {
                dominant = parent2;
                recessive = parent1;
            }
        }

        Genome dominantNetwork = dominant.getNetwork();
        Genome recessiveNetwork = recessive.getNetwork();
        Genome offspring = new Genome(dominantNetwork.getAmountOfInputs(), dominantNetwork.getAmountOfOutputs());

        // Add outputs bias
        for (int i = 0; i < dominantNetwork.getAmountOfOutputs(); i++) {
            int id = dominantNetwork.getAmountOfInputs() + i;

            NeuronGene crossNeuron = crossoverNeuron(dominantNetwork.getNeuronFromId(id), recessiveNetwork.getNeuronFromId(id));
            NeuronGene outputNeuron = offspring.getNeuronFromId(id);

            outputNeuron.setBias(crossNeuron.getBias());
        }

        // Inherit neurons
        for (NeuronGene dominantNeuron : dominantNetwork.getHiddenNeurons()) {
            NeuronGene recessiveNeuron = recessiveNetwork.getNeuronFromId(dominantNeuron.getId());

            if (recessiveNeuron == null) {
                offspring.addNeuron(dominantNeuron);
            } else {
                offspring.addNeuron(crossoverNeuron(dominantNeuron, recessiveNeuron));
            }
        }

        // Inherit links
        for (LinkGene dominantLink : dominantNetwork.getLinks()) {
            LinkGene recessiveLink = recessiveNetwork.findLink(dominantLink.getInputId(), dominantLink.getOutputId());

            if (recessiveLink == null) {
                offspring.addLink(dominantLink);
            } else {
                offspring.addLink(crossoverLink(dominantLink, recessiveLink));
            }
        }

        return offspring;
    }

    private NeuronGene crossoverNeuron(NeuronGene neuron1, NeuronGene neuron2) {
        double bias = (RandomUtil.random.nextDouble() > 0.5 ? neuron1.getBias() : neuron2.getBias()) + (RandomUtil.random.nextDouble() - 2) * config.getDouble("crossoverMutationSpeed");
        Activation activation = RandomUtil.random.nextDouble() > 0.5 ? neuron1.getActivation() : neuron2.getActivation();

        return new NeuronGene(neuron1.getId(), bias, activation);
    }

    private LinkGene crossoverLink(LinkGene link1, LinkGene link2) {
        double weight = (RandomUtil.random.nextDouble() > 0.5 ? link1.getWeight() : link2.getWeight()) + (RandomUtil.random.nextDouble() - 2) * config.getDouble("crossoverMutationSpeed");
        boolean enabled = link1.isEnabled() && link2.isEnabled();

        return new LinkGene(link1.getInputId(), link1.getOutputId(), weight, enabled);
    }

    public Individual[] getSamples(int amount) {
        List<Individual> shuffledList = new ArrayList<>(individuals);
        Collections.shuffle(shuffledList);

        Individual[] samples = new Individual[amount];
        for (int i = 0; i < amount; i++) {
            samples[i] = shuffledList.get(i);
        }

        return samples;
    }

    public Individual[] getBestSamplesSorted(int amount) {
        List<Individual> sortedList = new ArrayList<>(individuals);
        sortedList.sort(Comparator.comparing(Individual::getFitness).reversed());

        Individual[] samples = new Individual[amount];

        for (int i = 0; i < amount; i++) {
            samples[i] = sortedList.get(i);
        }

        return samples;
    }

    public static int getGlobalInnovationId() {
        return globalInnovationId;
    }

    public static void updateGlobalInnovationId() {
        globalInnovationId++;
    }

    public static int getLinkId(LinkGene link) {
        return linkToId.getOrDefault(link.convertToLong(), Integer.MIN_VALUE);
    }

    public static void updateLinkToId(LinkGene link, int id) {
        linkToId.put(link.convertToLong(), id);
    }

    public int getPopulationSize() {
        return populationSize;
    }

    public List<Individual> getIndividuals() {
        return individuals;
    }

    public int getGenerations() {
        return generations;
    }

    public int getSpeciesAmount() {
        return currentSpeciesAmount;
    }

    public float getLargestDelta() {
        return largestDelta;
    }

    public float getHighestDeltaBelowThreshold() {
        return highestDeltaBelowThreshold;
    }

    public static ConfigLoader getConfig() {
        return config;
    }

    public void setFitnessComputer(FitnessComputer fitnessComputer) {
        this.fitnessComputer = fitnessComputer;
    }

    public double getHighestFitness() {
        return highestFitness;
    }

    public double getAverageFitness() {
        return averageFitness;
    }

    public FitnessComputer getFitnessComputer() {
        return fitnessComputer;
    }
}