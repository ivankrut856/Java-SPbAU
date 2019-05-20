package fr.ladybug.team;


import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;


public class Cell {
    private PresenceStatus presenceStatus;
    private int x;
    private int y;
    private CellView view;

    private List<Consumer<Cell>> onDestroyListeners = new ArrayList<>();

    public Cell(int x, int y, PresenceStatus presenceStatus, CellView view) {
        this.x = x;
        this.y = y;
        this.presenceStatus = presenceStatus;
        this.view = view;
        getView().chooseTexture(presenceStatus.value);
    }

    public PresenceStatus getPresenceStatus() {
        return presenceStatus;
    }

    public void setPresenceStatus(PresenceStatus status) {
        presenceStatus = status;
        getView().chooseTexture(presenceStatus.value);
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public CellView getView() {
        return view;
    }

    public void update() {
    }

    public void addOnDestroyListener(Consumer<Cell> listener) {
        onDestroyListeners.add(listener);
    }

    public void destroy() {
        presenceStatus = PresenceStatus.EMPTY;
        getView().chooseTexture(presenceStatus.value);
        for (var listener : onDestroyListeners) {
            listener.accept(this);
        }
    }

    public enum PresenceStatus {
        EMPTY (0),
        BOTTOM_HALF (1),
        TOP_HALF (2),
        FULL (3);

        private int value;
        PresenceStatus(int value) {
            this.value = value;
        }
    }
}
