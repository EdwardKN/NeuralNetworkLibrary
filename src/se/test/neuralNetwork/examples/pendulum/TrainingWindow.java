package se.test.neuralNetwork.examples.pendulum;

import se.test.neuralNetwork.src.Individual;
import se.test.neuralNetwork.src.Population;

import javax.swing.*;

public class TrainingWindow extends JFrame{

    private GameLoop gameLoop = new GameLoop(5,true,false);

    private JLabel[] labels;

    private Population population;
    public TrainingWindow() {
        boolean render = false;

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
        setTitle("Träna!");

        if(render){
            population = Population.importFromJson("test.json");
            Pendulum pendulum;
            if(population != null){
                Individual best = population.getBestSamplesSorted(1)[0];
                System.out.println(best.getFitness());
                pendulum = new Pendulum(best, true);

            }else {
                pendulum = new Pendulum();
            }
            add(pendulum);
            setSize(Pendulum.getWIDTH(), HEIGHT);
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setSize(1280, 720);
            setVisible(true);
        }else{
            setSize(200, 200);

            population = new Population(10000,4,1,"test.json",true);

            population.setFitnessComputer(new FitnessComputerPendulum(population));

            population.run(150);
        }



        labels = new JLabel[]{new JLabel("")};

        labels[0].setBounds(100,100,100,100);


        for (JLabel label : labels) {
            add(label);
        }
        gameLoop.addUpdateListener(this::update);

        gameLoop.start();
    }
    public void update(){
        if(population != null){
            labels[0].setText(String.valueOf(population.getGenerations()));
        }

        repaint();
    }
}
