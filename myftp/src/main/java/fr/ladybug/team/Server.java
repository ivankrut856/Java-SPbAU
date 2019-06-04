package fr.ladybug.team;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
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

    private void executeGet(SocketChannel output, String path) throws IOException {
        ByteBuffer buf = ByteBuffer.allocate(1024);
        var file = new File(path);
        if (!file.isFile()) {
            System.out.println("Nonexistent file.");
            buf.putInt(-1);
            output.write(buf);
            return;
        }

        System.out.println("File length: " + file.length());
        buf.putLong(file.length());
        output.write(buf);
        try (var input = new DataInputStream(new FileInputStream(file))) {
            byte[] buffer = new byte[1024];
            for (int read = input.read(buffer); read != -1; read = input.read(buffer)) {
                output.write(ByteBuffer.wrap(buffer));
            }
        }
    }

    private void executeList(SocketChannel output, String path) throws IOException {
        ByteBuffer buf = ByteBuffer.allocate(4);
        var file = new File(path);
        if (!file.exists()) {
//            output.writeInt(-1);
            buf.putInt(-1);
            output.write(buf);
            return;
        }

        ArrayList<String> result = new ArrayList<>();
        int size = getDirectoryTree(file, result);
//        output.writeInt(size);
        buf.putInt(size);
        output.write(buf);
        for (var info : result) {
//            output.writeUTF(info);
            output.write(ByteBuffer.wrap(info.getBytes()));
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
}