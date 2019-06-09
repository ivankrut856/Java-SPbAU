package fr.ladybug.team.server;

import com.google.common.primitives.Ints;
import fr.ladybug.team.client.Query;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import static com.google.common.base.Preconditions.checkState;
import static java.lang.Integer.max;

/**
 * Class responsible for running a server that can execute the commands list and get.
 * The get command accepts the path to a file and sends the file's size and its content.
 * The list command accepts the path to a directory and sends the amount of Files in it, as well as a list of names,
 * and a boolean parameter that says whether a given File is a directory.
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
        Server server;
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
     * @param address    the address for the server.
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
        logger.info("Server started on port " + portNumber + ".");

        acceptingThread = new Thread(() -> {
            try {
                accept();
            } catch (IOException e) {
                logger.severe("IOException while processing accept connection: " + e.getMessage());
            }
        });
        readingThread = new Thread(() -> {
            try {
                read();
            } catch (IOException e) {
                logger.severe("IOException while processing read connection: " + e.getMessage());
            }
        });
        writingThread = new Thread(() -> {
            try {
                write();
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
        threadPool.shutdown();
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
                    var socketChannel = ((ServerSocketChannel) key.channel()).accept();
                    socketChannel.configureBlocking(false);
                    socketChannel.register(readSelector, SelectionKey.OP_READ,
                            new TransmissionController(socketChannel, writeSelector, threadPool));
                    readSelector.wakeup();
                    logger.info("Connection Accepted: " + socketChannel.getLocalAddress());
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
                    var controller = (TransmissionController) key.attachment();
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
                    var controller = (TransmissionController) key.attachment();
                    controller.processWrite(key);
                } else {
                    logger.severe("Error: key not supported by server.");
                }
                iterator.remove();
            }
        }
    }
}