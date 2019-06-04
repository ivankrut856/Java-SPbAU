package fr.ladybug.team;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;

import java.io.*;
import java.net.Socket;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.util.List;
import java.util.Stack;
import java.util.stream.Collectors;

public class Client {
    private Socket server;
    private InputStream inputStream;
    private OutputStream outputStream;

    private Stack<String> fileTree;

    public Client(String remoteAddress) throws IOException {
        server = new Socket(remoteAddress, 8179);
        inputStream = server.getInputStream();
        outputStream = server.getOutputStream();

        fileTree = new Stack<>();
        fileTree.add(".");
    }

    public void load(final ObservableList<FileView> dataSupplier) {
        dataSupplier.clear();
        dataSupplier.addAll(FXCollections.observableArrayList(FileView.LOADING));
        var client = this;
        var task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                System.out.println("call");
                var response = ResponseList.fromBytes(client.makeQuery(new Query(1, getFullPath())));
                Platform.runLater(() -> {
                    dataSupplier.clear();
                    dataSupplier.add(FileView.PARENT);
                    dataSupplier.addAll(FXCollections.observableArrayList(response.toFileViews()));
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

    public byte[] makeQuery(Query query) throws IOException {
        query.goTo(outputStream);
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

    public void saveFile(String filename) {
        try {
            var response = ResponseGet.fromBytes(makeQuery(new Query(2, getFullPath() + FileSystems.getDefault().getSeparator() + filename)));
            System.out.println(new String(response.getFileContent()));
        } catch (IOException e) {
            throw new RuntimeException("", e);
        }
    }
}
