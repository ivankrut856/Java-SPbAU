package fr.ladybug.team.views;

import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;

/** The render info class for tank's body */
public class TankBodyView {
    private Rectangle rectangle;
    private ImagePattern texture;

    public TankBodyView(Rectangle rectangle, ImagePattern texture) {
        this.rectangle = rectangle;
        this.texture = texture;
        this.rectangle.setFill(texture);
    }

    public Rectangle getRectangle() {
        return rectangle;
    }
}
