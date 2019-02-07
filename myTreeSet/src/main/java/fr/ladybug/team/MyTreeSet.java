package fr.ladybug.team;

import org.jetbrains.annotations.NotNull;

import java.util.AbstractSet;
import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class MyTreeSet<T> extends AbstractSet<T> {
    private int size = 0;
    private Comparator<T> comparator = null;
    private boolean descendingComparision = false;
    private TreeNode root = null;

    private class TreeNode {
        private TreeNode left = null, right = null;
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
    public MyTreeSet() {
    }

    public MyTreeSet(Comparator<T> comparator) {
        this.comparator = comparator;
    }

    public @NotNull Iterator<T> descendingIterator() {
        return descendingSet().iterator();
    }

    @Override
    public @NotNull Iterator<T> iterator() {
        return new MyTreeSetIterator();
    }

    @Override
    public int size() {
        return size;
    }

    public @NotNull MyTreeSet<T> descendingSet() {
        var copySet = new MyTreeSet<T>(comparator);
        copySet.root = root;
        copySet.size = size;
        copySet.descendingComparision = !descendingComparision;
        return copySet;
    }

    public T first() {
        if (root != null) {
            return root.first().value;
        }
        return null;
    }

    public T last() {
        if (root != null) {
            return root.last().value;
        }
        return null;
    }

    public T lower(@NotNull T element) {
        return null;
    }

    public T floor(@NotNull T element) {
        return null;
    }

    public T ceiling(@NotNull T element) {
        return null;
    }

    public T higher(@NotNull T element) {
        return null;
    }


    @Override
    @SuppressWarnings("unchecked")
    public boolean add(T element) {
        if (element == null)
            throw new NullPointerException();

        if (root == null) {
            root = new TreeNode(element);

            size++;
            return true;
        }

        TreeNode startNode = root;
        while (true) {
            int result;
            if (comparator == null) {
                result = ((Comparable<T>)(startNode.value)).compareTo(element);
            }
            else {
                result = comparator.compare(startNode.value, element);
            }

//            Not actually right, saved for history
//            if (descendingComparision) {
//                result = -result;
//            }

            if (result == 0) {
                return false;
            }
            else if (result < 0) {
                if (startNode.right == null) {
                    startNode.right = new TreeNode(element);
                    startNode.right.parent = startNode;

                    size++;
                    return true;
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
                    return true;
                }
                else {
                    startNode = startNode.left;
                }
            }
        }

    }

    private class MyTreeSetIterator implements Iterator<T> {

        private TreeNode currentNode;
        private TreeNode lastNextedNode = null;
        private boolean hasNexted = false;

        private MyTreeSetIterator() {
            if (root == null) {
                return;
            }

            if (descendingComparision) {
                currentNode = root.last();
            }
            else {
                currentNode = root.first();
            }
        }

        @Override
        public boolean hasNext() {
            return currentNode != null;
        }

        @Override
        public @NotNull T next() {
            if (!hasNext())
                throw new NoSuchElementException();

            lastNextedNode = currentNode;
            if (descendingComparision) {
                currentNode = currentNode.previous();
            }
            else {
                currentNode = currentNode.next();
            }

            hasNexted = true;
            return lastNextedNode.value;
        }

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
                T tmp = next.value;
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
