package fr.ladybug.team.lightfuture;

import java.util.function.Function;

/**
 * Presents async tasks with result executing in thread pool
 * Instances of the interface can be submitted to the pool
 * @param <T> the type of the result
 */
public interface LightFuture<T> {
    /**
     * Whether the result of task execution was decided or not
     * @return true in case that task result has been decided, false otherwise
     */
    boolean isReady();

    /**
     * Lock until the result of the task is decided
     * @return the result value in case of successful finish
     * @throws LightExecutionException the LightExecutionException is thrown when there's some exception thrown
     * in task's body, one is suppressed inside LightExecutionException
     * @throws InterruptedException the InterruptedException is thrown when it's detected that executing thread was interrupted
     */
    T get() throws LightExecutionException, InterruptedException;

    /**
     * Constructs dependent tasks performing a function application to the result of current task
     * @param function the function to apply
     * @param <R> the result type of the function
     * @return new task performing the function application to the result of current task
     * @throws InterruptedException the exception is thrown when it's detected that executing thread was interrupted
     */
    <R> LightFuture<R> thenApply(Function<? super T, R> function) throws InterruptedException;

    /**
     * Signals to the task to consider itself successfully done with a result
     * @param result the result with which has been done
     */
    void done(T result);

    /**
     * Signals to the task to consider itself failed with an exception
     * @param thrownException the exception thrown during the task execution
     */
    void failed(Throwable thrownException);

    /**
     * Signal to the task to consider itself interrupted
     */
    void interrupted();
}
