package fr.ladybug.team;

import javafx.application.Platform;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/** Class representing map of the game and providing usefull information about the cells' status */
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

    /** Returns whether the cell is present in the world or empty
     * @param i the row number of the cell
     * @param j the column number of the cell
     * @return the cell's state: either full or empty
     */
    public static MapState getState(int i, int j) {
        int mask = image.getRGB(j, i);
        switch (mask) {
            case 0xff0000ff:
                return MapState.BLOCK;
            case 0xff00ff00:
                return MapState.TARGET;
            default:
                return MapState.EMPTY;
        }
    }

    public enum MapState {
        EMPTY,
        BLOCK,
        TARGET
    }
}
