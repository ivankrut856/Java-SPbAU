package fr.ladybug.team.server;

import com.google.common.primitives.Ints;
import fr.ladybug.team.client.Query;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.logging.Logger;

import static com.google.common.base.Preconditions.checkState;
import static java.lang.Integer.max;

/** Class responsible for all transmissions between the server and the clients' SocketChannels. */
class TransmissionController {
    private @NotNull SocketChannel channel;
    private @NotNull static final Logger logger = Logger.getAnonymousLogger();

    private @NotNull final Selector writeSelector;
    private @NotNull final ExecutorService threadPool;

    private @NotNull InputTransmission inputTransmission = new InputTransmission();
    private @NotNull OutputTransmission outputTransmission = new OutputTransmission();

    /** Creates a TransmissionController that transmits to the given channel. */
    TransmissionController(@NotNull SocketChannel channel,
                           @NotNull Selector writeSelector, @NotNull ExecutorService threadPool) {
        this.channel = channel;
        this.writeSelector = writeSelector;
        this.threadPool = threadPool;
    }

    /**
     * Sets the query for the outputTransmission in accordance with the server's protocol.
     * The transmission consists of the length of the data followed by the data itself.
     * @param data a byte array that should be transmitted.
     */
    void addOutputQuery(@NotNull byte[] data) {
        try {
            channel.register(writeSelector, SelectionKey.OP_WRITE, this);
            writeSelector.wakeup();
        } catch (ClosedChannelException e) {
            logger.info("The client has disconnected.");
            return;
        }
        outputTransmission.sendData(ByteBuffer.wrap(ArrayUtils.addAll(Ints.toByteArray(data.length), data)));
    }

    /** Adds an output query for a method called on an incorrect file. */
    void addQueryForIncorrectFile() {
        addOutputQuery(Ints.toByteArray(-1));
    }

    /** Adds an output query for a failed method called on an incorrect file. */
    void addFailedQuery() {
        addOutputQuery(Ints.toByteArray(-2));
    }

    /** Method that should be called when the channel is ready to be read from. */
    void processRead() {
        try {
            logger.info("Trying to read from " + channel.getLocalAddress());
        } catch (IOException e) {
            logger.severe("Could not get local address of channel the controller is reading from.");
        }
        if (!inputTransmission.hasReadSize()) { // read size of next package
            inputTransmission.readSize();
        } else if (!inputTransmission.hasReadData()) { // read the package data
            inputTransmission.readData();
        }

        if (inputTransmission.hasReadData()) {
            logger.info("Read package with size " + inputTransmission.packageSize);
            if (!inputTransmission.isSizeCorrect()) {
                logger.severe("Invalid package size: " + inputTransmission.packageSize);
                addFailedQuery();
                inputTransmission.reset();
                return;
            }
            inputTransmission.finalizeRead();
            int queryType = inputTransmission.queryTypeBuffer.getInt();
            String query = new String(inputTransmission.receivedData.array());
            if (queryType == Query.QueryType.GET.value()) {
                threadPool.submit(() -> QueryExecutor.executeGet(this, query));
            } else if (queryType == Query.QueryType.LIST.value()) {
                threadPool.submit(() -> QueryExecutor.executeList(this, query));
            } else {
                logger.severe("Invalid query type: " + queryType);
                this.addFailedQuery();
            }
            logger.info("Submitted a query for " + query);
            inputTransmission.reset();
        }
    }

    /** Method that should be called when the channel is ready to be written to. */
    void processWrite(@NotNull SelectionKey key) {
        try {
            logger.info("Trying to write to " + channel.getLocalAddress());
        } catch (IOException e) {
            logger.severe("Could not get local address of channel the controller is writing to.");
        }
        if (outputTransmission.shouldSendData() && outputTransmission.hasSentData()) {
            logger.info("Sent response.");
            outputTransmission.finalizeWrite();
            key.cancel();
        } else if (outputTransmission.packageToSend != null) {
            outputTransmission.writeData();
        }
    }

    /** Class that controls the interaction with incoming data from clients' channel. */
    private class InputTransmission {
        private final int defaultPackageSize = -1; // a package size that сould not have been possibly sent.
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
                receivedData = ByteBuffer.allocate(max(0, packageSize - Integer.BYTES));
            }
        }

        private boolean isSizeCorrect() {
            return inputTransmission.packageSize > Integer.BYTES; // all packages have at least an int
        }

        private void readData() {
            readCorrectly(new ByteBuffer[]{queryTypeBuffer, receivedData});
        }

        private void readCorrectly(ByteBuffer[] byteBuffers) {
            try {
                while (true) {
                    long bytesRead = channel.read(byteBuffers);
                    if (bytesRead == 0)
                        break;
                    if (bytesRead == -1) {
                        logger.info("Closed channel " + channel.getLocalAddress());
                        channel.close(); //closes channel elegantly if disconnect happened.
                        break;
                    }

                }
            } catch (IOException e) {
                logger.severe("Failed read from channel: " + e.getMessage());
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

    /** Class that controls the interaction with the outgoing data to clients' channel. */
    private class OutputTransmission {
        private @Nullable ByteBuffer packageToSend;

        private void sendData(@NotNull ByteBuffer packageToSend) {
            checkState(this.packageToSend == null); // transmissions should come by one as clients are blocking.
            this.packageToSend = packageToSend;
        }

        private boolean shouldSendData() {
            return packageToSend != null;
        }

        private boolean hasSentData() {
            return packageToSend != null && !packageToSend.hasRemaining();
        }

        private void writeData() {
            checkState(packageToSend != null);
            try {
                channel.write(packageToSend);
            } catch (IOException e) {
                logger.severe("Writing to channel failed:" + e.getMessage());
            }
        }

        private void finalizeWrite() {
            packageToSend = null;
        }
    }
}