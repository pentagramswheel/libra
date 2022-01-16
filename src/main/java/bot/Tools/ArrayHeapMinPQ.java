package bot.Tools;

import java.util.NoSuchElementException;
import java.util.TreeMap;

/** @author  Wil Aquino
 *  Date:    October 26, 2020
 *  Project: Extrinsic MinPQ
 *  Module:  ArrayHeapMinPQ.java
 *  Purpose: Provides implementation of an extrinsic min-heap
 *           priority queue.
 */
public class ArrayHeapMinPQ<T> {

    /** ArrayList to store the min-heap in. */
    private Node[] heap;

    /** A set which maps all items to their corresponding node. */
    private final TreeMap<T, Node> map;

    /** Size of the queue. */
    private int size;

    /** The minimum usage of the queue. */
    private static final double MIN_USAGE = 0.25;

    /** Initialize the queue. */
    public ArrayHeapMinPQ() {
        heap = new ArrayHeapMinPQ.Node[8];
        heap[0] = null;
        size = 0;

        map = new TreeMap<>();
    }

    /**
     * Retrieves the index of the node left to the given index.
     * @param i the given index.
     * @return the left's index.
     */
    private int leftIndex(int i) {
        return 2 * i;
    }

    /**
     * Retrieves the index of the node right to the given index.
     * @param i the given index.
     * @return the right's index.
     */
    private int rightIndex(int i) {
        return (2 * i) + 1;
    }

    /**
     * Retrieves the parent index of the given index.
     * @param i the given index.
     * @return the index's parent index.
     */
    private int parentIndex(int i) {
        return i / 2;
    }

    /**
     * Retrieves the priority of the node at index i.
     * @param i the node index to look at.
     * @return node i's priority.
     */
    private double priority(int i) {
        return heap[i].getPriority();
    }

    /**
     * Resize the queue to the given capacity.
     * @param capacity the capacity of the resized queue.
     */
    private void resize(int capacity) {
        Node[] newArray = new ArrayHeapMinPQ.Node[capacity];
        System.arraycopy(heap, 0, newArray, 0, size() + 1);
        heap = newArray;
    }

    /**
     * Checks if an item is within the queue. Runs in O(log(n)) time.
     * @param item the item to check for.
     * @return whether the item is present or not.
     */
    public boolean contains(T item) {
        return map.containsKey(item);
    }

    /**
     * Swaps two nodes within the queue.
     * @param i the index of the first node.
     * @param j the index of the second node.
     */
    private void swap(int i, int j) {
        Node temp = heap[i];
        int store = heap[i].getIndex();

        heap[i].setIndex(heap[j].getIndex());
        heap[j].setIndex(store);

        heap[i] = heap[j];
        heap[j] = temp;
    }

    /**
     * Swim a node up the heap until it is in the right place.
     * @param i the index of the node to swim.
     */
    private void bubbleUp(int i) {
        int pIndex = parentIndex(i);
        boolean notAtRoot = pIndex != 0;
        boolean weShouldSwim = notAtRoot && priority(i) < priority(pIndex);

        if (weShouldSwim) {
            swap(i, pIndex);
            bubbleUp(pIndex);
        }
    }

    /**
     * Adds a non-null item, with a certain priority to the queue.
     * Runs in O(log(n)) amortized time.
     * @param item the item to insert.
     * @param priority the priority of the item within the queue.
     */
    public void add(T item, double priority) {
        if (contains(item)) {
            throw new IllegalArgumentException("Item is already present.");
        } else if (size() == heap.length - 1) {
            resize(2 * heap.length);
        }

        int end = size() + 1;

        Node newNode = new Node(item, priority, end);
        heap[end] = newNode;
        map.put(item, newNode);
        size++;

        bubbleUp(end);
    }

    /** Checks if the queue is empty. */
    private void checkIfEmpty() {
        if (size() == 0) {
            throw new NoSuchElementException("The queue is empty");
        }
    }

    /**
     * Retrieves the item with the smallest priority.
     * Runs in O(log(n)) time.
     * @return said smallest item.
     */
    public T getSmallest() {
        checkIfEmpty();
        return heap[1].getItem();
    }

    /**
     * Sink a node down the heap until it is in the right place.
     * @param i the index of the node to swim.
     */
    private void bubbleDown(int i) {
        int lIndex = leftIndex(i);
        int rIndex = rightIndex(i);

        boolean notAtEnd = i != size();

        boolean sinkLeft = lIndex <= size() && priority(lIndex) < priority(i);
        boolean sinkRight =
                rIndex <= size && priority(rIndex) < priority(i);
        boolean weShouldSink = notAtEnd && (sinkLeft || sinkRight);

        if (weShouldSink) {
            int smallerIndex = lIndex;
            if (rIndex <= size() && priority(rIndex) < priority(lIndex)) {
                smallerIndex = rIndex;
            }

            swap(i, smallerIndex);
            bubbleDown(smallerIndex);
        }
    }

    /**
     * Retrieves AND removes the item within the smallest priority.
     * Runs in O(log(n) amortized time.
     * @return said smallest item.
     */
    public T removeSmallest() {
        checkIfEmpty();
        T smallestItem = getSmallest();

        swap(1, size());
        heap[size()] = null;
        map.remove(smallestItem);
        size--;

        if (size() < MIN_USAGE * (heap.length - 1)) {
            resize(heap.length / 2);
        }
        bubbleDown(1);

        return smallestItem;
    }

    /**
     * Returns the number of items in the queue.
     * Runs in O(log(n)) time.
     * @return the size of the queue.
     */
    public int size() {
        return size;
    }

    /**
     * Changes the priority of an item within the queue.
     * Runs in O(log(n)) time.
     * @param item the item to change the priority of.
     * @param priority the new priority to set for the item.
     */
    public void changePriority(T item, double priority) {
        if (!contains(item)) {
            throw new NoSuchElementException("Item does not exist.");
        }

        map.get(item).setPriority(priority);

        int currIndex = map.get(item).getIndex();
        bubbleUp(currIndex);
        bubbleDown(currIndex);
    }

    /** Nested class for nodes within the queue. */
    private class Node {

        /** Storage for the item within the node. */
        private final T item;

        /** Priority of the item within the node. */
        private double priority;

        /** Index of the node within the queue. */
        private int index;

        /**
         * Initialize the node.
         * @param store the item to store within this node.
         * @param p the priority of this node.
         * @param i the index of this node.
         */
        Node(T store, double p, int i) {
            item = store;
            priority = p;
            index = i;
        }

        /**
         * Retrieve the item contained within this node.
         * @return said item.
         */
        public T getItem() {
            return item;
        }

        /**
         * Retrieve the priority of this node.
         * @return said priority.
         */
        public double getPriority() {
            return priority;
        }

        /**
         * Retrieve the index of this node.
         * @return said index.
         */
        public int getIndex() {
            return index;
        }

        /**
         * Set the priority of this node.
         * @param newPriority the priority to set.
         */
        public void setPriority(double newPriority) {
            priority = newPriority;
        }

        /**
         * Set the index of this node.
         * @param i the new index.
         */
        public void setIndex(int i) {
            index = i;
        }

        /**
         * Retrieves the string representation of the node.
         * @return said string.
         */
        @Override
        public String toString() {
            return "" + getPriority();
        }
    }
}
