package fr.ladybug.team.models;


import fr.ladybug.team.views.CellView;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/** Class representing cells in the game world */
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

    /** Update method of the cell. Potentially usefull */
    public void update() {
        // Sorry, but empty
    }

    /** Adds lister hearing destory event of the cell */
    public void addOnDestroyListener(Consumer<Cell> listener) {
        onDestroyListeners.add(listener);
    }

    /** Actualizes status of the destroyed cell */
    public void destroy() {
        presenceStatus = PresenceStatus.EMPTY;
        getView().chooseTexture(presenceStatus.value);
        for (var listener : onDestroyListeners) {
            listener.accept(this);
        }
    }

    /** Class representing how alive the cell is. Currently there's two working options */
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
