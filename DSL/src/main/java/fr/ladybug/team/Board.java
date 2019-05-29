package fr.ladybug.team;

import javafx.application.Platform;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Main gamestate class */
public class Board {
    private final Cell[][] cells;
    private State state = State.GENERAL;
    private int selected_item = -1;
    private int n;
    private int left;
    private Runnable updater;
    private boolean inProgress = false;

    /**
     * Single constructor
     * @param n the size of the field
     * @param updater the callback of field changes
     */
    public Board(int n, Runnable updater) {
        if (n % 2 != 0 || n <= 0)
            throw new IllegalArgumentException("N must be positive even number");

        this.n = n;
        left = n * n;
        this.updater = updater;
        cells = new Cell[n][n];
    }

    /**
     * Whether non-pair cards are still shown or not
     * @return Whether non-pair cards are still shown or not
     */
    public boolean isInProgress() {
        synchronized (cells) {
            return inProgress;
        }
    }

    /**
     * The text representation of the cell's state
     * @param i the row's number of the cell
     * @param j the column's number of the cell
     * @return the cell's state's text representation
     */
    public String getText(int i, int j) {
        synchronized (cells) {
            return cells[i][j].hidden ? "?" : String.valueOf(cells[i][j].value);
        }
    }

    /** Fills the board cells randomly */
    public void fillCellsRandomly() {
        int numbers_count = n * n / 2;
        var numbers = new ArrayList<Integer>();
        for (int i = 0; i < numbers_count; i++) {
            numbers.add(i);
            numbers.add(i);
        }
        Collections.shuffle(numbers);
        fillCellsWithOrder(numbers);
    }

    /** Fills the board cells consistent with given order */
    public void fillCellsWithOrder(List<Integer> numbers) {
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                cells[i][j] = new Cell(numbers.get(i * n + j));
            }
        }
    }

    /**
     * Changes the state according to cell click action
     * @param i the row's number of the cell
     * @param j the column's number of the cell
     * @return whether click was legal or not
     */
    public boolean onClick(int i, int j) {
        var cell = cells[i][j];
        if (!cell.hidden)
            return false;

        if (state == State.GENERAL) {
            state = State.ONE_SELECTED;
            selected_item = i * n + j;
            cells[i][j].hidden = false;
        }
        else if (state == State.ONE_SELECTED) {
            var previously_selected = selected_item;
            if (cells[i][j].value == cells[previously_selected / n][previously_selected % n].value) {
                state = State.GENERAL;
                cells[i][j].hidden = false;
                cells[previously_selected / n][previously_selected % n].hidden = false;
                left -= 2;
            }
            else {
                state = State.GENERAL;
                cells[i][j].hidden = false;
                cells[previously_selected / n][previously_selected % n].hidden = false;
                inProgress = true;
                new Thread(() -> visibleHandler(i * n + j, previously_selected)).start();
            }
        }

        updater.run();
        return true;
    }

    /**
     * Delayed action performing when non-pair cards should be hide again
     * @param selected_item1 the position of the first card
     * @param selected_item2 the position of the second card
     */
    private void visibleHandler(int selected_item1, int selected_item2) {
        try {
            Thread.sleep(1500);
            synchronized (cells) {
                inProgress = false;
                cells[selected_item1 / n][selected_item1 % n].hidden = true;
                cells[selected_item2 / n][selected_item2 % n].hidden = true;
            }
            updater.run();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    /**
     * Whether the card is hidden or not
     * @param i the row index of the cell
     * @param j the column index of the cell
     * @return Whether the card is hidden or not
     */
    public boolean checkHidden(int i, int j) {
        return cells[i][j].hidden;
    }

    /** Whether all card are shown or not */
    public boolean hasWon() {
        return left == 0;
    }

    /** Internal class representing Cell of the field */
    public static class Cell {
        private boolean hidden = true;
        private int value;

        Cell(int value) {
            this.value = value;
        }
    }

    /** States of the game:
     *  GENERAL -- No selection performed
     *  ONE_SELECTED -- One of two cards is selected
     */
    public enum State {
        GENERAL,
        ONE_SELECTED,
    }
}