package fr.ladybug.team;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Arrays;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;


public class Client {

    private static BlockingQueue<Task> tasks;

    private static SocketChannel channel;
    private static Selector inputSelector;
    private static Selector writeSelector;
    private static Selector acceptSelector;
    private static Scanner scanner = new Scanner(System.in);

    private static final Object trigger = new Object();

    public static void main(String[] args) throws IOException, InterruptedException {
        printHelpMessage();
        inputSelector = Selector.open();
        writeSelector = Selector.open();

        new Thread(() -> fakeServer()).start();

        InetSocketAddress serverAddress = askAddress();
        channel = SocketChannel.open();
        channel.configureBlocking(false);
        if (channel.connect(serverAddress)) {
            senderInit();
        }
        channel.register(inputSelector, SelectionKey.OP_READ | SelectionKey.OP_CONNECT);

        tasks = new ArrayBlockingQueue<>(1, true);
        Thread receiverThread = new Thread(() -> receiver());
        receiverThread.start();



        while (true) {
            String[] command = askCommand().split("\\s+");
            if (command[0].equals("exit")) {
                break;
            }

            var task = new Task(getIdByName(command[0]), Arrays.copyOfRange(command, 1, command.length));
            tasks.put(task);
        }

        System.out.println("Goodbye!");
    }

    private static void fakeServer() {
        try {
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.bind(new InetSocketAddress(InetAddress.getLocalHost(), 8179));
            serverSocketChannel.accept();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void senderInit() throws ClosedChannelException {
        channel.register(writeSelector, SelectionKey.OP_WRITE);
        Thread senderThread = new Thread(() -> sender());
        senderThread.start();
    }

    private static int getIdByName(String name) {
        switch (name) {
            case "list":
                return 1;
            case "get":
                return 2;
            default:
                return -1;
        }
    }

   /* private static void connecter() {
        try {
            System.out.println("Connecter:\n");
            int ready = 0;
            while (true) {
                ready = acceptSelector.select();
                System.out.println("Selected smth");
                if (ready == 0)
                    continue;
                Set<SelectionKey> keys = acceptSelector.selectedKeys();
                var iterator = keys.iterator();
                while (iterator.hasNext()) {
                    var key = iterator.next();


                    iterator.remove();
                }
            }
        } catch (IOException ignore) {

        }
    }*/

    private static void receiver() {
        try {
            System.out.println("Receiver:\n");
            int ready = 0;
            while (true) {
                ready = inputSelector.select();
                if (ready == 0)
                    continue;
                Set<SelectionKey> keys = inputSelector.selectedKeys();
                var iterator = keys.iterator();
                while (iterator.hasNext()) {
                    var key = iterator.next();

                    if (key.isConnectable()) {
                        var channel = (SocketChannel) key.channel();
                        channel.finishConnect()
                        System.out.println("Ok!");
                        senderInit();
                    }

                    iterator.remove();
                }
            }
        } catch (IOException ignore) {

        }
    }

    private static void sender() {
        try {
            System.out.println("Init");
            int ready = 0;
            while (true) {
                var task = tasks.take();
                ready = writeSelector.select();
                if (ready == 0)
                    continue;
                Set<SelectionKey> keys = writeSelector.selectedKeys();
                var iterator = keys.iterator();
                while (iterator.hasNext()) {
                    var key = iterator.next();

                    if (key.isWritable()) {
                        var channel = (SocketChannel) key.channel();
                        var byteBuffer = ByteBuffer.wrap(task.toBytes());
                        channel.write(byteBuffer);
                    }

                    iterator.remove();
                }
            }
        }
        catch (IOException | InterruptedException ignore) {

        }
    }

    private static @NotNull String askCommand() {
        return scanner.nextLine();
    }

    private static InetSocketAddress askAddress() throws UnknownHostException {
        return new InetSocketAddress(InetAddress.getByName("172.20.52.213"), 8179);
    }

    private static void printHelpMessage() {
    }

    private static class Task {
        private int taskName;
        private String[] args;

        private Task(int taskName, String[] args) {
            this.taskName = taskName;
            this.args = args;
        }

        public int getTaskName() {
            return taskName;
        }

        public String[] getArgs() {
            return args;
        }

        public byte[] toBytes() throws IOException {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream(1024);
            var stream = new DataOutputStream(bytes);
            stream.writeInt(taskName);
            for (var arg : args) {
                stream.writeBytes(arg);
            }
            return bytes.toByteArray();
        }
    }
}
