package fr.ladybug.team.views;

import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;

import java.util.List;

/** The render info class for class Cell */
public class CellView {
    private static final int DEFAULT_TEXTURE = 0;
    private Rectangle rectangle;
    private List<ImagePattern> textures;

    public CellView(Rectangle rectangle, List<ImagePattern> textures) {
        this.textures = textures;
        this.rectangle = rectangle;
        chooseTexture(DEFAULT_TEXTURE);
    }

    public Rectangle getRectangle() {
        return rectangle;
    }

    /** Changes texture
     * @param index the index of the new texture in the textures list
     */
    public void chooseTexture(int index) {
        rectangle.setFill(textures.get(index));
    }
}
