package fr.ladybug.team;

import com.google.common.primitives.Ints;
import org.apache.commons.lang3.ArrayUtils;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.google.common.base.Preconditions.checkState;

public class Server {
    private ServerSocketChannel serverSocket;

    private Selector acceptSelector;

    private Selector readSelector;
    private Selector writeSelector;

    private InetSocketAddress listenAddress;
    private ExecutorService threadPool = Executors.newFixedThreadPool(5);

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
                    readSelector.wakeup();
                    System.out.println("Connection Accepted: " + sc.getLocalAddress() + "\n");
                } else if (key.isReadable()) {
                    throw new RuntimeException("wtf?");
                } else {
                    System.err.println("Error: key not supported by server.");
                }
                iterator.remove();
            }
        }
    }

    public void read() throws IOException {
        while (!Thread.currentThread().isInterrupted()) {
            readSelector.selectedKeys().clear();
            int readyCount = readSelector.select();
            if (readyCount == 0) {
                continue;
            }

            Set<SelectionKey> readyKeys = readSelector.selectedKeys();
            Iterator iterator = readyKeys.iterator();
            while (iterator.hasNext()) {
                SelectionKey key = (SelectionKey) iterator.next();

                if (!key.isValid()) {
                    key.cancel();
                    continue;
                }

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
        while (!Thread.currentThread().isInterrupted()) {
            writeSelector.selectedKeys().clear();
            int readyCount = writeSelector.select();
            if (readyCount == 0) {
                continue;
            }

            Set<SelectionKey> readyKeys = writeSelector.selectedKeys();
            Iterator iterator = readyKeys.iterator();
            while (iterator.hasNext()) {
                SelectionKey key = (SelectionKey) iterator.next();

                if (!key.isValid()) {
                    key.cancel();
                    continue;
                }

                if (key.isWritable()) {
                    processWrite(key);
                } else {
                    System.err.println("Error: key not supported by server.");
                }
                iterator.remove();
            }
        }
    }

    private void processRead(SelectionKey key) {
        TransmissionController transmissionController = (TransmissionController) key.attachment();
        TransmissionController.InputTransmission currentStatus = transmissionController.inputTransmission;
        System.out.println("Started processing read.");
        if (!currentStatus.hasReadSize()) {
            // read size of next package
            currentStatus.readSize();
        } else {
            // read the package data
            if (currentStatus.packageSize <= Integer.BYTES) {
                System.err.println("Invalid package size: " + currentStatus.packageSize);
                transmissionController.addFailedQuery();
                currentStatus.reset();
            } else {
                currentStatus.readData();
            }
        }

        System.out.println("Read smth from current channel.");
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
                transmissionController.addFailedQuery();
            }
            System.out.println("Submitted a query.");
            currentStatus.reset();
        }
    }

    private void processWrite(SelectionKey key) {
        System.out.println("Processing...");
        TransmissionController transmissionController = (TransmissionController) key.attachment();
        TransmissionController.OutputTransmission currentStatus = transmissionController.outputTransmission;

        if (currentStatus == null) {
            System.out.println("null");
            return;
        } else if (currentStatus.hasSentData()) {
            System.out.println("to cancel");
            key.cancel();
            transmissionController.outputTransmission = null;
        } else {
            System.out.println("writing..");
            currentStatus.writeData();
        }
    }

    private void executeGet(TransmissionController controller, String pathName) {
        var path = Paths.get(pathName);
        if (!Files.isRegularFile(path)) {
            System.out.println("Nonexistent file.");
            controller.addFailedQuery();
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
            controller.addFailedQuery();
        }

        var fileList = path.toFile().listFiles();
        if (fileList == null) {
            System.err.println("Could not get list of files in directory.");
            controller.addFailedQuery();
        } else {
            int size = fileList.length;
            System.out.println("Found " + size + " files.");
            byte[] result = Ints.toByteArray(size);
            for (var file : fileList) {
                result = ArrayUtils.addAll(result, fileToBytes(file));
            }
            controller.addOutputQuery(result);
        }
    }

    private byte[] fileToBytes(File file) {
        String fileName = file.getName();
        byte[] isDirectory = new byte[]{(byte)(file.isDirectory() ? 1 : 0)};
        return ArrayUtils.addAll(ArrayUtils.addAll(Ints.toByteArray(fileName.length()), fileName.getBytes()), isDirectory);
    }

    private class TransmissionController {
        SocketChannel channel;
        private InputTransmission inputTransmission;
        private OutputTransmission outputTransmission;

        private TransmissionController(SocketChannel channel) {
            this.channel = channel;
            inputTransmission = new InputTransmission();
        }

        private void addOutputQuery(byte[] data) {
            checkState(outputTransmission == null); // transmissions should come by one as clients are blocking.
            try {
                channel.register(writeSelector, SelectionKey.OP_WRITE, this);
                writeSelector.wakeup();
            } catch (ClosedChannelException e) {
                System.out.println("The client has disconnected");
                return;
            }
            outputTransmission = new OutputTransmission(ByteBuffer.wrap(ArrayUtils.addAll(Ints.toByteArray(data.length), data)));
        }

        private void addFailedQuery() {
            addOutputQuery(Ints.toByteArray(-1));
        }

        private class InputTransmission {
            private final int defaultPackageSize = -5;
            private ByteBuffer packageSizeBuffer = ByteBuffer.allocate(Integer.BYTES);
            private ByteBuffer queryTypeBuffer = ByteBuffer.allocate(Integer.BYTES);
            private ByteBuffer receivedData;
            private int packageSize = defaultPackageSize;

            private boolean hasReadSize() {
                return packageSize != defaultPackageSize;
            }

            private boolean hasReadData() {
                return receivedData != null && !receivedData.hasRemaining();
            }

            private void readSize() {
                readCorrectly(new ByteBuffer[]{packageSizeBuffer});
                if (!packageSizeBuffer.hasRemaining()) {
                    packageSizeBuffer.flip();
                    packageSize = packageSizeBuffer.getInt();
                    receivedData = ByteBuffer.allocate(packageSize - Integer.BYTES);
                }
            }

            private void readData() {
                checkState(receivedData != null); // should be called after packageSize was read.
                readCorrectly(new ByteBuffer[]{queryTypeBuffer, receivedData});
            }

            private void readCorrectly(ByteBuffer[] byteBuffers) {
                long result = 0;
                try {
                    result = channel.read(byteBuffers);
                } catch (IOException e) {
                    System.err.println("Failed read from channel: " + e.getMessage());
                }
                if (result == -1) {
                    //do stuff
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
                return !sentData.hasRemaining();
            }

            private void writeData() {
                try {
                    channel.write(sentData);
                } catch (IOException e) {
                    System.err.println("Writing to channel failed:" + e.getMessage());
                }
            }
        }
    }
}