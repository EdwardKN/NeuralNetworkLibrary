import se.klinghammer.neuralNetworkLibrary.Individual;
import se.klinghammer.neuralNetworkLibrary.Population;

import javax.swing.*;

public class TrainingWindow extends JFrame {

    private GameLoop gameLoop = new GameLoop(5, true, false);

    private JLabel[] labels;

    private Population population;

    private double highestFitness = 0;

    public TrainingWindow() {
        Population.setConfigPath("examples/pendulum/resources/network");
        boolean render = true;

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
            setSize(Pendulum.getWIDTH(), HEIGHT);
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setSize(420, 400);
            setVisible(true);
        } else {
            setSize(200, 200);

            population = new Population(10000, 4, 1, "examples/pendulum/resources/exported.json", true);

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
        gameLoop.addUpdateListener(this::update);

        gameLoop.start();
    }

    public void update() {
        if (population != null) {
            labels[0].setText(String.valueOf(population.getGenerations()));
            labels[1].setText(String.valueOf(population.getSpeciesAmount()));
            if (population.getHighestFitness() != highestFitness) {
                labels[2].setText(String.valueOf(highestFitness));
                labels[3].setText(String.valueOf(population.getHighestFitness()));
                highestFitness = population.getHighestFitness();
            }
            labels[4].setText(String.valueOf(population.getAverageFitness()));

        }

        repaint();
    }
}
