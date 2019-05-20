package fr.ladybug.team;

import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;


public class ProjectileView {
    private Rectangle rectangle;
    private ImagePattern texture;

    public ProjectileView(Rectangle rectangle, ImagePattern texture) {
        this.rectangle = rectangle;
        this.texture = texture;

        this.rectangle.setFill(texture);
    }

    public Rectangle getRectangle() {
        return rectangle;
    }
}
