package fr.ladybug.trie;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/** Simple serialization interface */
public interface Serializable {
    /**
     * Method writes serializable object into the output stream
     * @param out the stream into which output data is to be written
     * @throws IOException throws exception in case of general file issues
     */
    void serialize(OutputStream out) throws IOException;

    /**
     * Methods reads serializable object from the input stream
     * @param in the stream from which data is to be read
     * @throws IOException throws exception in case of general file issues
     * @throws java.io.StreamCorruptedException throws exception if class cannot be reconstructed with given data
     */
    void deserialize(InputStream in) throws IOException;
}
