package fr.ladybug.team.lightfuture;

import fr.ladybug.team.ThreadPool;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

/** {@inheritDoc} */
public class FreeFuture<R> implements LightFuture<R> {
    /** Status of the task {@see #TaskStatus} */
    private TaskStatus status;

    /** null if no exception was thrown during execution, otherwise it's one which was */
    private Throwable thrownException;
    /** Task result */
    private R result;

    /** Main essence of the task */
    private Supplier<R> action;
    /** Thread pool in which all tasks are to be executed */
    private ThreadPool executor;
    /** List of depended tasks */
    private List<FreeFuture<?>> applicableTasks;

    /**
     * Creates and submits tasks with the action to execute in the executor thread pool
     * @param action the action which is task main essence
     * @param executor the executor in which the task is to be executed
     * @param <R> the result type of the task
     * @return the task object
     */
    public static <R> FreeFuture<R> createTask(Supplier<R> action, ThreadPool executor) {
        var task = new FreeFuture<R>(action, executor);
        task.submit();
        return task;
    }

    /**
     * Creates not submitted tasks with the action to execute in the executor thread pool
     * @param action the action which is task main essence
     * @param executor the executor in which the task is to be executed
     * @param <R> the result type of the task
     * @return the task object
     */
    public static <R> FreeFuture<R> createDelayedTask(Supplier<R> action, ThreadPool executor) {
        return new FreeFuture<>(action, executor);
    }

    /**
     * Submits the task to executor thread pool
     * @throws IllegalStateException the IllegalStateException is to be thrown when trying to submit once more after submission
     */
    public synchronized void submit() {
        if (status != TaskStatus.NOT_STARTED)
            throw new IllegalStateException("Task has already been submitted");
        status = TaskStatus.SUBMITTED;
        executor.submitTask(new ThreadPool.ThreadTask<>(action, this));
    }

    /**
     *  Simple constructor initiating delayed task with the supplier as an action
      * @param action the supplier which is to be execution
     * @param executor the thread pool in which supplier is to be executed
     */
    private FreeFuture(Supplier<R> action, ThreadPool executor) {
        status = TaskStatus.NOT_STARTED;
        this.action = action;
        this.executor = executor;
        applicableTasks = new ArrayList<>();
    }

    @Override
    public synchronized boolean isReady() {
        switch (status) {
            case NOT_STARTED:
            case SUBMITTED:
                return false;
            default:
                return true;
        }
    }

    @Override
    public synchronized R get() throws LightExecutionException, InterruptedException {
        while (status == TaskStatus.SUBMITTED || status == TaskStatus.NOT_STARTED) {
            this.wait();
        }

        if (status == TaskStatus.FAILED) {
            var resultException = new LightExecutionException();
            resultException.addSuppressed(thrownException);
            throw resultException;
        }

        if (status == TaskStatus.INTERRUPTED) {
            throw new InterruptedException("The thread performing the task was interrupted");
        }

        return result;
    }

    /**
     * Puts function form task as dependency to the list of dependencies. It will be started after this task
     * @param function the function to apply
     * @param <S> the result type of the function
     * @return New task corresponding to the function
     */
    private <S> FreeFuture<S> setUpApplicableTask(Function<? super R, S> function) {
        return createDelayedTask(() -> {
            R dependency;
            try {
                dependency = this.get();
            }
            catch (LightExecutionException | InterruptedException e) {
                throw new IllegalStateException("Dependency computation was not finished successfully");
            }

            return function.apply(dependency);
        }, executor);
    }

    @Override
    public synchronized <S> FreeFuture<S> thenApply(Function<? super R, S> function) throws IllegalStateException {
        var task = setUpApplicableTask(function);
        if (status == TaskStatus.DONE) {
            task.submit();
        }
        else {
            applicableTasks.add(task);
        }

        return task;
    }

    @Override
    public synchronized void finish(R result) {
        status = TaskStatus.DONE;
        this.result = result;
        this.notifyAll();
        for (var task : applicableTasks) {
            task.submit();
        }
    }

    @Override
    public synchronized void fail(Throwable thrownException) {
        status = TaskStatus.FAILED;
        this.thrownException = thrownException;
        this.notifyAll();
        for (var task : applicableTasks) {
            task.fail(thrownException);
        }
    }

    @Override
    public synchronized void interrupt() {
        status = TaskStatus.INTERRUPTED;
        this.notifyAll();
        for (var task : applicableTasks) {
            task.interrupt();
        }
    }

    /**
     * Represents a status of the task. Possible values:
     * NOT_STARTED -- when the task has not yet been submitted to thread pool
     * SUBMITTED -- the task has been submitted, but decision about result has not been made
     * DONE -- the task successfully done with a result
     * FAILED -- the task failed with an exception
     * INTERRUPTED -- the task has been interrupted
     */
    private enum TaskStatus {
        NOT_STARTED, SUBMITTED, DONE, FAILED, INTERRUPTED;
    }
}
