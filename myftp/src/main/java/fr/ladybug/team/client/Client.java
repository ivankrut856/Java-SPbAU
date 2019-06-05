package fr.ladybug.team.client;

import javafx.application.Platform;
import javafx.concurrent.Task;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.net.Socket;
import java.nio.file.FileSystems;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.function.Consumer;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 *  Class responsible for running a client that can send the commands list and get to a server.
 *  The get command accepts the path to a file and sends the file's size and its content.
 *  The list command accepts the path to a directory and sends the amount of Files in it, as well as a list of names,
 *  and a boolean parameter that says whether a given File is a directory.
 */
public class Client {
    private @NotNull Socket server;
    private @NotNull InputStream inputStream;
    private @NotNull OutputStream outputStream;
    private @NotNull Stack<String> fileTree;
    private @NotNull static final Logger logger = Logger.getAnonymousLogger();

    /** Client should not be able to walk any lower than the base directory for security reasons. */
    private static final String baseDirectory = ".";

    /**
     * Basic constructor for a client.
     * Performs binding a socket to a remote server with given remote address and port.
     * Initializes basic state of the remote filesystem.
     * @param remoteAddress the remote address of the target server.
     * @param port the port to which the socket will be bound.
     * @throws IOException the exception is thrown when it is not possible to connect to the remote server.
     */
    public Client(String remoteAddress, int port) throws IOException {
        server = new Socket(remoteAddress, port);

        inputStream = server.getInputStream();
        outputStream = server.getOutputStream();

        fileTree = new Stack<>();
        fileTree.add(baseDirectory);
    }

    /**
     * Loads a list of files in the current directory.
     * @param onLoad the callback to successfully loaded list of files.
     * @param onError the callback to failure.
     */
    public void load(Consumer<List<FileView>> onLoad, Consumer<String> onError) {
        var client = this;
        var task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                var response = ResponseList.fromBytes(client.makeQuery(new Query(Query.QueryType.LIST, getFullPath())));
                if (!response.isValid()) {
                    Platform.runLater(() -> onError.accept(response.getError()));
                    return null;
                }
                List<FileView> folders = new ArrayList<>();
                if (fileTree.size() != 1)
                    folders.add(FileView.PARENT);
                folders.addAll(response.toFileViews());
                Platform.runLater(() -> {
                    onLoad.accept(folders);
                });
                return null;
            }
        };
        new Thread(task).start();
    }

    /**
     * Constructs full path from root directory to current directory.
     * @return the full path from root directory to current directory
     */
    private String getFullPath() {
        return fileTree.stream().collect(Collectors.joining(FileSystems.getDefault().getSeparator()));
    }

    /**
     * Changes the remote filesystem's working directory by pushing given directory to the current file tree.
     * @param directory the directory to push to the current file tree.
     */
    public void moveToDirectory(String directory) {
        logger.info("Moved to directory " + directory);
        fileTree.add(directory);
    }

    /** Changes the remote filesystem's working directory by removing top directory from the current file tree. */
    public void moveToParentDirectory() {
        logger.info("Left current directory for parent");
        fileTree.pop();
    }

    public byte[] makeQuery(Query query) throws IOException {
        logger.info("Message of the query executed: " + query.getQueryBody());
        logger.info("Task of the query executed: " + query.getQueryType());
        query.printToStream(outputStream);
        return readNextPackage(inputStream);
    }

    private byte[] readNextPackage(InputStream inputStream) throws IOException {
        var dataInputStream = new DataInputStream(inputStream);
        int packageSize = dataInputStream.readInt();
        var bytes = dataInputStream.readNBytes(packageSize);

        if (bytes.length != packageSize) {
            throw new IOException("Package has been damaged.");
        }
        return bytes;
    }

    /** Shuts down the client. */
    public void shutdown() throws IOException {
        if (server.isClosed())
            throw new IllegalStateException("The client is already shut down.");
        server.close();
    }

    /**
     * Saves the file with given filename in current working directory into current working folder in local machine.
     * @param filename the filename of the file which is to be saved.
     * @param onFinishInformer the callback for save finish.
     */
    public void saveFile(String filename, Consumer<String> onFinishInformer) {
        byte[] content;
        try {
            var response = ResponseGet.fromBytes(makeQuery(new Query(Query.QueryType.GET,
                    getFullPath() + FileSystems.getDefault().getSeparator() + filename)));
            if (!response.isValid()) {
                logger.severe("Query failed: " + response.getError());
                onFinishInformer.accept("Query failed.");
                return;
            }
            content = response.getFileContent();

        } catch (IOException e) {
            logger.severe("Downloading file failed: " + e.getMessage());
            onFinishInformer.accept("Could not download the file.");
            return;
        }

        try {
            FileUtils.copyToFile(new ByteArrayInputStream(content), new File(filename));
        } catch (IOException e) {
            logger.severe("Saving file failed: " + e.getMessage());
            onFinishInformer.accept("Could not save the file.");
            return;
        }

        onFinishInformer.accept(String.format("%s was successfully downloaded", filename));
    }
}
