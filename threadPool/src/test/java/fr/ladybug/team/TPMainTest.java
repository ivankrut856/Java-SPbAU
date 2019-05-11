package fr.ladybug.team;

import fr.ladybug.team.lightfuture.FreeFuture;
import fr.ladybug.team.lightfuture.LightExecutionException;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Random;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

class TPMainTest {

    private final int n = 100;
    private final int[] sharedMemory = new int[n];

    @Test
    void testSingleThreadExecutorBasic() throws LightExecutionException, InterruptedException {
        var pool = new ThreadPool(1);
        var task = FreeFuture.createTask(() -> {
            return "A";
        }, pool);
        assertEquals("A", task.get());
    }

    @Test
    void testTwiceSubmission() throws LightExecutionException, InterruptedException {
        var pool = new ThreadPool(1);
        var task = FreeFuture.createTask(() -> {
            return "A";
        }, pool);
        assertThrows(IllegalStateException.class, task::submit);
        assertEquals("A", task.get());
    }

    @Test
    void testSeveralThreadsComrades() throws LightExecutionException, InterruptedException {
        var pool = new ThreadPool(3);
        sharedMemory[0] = 0;
        var tasks = new ArrayList<FreeFuture<?>>(100);
        for (int i = 0; i < 100; i++) {
            tasks.add(FreeFuture.createDelayedTask(() -> {
                for (int j = 0; j < 1000; j++) {
                    synchronized (sharedMemory) {
                        sharedMemory[0]++;
                    }
                }
                return "";
            }, pool));
        }
        for (int i = 0; i < 100; i++) {
            tasks.get(i).submit();
        }
        for (int i = 0; i < 100; i++) {
            tasks.get(i).get();
        }
        assertEquals(100000, sharedMemory[0]);
    }

    @Test
    void testNoTaskShutdown() {
        var pool = new ThreadPool(10);
        assertDoesNotThrow(pool::shutdown);
    }

    @Test
    void testAllNThreadsPresented() throws InterruptedException {
        var pool = new ThreadPool(n);
        ArrayList<FreeFuture<String>> tasks = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            sharedMemory[i] = 0;
            int finalI = i;
            tasks.add(FreeFuture.createTask(() -> {
                sharedMemory[finalI] = 1;
                while (!Thread.currentThread().isInterrupted()) {
                    ;
                }
                return "";
            }, pool));
        }

        for (int i = 0; i < n; i++) {
            while (sharedMemory[i] != 1) {
                Thread.sleep(100);
            }
        }

        pool.shutdown();

    }

    @Test
    void testOneThenNext100() throws LightExecutionException, InterruptedException {
        var threadPool = new ThreadPool(10);
        var task = FreeFuture.createDelayedTask(() -> {
            return 0;
        }, threadPool);
        var firstTask = task;
        for (int i = 1; i < 100; i++) {
            task = task.thenApply((j) -> {
                return j + 1;
            });
        }

        firstTask.submit();
        assertEquals(99, (int)task.get());
    }

    @Test
    void testRuntimeErrorSupplier() {
        var pool = new ThreadPool(1);
        try {
            FreeFuture.createTask(() -> {
                throw new RuntimeException("Oops!");
            }, pool).get();
            fail("No throw");
        }
        catch (LightExecutionException e) {
            assertEquals(1, e.getSuppressed().length);
            assertEquals("Oops!", e.getSuppressed()[0].getMessage());
        }
        catch (Throwable e) {
            fail("Wrong throw");
        }
    }

    @Test
    void testSubmitAfterShutdown() throws LightExecutionException, InterruptedException {
        var pool = new ThreadPool(1);
        pool.shutdown();
        assertThrows(IllegalStateException.class, () -> FreeFuture.createTask(() -> {
            return 1;
        }, pool).get());
    }

}