package fr.ladybug.team;

import java.io.*;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.zip.DataFormatException;

public class Client {
    private Socket server;
    private InputStream inputStream;
    private OutputStream outputStream;

    public Client(String remoteAddress) throws IOException {
        server = new Socket(remoteAddress, 8179);
        System.out.println(server.isConnected());
        System.out.println("Connected tipa");
        inputStream = server.getInputStream();
        outputStream = server.getOutputStream();
        outputStream.write(34);

    }

    public byte[] makeQuery(Query query) throws IOException {
        query.goTo(outputStream);
        return readNextPackage(inputStream);
    }

    private byte[] readNextPackage(InputStream inputStream) throws IOException {
        DataInputStream dataInputStream = new DataInputStream(inputStream);
        int packageSize = dataInputStream.readInt();
        var bytes = dataInputStream.readNBytes(packageSize);

        if (bytes.length != packageSize)
            throw new IOException("Package's been fucked up");

        return bytes;
    }

    public void shutdown() throws IOException {
        if (server.isClosed())
            throw new IllegalStateException("The client is already shutdown");
        server.close();
    }
}
