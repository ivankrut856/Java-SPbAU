package fr.ladybug.team;

import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.IntPredicate;

/**
 * This is set structure based on unbalanced binary tree
 * The elements of the set must implement Comparable<T> or comparator must be passed to constructor
 * The set cannot contain same elements
 * The set cannot contain null
 * @param <T> the type maintained by set
 */
public class MyTreeSetImpl<T> extends AbstractSet<T> implements MyTreeSet<T> {
    private int size = 0;
    private Comparator<? super T> comparator = null;
    /** Whether or not comparision inverted */
    private boolean descendingComparison = false;
    /** Root node of the inner tree */
    private TreeNode root = null;

    /** Empty constructor without comparator
     * Element of the new set must implement Comparable<T>
     */
    public MyTreeSetImpl() {
    }

    /** Empty constructor with comparator
     * All comparisons will be provided by the comparator
     * @param comparator the comparator which provides all comparisons
     */
    public MyTreeSetImpl(Comparator<? super T> comparator) {
        this.comparator = comparator;
    }

    /** Set iterator which direction is inverted */
    public @NotNull Iterator<T> descendingIterator() {
        return descendingSet().iterator();
    }

    /** Set iterator */
    @Override
    public @NotNull Iterator<T> iterator() {
        return new MyTreeSetIterator();
    }

    /** Returns size of the set */
    @Override
    public int size() {
        return size;
    }

    /** Return new set (backed by base set) with inverted comparisons */
    @Override
    public @NotNull MyTreeSetImpl<T> descendingSet() {
        var copySet = new MyTreeSetImpl<T>(comparator);
        copySet.root = root;
        copySet.size = size;
        copySet.descendingComparison = !descendingComparison;
        return copySet;
    }

    /** Returns first (lowest) element of the set, null if the set is empty */
    public T first() {
        if (root != null) {
            if (descendingComparison) {
                return root.last().value;
            }
            else {
                return root.first().value;
            }
        }
        return null;
    }

    /** Returns last (highest) element of the set, null if the set is empty */
    public T last() {
        if (root != null) {
            if (descendingComparison) {
                return root.first().value;
            }
            else {
                return root.last().value;
            }
        }
        return null;
    }

    /** Inner function for conditional compare (the set with comparator or not) */
    @SuppressWarnings("unchecked")
    private int innerCompare(T a, T b) {
        int result;
        if (comparator == null) {
            result = ((Comparable<T>)a).compareTo(b);
        }
        else {
            result = comparator.compare(a, b);
        }
        return result;
    }


    /** Returns greatest element which is less than given
     * @param element the element to be matched
     * @return greatest element which is less than given, null if no such element
     */
    public T lower(@NotNull T element) {
        return binarySearchPredicate(element, x -> x < 0, x -> x < 0);
    }

    /** Returns lowest element which is greater than given
     * @param element the element to be matched
     * @return lowest element which is greater than given, null if no such element
     */
    public T higher(@NotNull T element) {
        return binarySearchPredicate(element, x -> x > 0, x -> x <= 0);
    }


    /** Returns greatest element which is less or equal than given
     * @param element the element to be matched
     * @return greatest element which is less or equal than given, null if no such element
     */
    public T floor(@NotNull T element) {
        return binarySearchPredicate(element, x -> x <= 0, x -> x <= 0);
    }

    /** Returns lowest element which is greater or equal than given
     * @param element the element to be matched
     * @return lowest element which is greater or equal than given, null if no such element
     */
    public T ceiling(@NotNull T element) {
        return binarySearchPredicate(element, x -> x >= 0, x -> x < 0);
    }

    /**
     * Helper-method performs binarySearch and lookup by two predicate specifying behavior
     * @param element the element to lookup
     * @param lookPredicate the predicate used to decide to look at the current element or not
     * @param directionPredicate the predicate used to decide in which direction search goes
     * @return last element of the tree which was viewed or null if there were no such element
     */
    private T binarySearchPredicate(@NotNull T element, @NotNull IntPredicate lookPredicate, @NotNull IntPredicate directionPredicate) {
        TreeNode currentNode = root;
        T currentResult = null;
        while (currentNode != null) {
            int structuralResult = innerCompare(currentNode.value, element);
            int effectiveResult = descendingComparison ? -structuralResult : structuralResult;

            if (lookPredicate.test(effectiveResult)) {
                currentResult = currentNode.value;
            }
            if ((directionPredicate.test(effectiveResult)) ^ descendingComparison) {
                currentNode = currentNode.right;
            }
            else {
                currentNode = currentNode.left;
            }
        }
        return currentResult;
    }


    /** Adds new element to the set, throws IllegalArgumentException if the element which is to be added is null
     * @param element the element which is to be added
     * @return true if element added successfully, false if there were duplicate element already
     * @throws IllegalArgumentException if the element which is to be added is null
     */
    @Override
    public boolean add(@NotNull T element) throws IllegalArgumentException {
        //noinspection ConstantConditions
        if (element == null) {
            // Important! In runtime @NotNull does not work hence 'if' presented. Do not delete nor complain
            throw new IllegalArgumentException("Element cannot be null");
        }


        if (root == null) {
            root = new TreeNode(element);

            size++;
            return true;
        }

        TreeNode startNode = root;
        boolean added = false;
        while (!added) {
            int result = innerCompare(startNode.value, element);

            if (result == 0) {
                break;
            }
            else if (result < 0) {
                if (startNode.right == null) {
                    startNode.right = new TreeNode(element);
                    startNode.right.parent = startNode;

                    size++;
                    added = true;
                }
                else {
                    startNode = startNode.right;
                }
            }
            else {
                if (startNode.left == null) {
                    startNode.left = new TreeNode(element);
                    startNode.left.parent = startNode;

                    size++;
                    added = true;
                }
                else {
                    startNode = startNode.left;
                }
            }
        }

        return added;
    }

    /** Inner binary tree node class
     * Tree structure does not depend on comparison (inverted or not)
     * Outer method must maintain inversion by itself
     */
    private class TreeNode {
        private TreeNode left = null;
        private TreeNode right = null;
        private TreeNode parent = null;
        private T value;

        private TreeNode(@NotNull T x) {
            value = x;
        }

        private @NotNull TreeNode first() {
            if (left != null) {
                return left.first();
            }
            return this;
        }

        private @NotNull TreeNode last() {
            if (right != null) {
                return right.last();
            }
            return this;
        }

        private TreeNode next() {
            if (right != null) {
                return right.first();
            }
            TreeNode current = this;
            TreeNode candidate = parent;
            while (candidate != null) {
                if (candidate.left == current) {
                    return candidate;
                }
                current = candidate;
                candidate = current.parent;
            }
            return null;
        }

        private TreeNode previous() {
            if (left != null) {
                return left.last();
            }
            TreeNode current = this;
            TreeNode candidate = parent;
            while (candidate != null) {
                if (candidate.right == current) {
                    return candidate;
                }
                current = candidate;
                candidate = current.parent;
            }
            return null;
        }

    }

    /** Simple set-iterator class */
    public class MyTreeSetIterator implements Iterator<T> {
        /** Node just after the iterator */
        private TreeNode currentNode;
        /** Node just before the iterator */
        private TreeNode lastNextedNode = null;
        /** Whether node just before the iterator is just returned by next or not */
        private boolean hasNexted = false;

        /** Constructor sets iterator at the beginning of the parent set */
        private MyTreeSetIterator() {
            if (root == null) {
                return;
            }

            if (descendingComparison) {
                currentNode = root.last();
            }
            else {
                currentNode = root.first();
            }
        }

        /**
         * Returns whether iterator has next element to return or not
         * @return true if there are next element, false otherwise
         */
        @Override
        public boolean hasNext() {
            return currentNode != null;
        }

        /** Returns the element just after the iterator
         * @return next element
         * @throws NoSuchElementException if there no such element
         */
        @Override
        public @NotNull T next() {
            if (!hasNext())
                throw new NoSuchElementException();

            lastNextedNode = currentNode;
            if (descendingComparison) {
                currentNode = currentNode.previous();
            }
            else {
                currentNode = currentNode.next();
            }

            hasNexted = true;
            return lastNextedNode.value;
        }

        /**
         * Removes the element which is just returned by next
         * @throws IllegalStateException if there are no such element
         */
        @Override
        public void remove() {
            if (!hasNexted)
                throw new IllegalStateException();

            hasNexted = false;

            if (lastNextedNode.right == null) {
                if (lastNextedNode.left != null) {
                    lastNextedNode.left.parent = lastNextedNode.parent;
                }
                if (lastNextedNode.parent != null) {
                    if (lastNextedNode.parent.left == lastNextedNode) {
                        lastNextedNode.parent.left = lastNextedNode.left;
                    }
                    else {
                        lastNextedNode.parent.right = lastNextedNode.left;
                    }
                }
                else {
                    root = lastNextedNode.left;
                }
            }
            else {
                TreeNode next = lastNextedNode.next();
                T tmp = Objects.requireNonNull(next).value;
                next.value = lastNextedNode.value;
                lastNextedNode.value = tmp;

                // if (next.left == null) -- always true
                if (next.right != null) {
                    next.right.parent = next.parent;
                }
                if (next.parent != null) {
                    if (next.parent.left == next) {
                        next.parent.left = next.right;
                    }
                    else {
                        next.parent.right = next.right;
                    }
                }
                else {
                    root = next.right;
                }
            }
            size--;
        }
    }
}
