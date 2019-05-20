package fr.ladybug.team;

import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;

import java.util.List;

public class CellView {
    private Rectangle rectangle;
    private List<ImagePattern> textures;

    public CellView(Rectangle rectangle, List<ImagePattern> textures) {
        this.textures = textures;
        this.rectangle = rectangle;
        chooseTexture(0);
    }

    public Rectangle getRectangle() {
        return rectangle;
    }

    public void chooseTexture(int index) {
//        System.out.println("Texture: " + index);
        rectangle.setFill(textures.get(index));
    }
}
