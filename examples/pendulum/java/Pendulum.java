import se.klinghammer.neuralNetworkLibrary.Activation;
import se.klinghammer.neuralNetworkLibrary.Individual;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Pendulum extends JPanel {
    private static final double GRAVITY = 9.82 / 50;
    private static final int WIDTH = 400;
    private static final int HEIGHT = 600;

    private double length = 100;
    private double angle = Math.PI * 1.5;
    private double angularVelocity = 0;
    private double angularAcceleration = 0;

    private double damping = 0.998;

    private double cartX = WIDTH / 2.0;
    private double cartVelocity = 0;
    private double oldVelocity = 0;
    private final int FPS = 60;

    private final GameLoop gameLoop = new GameLoop(FPS, true, false);

    private boolean rendering = false;

    private Individual network;

    private Runnable taskOnFinish;

    private NetworkAdapter networkAdapter;

    private Propagater propagater;

    private int loopsToRun = 50 * 60;

    private double cartAcc = 0;

    public Pendulum(Individual sample, Runnable taskOnFinish) {
        network = sample;

        networkAdapter = new NetworkAdapter();
        propagater = new Propagater(sample, networkAdapter);

        this.taskOnFinish = taskOnFinish;
        runloops();
    }

    public Pendulum() {
        rendering = true;

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                System.out.println("Key Pressed: " + e.getKeyCode());
                if (e.getKeyCode() == KeyEvent.VK_LEFT) {
                    cartAcc = -1;
                } else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                    cartAcc = 1;
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_LEFT || e.getKeyCode() == KeyEvent.VK_RIGHT) {
                    cartAcc = 0;
                }
            }
        });


        setFocusable(true);
        requestFocusInWindow();

        gameLoop.addUpdateListener(this::update);
        gameLoop.addRenderListener(this::draw);

        gameLoop.start();
    }

    public Pendulum(Individual sample, boolean rendering) {
        this.rendering = rendering;

        network = sample;

        networkAdapter = new NetworkAdapter();
        propagater = new Propagater(sample, networkAdapter);

        setFocusable(true);

        gameLoop.addUpdateListener(this::update);

        if (rendering) {
            gameLoop.addRenderListener(this::draw);
            ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

            scheduler.schedule(gameLoop::start, 1, TimeUnit.SECONDS);

            scheduler.shutdown();
        } else {
            gameLoop.start();
        }

    }

    private void runloops() {
        double score = 0;
        for (int i = 0; i < loopsToRun; i++) {
            update();

            double height = Math.max(0, getBallPosY());
            double cartCentering = Math.max(0, 1 - Math.abs(getCartX()));

            double stability = Math.abs(angularVelocity);

            if (getBallPosY() > 0.8) {
                score += height * cartCentering;// - stability;
            }

        }
        network.setFitness(score / loopsToRun);
        taskOnFinish.run();
    }

    private void propagate() {
        //System.out.println(getCartX() + " " + getBallPosX() + " " + getBallPosY() + " " + getAngularVelocity());
        networkAdapter.setPos(getCartX());
        networkAdapter.setBallX(getBallPosX());
        networkAdapter.setBallY(getBallPosY());
        networkAdapter.setAngularVelocity(getAngularVelocity());

        propagater.propagate();
    }

    @Override
    protected void paintComponent(Graphics g) {

        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        //g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Calculate the position of the pendulum bob relative to the cart
        double bobX = cartX + length * Math.cos(angle);
        double bobY = HEIGHT - 350 - length * Math.sin(angle);

        // Draw the cart
        g2d.setColor(Color.BLUE);
        g2d.fillRect((int) (cartX - 20), HEIGHT - 350, 40, 20);

        // Draw the pendulum arm
        g2d.setStroke(new BasicStroke(2));
        g2d.setColor(Color.BLACK);
        g2d.drawLine((int) cartX, HEIGHT - 350, (int) bobX, (int) bobY);

        // Draw the pendulum bob
        g2d.setColor(Color.RED);
        g2d.fillOval((int) bobX - 10, (int) bobY - 10, 20, 20);
    }


    public void draw() {
        if (rendering) {
            requestFocusInWindow();
        }
        repaint();
    }

    public void setCartAcc(double newAcc) {
        cartAcc = newAcc * 10;
    }

    public double getCartX() {
        return (cartX * 2) / WIDTH - 1;
    }

    public double getAngularVelocity() {
        return Activation.Sigmoid.activate(angularVelocity);
    }

    public double getBallPosX() {
        return Math.cos(angle);
    }

    public double getBallPosY() {
        return Math.sin(angle);
    }

    public void update() {
        if (propagater != null) {
            propagate();
            setCartAcc(propagater.getCartAcc());
        }

        cartVelocity += cartAcc;
        cartX += cartVelocity;

        if (cartX <= 0) {
            cartX = 0;
            cartAcc -= 0;
            cartVelocity = 0;
        } else if (cartX >= WIDTH) {
            cartX = WIDTH;
            cartAcc = 0;
            cartVelocity = 0;
        }

        angularAcceleration = -GRAVITY / length * Math.cos(angle) + cartAcc / length * Math.sin(angle);

        angularVelocity += angularAcceleration;

        angularVelocity *= damping;
        angle += angularVelocity;

        cartVelocity *= damping;

        if (cartX <= 0 || cartX >= WIDTH) {
            cartAcc = 0;
        }
    }

    public static int getWIDTH() {
        return WIDTH;
    }


}
