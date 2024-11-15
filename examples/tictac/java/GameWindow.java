import se.klinghammer.neuralNetworkLibrary.Individual;
import se.klinghammer.neuralNetworkLibrary.Population;

import javax.swing.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class GameWindow extends JFrame {

    private GameLoop gameLoop = new GameLoop(1, true, false);
    private TicTacGame ticTacGame;

    private JLabel[] labels;

    private Population population;

    private double highestFitness = 0;

    private int genWithHighest = 0;

    private JProgressBar jProgressBar;

    public GameWindow() {
        Population.setConfigPath("examples/pendulum/resources/network");

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("Tic Tac Toe");

        boolean rendering = false;

        int players = 1;
        if (rendering) {

            population = Population.importFromJson("examples/tictac/resources/exported.json");
            ticTacGame = new TicTacGame(true);

            if (players == 0) {
                for (Individual individual : population.getBestSamplesSorted(2)) {
                    individual.getNetwork().createInputPaths();
                    individual.getNetwork().propagate(new double[individual.getNetwork().getAmountOfInputs()]);
                }
                ticTacGame.setSamples(population.getBestSamplesSorted(2));
            } else if (players == 1) {
                population.getBestSamplesSorted(1)[0].getNetwork().createInputPaths();
                population.getBestSamplesSorted(1)[0].getNetwork().propagate(new double[population.getBestSamplesSorted(1)[0].getNetwork().getAmountOfInputs()]);
                ticTacGame.setSample(population.getBestSamplesSorted(1)[0]);
            }

            if (players != 2) {
                ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

                scheduler.schedule(() -> {
                    ticTacGame.run(500);
                }, 1, TimeUnit.SECONDS);

                scheduler.shutdown();
            }


            add(ticTacGame);
            setSize((ticTacGame.getPreferredSize().width), (ticTacGame.getPreferredSize().height) + 40);

            setLayout(null);
            setVisible(true);
            //pack();

            gameLoop.addRenderListener(this::render);
        } else {
            setLayout(null);
            setSize(200, 210);
            setVisible(true);
            population = Population.importFromJson("examples/tictac/resources/exported.json");

            if (population == null) {
                population = new Population(250, NetworkAdapter.getInputAmount(), Propagater.getOutputAmount(), "examples/tictac/resources/exported.json", true);
            }

            population.setFitnessComputer(new FitnessComputerTicTacToe(population));

            population.run(250);

            labels = new JLabel[6];

            for (int i = 0; i < labels.length; i++) {
                labels[i] = new JLabel("");
                labels[i].setBounds(10, 20 + i * 20, 250, 10);
            }

            for (JLabel label : labels) {
                add(label);
            }

            jProgressBar = new JProgressBar(0, (population.getPopulationSize() * (population.getPopulationSize() - 1)));

            jProgressBar.setBounds(20, labels.length * 20, 140, 10);

            add(jProgressBar);

            JButton stop = new JButton("Stopp");
            stop.setBounds(20, (labels.length + 1) * 20, 140, 20);
            add(stop);

            stop.addActionListener((e) -> {
                population.exportToJson();
            });

            gameLoop.addUpdateListener(this::update);

        }
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
                jProgressBar.setValue(((FitnessComputerTicTacToe) population.getFitnessComputer()).getDone());
            } else {
                jProgressBar.setValue(0);
            }
        }

        repaint();
    }

    public void render() {
        ticTacGame.repaint();
    }

    public static double round(double value, int decimalPlaces) {
        if (decimalPlaces < 0) throw new IllegalArgumentException("Decimal places must be non-negative");

        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(decimalPlaces, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}
