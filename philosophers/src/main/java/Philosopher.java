import java.util.Random;
import java.util.function.Function;

public class Philosopher implements Runnable {
    private Random rnd;

    private static int numbers = 0;
    private int number;
    private int times = 0;

    private final Object rightFork;
    private final Object leftFork;

//    static Function<Integer, Data> strategy;

    // Левак?!?
    private boolean isLeftist = false;

    public Philosopher(Object rightFork, Object leftFork) {
        this.rightFork = rightFork;
        this.leftFork = leftFork;
        number = numbers;
        numbers++;
        rnd = new Random();
    }

    public Philosopher(Object rightFork, Object leftFork, Random rnd) {
        this.rightFork = rightFork;
        this.leftFork = leftFork;
        number = numbers;
        numbers++;
        this.rnd = rnd;
    }

    public void run() {
        while (true) {
            eat();
            try {
                Thread.sleep(10 + rnd.nextInt(20));
            } catch (InterruptedException ignore) {
                break;
            }
        }
    }

    public void eat() {
        if (isLeftist) {
            synchronized (leftFork) {
                synchronized (rightFork) {
                    times++;
                    int time = rnd.nextInt(20) + 10;
                    System.out.println("Ima gonna eat for " + ((double) time / 1000) + " seconds");
                    System.out.println("My number is " + number);
                    System.out.println("This is my " + times + "!");
                }
            }
        }
        else {
            synchronized (rightFork) {
                synchronized (leftFork) {
                    times++;
                    int time = rnd.nextInt(20) + 10;
                    System.out.println("Ima gonna eat for " + ((double) time / 1000) + " seconds");
                    System.out.println("My number is " + number);
                    System.out.println("This is my " + times + "!");
                }
            }
        }
    }

    public void setLeftist(boolean leftist) {
        isLeftist = leftist;
    }

    public int getTimes() {
        return times;
    }
}
