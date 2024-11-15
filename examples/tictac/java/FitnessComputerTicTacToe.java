import se.klinghammer.neuralNetworkLibrary.CustomThreadPool;
import se.klinghammer.neuralNetworkLibrary.FitnessComputer;
import se.klinghammer.neuralNetworkLibrary.Individual;
import se.klinghammer.neuralNetworkLibrary.Population;

import java.util.List;

public class FitnessComputerTicTacToe implements FitnessComputer {


    private boolean isStopped = true;

    public static CustomThreadPool customThreadPool;

    private Population population;

    private int done;

    private int total;

    public FitnessComputerTicTacToe(Population population) {
        this.population = population;
    }

    @Override
    public void start() {
        if (!isStopped) {
            return;
        }
        isStopped = false;
        if (customThreadPool != null) {
            customThreadPool.stop();
        }
        done = 0;
        customThreadPool = new CustomThreadPool(8);
        List<Individual> individuals = population.getIndividuals();

        total = population.getPopulationSize() * (population.getPopulationSize() - 1);

        for (Individual individual1 : individuals) {
            for (Individual individual2 : individuals) {
                if (individual1 != individual2) {
                    customThreadPool.submit(() -> {
                        try {
                            TicTacGame ticTacGame = new TicTacGame(false);

                            ticTacGame.setTaskOnFinish(this::incrementDone);

                            ticTacGame.setSamples(new Individual[]{individual1, individual2});

                            ticTacGame.run(0);

                        } catch (Exception e) {
                            System.err.println("Shit failed, not retrying, fuck you: " + e.getMessage());

                            for (StackTraceElement stackTraceElement : e.getStackTrace()) {
                                System.err.println("Line: " + stackTraceElement.getLineNumber() + " in " + stackTraceElement.getClassName());
                            }
                            System.out.println("he");
                        }
                    });
                }

            }
        }

    }

    private synchronized void incrementDone() {
        done++;
        if (done == total) {
            isStopped = true;


            double[] averaged = new double[population.getPopulationSize()];
            for (int i = 0; i < averaged.length; i++) {
                if (population.getIndividuals().get(i).getFitness() != 0.0) {
                    averaged[i] = population.getIndividuals().get(i).getFitness() / (((population.getPopulationSize() - 1) * population.getPopulationSize()) / 2.0);
                } else {
                    averaged[i] = 0;
                }
            }
            for (int i = 0; i < averaged.length; i++) {
                population.getIndividuals().get(i).setFitness(averaged[i]);
            }

            population.hasCalculatedFitness();
        }
    }

    public int getDone() {
        return done;
    }

    public void stop() {
        if (customThreadPool != null) {
            customThreadPool.stop();
        }
    }
}
