import se.klinghammer.neuralNetworkLibrary.CustomThreadPool;
import se.klinghammer.neuralNetworkLibrary.FitnessComputer;
import se.klinghammer.neuralNetworkLibrary.Individual;
import se.klinghammer.neuralNetworkLibrary.Population;

import java.util.List;

public class FitnessComputerPendulum implements FitnessComputer {


    private boolean isStopped = true;

    public static CustomThreadPool customThreadPool;

    private Population population;

    private int done;

    public FitnessComputerPendulum(Population population) {
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
        customThreadPool = new CustomThreadPool(20);
        List<Individual> individuals = population.getIndividuals();

        for (Individual individual : individuals) {
            customThreadPool.submit(() -> {
                try {
                    new Pendulum(individual, this::incrementDone);
                } catch (Exception e) {
                    System.err.println("Shit failed, not retrying, fuck you: " + e.getMessage());

                    for (StackTraceElement stackTraceElement : e.getStackTrace()) {
                        System.err.println("Line: " + stackTraceElement.getLineNumber() + " in " + stackTraceElement.getClassName());
                    }
                }
            });
        }
    }

    private synchronized void incrementDone() {
        done++;
        if (done == population.getPopulationSize()) {
            isStopped = true;
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
