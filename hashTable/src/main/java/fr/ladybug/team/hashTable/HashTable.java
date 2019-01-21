package fr.ladybug.team.hashTable;

import fr.ladybug.team.hashTable.List.MapEntry;

/** Hashtable of strings with dynamic density */
public class HashTable {

    /** Initial hash-space taken by the table */
    private final int INITIAL_SPACE = 2;
    /** Maximal average density of the table, where density = size / hash-space */
    private final int MAXIMAL_DENSITY = 2;
    /** A number representing the factor of hash-space expansion */
    private final int EXPAND_FACTOR = 2;
    private int size;
    private List[] lists;

    private static int mod(int x, int y)
    {
        int res = x % y;
        if (res < 0)
        {
            res += y;
        }
        return res;
    }

    /** Constructs empty table with certain initial space */
    public HashTable() {
        size = 0;
        lists = new List[INITIAL_SPACE];
        for (int i = 0; i < INITIAL_SPACE; i++) {
            lists[i] = new List();
        }
    }

    /** In case of high density expands hash-space */
    private void rebuild() {
        var elements = new MapEntry[lists.length][];
        for (int i = 0; i < lists.length; i++) {
            elements[i] = lists[i].getData();
        }
        lists = new List[lists.length * EXPAND_FACTOR];
        for (int i = 0; i < lists.length; i++) {
            lists[i] = new List();
        }
        for (MapEntry[] element : elements) {
            for (MapEntry mapEntry : element) {
                put(mapEntry.key, mapEntry.value);
            }
        }
    }

    public int getSize() {
        return size;
    }

    /**
     * Checks whether key is contained by the table or not
     * @param key key it searches by
     * @return boolean true if key is contained
     */
    public boolean contains(String key) {
        int position = mod(key.hashCode(), lists.length);
        return lists[position].contains(key);
    }

    /**
     * Gets value by key
     * @param key the key it searches by
     * @return value if exists, null otherwise
     */
    public String get(String key) {
        int position = mod(key.hashCode(), lists.length);
        return lists[position].get(key);
    }

    /**
     * Adds or changes value by key
     * @param key the key value of which is to be changed or added
     * @param value the value which is to be putted
     * @return previous value of this key, null if doesn't exist
     */
    public String put(String key, String value) {
        int position = mod(key.hashCode(), lists.length);

        boolean keyExistedBefore = lists[position].contains(key);
        String res = lists[position].put(key, value);
        if (!keyExistedBefore) {
            size++;
            if (getSize() > lists.length * MAXIMAL_DENSITY)
                rebuild();
        }
        return res;
    }

    /**
     * Removes element with specified key
     * @param key the key value of which is to be removed
     * @return last value by this key, null otherwise
     */
    public String remove(String key) {
        int position = mod(key.hashCode(), lists.length);
        String res = lists[position].remove(key);
        if (res != null) {
            size--;
        }
        return res;
    }

    /** Removes all elements from the table */
    public void clear() {
        size = 0;
        for (int i = 0; i < lists.length; i++) {
            lists[i] = new List();
        }
    }


}
