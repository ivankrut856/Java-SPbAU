package fr.ladybug.team;

import java.io.*;
import java.net.InetSocketAddress;
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
                    try (var os = new DataOutputStream(sc.socket().getOutputStream())) {
                        os.writeChars("Connected");
                    } catch (IOException e) {
                        //whatever
                    }
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
        SocketChannel channel = (SocketChannel)key.channel();
        try (var inputStream = new DataInputStream(channel.socket().getInputStream())) {
            int queryNumber = inputStream.readInt();
            String query = inputStream.readUTF();
            if (queryNumber == 1) {
                executeGet(new DataOutputStream(channel.socket().getOutputStream()), query.substring(1));
            } else if (queryNumber == 2) {
                executeList(new DataOutputStream(channel.socket().getOutputStream()), query.substring(1));
            } else {
                System.err.println("Malformed query.");
            }
        } catch (IOException e) {
            System.err.println("Socket failed to open IO streams.");
        }
    }

    private void executeGet(DataOutputStream output, String path) throws IOException {
        var file = new File(path);
        if (!file.isFile()) {
            output.writeInt(-1);
            return;
        }

        output.writeLong(file.length());
        try (var input = new DataInputStream(new FileInputStream(file))) {
            byte[] buffer = new byte[1024];
            for (int read = input.read(buffer); read != -1; read = input.read(buffer)) {
                output.write(buffer, 0, read);
            }
        }
    }

    private void executeList(DataOutputStream output, String path) throws IOException {
        var file = new File(path);
        if (!file.exists()) {
            output.writeInt(-1);
            return;
        }

        ArrayList<String> result = new ArrayList<>();
        int size = getDirectoryTree(file, result);
        output.writeInt(size);
        for (var info : result) {
            output.writeUTF(info);
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
