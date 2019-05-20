package fr.ladybug.team;

import javafx.scene.paint.ImagePattern;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;

import java.util.List;


public class TankGunView {
    private Rectangle rectangle;
    private List<ImagePattern> textures;
    int currentTextureIndex = 0;

    public TankGunView(Rectangle rectangle, List<ImagePattern> textures) {
        this.rectangle = rectangle;
        this.textures = textures;
        chooseTexture(currentTextureIndex);
    }

    public Rectangle getRectangle() {
        return rectangle;
    }

    public ImagePattern getTexture() {
        return textures.get(currentTextureIndex);
    }

    public void chooseTexture(int index) {
        rectangle.setFill(textures.get(index));
        currentTextureIndex = index;
    }
}
