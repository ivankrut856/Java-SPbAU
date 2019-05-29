package fr.ladybug.team;

import javafx.application.Platform;

import java.util.ArrayList;
import java.util.Collections;

public class Board {
    private final Cell[][] cells;
    private State state = State.GENERAL;
    private int selected_item = -1;
    private int n;
    private int left;
    private Runnable updater;
    private boolean inProgress = false;

    public Board(int n, Runnable updater) {
        if (n % 2 != 0)
            throw new IllegalArgumentException("N must be even");

        this.n = n;
        left = n * n;
        this.updater = updater;
        cells = new Cell[n][n];
    }

    public boolean isInProgress() {
        synchronized (cells) {
            return inProgress;
        }
    }

    public String getText(int i, int j) {
        synchronized (cells) {
            return cells[i][j].hidden ? "?" : String.valueOf(cells[i][j].value);
        }
    }

    public void fillCellsRandomly() {
        int numbers_count = n * n / 2;
        var numbers = new ArrayList<Integer>();
        for (int i = 0; i < numbers_count; i++) {
            numbers.add(i);
            numbers.add(i);
        }
        Collections.shuffle(numbers);
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                cells[i][j] = new Cell(numbers.get(i * n + j));
            }
        }
    }

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

    private void visibleHandler(int selected_item1, int selected_item2) {
        try {
            Thread.sleep(1500);
            synchronized (cells) {
                inProgress = false;
                cells[selected_item1 / n][selected_item1 % n].hidden = true;
                cells[selected_item2 / n][selected_item2 % n].hidden = true;
            }
            Platform.runLater(updater);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    public boolean checkHidden(int i, int j) {
        return cells[i][j].hidden;
    }

    public boolean hasWon() {
        return left == 0;
    }

    public static class Cell {
        private boolean hidden = true;
        private int value;

        Cell(int value) {
            this.value = value;
        }
    }

    public enum State {
        GENERAL,
        ONE_SELECTED,
    }
}