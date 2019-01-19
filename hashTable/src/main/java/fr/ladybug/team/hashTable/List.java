package fr.ladybug.team.hashTable;

import java.util.Arrays;

/**
 * Autoexpandable key-value list of strings. Not autoshrinkable
 */
public class List {
    /** Actual size of list */
    private int size;
    /** Allocated space for elements (in elements) */
    private int capacity;

    private MapEntry[] data;

    /** Constructs empty list with additional space for 2 more elements */
    public List() {
        size = 0;
        capacity = 2;
        data = new MapEntry[capacity];
        for (int i = 0; i < capacity; i++) {
            data[i] = new MapEntry(null, null);
        }
    }

    /** In case of deficit of free space reallocates internal memory. Makes it 2 times bigger */
    private void rebuild() {
        MapEntry[] newData = Arrays.copyOf(data, capacity * 2);
        for (int i = capacity; i < capacity * 2; i++) {
            newData[i] = new MapEntry(null, null);
        }

        data = newData;
        capacity *= 2;
    }

    /**
     * Checks whether list contains key or not
     * @param key the key which is to be checked
     * @return boolean true if contains
     */
    public boolean contains(String key) {
        for (int i = 0; i < getSize(); i++) {
           if (data[i].key.equals(key)) {
               return true;
           }
        }
        return false;
    }

    /**
     * Gets value by key.
     * @param key the key value of which is to be returned
     * @return value in case key exists, null otherwise
     */
    public String get(String key) {
        for (int i = 0; i < getSize(); i++) {
            if (data[i].key.equals(key)) {
                return data[i].value;
            }
        }
        return null;
    }

    /**
     * Adds or changes value by key
     * @param key the key value of which is to be changed
     * @param value the value which is to be putted
     * @return previous value by this key, null if no such
     */
    public String put(String key, String value) {
        for (int i = 0; i < getSize(); i++) {
            if (data[i].key.equals(key)) {
                String val = data[i].value;
                data[i].value = value;
                return val;
            }
        }

        if (size >= capacity) {
            rebuild();
        }
        data[size].value = value;
        data[size].key = key;
        size++;
        return null;
    }

    /**
     * Removes value by key
     * @param key the key value of which is to be removed
     * @return last value by this key, null if no such
     */
    public String remove(String key) {
        boolean shifting = false;
        String res = null;
        for (int i = 0; i < getSize(); i++) {
            if (data[i].key.equals(key)) {
                shifting = true;
                res = data[i].value;
            }
            if (shifting && i + 1 < getSize()) {
                MapEntry tmp = data[i];
                data[i] = data[i + 1];
                data[i + 1] = tmp;
            }
        }

        size--;
        data[size] = new MapEntry(null, null);
        return res;
    }

    public int getSize() {
        return size;
    }

    /**
     * Copy-getter of internal array
     * @return Copy of internal array containing only real elements
     * */
    public MapEntry[] getData() {
        return Arrays.copyOf(data, size);
    }
}
