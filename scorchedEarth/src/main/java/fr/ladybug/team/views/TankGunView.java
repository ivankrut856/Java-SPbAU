package fr.ladybug.team.views;

import javafx.scene.paint.ImagePattern;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;

import java.util.List;

/** The render info class for tank's gun */
public class TankGunView {
    private Rectangle rectangle;
    private List<ImagePattern> textures;
    private int currentTextureIndex = 0;

    public TankGunView(Rectangle rectangle, List<ImagePattern> textures) {
        this.rectangle = rectangle;
        this.textures = textures;
        chooseTexture(currentTextureIndex);
    }

    public Rectangle getRectangle() {
        return rectangle;
    }

    public List<ImagePattern> getTextures() {
        return textures;
    }

    /** Changes texture
     * @param index the index of the new texture in the textures list
     */
    public void chooseTexture(int index) {
        rectangle.setFill(textures.get(index));
        currentTextureIndex = index;
    }
}
