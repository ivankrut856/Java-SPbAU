package fr.ladybug.team;

import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;

public class TargetView {
    private Rectangle rectangle;
    private ImagePattern texture;

    public TargetView(Rectangle rectangle, ImagePattern texture) {
        this.rectangle = rectangle;
        this.texture = texture;
        this.rectangle.setFill(texture);
        System.out.println(texture.getImage());
    }

    public Rectangle getRectangle() {
        return rectangle;
    }
}
