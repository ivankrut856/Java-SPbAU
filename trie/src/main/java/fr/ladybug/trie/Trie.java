package fr.ladybug.trie;

import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


/** Trie -- Classical data structure */
public class Trie implements Serializable {
    /** Root of inner tree representation*/
    private TrieNode headNode = new TrieNode();

    /**
     * Method adds new element into the trie. Linear time
     * @param element the element to add to the trie
     * @return true if element already presented, false otherwise
     */
    public boolean add(@NotNull String element) {
        int currentPosition = 0;
        TrieNode currentNode = headNode;
        while (currentPosition < element.length()) {
            currentNode.passingThrough++;
            currentNode = currentNode.forceGo(element.charAt(currentPosition));
            currentPosition++;
        }
        boolean alreadyPresented = currentNode.endingsCount > 0;
        currentNode.endingsCount++;

        return alreadyPresented;
    }

    /**
     * Method checks whether element has been added to the trie or not. Linear time
     * @param element the element which presence it checks
     * @return true if element presented in the trie, false otherwise
     */
    public boolean contains(@NotNull String element) {
        int currentPosition = 0;
        TrieNode currentNode = headNode;
        while (currentPosition < element.length()) {
            if (!currentNode.canGo(element.charAt(currentPosition)))
                return false;
            currentNode = currentNode.forceGo(element.charAt(currentPosition));
            currentPosition++;
        }

        return currentNode.endingsCount > 0;
    }

    /**
     * Method removes element from the trie if it presented
     * @param element the element which is to be removed
     * @return true if element was presented before removal, false otherwise
     */
    public boolean remove(@NotNull String element) {
        if (!contains(element)) {
            return false;
        }
        int currentPosition = 0;
        TrieNode currentNode = headNode;
        while (currentPosition < element.length()) {
            currentNode.passingThrough--;
            currentNode = currentNode.forceGo(element.charAt(currentPosition));
            currentPosition++;
        }
        currentNode.endingsCount--;

        return true;
    }

    /**
     * Size-getter. Size is the property which shows how many elements are presented in the trie
     */
    public int getSize() {
        return headNode.passingThrough + headNode.endingsCount;
    }

    /**
     * Method calculates the number of elements in the trie which have certain prefix
     * @param prefix the prefix which is to be prefix of counted element
     * @return the number of elements with the prefix
     */
    public int howManyStartWithPrefix(@NotNull String prefix) {
        int currentPosition = 0;
        TrieNode currentNode = headNode;
        while (currentPosition < prefix.length()) {
            if (!currentNode.canGo(prefix.charAt(currentPosition)))
                return 0;
            currentNode = currentNode.forceGo(prefix.charAt(currentPosition));
            currentPosition++;
        }

        return currentNode.endingsCount + currentNode.passingThrough;
    }

    /** Serialization based on headNode serialization */
    @Override
    public void serialize(@NotNull OutputStream out) throws IOException {
        headNode.serialize(out);
    }

    /** Deserialization based on headNode deserialization */
    @Override
    public void deserialize(@NotNull InputStream in) throws IOException {
        headNode.deserialize(in);
    }

    /**
     * Inner-class implementing tree structure.
     */
    private class TrieNode implements Serializable {
        /** Links to another nodes by character */
        private HashMap<Character, TrieNode> links = new HashMap<>();
        /** How many elements have end at this node */
        private int endingsCount = 0;
        /** How many elements have end below (at the children of) this node */
        private int passingThrough = 0;

        /** Checks whether the node has link by this character */
        private boolean canGo(char c) {
            return links.containsKey(c);
        }

        /** If the link by certain character exists then returns endpoint of the link, otherwise creates them first */
        private @NotNull TrieNode forceGo(char c) {
            if (links.containsKey(c))
                return links.get(c);
            var newNode = new TrieNode();
            links.put(c, newNode);
            return newNode;
        }

        /** Field-wise serialization */
        @Override
        public void serialize(@NotNull OutputStream out) throws IOException {
            var stream = new DataOutputStream(out);
            serializeIntoDataStream(stream);
        }

        /** Serialization helper */
        private void serializeIntoDataStream(@NotNull DataOutputStream stream) throws IOException {
            stream.writeInt(endingsCount);
            stream.writeInt(passingThrough);
            stream.writeInt(links.size());
            for (var entry : links.entrySet()) {
                stream.writeChar(entry.getKey());
                entry.getValue().serializeIntoDataStream(stream);
            }
        }

        /** Field-wise deserialization */
        @Override
        public void deserialize(@NotNull InputStream in) throws IOException {
            var stream = new DataInputStream(in);
            deserializeFromDataStream(stream);
        }

        /** Serialization helper */
        private void deserializeFromDataStream(@NotNull DataInputStream stream) throws IOException {
            endingsCount = stream.readInt();
            if (endingsCount < 0)
                throw new StreamCorruptedException("Endings count cannot be negative. Stream corrupted");
            passingThrough = stream.readInt();
            if (passingThrough < 0)
                throw new StreamCorruptedException("Passing through count cannot be negative. Stream corrupted");
            int linksSize = stream.readInt();
            if (linksSize < 0)
                throw new StreamCorruptedException("Size of links collection cannot be negative. Stream corrupted");
            links = new HashMap<>();
            for (int i = 0; i < linksSize; i++) {
                char currentKey = stream.readChar();
                var currentChild = new TrieNode();
                currentChild.deserializeFromDataStream(stream);
                links.put(currentKey, currentChild);
            }
        }
    }
}

