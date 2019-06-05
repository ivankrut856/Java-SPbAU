package fr.ladybug.team.client;

import org.jetbrains.annotations.NotNull;

import java.nio.file.FileSystems;

/** Representation of a file in the directory tree. */
public class FileView {
    private @NotNull String fileName;
    private boolean isDirectory;

    /** View that is shown while the current directory is loading. */
    public static final @NotNull FileView LOADING = new FileView("Loading...", false);
    /** View for the parent directory. */
    public static final @NotNull FileView PARENT = new FileView("..", true);

    /**
     * Creates a fileView with the given name and directory status.
     * @param fileName The name of the file.
     * @param isDirectory true if the file is a directory, false otherwise.
     */
    public FileView(@NotNull String fileName, boolean isDirectory) {
        this.fileName = fileName;
        this.isDirectory = isDirectory;
    }

    /** String representation of the FileView. */
    @Override
    public String toString() {
        if (isDirectory)
            return fileName + FileSystems.getDefault().getSeparator();
        return fileName;
    }

    /** Getter for the fileName field. */
    public @NotNull String getFileName() {
        return fileName;
    }

    /** Getter for the isDirectory field. */
    public boolean isDirectory() {
        return isDirectory;
    }
}
