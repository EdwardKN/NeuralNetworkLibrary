package se.test.neuralNetwork.examples.pendulum;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GameLoop {
    private static final Logger LOGGER = Logger.getLogger(GameLoop.class.getName());

    private boolean running;
    private final double NANOSECONDS_PER_UPDATE;
    private Thread gameThread;
    private final List<UpdateListener> updateListeners;
    private final List<RenderListener> renderListeners;
    private final boolean vSync;
    private final double NANOSECONDS_PER_FRAME_UPDATE;

    private final boolean canRenderUnprocessedFrames;

    public GameLoop(double updatesPerSecond, boolean vSync, boolean canRenderUnprocessedFrames) {
        // Time between updates in seconds
        double UPDATE_INTERVAL = 1.0 / updatesPerSecond;
        NANOSECONDS_PER_UPDATE = UPDATE_INTERVAL * 1_000_000_000;

        double FRAME_UPDATE_INTERVAL = 1.0 / new ScreenRefreshRateDetector().getScreenRefreshRate();
        NANOSECONDS_PER_FRAME_UPDATE = FRAME_UPDATE_INTERVAL * 1_000_000_000;

        updateListeners = new ArrayList<>();
        renderListeners = new ArrayList<>();
        this.vSync = vSync;
        this.canRenderUnprocessedFrames = canRenderUnprocessedFrames;
    }

    public void addUpdateListener(UpdateListener listener) {
        updateListeners.add(listener);
    }

    public void addRenderListener(RenderListener listener) {
        renderListeners.add(listener);
    }


    public void start() {
        if (running) return;

        running = true;
        gameThread = new Thread(this::runLoop);
        gameThread.start();
    }

    public void stop() {
        running = false;
        try {
            gameThread.join();
        } catch (InterruptedException e) {
            LOGGER.log(Level.SEVERE, "Failed to stop the game thread cleanly.");
        }
    }

    private void runLoop() {
        long lastTime = System.nanoTime();
        double unprocessedTime = 0;

        long lastRenderTime = System.nanoTime();
        double unprocessedRenderTime = 0;
        while (running) {
            long currentTime = System.nanoTime();
            long elapsedTime = currentTime - lastTime;
            lastTime = currentTime;
            unprocessedTime += elapsedTime;

            while (unprocessedTime >= NANOSECONDS_PER_UPDATE) {
                update();
                unprocessedTime -= NANOSECONDS_PER_UPDATE;
            }

            if (!vSync) {
                render();
            } else {
                long currentRenderTime = System.nanoTime();
                long elapsedRenderTime = currentRenderTime - lastRenderTime;
                lastRenderTime = currentRenderTime;
                unprocessedRenderTime += elapsedRenderTime;

                while (unprocessedRenderTime >= NANOSECONDS_PER_FRAME_UPDATE) {
                    render();
                    if (canRenderUnprocessedFrames) {
                        unprocessedRenderTime -= NANOSECONDS_PER_FRAME_UPDATE;
                    } else {
                        unprocessedRenderTime = 0;
                    }
                }
            }
        }
    }

    private void update() {
        for (UpdateListener listener : updateListeners) {
            listener.update();
        }
    }

    private void render() {
        for (RenderListener listener : renderListeners) {
            listener.render();
        }
    }


    public interface UpdateListener {
        void update();
    }

    public interface RenderListener {
        void render();
    }
}
