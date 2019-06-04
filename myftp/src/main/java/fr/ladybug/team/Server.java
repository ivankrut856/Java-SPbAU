package fr.ladybug.team;

import com.google.common.primitives.Ints;
import org.apache.commons.lang3.ArrayUtils;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
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
                    sc.register(selector, SelectionKey.
                            OP_READ);
                    sc.write(ByteBuffer.wrap("Connected".getBytes()));
                    System.out.println("Connection Accepted: " + sc.getLocalAddress() + "\n");
                } else if (key.isReadable()) {
                    processConnection(key);
                } else {
                    System.err.println("Error: key not supported by server.");
                }
            }
        }
    }

    private void processConnection(SelectionKey key) {
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        SocketChannel channel = (SocketChannel)key.channel();
        int bytesRead = -1;
        try {
            bytesRead = channel.read(buffer);
        } catch (IOException e) {
            System.err.println("IOException reading from channel.");
        }
        buffer.flip();
        int num = buffer.getInt();
        StringBuilder query = new StringBuilder();
        try {
            char next = buffer.getChar();
            while (next != '\0') {
                query.append(next);
                next = buffer.getChar();
            };
        } catch (BufferUnderflowException ignored) {
        }
        System.out.println("Received query number is " + num);
        System.out.println("Received query is " + query);
        try {
            if (num == 2) {
                executeGet(channel, query.substring(1));
            } else if (num == 1) {
                executeList(channel, query.substring(1));
            } else {
                System.err.println("Wrong query id");
            }
        } catch (IOException e) {
            System.err.println("IOException while reading query.");
        }
    }

    private int getDirectoryTree(File parent, ArrayList<String> result) {
        int size = 0;
        if (parent.isDirectory()) {
            File[] children = parent.listFiles();
            if (children != null) {
                for (var child : children) {
                    result.add(child.getPath() + " " + child.isDirectory());
                    size++;
                }
            }
        }
        return size;
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
//        Saved this as an example of normal-person buffer usage. Hopefully will never need it.
//        ByteBuffer buf = ByteBuffer.allocate(Integer.BYTES);
//        buf.putInt(-1);
//        buf.flip();
//        channel.write(buf);
        channel.write(ByteBuffer.wrap(Ints.toByteArray(-1)));
    }

    /**
     * Outputs the result of a successful operation to the given channel.
     * First it outputs the length of the remaining data, as an integer, then the data as a byte array.
     * @param channel the channel to write to.
     * @param result the result of the operation that should be sent through the channel.
     */
    private void writeSuccess(SocketChannel channel, byte[] result) throws IOException {
        channel.write(ByteBuffer.wrap(Ints.toByteArray(result.length)));
        channel.write(ByteBuffer.wrap(result));
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
}