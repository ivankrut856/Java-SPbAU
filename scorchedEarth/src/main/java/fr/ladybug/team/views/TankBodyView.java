package fr.ladybug.team.views;

import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;

import static fr.ladybug.team.models.Model.CELL_HEIGHT;
import static fr.ladybug.team.models.Model.CELL_WIDTH;

/** The render info class for tank's body */
public class TankBodyView {
    public static int TANK_BODY_TEXTURE_DEFAULT_WIDTH = 5 * CELL_WIDTH;
    public static int TANK_BODY_TEXTURE_DEFAULT_HEIGHT = 3 * CELL_HEIGHT;

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
