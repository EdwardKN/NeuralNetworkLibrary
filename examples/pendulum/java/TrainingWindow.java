import se.klinghammer.neuralNetworkLibrary.Individual;
import se.klinghammer.neuralNetworkLibrary.Population;

import javax.swing.*;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class TrainingWindow extends JFrame {

    private GameLoop gameLoop = new GameLoop(5, true, false);

    private JLabel[] labels;

    private Population population;

    private double highestFitness = 0;

    private int genWithHighest = 0;

    private JProgressBar jProgressBar;

    public TrainingWindow() {
        Population.setConfigPath("examples/pendulum/resources/network");
        boolean render = false;

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
        setTitle("Träna!");

        if (render) {
            population = Population.importFromJson("examples/pendulum/resources/exported.json");
            Pendulum pendulum;

            if (population != null) {
                Individual best = population.getBestSamplesSorted(1)[0];
                System.out.println(best.getFitness());
                pendulum = new Pendulum(best, true);
            } else {
                pendulum = new Pendulum();
            }
            add(pendulum);
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setSize(440, 400);
            setVisible(true);
        } else {
            setLayout(null);
            setSize(200, 200);
            population = Population.importFromJson("examples/pendulum/resources/exported.json");

            if (population == null) {
                population = new Population(2000, 4, 1, "examples/pendulum/resources/exported.json", true);
            }

            population.setFitnessComputer(new FitnessComputerPendulum(population));

            population.run(100);
        }
        labels = new JLabel[6];

        for (int i = 0; i < labels.length; i++) {
            labels[i] = new JLabel("");
            labels[i].setBounds(10, 20 + i * 20, 250, 10);
        }

        for (JLabel label : labels) {
            add(label);
        }

        jProgressBar = new JProgressBar(0, 2000);

        jProgressBar.setBounds(20, labels.length * 20, 140, 10);

        add(jProgressBar);

        gameLoop.addUpdateListener(this::update);

        gameLoop.start();
    }

    public void update() {
        if (population != null) {
            labels[0].setText(String.valueOf(population.getGenerations()));
            labels[1].setText(String.valueOf(population.getSpeciesAmount()));
            if (population.getHighestFitness() != highestFitness) {
                labels[2].setText(round(highestFitness, 5) + " (" + genWithHighest + ")");
                labels[3].setText(round(population.getHighestFitness(), 5) + " (" + population.getGenerations() + ") ( +" + round(population.getHighestFitness() - highestFitness, 5) + ")");
                highestFitness = population.getHighestFitness();
                genWithHighest = population.getGenerations();
            }
            labels[4].setText(String.valueOf(round(population.getAverageFitness(), 5)));

            if (population.getFitnessComputer() != null) {
                jProgressBar.setValue(((FitnessComputerPendulum) population.getFitnessComputer()).getDone());
            } else {
                jProgressBar.setValue(0);
            }
        }

        repaint();
    }

    public static double round(double value, int decimalPlaces) {
        if (decimalPlaces < 0) throw new IllegalArgumentException("Decimal places must be non-negative");

        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(decimalPlaces, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}
