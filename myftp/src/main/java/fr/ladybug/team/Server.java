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
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Set;

public class Server {
    private Selector selector;
    private InetSocketAddress listenAddress;

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
        var server = new Thread(() -> {
            try {
                new Server().startServer(address, portNumber);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        server.setDaemon(false);
        server.start();


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

        while (server.isAlive()) {
            server.interrupt();
        }
    }

    public void startServer(String address, int portNumber) throws IOException {
        this.selector = Selector.open();
        ServerSocketChannel socketChannel = ServerSocketChannel.open();
        socketChannel.configureBlocking(false);

        listenAddress = new InetSocketAddress(address, portNumber);
        socketChannel.socket().bind(listenAddress);
        socketChannel.register(this.selector, SelectionKey.OP_ACCEPT);
        System.out.println("Server started on port " + portNumber + ".");

        while (!Thread.currentThread().isInterrupted()) {
            int readyCount = selector.select();
            if (readyCount == 0) {
                continue;
            }

            Set<SelectionKey> readyKeys = selector.selectedKeys();
            Iterator iterator = readyKeys.iterator();
            while (iterator.hasNext()) {
                SelectionKey key = (SelectionKey) iterator.next();

                // Remove key from set so we don't process it twice
                iterator.remove();

                if (key.isAcceptable()) {
                    SocketChannel sc = socketChannel.accept();
                    sc.configureBlocking(false);
                    sc.register(selector, SelectionKey.OP_READ, new InputTransmission());
                    sc.write(ByteBuffer.wrap("Connected".getBytes()));
                    System.out.println("Connection Accepted: " + sc.getLocalAddress() + "\n");
                } else if (key.isReadable()) {
                    processRead(key);
                } else {
                    System.err.println("Error: key not supported by server.");
                }
            }
        }
    }

    private void processRead(SelectionKey key) {
        InputTransmission currentStatus = (InputTransmission)key.attachment();
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
            // execute something??
        }
    }

    private void executeGet(SocketChannel output, String pathName) throws IOException {
        var path = Paths.get(pathName);
        if (!Files.isRegularFile(path)) {
            System.out.println("Nonexistent file.");
            writeFailure(output);
        }

        byte[] fileBytes = Files.readAllBytes(path);
        byte[] lengthBytes = Ints.toByteArray(fileBytes.length);
        System.out.println("File size: " + fileBytes.length);
        writeSuccess(output, ArrayUtils.addAll(lengthBytes, fileBytes));
    }

    private void executeList(SocketChannel output, String pathName) throws IOException {
        var path = Paths.get(pathName);
        if (!Files.exists(path)) {
            System.out.println("Nonexistent file");
            writeFailure(output);
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
        writeSuccess(output, result);
    }

    /**
     * Outputs the result of a failed operation to the given channel: -1 byte of information.
     * @param channel the channel to write to.
     */
    private void writeFailure(SocketChannel channel) throws IOException {
        var output = new OutputTransmission(ByteBuffer.wrap(Ints.toByteArray(-1)));
        //TODO register with selector
    }

    /**
     * Outputs the result of a successful operation to the given channel.
     * First it outputs the length of the remaining data, as an integer, then the data as a byte array.
     * @param channel the channel to write to.
     * @param result the result of the operation that should be sent through the channel.
     */
    private void writeSuccess(SocketChannel channel, byte[] result) throws IOException {
        var output = new OutputTransmission(ByteBuffer.wrap(ArrayUtils.addAll(Ints.toByteArray(result.length), result)));
        //TODO register with selector
    }

    /**
     * Gets the necessary info about the given file and converts it to a byte array.
     * Format: an integer (the file name length), a string (the file name) and a boolean (is the file a directory), concatenated.
     * @param file the file to get information about.
     * @return the resulting byte array.
     */
    private byte[] fileToBytes(File file) {
        String fileName = file.getName();
        byte[] isDirectory = new byte[]{(byte)(file.isDirectory() ? 1 : 0)};
        return ArrayUtils.addAll(ArrayUtils.addAll(Ints.toByteArray(fileName.length()), fileName.getBytes()), isDirectory);
    }

    private class InputTransmission {
        private final int defaultPackageSize = -5;
        private ByteBuffer packageSizeBuffer = ByteBuffer.allocate(Integer.BYTES);
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
                if (packageSize >= 0) {
                    receivedData = ByteBuffer.allocate(packageSize);
                }
            }
        }

        private void readData(SocketChannel channel) {
            try {
                channel.read(receivedData);
            } catch (IOException e) {
                System.err.println("Failed read from channel: " + e.getMessage());
            }
        }

        private void reset() {
            packageSizeBuffer = ByteBuffer.allocate(Integer.BYTES);
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