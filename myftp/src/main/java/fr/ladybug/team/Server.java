package fr.ladybug.team;

import com.google.common.primitives.Ints;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import static com.google.common.base.Preconditions.checkState;

/**
 *  Class responsible for running a server that can execute the commands list and get.
 *  The get command accepts the path to a file and sends the file's size and its content.
 *  The list command accepts the path to a directory and sends the amount of Files in it, as well as a list of names,
 *  and a boolean parameter that says whether a given File is a directory.
 */
public class Server {
    private final @NotNull Selector acceptSelector;
    private final @NotNull Selector readSelector;
    private final @NotNull Selector writeSelector;

    private final @NotNull Thread acceptingThread;
    private final @NotNull Thread readingThread;
    private final @NotNull Thread writingThread;
    private volatile boolean wasShutdown = false;

    private final static int NUMBER_OF_THREADS = 5;
    private final @NotNull ExecutorService threadPool = Executors.newFixedThreadPool(NUMBER_OF_THREADS);
    private @NotNull static final Logger logger = Logger.getAnonymousLogger();

    /**
     * Runs a server on the given address with the given port number.
     * @param args two arguments: the address and the port number.
     */
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
            logger.severe("Failed to start server: " + e.getMessage());
            return;
        }

        try (var input = new BufferedReader(new InputStreamReader(System.in));
             var output = new PrintWriter(System.out, true)) {
            output.println("\"exit\" stops server.");
            while (true) {
                String query = input.readLine();
                if (query.equals("exit")) {
                    try {
                        server.shutdown();
                    } catch (InterruptedException e) {
                        logger.severe("Interrupted during shutdown: " + e.getMessage());
                    }
                    break;
                }
            }
        } catch (IOException e) {
            logger.severe("IOException while reading commands: " + e.getMessage());
        }
    }

    /**
     * Creates and starts a server with the given address on the given port number.
     * @param address the address for the server.
     * @param portNumber the port number for the server.
     * @throws IOException if server could not be created.
     */
    public Server(@NotNull String address, int portNumber) throws IOException {
        acceptSelector = Selector.open();
        readSelector = Selector.open();
        writeSelector = Selector.open();

        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);

        var listenAddress = new InetSocketAddress(address, portNumber);
        serverSocketChannel.socket().bind(listenAddress);
        serverSocketChannel.register(acceptSelector, SelectionKey.OP_ACCEPT);
        System.out.println("Server started on port " + portNumber + ".");

        acceptingThread = new Thread(() -> {
            try {
                this.accept();
            } catch (IOException e) {
                logger.severe("IOException while processing accept connection: " + e.getMessage());
            }
        });
        readingThread = new Thread(() -> {
            try {
                this.read();
            } catch (IOException e) {
                logger.severe("IOException while processing read connection: " + e.getMessage());
            }
        });
        writingThread = new Thread(() -> {
            try {
                this.write();
            } catch (IOException e) {
                logger.severe("IOException while processing write connection: " + e.getMessage());
            }
        });

        initializeThread(acceptingThread);
        initializeThread(readingThread);
        initializeThread(writingThread);
    }

    /** Starts the given process as a daemon. */
    private void initializeThread(Thread serverProcess) {
        serverProcess.setDaemon(true);
        serverProcess.start();
    }

    /**
     * Shuts down the server by interrupting all threads it is running and joining them.
     * @throws InterruptedException if joining threads was interrupted.
     */
    public void shutdown() throws InterruptedException {
        wasShutdown = true;
        acceptingThread.interrupt();
        readingThread.interrupt();
        writingThread.interrupt();
        acceptingThread.join();
        readingThread.join();
        writingThread.join();
    }

    /** Algorithm for the thread that accepts connections. */
    private void accept() throws IOException {
        while (!wasShutdown) {
            if (acceptSelector.select() == 0) {
                continue;
            }
            var iterator = acceptSelector.selectedKeys().iterator();
            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                if (key.isAcceptable()) {
                    var socketChannel = ((ServerSocketChannel)key.channel()).accept();
                    socketChannel.configureBlocking(false);
                    socketChannel.register(readSelector, SelectionKey.OP_READ, new TransmissionController(socketChannel));
                    readSelector.wakeup();
                    System.out.println("Connection Accepted: " + socketChannel.getLocalAddress() + "\n");
                } else {
                    logger.severe("Error: key not supported by server.");
                }
                iterator.remove();
            }
        }
    }

    /** Process for the thread that reads from clients' connections. */
    private void read() throws IOException {
        while (!wasShutdown) {
            if (readSelector.select() == 0) {
                continue;
            }
            var iterator = readSelector.selectedKeys().iterator();
            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                if (!key.isValid()) {
                    key.cancel();
                    continue;
                } else if (key.isReadable()) {
                    TransmissionController controller = (TransmissionController)key.attachment();
                    controller.processRead();
                } else {
                    logger.severe("Error: key not supported by server.");
                }
                iterator.remove();
            }
        }
    }

    /** Process for the thread that writes to clients' connections. */
    private void write() throws IOException {
        while (!wasShutdown) {
            writeSelector.selectedKeys().clear();
            if (writeSelector.select() == 0) {
                continue;
            }
            var iterator = writeSelector.selectedKeys().iterator();
            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                if (!key.isValid()) {
                    key.cancel();
                    continue;
                } else if (key.isWritable()) {
                    TransmissionController controller = (TransmissionController)key.attachment();
                    controller.processWrite(key);
                } else {
                    System.out.println("Error: key not supported by server.");
                }
                iterator.remove();
            }
        }
    }

    /**
     * Method that executes a get query and sends the result to the given transmission controller.
     * @param controller the controller to send the response to.
     * @param pathName the path of the file for which the get query should be executed.
     */
    private void executeGet(@NotNull TransmissionController controller, @NotNull String pathName) {
        System.out.println("Executing get for " + pathName);
        var path = Paths.get(pathName);
        if (!Files.isRegularFile(path)) {
            System.out.println("File does not exist.");
            controller.addQueryForIncorrectFile();
        }
        byte[] fileBytes = new byte[0];
        try {
            fileBytes = Files.readAllBytes(path);
        } catch (IOException e) {
            System.out.println("Failed to read file " + pathName);
        }
        byte[] lengthBytes = Ints.toByteArray(fileBytes.length);
        System.out.println("File size: " + fileBytes.length);
        controller.addOutputQuery(ArrayUtils.addAll(lengthBytes, fileBytes));
    }

    /**
     * Method that executes a list query and sends the result to the given transmission controller.
     * @param controller the controller to send the response to.
     * @param pathName the path of the file for which the list query should be executed.
     */
    private void executeList(@NotNull TransmissionController controller, @NotNull String pathName) {
        System.out.println("Executing list for " + pathName);
        var path = Paths.get(pathName);
        if (!Files.isDirectory(path)) {
            System.out.println("File does not exist or is not a directory.");
            controller.addQueryForIncorrectFile();
        }
        var fileList = path.toFile().listFiles();
        if (fileList == null) {
            System.out.println("Could not get list of files in directory.");
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

    /**
     * Method that converts information about a file into a byte array according to the server's protocol.
     * The byte array consists of an integer (the file name's size in bytes), the file name,
     * and a boolean (is the file a directory), concatenated.
     * @param file the file to serialize.
     * @return a byte array with the information about the given file.
     */
    private @NotNull byte[] fileToBytes(@NotNull File file) {
        String fileName = file.getName();
        byte[] isDirectory = new byte[]{(byte)(file.isDirectory() ? 1 : 0)};
        byte[] encodedFile = fileName.getBytes();
        return ArrayUtils.addAll(ArrayUtils.addAll(Ints.toByteArray(encodedFile.length), encodedFile), isDirectory);
    }

    /** Class responsible for all transmissions between the server and the clients' SocketChannels. */
    private class TransmissionController {
        private @NotNull SocketChannel channel;
        private @NotNull InputTransmission inputTransmission = new InputTransmission();
        private @NotNull OutputTransmission outputTransmission = new OutputTransmission();

        private TransmissionController(@NotNull SocketChannel channel) {
            this.channel = channel;
        }

        /**
         * Sets the query for the outputTransmission in accordance with the server's protocol.
         * The transmission consists of the length of the data followed by the data itself.
         * @param data a byte array that should be transmitted.
         */
        private void addOutputQuery(@NotNull byte[] data) {
            try {
                channel.register(writeSelector, SelectionKey.OP_WRITE, this);
                writeSelector.wakeup();
            } catch (ClosedChannelException e) {
                System.out.println("The client has disconnected");
                return;
            }
            outputTransmission.sendData(ByteBuffer.wrap(ArrayUtils.addAll(Ints.toByteArray(data.length), data)));
        }

        /** Adds an output query for a method called on an incorrect file. */
        private void addQueryForIncorrectFile() {
            addOutputQuery(Ints.toByteArray(-1));
        }

        /** Adds an output query for a failed method called on an incorrect file. */
        private void addFailedQuery() {
            addOutputQuery(Ints.toByteArray(-2));
        }

        /** Method that should be called when the channel is ready to be read from. */
        private void processRead() {
            System.out.println("Started processing read.");
            if (!inputTransmission.hasReadSize()) { // read size of next package
                inputTransmission.readSize();
            } else { // read the package data
                if (inputTransmission.packageSize <= Integer.BYTES) { // all packages have at least an int
                    System.out.println("Invalid package size: " + inputTransmission.packageSize);
                    addFailedQuery();
                    inputTransmission.reset();
                } else {
                    inputTransmission.readData();
                }
            }

            System.out.println("Read from current channel.");
            if (inputTransmission.hasReadData()) {
                inputTransmission.finalizeRead();
                int queryType = inputTransmission.queryTypeBuffer.getInt();
                String query = new String(inputTransmission.receivedData.array());
                if (queryType == 2) {
                    threadPool.submit(() -> executeGet(this, query));
                } else if (queryType == 1) {
                    threadPool.submit(() -> executeList(this, query));
                } else {
                    System.out.println("Invalid query type: " + queryType);
                    this.addFailedQuery();
                }
                System.out.println("Submitted a query.");
                inputTransmission.reset();
            }
        }

        /** Method that should be called when the channel is ready to be written to. */
        private void processWrite(@NotNull SelectionKey key) {
            System.out.println("Processing...");
            if (outputTransmission.hasSentData()) {
                System.out.println("to cancel");
                key.cancel();
            } else {
                System.out.println("writing..");
                outputTransmission.writeData();
            }
        }

        /** Class that controls the server's interaction with incoming data from clients' channel. */
        private class InputTransmission {
            private final int defaultPackageSize = -5;
            private @NotNull ByteBuffer packageSizeBuffer = ByteBuffer.allocate(Integer.BYTES);
            private @NotNull ByteBuffer queryTypeBuffer = ByteBuffer.allocate(Integer.BYTES);
            private @NotNull ByteBuffer receivedData = ByteBuffer.allocate(0);
            private int packageSize = defaultPackageSize;

            private boolean hasReadSize() {
                return packageSize != defaultPackageSize;
            }

            private boolean hasReadData() {
                return hasReadSize() && !receivedData.hasRemaining();
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
                readCorrectly(new ByteBuffer[]{queryTypeBuffer, receivedData});
            }

            private void readCorrectly(ByteBuffer[] byteBuffers) {
                try {
                    if (channel.read(byteBuffers) == -1) {
                        channel.close(); //closes channel elegantly if disconnect happened.
                    }
                } catch (IOException e) {
                    System.out.println("Failed read from channel: " + e.getMessage());
                }
            }

            private void finalizeRead() {
                receivedData.flip();
                queryTypeBuffer.flip();
            }

            private void reset() {
                packageSizeBuffer.clear();
                queryTypeBuffer.clear();
                packageSize = defaultPackageSize;
            }
        }

        /** Class that controls the server's interaction with the outgoing data to clients' channel. */
        private class OutputTransmission {
            private ByteBuffer packageToSend = ByteBuffer.allocate(0);

            private void sendData(ByteBuffer packageToSend) {
                checkState(hasSentData()); // transmissions should come by one as clients are blocking.
                this.packageToSend = packageToSend;
            }

            private boolean hasSentData() {
                return !packageToSend.hasRemaining();
            }

            private void writeData() {
                try {
                    channel.write(packageToSend);
                } catch (IOException e) {
                    System.out.println("Writing to channel failed:" + e.getMessage());
                }
            }
        }
    }
}