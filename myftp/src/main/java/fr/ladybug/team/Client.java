package fr.ladybug.team;

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

public class Client {
    private Socket server;
    private InputStream inputStream;
    private OutputStream outputStream;

    private Stack<String> fileTree;

    private @NotNull static final Logger logger = Logger.getAnonymousLogger();

    public Client(String remoteAddress) throws IOException {
        server = new Socket(remoteAddress, 8179);

        inputStream = server.getInputStream();
        outputStream = server.getOutputStream();

        fileTree = new Stack<>();
        fileTree.add(".");
    }

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

    public String getFullPath() {
        return fileTree.stream().collect(Collectors.joining(FileSystems.getDefault().getSeparator()));
    }

    public void pushDir(String s) {
        fileTree.add(s);
    }

    public void popDir() {
        fileTree.pop();
    }

    private byte[] makeQuery(Query query) throws IOException {
        logger.info("Message of the query executed: " + query.getQueryBody());
        logger.info("Task of the query executed: " + query.getQueryType());
        query.printToStream(outputStream);
        return readNextPackage(inputStream);
    }

    private byte[] readNextPackage(InputStream inputStream) throws IOException {
        DataInputStream dataInputStream = new DataInputStream(inputStream);
        int packageSize = dataInputStream.readInt();
        var bytes = dataInputStream.readNBytes(packageSize);

        if (bytes.length != packageSize)
            throw new IOException("Package's been fucked up");

        return bytes;
    }

    public void shutdown() throws IOException {
        if (server.isClosed())
            throw new IllegalStateException("The client is already shutdown");
        server.close();
    }

    public void saveFile(String filename, Consumer<String> onFinishInformer) {
        byte[] content = null;
        try {
            var response = ResponseGet.fromBytes(makeQuery(new Query(Query.QueryType.GET, getFullPath() + FileSystems.getDefault().getSeparator() + filename)));
            if (!response.isValid()) {
                System.err.println(response.getError());
            }
            content = response.getFileContent();

        } catch (IOException ignore) {
            onFinishInformer.accept("Can not download file");
            return;
        }

        try {
            FileUtils.copyToFile(new ByteArrayInputStream(content), new File(filename));
        } catch (IOException e) {
            onFinishInformer.accept("Can not save the file. Check your privilege");
            return;
        }

        onFinishInformer.accept(String.format("%s was successfully downloaded", filename));
    }
}
