package io.jpower.sgf.collection;

import java.util.Collection;
import java.util.ConcurrentModificationException;

/**
 * @author zheng.sun
 */
public class IntHashSet extends AbstractIntSet implements IntSet, Cloneable, java.io.Serializable {

    private static final long serialVersionUID = 6035850913102974648L;

    private transient IntHashMap<Object> map;

    // Dummy value to associate with an Object in the backing Map
    private static final Object PRESENT = new Object();

    /**
     * Constructs a new, empty set; the backing <tt>HashMap</tt> instance has
     * default initial capacity (16) and load factor (0.75).
     */
    public IntHashSet() {
        map = new IntHashMap<>();
    }

    /**
     * Constructs a new set containing the elements in the specified collection.
     * The <tt>HashMap</tt> is created with default load factor (0.75) and an
     * initial capacity sufficient to contain the elements in the specified
     * collection.
     *
     * @param c the collection whose elements are to be placed into this set
     * @throws NullPointerException if the specified collection is null
     */
    public IntHashSet(Collection<Integer> c) {
        map = new IntHashMap<>(Math.max((int) (c.size() / .75f) + 1, 16));
        addAll(c);
    }

    public IntHashSet(int[] a) {
        map = new IntHashMap<>(Math.max((int) (a.length / .75f) + 1, 16));
        addAll(a);
    }

    public IntHashSet(IntSet s) {
        map = new IntHashMap<>(Math.max((int) (s.size() / .75f) + 1, 16));
        addAll(s);
    }

    /**
     * Constructs a new, empty set; the backing <tt>HashMap</tt> instance has
     * the specified initial capacity and the specified load factor.
     *
     * @param initialCapacity the initial capacity of the hash map
     * @param loadFactor      the load factor of the hash map
     * @throws IllegalArgumentException if the initial capacity is less than zero, or if the load
     *                                  factor is nonpositive
     */
    public IntHashSet(int initialCapacity, float loadFactor) {
        map = new IntHashMap<>(initialCapacity, loadFactor);
    }

    public IntHashSet(int initialCapacity) {
        map = new IntHashMap<>(initialCapacity);
    }

    /**
     * Returns an iterator over the elements in this set. The elements are
     * returned in no particular order.
     *
     * @return an Iterator over the elements in this set
     * @see ConcurrentModificationException
     */
    public IntIterator iterator() {
        return map.keySet().iterator();
    }

    /**
     * Returns the number of elements in this set (its cardinality).
     *
     * @return the number of elements in this set (its cardinality)
     */
    public int size() {
        return map.size();
    }

    /**
     * Returns <tt>true</tt> if this set contains no elements.
     *
     * @return <tt>true</tt> if this set contains no elements
     */
    public boolean isEmpty() {
        return map.isEmpty();
    }

    /**
     * Returns <tt>true</tt> if this set contains the specified element. More
     * formally, returns <tt>true</tt> if and only if this set contains an
     * element <tt>e</tt> such that
     * <tt>(o==null&nbsp;?&nbsp;e==null&nbsp;:&nbsp;o.equals(e))</tt>.
     *
     * @param o element whose presence in this set is to be tested
     * @return <tt>true</tt> if this set contains the specified element
     */
    public boolean contains(int o) {
        return map.containsKey(o);
    }

    /**
     * Adds the specified element to this set if it is not already present. More
     * formally, adds the specified element <tt>e</tt> to this set if this set
     * contains no element <tt>e2</tt> such that
     * <tt>(e==null&nbsp;?&nbsp;e2==null&nbsp;:&nbsp;e.equals(e2))</tt>. If this
     * set already contains the element, the call leaves the set unchanged and
     * returns <tt>false</tt>.
     *
     * @param e element to be added to this set
     * @return <tt>true</tt> if this set did not already contain the specified
     * element
     */
    public boolean add(int e) {
        return map.put(e, PRESENT) == null;
    }

    /**
     * Removes the specified element from this set if it is present. More
     * formally, removes an element <tt>e</tt> such that
     * <tt>(o==null&nbsp;?&nbsp;e==null&nbsp;:&nbsp;o.equals(e))</tt>, if this
     * set contains such an element. Returns <tt>true</tt> if this set contained
     * the element (or equivalently, if this set changed as a result of the
     * call). (This set will not contain the element once the call returns.)
     *
     * @param o object to be removed from this set, if present
     * @return <tt>true</tt> if the set contained the specified element
     */
    public boolean remove(int o) {
        return map.remove(o) == PRESENT;
    }

    /**
     * Removes all of the elements from this set. The set will be empty after
     * this call returns.
     */
    public void clear() {
        map.clear();
    }

    /**
     * Returns a shallow copy of this <tt>HashSet</tt> instance: the elements
     * themselves are not cloned.
     *
     * @return a shallow copy of this set
     */
    @SuppressWarnings("unchecked")
    public Object clone() {
        try {
            IntHashSet newSet = (IntHashSet) super.clone();
            newSet.map = (IntHashMap<Object>) map.clone();
            return newSet;
        } catch (CloneNotSupportedException e) {
            throw new InternalError();
        }
    }

    /**
     * Save the state of this <tt>HashSet</tt> instance to a stream (that is,
     * serialize it).
     *
     * @serialData The capacity of the backing <tt>HashMap</tt> instance (int),
     * and its load factor (float) are emitted, followed by the size
     * of the set (the number of elements it contains) (int),
     * followed by all of its elements (each an Object) in no
     * particular order.
     */
    private void writeObject(java.io.ObjectOutputStream s) throws java.io.IOException {
        // Write out any hidden serialization magic
        s.defaultWriteObject();

        // Write out HashMap capacity and load factor
        s.writeInt(map.capacity());
        s.writeFloat(map.loadFactor());

        // Write out size
        s.writeInt(map.size());

        // Write out all elements in the proper order.
        for (IntMap.Entry<Object> e : map.entrySet())
            s.writeInt(e.getKey());
    }

    /**
     * Reconstitute the <tt>HashSet</tt> instance from a stream (that is,
     * deserialize it).
     */
    private void readObject(java.io.ObjectInputStream s)
            throws java.io.IOException, ClassNotFoundException {
        // Read in any hidden serialization magic
        s.defaultReadObject();

        // Read in HashMap capacity and load factor and create backing HashMap
        int capacity = s.readInt();
        float loadFactor = s.readFloat();
        map = new IntHashMap<Object>(capacity, loadFactor);

        // Read in size
        int size = s.readInt();

        // Read in all elements in the proper order.
        for (int i = 0; i < size; i++) {
            int e = s.readInt();
            map.put(e, PRESENT);
        }
    }

}
