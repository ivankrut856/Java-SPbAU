package fr.ladybug.team.server;

import com.google.common.primitives.Ints;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;

/** Class responsible for executing the queries sent to the server and submitting their result to a TransmissionController. */
class QueryExecutor {
    private @NotNull static final Logger logger = Logger.getAnonymousLogger();

    /**
     * Method that executes a get query and sends the result to the given transmission controller.
     * @param controller the controller to send the response to.
     * @param pathName   the path of the file for which the get query should be executed.
     */
    static void executeGet(@NotNull TransmissionController controller, @NotNull String pathName) {
        logger.info("Executing get for " + pathName);
        Path path;
        try {
            path = Paths.get(pathName);
        } catch (InvalidPathException notAFile) {
            logger.info("File " + pathName + " does not exist.");
            controller.addQueryForIncorrectFile();
            return;
        }
        if (!Files.isRegularFile(path)) {
            logger.info("File " + pathName + " does not exist.");
            controller.addQueryForIncorrectFile();
            return;
        }
        var fileBytes = new byte[0];
        try {
            fileBytes = Files.readAllBytes(path);
        } catch (IOException e) {
            logger.severe("Failed to read file " + pathName);
        }
        var lengthBytes = Ints.toByteArray(fileBytes.length);
        logger.info("Successfully read file, size is " + fileBytes.length);
        controller.addOutputQuery(ArrayUtils.addAll(lengthBytes, fileBytes));
    }

    /**
     * Method that executes a list query and sends the result to the given transmission controller.
     * @param controller the controller to send the response to.
     * @param pathName   the path of the file for which the list query should be executed.
     */
    static void executeList(@NotNull TransmissionController controller, @NotNull String pathName) {
        logger.info("Executing list for " + pathName);
        Path path;
        try {
            path = Paths.get(pathName);
        } catch (InvalidPathException notADirectory) {
            logger.info("Directory " + pathName + " does not exist.");
            controller.addQueryForIncorrectFile();
            return;
        }
        if (!Files.isDirectory(path)) {
            logger.info("File " + pathName + " does not exist or is not a directory.");
            controller.addQueryForIncorrectFile();
            return;
        }
        var fileList = path.toFile().listFiles();
        if (fileList == null) {
            logger.severe("Could not get list of files in directory.");
            controller.addFailedQuery();
        } else {
            int size = fileList.length;
            logger.info("Successfully listed files in directory, found " + size + " files.");
            var result = Ints.toByteArray(size);
            for (var file : fileList) {
                result = ArrayUtils.addAll(result, fileToBytes(file));
            }
            controller.addOutputQuery(result);
        }
    }

    /**
     * Method that converts information about a file into a byte array according to the server's protocol.
     * The byte array consists of an integer (the file name's size in bytes), the file name,
     * and a boolean (is the file a directory), concatenated.
     * @param file the file to serialize.
     * @return a byte array with the information about the given file.
     */
    private static @NotNull byte[] fileToBytes(@NotNull File file) {
        String fileName = file.getName();
        var isDirectory = new byte[]{(byte) (file.isDirectory() ? 1 : 0)};
        var encodedFile = fileName.getBytes();
        return ArrayUtils.addAll(ArrayUtils.addAll(Ints.toByteArray(encodedFile.length), encodedFile), isDirectory);
    }
}