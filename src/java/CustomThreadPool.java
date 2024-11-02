
import java.util.LinkedList;
import java.util.Queue;

public class CustomThreadPool {
    private final WorkerThread[] threads;
    private final Queue<Runnable> taskQueue = new LinkedList<>();

    public CustomThreadPool(int poolSize) {
        this.threads = new WorkerThread[poolSize];

        // Initialize and start worker threads
        for (int i = 0; i < poolSize; i++) {
            threads[i] = new WorkerThread();
            threads[i].start();
        }
    }

    // Method to submit a task to the pool
    public synchronized void submit(Runnable task) {
        taskQueue.add(task);
        notify();  // Notify a waiting thread that a new task is available
    }

    // WorkerThread class that processes tasks from the queue
    private class WorkerThread extends Thread {
        private volatile boolean running = true;

        public void shutdown() {
            running = false;  // Set the running flag to false
            this.interrupt();  // Interrupt the thread in case it's waiting
        }

        @Override
        public void run() {
            while (running) {
                Runnable task;
                synchronized (CustomThreadPool.this) {
                    while (taskQueue.isEmpty() && running) {  // Only wait if the pool is running
                        try {
                            CustomThreadPool.this.wait();
                        } catch (InterruptedException e) {
                            // Allow interruption to exit the thread
                            if (!running) {
                                return;  // Exit the thread if it's no longer running
                            }
                            Thread.currentThread().interrupt();  // Preserve the interrupt status
                        }
                    }
                    if (!running) {
                        return;  // Exit if shutdown is called
                    }
                    task = taskQueue.poll();
                }
                if (task != null) {
                    task.run();  // Execute the task
                }
            }
        }
    }

    public synchronized void stop() {
        for (WorkerThread thread : threads) {
            thread.shutdown();
        }

        taskQueue.clear();

        notifyAll();
    }


}
