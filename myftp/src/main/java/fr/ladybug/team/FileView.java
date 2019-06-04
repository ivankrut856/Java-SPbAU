package fr.ladybug.team;

import javafx.scene.paint.Color;

import java.nio.file.FileSystem;
import java.nio.file.FileSystems;

public class FileView {

    private String filename;
    private boolean isDirectory;

    public static final FileView LOADING = new FileView("Loading...", false);
    public static final FileView PARENT = new FileView("..", true);

    public FileView(String filename, boolean isDirectory) {
        this.filename = filename;
        this.isDirectory = isDirectory;
    }

    @Override
    public String toString() {
        if (isDirectory)
            return filename + FileSystems.getDefault().getSeparator();
        return filename;
    }

    public String getFilename() {
        return filename;
    }

    public boolean isDirectory() {
        return isDirectory;
    }
}
