package fr.ladybug.team.views;

import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;

/** The render info class for class Target */
public class TargetView {
    private Rectangle rectangle;
    private ImagePattern texture;

    public TargetView(Rectangle rectangle, ImagePattern texture) {
        this.rectangle = rectangle;
        this.texture = texture;
        this.rectangle.setFill(texture);
    }

    public Rectangle getRectangle() {
        return rectangle;
    }
}
