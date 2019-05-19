package fr.ladybug.team;

import javafx.scene.image.Image;
import javafx.scene.paint.ImagePattern;

import java.util.ArrayList;
import java.util.List;

public class TextureSet {
    private List<ImagePattern> patterns = new ArrayList<>();

    private TextureSet() {}

    public static TextureSet getTextureSet(List<Image> images) {
        var object = new TextureSet();
        for (var image : images) {
            object.patterns.add(new ImagePattern(image));
        }
        return object;
    }

    public List<ImagePattern> getPatterns() {
        return patterns;
    }
}