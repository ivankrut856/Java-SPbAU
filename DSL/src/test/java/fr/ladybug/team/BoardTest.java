package fr.ladybug.team;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static org.junit.jupiter.api.Assertions.*;

class BoardTest {
    private Board board;
    private int baseTestSize = 4;

    @BeforeEach
    public void boardInit() {
        assertDoesNotThrow(() -> board = new Board(baseTestSize, () -> {}));
        var numbers = new ArrayList<Integer>();
        for (int i = 0; i < baseTestSize * baseTestSize / 2; i++) {
            numbers.add(i);
            numbers.add(i);
        }
        board.fillCellsWithOrder(numbers);
    }

    @Test
    public void testInit() {
    }

    @Test
    public void testTheSame() {
        board.onClick(0, 0);
        board.onClick(0, 1);
        assertFalse(board.checkHidden(0, 0));
        assertFalse(board.checkHidden(0, 1));
    }

    @Test
    public void testDifferentOnes() {
        Lock lock = new ReentrantLock();
        Condition condition = lock.newCondition();
        board = new Board(baseTestSize, () -> {
            lock.lock();
            condition.signalAll();
            lock.unlock();
        });
        var numbers = new ArrayList<Integer>();
        for (int i = 0; i < baseTestSize * baseTestSize / 2; i++) {
            numbers.add(i);
            numbers.add(i);
        }
        board.fillCellsWithOrder(numbers);

        lock.lock();
        board.onClick(0, 0);
        board.onClick(0, 2);
        condition.awaitUninterruptibly();
        assertTrue(board.checkHidden(0, 0));
        assertTrue(board.checkHidden(0, 2));
        lock.unlock();
    }

    @Test
    void testWin() {
        for (int i = 0; i < baseTestSize * baseTestSize; i++) {
            board.onClick(i / baseTestSize, i % baseTestSize);
        }
        assertTrue(board.hasWon());
    }

    @Test
    void tesBoardSizeArguments() {
        assertThrows(IllegalArgumentException.class, () -> new Board(0, null));
        assertThrows(IllegalArgumentException.class, () -> new Board(-1, null));
        assertThrows(IllegalArgumentException.class, () -> new Board(1, null));
    }
}