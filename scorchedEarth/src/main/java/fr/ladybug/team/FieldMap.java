package fr.ladybug.team;

import javafx.application.Platform;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class FieldMap {
    static {
        try {
            var inputStream = FieldMap.class.getClassLoader().getResourceAsStream("field-map.png");
            image = ImageIO.read(inputStream);
        } catch (IOException e) {
            System.err.println("Cannot load map");
            Platform.exit();
        }
    }

    private static BufferedImage image;
    public static boolean getState(int i, int j) {
        int mask = image.getRGB(j, i);
        return ((mask & 0x000000ff) ==  0x000000ff) || isTarget(i, j);
    }

    public static boolean isTarget(int i, int j) {
        int mask = image.getRGB(j, i);
        return ((mask & 0x0000ff00) ==  0x0000ff00);
    }
}
