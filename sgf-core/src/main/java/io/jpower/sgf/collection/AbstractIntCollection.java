package io.jpower.sgf.collection;

import java.util.Arrays;
import java.util.Collection;

/**
 * @author <a href="mailto:szhnet@gmail.com">szh</a>
 */
public abstract class AbstractIntCollection implements IntCollection {

    /**
     * Sole constructor. (For invocation by subclass constructors, typically
     * implicit.)
     */
    protected AbstractIntCollection() {
    }

    // Query Operations

    /**
     * Returns an iterator over the elements contained in this collection.
     *
     * @return an iterator over the elements contained in this collection
     */
    public abstract IntIterator iterator();

    public abstract int size();

    /**
     * {@inheritDoc}
     * <p>
     * <p>
     * This implementation returns <tt>size() == 0</tt>.
     */
    public boolean isEmpty() {
        return size() == 0;
    }

    /**
     * {@inheritDoc}
     * <p>
     * <p>
     * This implementation iterates over the elements in the collection,
     * checking each element in turn for equality with the specified element.
     *
     * @throws ClassCastException   {@inheritDoc}
     * @throws NullPointerException {@inheritDoc}
     */
    public boolean contains(int o) {
        IntIterator itr = iterator();
        while (itr.hasNext()) {
            if (o == itr.next()) {
                return true;
            }
        }
        return false;
    }

    /**
     * {@inheritDoc}
     * <p>
     * <p>
     * This implementation returns an array containing all the elements returned
     * by this collection's iterator, in the same order, stored in consecutive
     * elements of the array, starting with index {@code 0}. The length of the
     * returned array is equal to the number of elements returned by the
     * iterator, even if the size of this collection changes during iteration,
     * as might happen if the collection permits concurrent modification during
     * iteration. The {@code size} method is called only as an optimization
     * hint; the correct result is returned even if the iterator returns a
     * different number of elements.
     * <p>
     * <p>
     * This method is equivalent to:
     * <p>
     * <pre>
     * {
     *     &#64;code
     *     List<E> list = new ArrayList<E>(size());
     *     for (E e : this)
     *         list.add(e);
     *     return list.toArray();
     * }
     * </pre>
     */
    public int[] toArray() {
        // Estimate size of array; be prepared to see more or fewer elements
        int[] r = new int[size()];
        IntIterator itr = iterator();
        for (int i = 0; i < r.length; i++) {
            if (!itr.hasNext()) // fewer elements than expected
                return Arrays.copyOf(r, i);
            r[i] = itr.next();
        }
        return itr.hasNext() ? finishToArray(r, itr) : r;
    }

    /**
     * The maximum size of array to allocate. Some VMs reserve some header words
     * in an array. Attempts to allocate larger arrays may result in
     * OutOfMemoryError: Requested array size exceeds VM limit
     */
    private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;

    /**
     * Reallocates the array being used within toArray when the iterator
     * returned more elements than expected, and finishes filling it from the
     * iterator.
     *
     * @param r  the array, replete with previously stored elements
     * @param itr the in-progress iterator over this collection
     * @return array containing the elements in the given array, plus any
     * further elements returned by the iterator, trimmed to size
     */
    private static int[] finishToArray(int[] r, IntIterator itr) {
        int i = r.length;
        while (itr.hasNext()) {
            int cap = r.length;
            if (i == cap) {
                int newCap = cap + (cap >> 1) + 1;
                // overflow-conscious code
                if (newCap - MAX_ARRAY_SIZE > 0)
                    newCap = hugeCapacity(cap + 1);
                r = Arrays.copyOf(r, newCap);
            }
            r[i++] = itr.next();
        }
        // trim if overallocated
        return (i == r.length) ? r : Arrays.copyOf(r, i);
    }

    private static int hugeCapacity(int minCapacity) {
        if (minCapacity < 0) // overflow
            throw new OutOfMemoryError("Required array size too large");
        return (minCapacity > MAX_ARRAY_SIZE) ? Integer.MAX_VALUE : MAX_ARRAY_SIZE;
    }

    // Modification Operations

    /**
     * {@inheritDoc}
     * <p>
     * <p>
     * This implementation always throws an
     * <tt>UnsupportedOperationException</tt>.
     *
     * @throws UnsupportedOperationException {@inheritDoc}
     * @throws ClassCastException            {@inheritDoc}
     * @throws NullPointerException          {@inheritDoc}
     * @throws IllegalArgumentException      {@inheritDoc}
     * @throws IllegalStateException         {@inheritDoc}
     */
    public boolean add(int e) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     * <p>
     * <p>
     * This implementation iterates over the collection looking for the
     * specified element. If it finds the element, it removes the element from
     * the collection using the iterator's remove method.
     * <p>
     * <p>
     * Note that this implementation throws an
     * <tt>UnsupportedOperationException</tt> if the iterator returned by this
     * collection's iterator method does not implement the <tt>remove</tt>
     * method and this collection contains the specified object.
     *
     * @throws UnsupportedOperationException {@inheritDoc}
     * @throws ClassCastException            {@inheritDoc}
     * @throws NullPointerException          {@inheritDoc}
     */
    public boolean remove(int o) {
        IntIterator it = iterator();
        while (it.hasNext()) {
            if (o == it.next()) {
                it.remove();
                return true;
            }
        }
        return false;
    }

    // Bulk Operations

    /**
     * {@inheritDoc}
     * <p>
     * <p>
     * This implementation iterates over the specified collection, checking each
     * element returned by the iterator in turn to see if it's contained in this
     * collection. If all elements are so contained <tt>true</tt> is returned,
     * otherwise <tt>false</tt>.
     *
     * @throws ClassCastException   {@inheritDoc}
     * @throws NullPointerException {@inheritDoc}
     * @see #contains(int)
     */
    public boolean containsAll(Collection<Integer> c) {
        for (Integer e : c) {
            if (!contains(e)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean containsAll(int[] a) {
        for (int e : a) {
            if (!contains(e)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean containsAll(IntCollection c) {
        for (IntIterator itr = c.iterator(); itr.hasNext(); ) {
            if (!contains(itr.next())) {
                return false;
            }
        }
        return true;
    }

    /**
     * {@inheritDoc}
     * <p>
     * <p>
     * This implementation iterates over the specified collection, and adds each
     * object returned by the iterator to this collection, in turn.
     * <p>
     * <p>
     * Note that this implementation will throw an
     * <tt>UnsupportedOperationException</tt> unless <tt>add</tt> is overridden
     * (assuming the specified collection is non-empty).
     *
     * @throws UnsupportedOperationException {@inheritDoc}
     * @throws ClassCastException            {@inheritDoc}
     * @throws NullPointerException          {@inheritDoc}
     * @throws IllegalArgumentException      {@inheritDoc}
     * @throws IllegalStateException         {@inheritDoc}
     * @see #add(int)
     */
    public boolean addAll(Collection<Integer> c) {
        boolean modified = false;
        for (int e : c) {
            if (add(e)) {
                modified = true;
            }
        }
        return modified;
    }

    @Override
    public boolean addAll(int[] a) {
        boolean modified = false;
        for (int e : a) {
            if (add(e)) {
                modified = true;
            }
        }
        return modified;
    }

    @Override
    public boolean addAll(IntCollection c) {
        boolean modified = false;
        for (IntIterator itr = c.iterator(); itr.hasNext(); ) {
            if (add(itr.next())) {
                modified = true;
            }
        }
        return modified;
    }

    /**
     * {@inheritDoc}
     * <p>
     * <p>
     * This implementation iterates over this collection, checking each element
     * returned by the iterator in turn to see if it's contained in the
     * specified collection. If it's so contained, it's removed from this
     * collection with the iterator's <tt>remove</tt> method.
     * <p>
     * <p>
     * Note that this implementation will throw an
     * <tt>UnsupportedOperationException</tt> if the iterator returned by the
     * <tt>iterator</tt> method does not implement the <tt>remove</tt> method
     * and this collection contains one or more elements in common with the
     * specified collection.
     *
     * @throws UnsupportedOperationException {@inheritDoc}
     * @throws ClassCastException            {@inheritDoc}
     * @throws NullPointerException          {@inheritDoc}
     * @see #remove(int)
     * @see #contains(int)
     */
    public boolean removeAll(Collection<Integer> c) {
        boolean modified = false;
        IntIterator itr = iterator();
        while (itr.hasNext()) {
            if (c.contains(itr.next())) {
                itr.remove();
                modified = true;
            }
        }
        return modified;
    }

    @Override
    public boolean removeAll(int[] a) {
        boolean modified = false;
        IntIterator itr = iterator();
        while (itr.hasNext()) {
            if (contains(a, itr.next())) {
                itr.remove();
                modified = true;
            }
        }
        return modified;
    }

    @Override
    public boolean removeAll(IntCollection c) {
        boolean modified = false;
        IntIterator itr = iterator();
        while (itr.hasNext()) {
            if (c.contains(itr.next())) {
                itr.remove();
                modified = true;
            }
        }
        return modified;
    }

    /**
     * {@inheritDoc}
     * <p>
     * <p>
     * This implementation iterates over this collection, checking each element
     * returned by the iterator in turn to see if it's contained in the
     * specified collection. If it's not so contained, it's removed from this
     * collection with the iterator's <tt>remove</tt> method.
     * <p>
     * <p>
     * Note that this implementation will throw an
     * <tt>UnsupportedOperationException</tt> if the iterator returned by the
     * <tt>iterator</tt> method does not implement the <tt>remove</tt> method
     * and this collection contains one or more elements not present in the
     * specified collection.
     *
     * @throws UnsupportedOperationException {@inheritDoc}
     * @throws ClassCastException            {@inheritDoc}
     * @throws NullPointerException          {@inheritDoc}
     * @see #remove(int)
     * @see #contains(int)
     */
    public boolean retainAll(Collection<Integer> c) {
        boolean modified = false;
        IntIterator itr = iterator();
        while (itr.hasNext()) {
            if (!c.contains(itr.next())) {
                itr.remove();
                modified = true;
            }
        }
        return modified;
    }

    @Override
    public boolean retainAll(int[] a) {
        boolean modified = false;
        IntIterator itr = iterator();
        while (itr.hasNext()) {
            if (!contains(a, itr.next())) {
                itr.remove();
                modified = true;
            }
        }
        return modified;
    }

    private boolean contains(int[] array, int e) {
        for (int a : array) {
            if (a == e) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean retainAll(IntCollection c) {
        boolean modified = false;
        IntIterator itr = iterator();
        while (itr.hasNext()) {
            if (!c.contains(itr.next())) {
                itr.remove();
                modified = true;
            }
        }
        return modified;
    }

    /**
     * {@inheritDoc}
     * <p>
     * <p>
     * This implementation iterates over this collection, removing each element
     * using the <tt>Iterator.remove</tt> operation. Most implementations will
     * probably choose to override this method for efficiency.
     * <p>
     * <p>
     * Note that this implementation will throw an
     * <tt>UnsupportedOperationException</tt> if the iterator returned by this
     * collection's <tt>iterator</tt> method does not implement the
     * <tt>remove</tt> method and this collection is non-empty.
     *
     * @throws UnsupportedOperationException {@inheritDoc}
     */
    public void clear() {
        IntIterator it = iterator();
        while (it.hasNext()) {
            it.next();
            it.remove();
        }
    }

    // String conversion

    /**
     * Returns a string representation of this collection. The string
     * representation consists of a list of the collection's elements in the
     * order they are returned by its iterator, enclosed in square brackets
     * (<tt>"[]"</tt>). Adjacent elements are separated by the characters
     * <tt>", "</tt> (comma and space). Elements are converted to strings as by
     * {@link String#valueOf(Object)}.
     *
     * @return a string representation of this collection
     */
    public String toString() {
        IntIterator it = iterator();
        if (!it.hasNext())
            return "[]";

        StringBuilder sb = new StringBuilder();
        sb.append('[');
        for (; ; ) {
            int e = it.next();
            sb.append(e);
            if (!it.hasNext())
                return sb.append(']').toString();
            sb.append(',').append(' ');
        }
    }

}
