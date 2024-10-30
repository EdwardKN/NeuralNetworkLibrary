package se.test.neuralNetwork.src.visual;

import se.test.neuralNetwork.src.Genome;
import se.test.neuralNetwork.src.LinkGene;
import se.test.neuralNetwork.src.NeuronGene;

import java.awt.*;
import java.util.List;
import java.util.Queue;
import java.util.*;

public class NetworkDraw implements Content {
    final Window window = new Window(this, "Neural Network", true);

    private final int nodeSize = 10;

    private final Map<Integer, PositionedNode> positionedNodes = new HashMap<>();

    private Genome genome;

    public NetworkDraw(Genome genome) {
        this.genome = genome;

        init();
    }

    public void init() {
        precalculate();

        window.startGameLoop();
    }

    @Override
    public void update() {

    }

    @Override
    public void render() {



        window.getGraphics2D().setColor(Color.WHITE);

        window.getGraphics2D().fillRect(0, 0, 1000, 1000);

        for (PositionedNode value : positionedNodes.values()) {
            window.getGraphics2D().setColor(value.getType() == 0 ? Color.red.darker() : value.getType() == 1 ? Color.GREEN.darker() : Color.blue.darker());

            window.getGraphics2D().fillArc(value.getX() + nodeSize / 2, value.getY(), nodeSize, nodeSize, 0, 360);

        }

        for (LinkGene link : genome.getLinks()) {
            PositionedNode input = positionedNodes.get(link.getInputId());
            PositionedNode output = positionedNodes.get(link.getOutputId());

            int inputRealX = (input.getX() + nodeSize / 2);
            int inputRealY = (input.getY() + nodeSize / 2);
            int outputRealX = (output.getX() + nodeSize / 2);
            int outputRealY = (output.getY() + nodeSize / 2);


            window.getGraphics2D().setColor(input.getType() == 0 ? Color.red.darker() : input.getType() == 1 ? Color.GREEN.darker() : Color.blue.darker());


            drawLine(window.getGraphics2D(), inputRealX, inputRealY, outputRealX, outputRealY, nodeSize / 2);

            drawArrowhead(window.getGraphics2D(), inputRealX, inputRealY, outputRealX, outputRealY, nodeSize / 2);

        }

    }

    @Override
    public void close() {

    }

    public void precalculate() {


        List<NeuronGene> neurons = genome.getNeurons();

        List<NeuronGene> inputs = new ArrayList<>();

        for (int i = 0; i < genome.getAmountOfInputs(); i++) {
            inputs.add(neurons.get(i));
        }

        List<NeuronGene> outputs = new ArrayList<>();

        for (int i = genome.getAmountOfInputs(); i < genome.getAmountOfInputs() + genome.getAmountOfOutputs(); i++) {
            outputs.add(neurons.get(i));
        }

        Map<NeuronGene, Integer> inputDistances = calculateMinimumDistances(inputs);
        Map<NeuronGene, Integer> outputDistances = calculateMinimumDistances(outputs);
        outputDistances.replaceAll((key, value) -> value * -2);

        Map<NeuronGene, Integer> distances = calculateAverage(inputDistances, outputDistances);

        window.getGraphics2D().setColor(Color.BLACK);

        Optional<Integer> lowest = distances.values()
                .stream()
                .min(Integer::compare);

        int differentDistances = new HashSet<>(distances.values()).size();

        List<Integer> doneLevels = new ArrayList<>();
        for (Map.Entry<NeuronGene, Integer> entry : distances.entrySet()) {
            int distance = entry.getValue();

            if (!doneLevels.contains(distance)) {
                doneLevels.add(distance);

                List<NeuronGene> sameDistanceNeurons = getNeuronGenesWithSameDistance(distances, distance);
                for (int i = 0; i < sameDistanceNeurons.size(); i++) {
                    int adjustedDistance = Math.min(distance, Integer.MAX_VALUE / (window.getPanelWidth() - nodeSize * 2));
                    int x = (int) (((adjustedDistance - lowest.get()) / (double) (differentDistances)) * (window.getPanelWidth() - nodeSize * 2));
                    int y = (int) ((i + 0.5) / (double) sameDistanceNeurons.size() * (window.getPanelHeight() - nodeSize * 2));
                    positionedNodes.put(sameDistanceNeurons.get(i).getId(), new PositionedNode(x, y, inputs.contains(sameDistanceNeurons.get(i)) ? 0 : (outputs.contains(sameDistanceNeurons.get(i)) ? 2 : 1)));
                }
            }
        }


    }

    public static Map<NeuronGene, Integer> calculateAverage(Map<NeuronGene, Integer> map1, Map<NeuronGene, Integer> map2) {
        Map<NeuronGene, Integer> averageMap = new HashMap<>();

        for (NeuronGene key : map1.keySet()) {
            if (map2.containsKey(key)) {
                int value1 = map1.get(key);
                int value2 = map2.get(key);

                double average = (value1 + value2) / 2.0;
                averageMap.put(key, (int) Math.ceil(average));
            }
        }

        return averageMap;
    }

    private void drawLine(Graphics2D g2d, int x1, int y1, int x2, int y2, int offset) {
        double angle = Math.atan2(y2 - y1, x2 - x1);

        double baseX = x1 + offset * Math.cos(angle);
        double baseY = y1 + offset * Math.sin(angle);

        double endX = x2 - offset * Math.cos(angle);
        double endY = y2 - offset * Math.sin(angle);

        g2d.drawLine((int) baseX, (int) baseY, (int) endX, (int) endY);


    }

    private void drawArrowhead(Graphics2D g2d, int x1, int y1, int x2, int y2, int offset) {
        double angle = Math.atan2(y2 - y1, x2 - x1);

        double baseX = x2 - offset * Math.cos(angle);
        double baseY = y2 - offset * Math.sin(angle);

        int arrowSize = 10;

        int x3 = (int) (baseX - arrowSize * Math.cos(angle - Math.PI / 6));
        int y3 = (int) (baseY - arrowSize * Math.sin(angle - Math.PI / 6));
        int x4 = (int) (baseX - arrowSize * Math.cos(angle + Math.PI / 6));
        int y4 = (int) (baseY - arrowSize * Math.sin(angle + Math.PI / 6));

        Polygon arrowhead = new Polygon();
        arrowhead.addPoint((int) baseX, (int) baseY);
        arrowhead.addPoint(x3, y3);
        arrowhead.addPoint(x4, y4);

        g2d.fillPolygon(arrowhead);
    }

    public List<NeuronGene> getNeuronGenesWithSameDistance(Map<NeuronGene, Integer> map, int targetValue) {
        List<NeuronGene> matchingNeuronGenes = new ArrayList<>();

        for (Map.Entry<NeuronGene, Integer> entry : map.entrySet()) {
            if (entry.getValue() == targetValue) {
                matchingNeuronGenes.add(entry.getKey());
            }
        }

        return matchingNeuronGenes;
    }

    public Map<NeuronGene, Integer> calculateMinimumDistances(List<NeuronGene> targetNodes) {
        Map<NeuronGene, Integer> distances = new HashMap<>();
        Queue<NeuronGene> queue = new LinkedList<>();
        Set<NeuronGene> visited = new HashSet<>();

        for (NeuronGene node : genome.getNeurons()) {
            distances.put(node, Integer.MAX_VALUE);
        }

        for (NeuronGene targetNode : targetNodes) {
            distances.put(targetNode, 0);
            queue.add(targetNode);
            visited.add(targetNode);
        }

        while (!queue.isEmpty()) {
            NeuronGene current = queue.poll();
            int currentDistance = distances.get(current);

            for (LinkGene link : genome.getLinks()) {
                NeuronGene neighbor = null;
                if (genome.getNeuronFromId(link.getInputId()) == current) {
                    neighbor = genome.getNeuronFromId(link.getOutputId());
                } else if (genome.getNeuronFromId(link.getOutputId()) == current) {
                    neighbor = genome.getNeuronFromId(link.getInputId());
                }

                if (neighbor != null) {
                    int newDistance = currentDistance + 1;
                    if (newDistance < distances.get(neighbor)) {
                        distances.put(neighbor, newDistance);
                        queue.add(neighbor);
                    }
                }
            }
        }

        return distances;
    }


}