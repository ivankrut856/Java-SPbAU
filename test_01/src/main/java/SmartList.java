import java.util.*;
import java.util.List;

/**
 * ArrayList structure with smart memory management
 * @param <E> the type of element in the store
 */
public class SmartList<E> extends AbstractList<E> implements List<E> {
    private int size;
    private Object data;

    /**
     * Emtpy-collection constructor
     */
    public SmartList() {
        data = null;
        size = 0;
    }

    /**
     * Copy-constructor from some collection
     * @param collection the collection which is source of data coping to this list
     */
    public SmartList(Collection<? super E> collection) {
        for (Object element : collection) {
            add(size, (E)element);
        }
    }


    @Override
    /**
     * Returns element from the list by index
     * @param index the index on which element is returned
     * @throws IndexOutOfBoundsException in case of {@code index < 0 or index > size}
     */
    public E get(int index) {
        if (index < 0 || index > size)
            throw new IndexOutOfBoundsException();

        if (size == 1)
            return (E)data;
        else if (2 <= size && size <= 5) {
            return ((E[])data)[index];
        }
        else {
            return ((ArrayList<E>)data).get(index);
        }
    }

    @Override
    /**
     * Sets element at the position in the list
     * @param index the position to set at
     * @param element the element to set
     * @return previous element at the position
     * @throws IndexOutOfBoundsException in case of {@code index < 0 or index > size}
     */
    public E set(int index, E element) {
        if (index < 0 || index >= size)
            throw new IndexOutOfBoundsException();

        if (size == 1) {
            E oldElement = (E)data;
            data = element;
            return oldElement;
        }
        else if (size <= 5) {
            E oldElement = ((E[])data)[index];
            ((E[])data)[index] = element;
            return oldElement;
        }
        else {
            return ((ArrayList<E>)data).set(index, element);
        }
    }

    @Override
    /**
     * Adds element to the collection
     * @param index the index to add after
     * @param element the element to add
     * @throws IndexOutOfBoundsException in case of {@code index < 0 or index > size}
     */
    public void add(int index, E element) {
        if (index < 0 || index > size)
            throw new IndexOutOfBoundsException();
        if (size == 0) {
            data = element;
        }
        else if (size == 1) {
            E old = (E) data;
            data = (E[])(new Object[5]);
            ((E[])data)[0] = old;
            addToInternalArray(index, element);
        }
        else if (2 <= size && size < 5) {
            addToInternalArray(index, element);
        }
        else if (size == 5) {
            var newStorage = new ArrayList<E>();
            for (int i = 0; i < size; i++) {
                newStorage.add(((E[])data)[i]);
            }
            newStorage.add(index, element);
            data = newStorage;
        }
        else {
            ((ArrayList<E>)data).add(index, element);
        }
        size++;
    }

    /** Procedure for add to array if this is array representation which is chosen */
    private void addToInternalArray(int index, E element) {
        for (int i = size - 1; i >= index; i--) {
            ((E[])data)[i + 1] = ((E[])data)[i];
        }
        ((E[])data)[index] = element;
    }

    @Override
    /**
     * Removes elements from the list by index
     * @param index the index to remove from
     * @return previous element at the position
     * @throws IndexOutOfBoundsException in case of {@code index < 0 || index > size}
     */
    public E remove(int index) {
        if (index < 0 || index >= size)
            throw new IndexOutOfBoundsException();

        if (size == 1) {
            E oldElement = (E)data;
            data = null;

            size--;
            return oldElement;
        }
        else if (size == 2) {
            E oldElement = ((E[])data)[index];
            data = ((E[])data)[1 - index];

            size--;
            return oldElement;

        }
        else if (2 < size && size <= 5) {
            E oldElement = ((E[])data)[index];
            for (int i = index; i < size - 1; i++) {
                ((E[])data)[i] = ((E[])data)[i + 1];
            }

            size--;
            return oldElement;
        }
        else if (size == 6) {
            E oldElement = ((ArrayList<E>)data).remove(index);

            var newStorage = (E[])(new Object[5]);
            for (int i = 0; i < size - 1; i++) {
                newStorage[i] = ((ArrayList<E>)data).get(i);
            }
            data = newStorage;

            size--;
            return oldElement;
        }
        else {
            size--;
            return ((ArrayList<E>)data).remove(index);
        }
    }

    @Override
    /** Returns size of the collection */
    public int size() {
        return size;
    }
}
