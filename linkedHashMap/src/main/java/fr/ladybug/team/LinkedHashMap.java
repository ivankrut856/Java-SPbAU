package fr.ladybug.team;

import org.jetbrains.annotations.NotNull;

import java.util.*;

public class LinkedHashMap<K, V> extends AbstractMap<K, V> {
    private LinkedSet entries = new LinkedSet();

    @Override
    public @NotNull Set<Entry<K, V>> entrySet() {
        return entries;
    }

    private class LinkedEntry extends AbstractMap.SimpleEntry<K, V> {
        public LinkedEntry(K key, V value) {
            super(key, value);
        }
    }

    public class LinkedSet extends AbstractSet<Entry<K, V>> {
        private int size = 0;
        private List<Entry<K, V>> innerOrder = new LinkedList();

        private int currentTableSize = 100;
        V[] hashMap = (V[])(new Object[currentTableSize]);

        private void rebuild() {
            //TODO
        }

        private int indexLookupByKey(@NotNull Object key) {
            int hashCode = ((key.hashCode() % currentTableSize) + currentTableSize) % currentTableSize;
            while (hashMap[hashCode] != null && hashMap[hashCode].equals(key)) {
                hashCode++;
                if (hashCode > currentTableSize)
                    hashCode -= currentTableSize;
            }
            return hashCode;
        }

        private void putToMap(@NotNull Entry<K, V> entry) {
            int index = indexLookupByKey(entry.getKey());
            hashMap[index] = entry.getValue();
        }

        private boolean containsInMap(@NotNull Object key) {
            int index = indexLookupByKey(key);
            return hashMap[index] != null;
        }

        public V extractFromMap(@NotNull Object key) {
            int index = indexLookupByKey(key);
            return hashMap[index];
        }

        @Override
        public @NotNull Iterator<Entry<K, V>> iterator() {
            return innerOrder.listIterator();
        }

        @Override
        public int size() {
            return size;
        }

        private class LinkedList extends AbstractSequentialList<Entry<K, V>> {
            private LinkedNode<Entry<K, V>> headNode = null;

            @Override
            public @NotNull ListIterator<Entry<K, V>> listIterator(int index) {
                return new LinkedListIterator(index);
            }

            @Override
            public int size() {
                return size;
            }

            private class LinkedListIterator implements ListIterator<Entry<K, V>> {
                private LinkedNode<Entry<K, V>> currentNode;
                private int currentIndex = 0;

                LinkedListIterator(int index) {
                    currentNode = headNode;
                    int times = index;
                    while (hasNext() && times > 0) {
                        times--;
                        next();
                    }
                    if (times != 0)
                        throw new IndexOutOfBoundsException();
                }

                @Override
                public boolean hasNext() {
                    while (currentNode != null && !containsInMap(currentNode.value)) {
                        next();
                        remove();
                    }
                    return currentNode != null;
                }

                @Override
                public Entry<K, V> next() {
                    if (!hasNext())
                        throw new NoSuchElementException();
                    currentNode = currentNode.next;
                    currentIndex++;
                    return currentNode.value;
                }

                @Override
                public boolean hasPrevious() {
                    throw new UnsupportedOperationException();
                }

                @Override
                public Entry<K, V> previous() {
                    throw new UnsupportedOperationException();
                }

                @Override
                public int nextIndex() {
                    return currentIndex + 1;
                }

                @Override
                public int previousIndex() {
                    throw new UnsupportedOperationException();
                }

                @Override
                public void remove() {
                    if (currentIndex == 0)
                        throw new IllegalStateException();
                    currentNode.previous = currentNode.previous.previous;
                    if (currentNode.previous != null) {
                        currentNode.previous.next = currentNode;
                    }
                    currentIndex--;
                }

                @Override
                public void set(@NotNull Entry<K, V> t) {
                    if (currentIndex == 0)
                        throw new IllegalStateException();
                    currentNode.previous.value = t;
                }

                @Override
                public void add(@NotNull Entry<K, V> t) {
                    if (currentIndex == 0) {
                        currentNode.previous = new LinkedNode();
                        currentNode.previous.value = t;
                        currentNode.previous.next = currentNode;
                    }
                    else {
                        currentNode.previous.next = new LinkedNode();
                        currentNode.previous.next
                                .value = t;
                        currentNode.previous.next
                                .previous = currentNode.previous;
                        currentNode.previous.next
                                .next = currentNode;
                    }
                    currentIndex++;
                    putToMap(t);
                }
            }
        }
    }

    @Override
    public boolean containsKey(@NotNull Object key) {
        return entries.containsInMap(key);
    }

    @Override
    public V get(@NotNull Object key) {
        return entries.extractFromMap(key);
    }

    @Override
    public V put(@NotNull K key, V value) {
        V current = get(key);
        entries.putToMap(new LinkedEntry(key, value));
        return current;
    }

    private static class LinkedNode<T> {
        LinkedNode<T> next = null;
        LinkedNode<T> previous = null;
        T value;
    }
}
