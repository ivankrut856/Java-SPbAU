package fr.ladybug.team;

import com.google.common.primitives.Ints;
import org.apache.commons.lang3.ArrayUtils;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

public class Server {
    private ServerSocketChannel serverSocket;

    private Selector acceptSelector;

    private Selector readSelector;
    private Selector writeSelector;

    private InetSocketAddress listenAddress;
    private ExecutorService threadPool;

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Require 2 arguments: the address and port number");
            return;
        }

        String address = args[0];
        int portNumber;
        try {
            portNumber = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            System.out.println("Port number should be a number.");
            return;
        }
        Server server = null;
        try {
            server = new Server(address, portNumber);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        Server finalServer = server;
        var acceptThread = new Thread(() -> {
            try {
                finalServer.accept();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        var readThread = new Thread(() -> {
            try {
                finalServer.read();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        });
        var writeThread = new Thread(() -> {
            try {
                finalServer.write();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        });
        acceptThread.setDaemon(true);
        readThread.setDaemon(true);
        writeThread.setDaemon(true);
        acceptThread.start();
        readThread.start();
        writeThread.start();

        try (var input = new BufferedReader(new InputStreamReader(System.in));
             var output = new PrintWriter(System.out, true)) {
            output.println("\"exit\" stops server.");
            while (true) {
                String query = input.readLine();
                if (query.equals("exit")) {
                    break;
                }
            }
        } catch (IOException e) {
            System.err.println("IOException while reading.");
        }
    }

    public Server(String address, int portNumber) throws IOException {
        acceptSelector = Selector.open();
        readSelector = Selector.open();
        writeSelector = Selector.open();

        serverSocket = ServerSocketChannel.open();
        serverSocket.configureBlocking(false);

        listenAddress = new InetSocketAddress(address, portNumber);
        serverSocket.socket().bind(listenAddress);
        serverSocket.register(acceptSelector, SelectionKey.OP_ACCEPT);
        System.out.println("Server started on port " + portNumber + ".");
    }

    public void accept() throws IOException {
        while (!Thread.currentThread().isInterrupted()) {
            int readyCount = acceptSelector.select();
            if (readyCount == 0) {
                continue;
            }

            Set<SelectionKey> readyKeys = acceptSelector.selectedKeys();
            Iterator iterator = readyKeys.iterator();
            while (iterator.hasNext()) {
                SelectionKey key = (SelectionKey) iterator.next();

                if (key.isAcceptable()) {
                    var serverSocket = ((ServerSocketChannel)key.channel());
                    SocketChannel sc = serverSocket.accept();
                    sc.configureBlocking(false);
                    sc.register(readSelector, SelectionKey.OP_READ, new TransmissionController(sc));
                    System.out.println("Connection Accepted: " + sc.getLocalAddress() + "\n");
                } else {
                    System.err.println("Error: key not supported by server.");
                }
                iterator.remove();
            }
        }
    }

    public void read() throws IOException {
        while (!Thread.currentThread().isInterrupted()) {
            int readyCount = readSelector.select();
            System.out.println("wanna read");
            if (readyCount == 0) {
                continue;
            }

            Set<SelectionKey> readyKeys = readSelector.selectedKeys();
            Iterator iterator = readyKeys.iterator();
            while (iterator.hasNext()) {
                SelectionKey key = (SelectionKey) iterator.next();

                if (key.isReadable()) {
                    processRead(key);
                } else {
                    System.err.println("Error: key not supported by server.");
                }
                iterator.remove();
            }
        }
    }

    public void write() throws IOException {

    }

    private void processRead(SelectionKey key) {
        TransmissionController transmissionController = (TransmissionController) key.attachment();
        TransmissionController.InputTransmission currentStatus = transmissionController.inputTransmission;
        SocketChannel channel = (SocketChannel)key.channel();
        if (!currentStatus.hasReadSize()) {
            // read size of next package
            currentStatus.readSize(channel);
        } else {
            // read the package data
            //TODO checkArgument packageSize >= 0
            currentStatus.readData(channel);
        }
        // read happened, check if it ended OK
        if (currentStatus.hasReadData()) {
            // execute something
            currentStatus.finalizeRead();
            int queryType = currentStatus.queryTypeBuffer.getInt();
            String query = new String(currentStatus.receivedData.array(), StandardCharsets.UTF_8);
            if (queryType == 1) {
                threadPool.submit(() -> executeGet(transmissionController, query));
            } else if (queryType == 2) {
                threadPool.submit(() -> executeList(transmissionController, query));
            } else {
                System.err.println("Invalid query type: " + queryType);
            }
            currentStatus.reset();
        }
    }

    private void executeGet(TransmissionController controller, String pathName) {
        var path = Paths.get(pathName);
        if (!Files.isRegularFile(path)) {
            System.out.println("Nonexistent file.");
            controller.addOutputQuery(Ints.toByteArray(-1));
        }

        byte[] fileBytes = new byte[0];
        try {
            fileBytes = Files.readAllBytes(path);
        } catch (IOException e) {
            System.err.println("Failed to read file " + pathName);
        }
        byte[] lengthBytes = Ints.toByteArray(fileBytes.length);
        System.out.println("File size: " + fileBytes.length);
        controller.addOutputQuery(ArrayUtils.addAll(lengthBytes, fileBytes));
    }

    private void executeList(TransmissionController controller, String pathName) {
        var path = Paths.get(pathName);
        if (!Files.exists(path)) {
            System.out.println("Nonexistent file");
            controller.addOutputQuery(Ints.toByteArray(-1));
        }

        var fileList = path.toFile().listFiles();
        assert fileList != null; //TODO no.
        int size = fileList.length;
        System.out.println("Found " + size + " files.");
        byte[] result = Ints.toByteArray(size);

        //format of File encoding: the name length, then the name itself, then the boolean isDir.
        for (var file : fileList) {
            result = ArrayUtils.addAll(result, fileToBytes(file));
        }
        controller.addOutputQuery(result);
    }

    private byte[] fileToBytes(File file) {
        String fileName = file.getName();
        byte[] isDirectory = new byte[]{(byte)(file.isDirectory() ? 1 : 0)};
        return ArrayUtils.addAll(ArrayUtils.addAll(Ints.toByteArray(fileName.length()), fileName.getBytes()), isDirectory);
    }

    private class TransmissionController {
        SocketChannel outputChannel;
        private InputTransmission inputTransmission;
        private OutputTransmission outputTransmission;

        private TransmissionController(SocketChannel inputChannel) {
            outputChannel = inputChannel; // it's a channel
            inputTransmission = new InputTransmission();
        }

        private void addOutputQuery(byte[] data) {
            outputTransmission = new OutputTransmission(ByteBuffer.wrap(ArrayUtils.addAll(Ints.toByteArray(data.length), data)));
        }

        private class InputTransmission {
            private final int defaultPackageSize = -5;
            private ByteBuffer packageSizeBuffer = ByteBuffer.allocate(Integer.BYTES);
            private ByteBuffer queryTypeBuffer = ByteBuffer.allocate(Integer.BYTES);
            private ByteBuffer receivedData;
            private int packageSize;

            private boolean hasReadSize() {
                return packageSize != defaultPackageSize;
            }

            private boolean hasReadData() {
                return receivedData.hasRemaining();
            }

            private void readSize(SocketChannel channel) {
                try {
                    channel.read(packageSizeBuffer);
                } catch (IOException e) {
                    System.err.println("Failed read from channel: " + e.getMessage());
                }
                if (!packageSizeBuffer.hasRemaining()) {
                    packageSizeBuffer.flip();
                    packageSize = packageSizeBuffer.getInt();
                    receivedData = ByteBuffer.allocate(packageSize - Integer.BYTES);
                }
            }

            private void readData(SocketChannel channel) {
                try {
                    channel.read(new ByteBuffer[]{queryTypeBuffer, receivedData});
                } catch (IOException e) {
                    System.err.println("Failed read from channel: " + e.getMessage());
                }
            }

            private void finalizeRead() {
                queryTypeBuffer.flip();
                receivedData.flip();
            }

            private void reset() {
                packageSizeBuffer.clear();
                queryTypeBuffer.clear();
                packageSize = defaultPackageSize;
            }
        }

        private class OutputTransmission {
            private ByteBuffer sentData;

            private OutputTransmission(ByteBuffer sentData) {
                this.sentData = sentData;
            }

            private boolean hasSentData() {
                return false;
            }
        }
    }
}