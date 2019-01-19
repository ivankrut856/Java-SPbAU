package fr.ladybug.team.hashTable;

/** Hashtable of strings with dynamic density */
public class HashTable {

    private int mod(int x, int y)
    {
        int res = x % y;
        if (res < 0)
        {
            res += y;
        }
        return res;
    }

    /** Initial hash-space taken by the table */
    private final int initialSpace = 2;
    /** Maximal average density of the table, where density = size / hash-space */
    private final int maximalDensity = 2;
    /** A number representing the factor of hash-space expansion */
    private final int expandFactor = 2;

    /** Constructs empty table with certain initial space */
    public HashTable() {
        size = 0;
        lists = new List[initialSpace];
        for (int i = 0; i < initialSpace; i++) {
            lists[i] = new List();
        }
    }

    /** In case of high density expands hash-space */
    private void rebuild() {
        MapEntry[][] elems = new MapEntry[lists.length][];
        for (int i = 0; i < lists.length; i++) {
            elems[i] = lists[i].getData();
        }
        lists = new List[lists.length * expandFactor];
        for (int i = 0; i < lists.length; i++) {
            lists[i] = new List();
        }
        for (MapEntry[] elem : elems) {
            for (MapEntry mapEntry : elem) {
                put(mapEntry.key, mapEntry.value);
            }
        }
    }

    private int size;
    private List[] lists;

    public int getSize() {
        return size;
    }

    /**
     * Checks whether key is contained by the table or not
     * @param key key it searches by
     * @return boolean true if key is contained
     */
    public boolean contains(String key) {
        int pos = mod(key.hashCode(), lists.length);
        return lists[pos].contains(key);
    }

    /**
     * Gets value by key
     * @param key the key it searches by
     * @return value if exists, null otherwise
     */
    public String get(String key) {
        int pos = mod(key.hashCode(), lists.length);
        return lists[pos].get(key);
    }

    /**
     * Adds or changes value by key
     * @param key the key value of which is to be changed or added
     * @param value the value which is to be putted
     * @return previous value of this key, null if doesn't exist
     */
    public String put(String key, String value) {
        int pos = mod(key.hashCode(), lists.length);

        String res = lists[pos].put(key, value);
        if (res == null) {
            size++;
            if (getSize() > lists.length * maximalDensity)
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
        int pos = mod(key.hashCode(), lists.length);
        String res = lists[pos].remove(key);
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
