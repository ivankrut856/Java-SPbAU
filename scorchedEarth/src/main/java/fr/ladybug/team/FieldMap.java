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
    public static boolean getState(int i, int j) {
        int mask = image.getRGB(j, i);
        return ((mask & 0x000000ff) ==  0x000000ff) || isTarget(i, j);
    }

    /**
     * Returns whether the target is place at the cell
     * @param i the row number of the cell
     * @param j the column number of the cell
     * @return the cell's state: either holding target or not
     */
    public static boolean isTarget(int i, int j) {
        int mask = image.getRGB(j, i);
        return ((mask & 0x0000ff00) ==  0x0000ff00);
    }
}
