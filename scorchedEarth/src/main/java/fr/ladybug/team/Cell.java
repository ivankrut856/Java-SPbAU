package fr.ladybug.team;


import javafx.scene.image.Image;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;


public class Cell {
    private PresenceStatus presenceStatus;
    private int x;
    private int y;
    private Rectangle associatedNode;

    private TextureSet textureSet;

    private List<Consumer<Cell>> onDestroyListeners = new ArrayList<>();

    public Cell(int x, int y, PresenceStatus presenceStatus, Rectangle associatedNode, TextureSet textures) {
        this.x = x;
        this.y = y;
        this.presenceStatus = presenceStatus;
        this.associatedNode = associatedNode;

        if (textures.getPatterns().size() < 4)
            throw new IllegalArgumentException("Cell texture set wrong size");
        this.textureSet = textures;
    }

    public PresenceStatus getPresenceStatus() {
        return presenceStatus;
    }

    public void setPresenceStatus(PresenceStatus status) {
        presenceStatus = status;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public Rectangle getAssociatedNode() {
        return associatedNode;
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

    public void update() {
        getAssociatedNode().setFill(textureSet.getPatterns().get(presenceStatus.value));
    }

    public void addOnDestroyListener(Consumer<Cell> listener) {
        onDestroyListeners.add(listener);
    }

    public void destroy() {
        presenceStatus = PresenceStatus.EMPTY;
        for (var listener : onDestroyListeners) {
            listener.accept(this);
        }
    }
}
