package fr.ladybug.team;

import fr.ladybug.team.lightfuture.LightFuture;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Objects;
import java.util.function.Supplier;


/** Executor service for async tasks LightFuture */
public class ThreadPool {
    /** All threads of the pool */
    private ArrayList<Thread> threads;
    /** All tasks which has not been started yet */
    private final ArrayDeque<ThreadTask<?>> tasks;

    /** Whether the pool is shutdown or not */
    private boolean isShutdown = false;

    /** Constructs the pool with {@code threadCount} number of threads and starts task execution */
    public ThreadPool(int threadCount) {
        threads = new ArrayList<>(threadCount);
        tasks = new ArrayDeque<>();

        for (int i = 0; i < threadCount; i++) {
            threads.add(new Thread(this::threadWorker));
            threads.get(i).start();
        }
    }

    /**
     * Adds new task in ThreadTask form
     * @param supplier the supplier of the task
     */
    public void submitTask(ThreadTask<?> supplier) {
        synchronized (tasks) {
            if (isShutdown) {
                throw new IllegalStateException("The pool is shutdown");
            }
            tasks.addLast(supplier);
            tasks.notifyAll();
        }
    }

    /**
     * Shutdowns the pool. All threads and tasks are to be interrupted
     * @throws InterruptedException In case of thread interruption the exception will be thrown
     */
    public void shutdown() throws InterruptedException {
        synchronized (tasks) {
            isShutdown = true;
            for (var thread : threads) {
                thread.interrupt();
            }
            for (var thread : threads) {
                thread.join();
            }
        }
    }

    /** Main thread loop */
    private void threadWorker() {
        ThreadTask<?> task = null;
        try {
            while (!Thread.currentThread().isInterrupted()) {
                synchronized (tasks) {
                    while (tasks.size() == 0) {
                        tasks.wait();
                    }
                    task = tasks.pollFirst();
                    Objects.requireNonNull(task);
                }

                if (Thread.currentThread().isInterrupted()) {
                    task.charterer.interrupt();
                }
                task.perform();
            }
            synchronized (tasks) {
                while (tasks.size() != 0) {
                    task = tasks.pollFirst();
                    Objects.requireNonNull(task);
                    task.charterer.interrupt();
                }
            }
        }
        catch (Throwable e) {
            if (task != null) {
                task.charterer.fail(e);
            }
        }
    }

    /**
     * Represents thread pool tasks' interface between async tasks and supplier form tasks
     * @param <R> the result type of the task
     */
    public static class ThreadTask<R> {
        /** Action of the task itself */
        private Supplier<R> action;
        /** Async task corresponding to this supplier form task */
        private LightFuture<R> charterer;
        /** Simple wrapper of async tasks */
        public ThreadTask(Supplier<R> action, LightFuture<R> charterer) {
            this.action = action;
            this.charterer = charterer;
        }
        /** Calculates action and puts it into charterer's result */
        public void perform() {
            R result = action.get();
            charterer.finish(result);
        }
    }
}
