package io.jpower.sgf.collection;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.Serializable;
import java.util.AbstractSet;
import java.util.Arrays;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;

/**
 * Value为double的HashMap
 * <p>
 * <ul>
 * <li>修改自java.util.HashMap</li>
 * </ul>
 *
 * @param <K> the type of keys maintained by this map
 * @author <a href="mailto:szhnet@gmail.com">szh</a>
 */
public class DoubleValueHashMap<K> extends AbstractDoubleValueMap<K>
        implements DoubleValueMap<K>, Cloneable, Serializable {

    /**
     * The default initial capacity - MUST be a power of two.
     */
    static final int DEFAULT_INITIAL_CAPACITY = 1 << 4; // aka 16

    /**
     * The maximum capacity, used if a higher value is implicitly specified by
     * either of the constructors with arguments. MUST be a power of two <=
     * 1<<30.
     */
    static final int MAXIMUM_CAPACITY = 1 << 30;

    /**
     * The load factor used when none specified in constructor.
     */
    static final float DEFAULT_LOAD_FACTOR = 0.75f;

    /**
     * An empty table instance to share when the table is not inflated.
     */
    static final Entry<?>[] EMPTY_TABLE = {};

    /**
     * The table, resized as necessary. Length MUST Always be a power of two.
     */
    @SuppressWarnings("unchecked")
    transient Entry<K>[] table = (Entry<K>[]) EMPTY_TABLE;

    /**
     * The number of key-value mappings contained in this map.
     */
    transient int size;

    /**
     * The next size value at which to resize (capacity * load factor).
     *
     * @serial
     */
    // If table == EMPTY_TABLE then this is the initial capacity at which the
    // table will be created when inflated.
    int threshold;

    /**
     * The load factor for the hash table.
     *
     * @serial
     */
    final float loadFactor;

    /**
     * The number of times this HashMap has been structurally modified
     * Structural modifications are those that change the number of mappings in
     * the HashMap or otherwise modify its internal structure (e.g., rehash).
     * This field is used to make iterators on Collection-views of the HashMap
     * fail-fast. (See ConcurrentModificationException).
     */
    transient int modCount;

    /**
     * Constructs an empty <tt>HashMap</tt> with the specified initial capacity
     * and load factor.
     *
     * @param initialCapacity the initial capacity
     * @param loadFactor      the load factor
     * @throws IllegalArgumentException if the initial capacity is negative or the load factor is
     *                                  nonpositive
     */
    public DoubleValueHashMap(int initialCapacity, float loadFactor) {
        if (initialCapacity < 0)
            throw new IllegalArgumentException("Illegal initial capacity: " + initialCapacity);
        if (initialCapacity > MAXIMUM_CAPACITY)
            initialCapacity = MAXIMUM_CAPACITY;
        if (loadFactor <= 0 || Float.isNaN(loadFactor))
            throw new IllegalArgumentException("Illegal load factor: " + loadFactor);

        this.loadFactor = loadFactor;
        threshold = initialCapacity;
        init();
    }

    /**
     * Constructs an empty <tt>HashMap</tt> with the specified initial capacity
     * and the default load factor (0.75).
     *
     * @param initialCapacity the initial capacity.
     * @throws IllegalArgumentException if the initial capacity is negative.
     */
    public DoubleValueHashMap(int initialCapacity) {
        this(initialCapacity, DEFAULT_LOAD_FACTOR);
    }

    /**
     * Constructs an empty <tt>HashMap</tt> with the default initial capacity
     * (16) and the default load factor (0.75).
     */
    public DoubleValueHashMap() {
        this(DEFAULT_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR);
    }

    /**
     * Constructs a new <tt>HashMap</tt> with the same mappings as the specified
     * <tt>Map</tt>. The <tt>HashMap</tt> is created with default load factor
     * (0.75) and an initial capacity sufficient to hold the mappings in the
     * specified <tt>Map</tt>.
     *
     * @param m the map whose mappings are to be placed in this map
     * @throws NullPointerException if the specified map is null
     */
    public DoubleValueHashMap(Map<? extends K, Double> m) {
        this(Math.max((int) (m.size() / DEFAULT_LOAD_FACTOR) + 1, DEFAULT_INITIAL_CAPACITY),
                DEFAULT_LOAD_FACTOR);
        inflateTable(threshold);

        putAllForCreate(m);
    }

    public DoubleValueHashMap(DoubleValueMap<K> m) {
        this(Math.max((int) (m.size() / DEFAULT_LOAD_FACTOR) + 1, DEFAULT_INITIAL_CAPACITY),
                DEFAULT_LOAD_FACTOR);
        inflateTable(threshold);

        putAllForCreate(m);
    }

    private static int roundUpToPowerOf2(int number) {
        // assert number >= 0 : "number must be non-negative";
        return number >= MAXIMUM_CAPACITY ? MAXIMUM_CAPACITY
                : (number > 1) ? Integer.highestOneBit((number - 1) << 1) : 1;
    }

    /**
     * Inflates the table.
     */
    @SuppressWarnings("unchecked")
    private void inflateTable(int toSize) {
        // Find a power of 2 >= toSize
        int capacity = roundUpToPowerOf2(toSize);

        threshold = (int) Math.min(capacity * loadFactor, MAXIMUM_CAPACITY + 1);
        table = new Entry[capacity];
    }

    // internal utilities

    /**
     * Initialization hook for subclasses. This method is called in all
     * constructors and pseudo-constructors (clone, readObject) after HashMap
     * has been initialized but before any entries have been inserted. (In the
     * absence of this method, readObject would require explicit knowledge of
     * subclasses.)
     */
    void init() {
    }

    /**
     * Retrieve object hash code and applies a supplemental hash function to the
     * result hash, which defends against poor quality hash functions. This is
     * critical because HashMap uses power-of-two length hash tables, that
     * otherwise encounter collisions for hashCodes that do not differ in lower
     * bits. Note: Null keys always map to hash 0, thus index 0.
     */
    final int hash(Object k) {
        int h = k.hashCode();

        // This function ensures that hashCodes that differ only by
        // constant multiples at each bit position have a bounded
        // number of collisions (approximately 8 at default load factor).
        h ^= (h >>> 20) ^ (h >>> 12);
        return h ^ (h >>> 7) ^ (h >>> 4);
    }

    /**
     * Returns index for hash code h.
     */
    static int indexFor(int h, int length) {
        // assert Integer.bitCount(length) == 1 : "length must be a non-zero
        // power of 2";
        return h & (length - 1);
    }

    /**
     * Returns the number of key-value mappings in this map.
     *
     * @return the number of key-value mappings in this map
     */
    public int size() {
        return size;
    }

    /**
     * Returns <tt>true</tt> if this map contains no key-value mappings.
     *
     * @return <tt>true</tt> if this map contains no key-value mappings
     */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Returns the value to which the specified key is mapped, or {@code null}
     * if this map contains no mapping for the key.
     * <p>
     * <p>
     * More formally, if this map contains a mapping from a key {@code k} to a
     * value {@code v} such that {@code (key==null ? k==null :
     * key.equals(k))}, then this method returns {@code v}; otherwise it returns
     * {@code null}. (There can be at most one such mapping.)
     * <p>
     * <p>
     * A return value of {@code null} does not <i>necessarily</i> indicate that
     * the map contains no mapping for the key; it's also possible that the map
     * explicitly maps the key to {@code null}. The {@link #containsKey
     * containsKey} operation may be used to distinguish these two cases.
     *
     * @see #put(Object, double)
     */
    public double get(Object key) {
        return get(key, DEFAULT_VALUE);
    }

    public double get(Object key, double defaultrRetVal) {
        if (key == null)
            return getForNullKey(defaultrRetVal);
        Entry<K> entry = getEntry(key);

        return null == entry ? defaultrRetVal : entry.getValue();
    }

    /**
     * Offloaded version of get() to look up null keys. Null keys map to index
     * 0. This null case is split out into separate methods for the sake of
     * performance in the two most commonly used operations (get and put), but
     * incorporated with conditionals in others.
     */
    private double getForNullKey(double defaultRetVal) {
        if (size == 0) {
            return defaultRetVal;
        }
        for (Entry<K> e = table[0]; e != null; e = e.next) {
            if (e.key == null)
                return e.value;
        }
        return defaultRetVal;
    }

    /**
     * Returns <tt>true</tt> if this map contains a mapping for the specified
     * key.
     *
     * @param key The key whose presence in this map is to be tested
     * @return <tt>true</tt> if this map contains a mapping for the specified
     * key.
     */
    public boolean containsKey(Object key) {
        return getEntry(key) != null;
    }

    /**
     * Returns the entry associated with the specified key in the HashMap.
     * Returns null if the HashMap contains no mapping for the key.
     */
    final Entry<K> getEntry(Object key) {
        if (size == 0) {
            return null;
        }

        int hash = (key == null) ? 0 : hash(key);
        for (Entry<K> e = table[indexFor(hash, table.length)]; e != null; e = e.next) {
            Object k;
            if (e.hash == hash && ((k = e.key) == key || (key != null && key.equals(k))))
                return e;
        }
        return null;
    }

    /**
     * Associates the specified value with the specified key in this map. If the
     * map previously contained a mapping for the key, the old value is
     * replaced.
     *
     * @param key   key with which the specified value is to be associated
     * @param value value to be associated with the specified key
     * @return the previous value associated with <tt>key</tt>, or <tt>null</tt>
     * if there was no mapping for <tt>key</tt>. (A <tt>null</tt> return
     * can also indicate that the map previously associated
     * <tt>null</tt> with <tt>key</tt>.)
     */
    public double put(K key, double value) {
        return put(key, value, DEFAULT_VALUE);
    }

    public double put(K key, double value, double defaultRetVal) {
        if (table == EMPTY_TABLE) {
            inflateTable(threshold);
        }
        if (key == null)
            return putForNullKey(value, defaultRetVal);
        int hash = hash(key);
        int i = indexFor(hash, table.length);
        for (Entry<K> e = table[i]; e != null; e = e.next) {
            Object k;
            if (e.hash == hash && ((k = e.key) == key || key.equals(k))) {
                double oldValue = e.value;
                e.value = value;
                e.recordAccess(this);
                return oldValue;
            }
        }

        modCount++;
        addEntry(hash, key, value, i);
        return defaultRetVal;
    }

    /**
     * Offloaded version of put for null keys
     */
    private double putForNullKey(double value, double defaultRetVal) {
        for (Entry<K> e = table[0]; e != null; e = e.next) {
            if (e.key == null) {
                double oldValue = e.value;
                e.value = value;
                e.recordAccess(this);
                return oldValue;
            }
        }
        modCount++;
        addEntry(0, null, value, 0);
        return defaultRetVal;
    }

    /**
     * This method is used instead of put by constructors and pseudoconstructors
     * (clone, readObject). It does not resize the table, check for
     * comodification, etc. It calls createEntry rather than addEntry.
     */
    private void putForCreate(K key, double value) {
        int hash = null == key ? 0 : hash(key);
        int i = indexFor(hash, table.length);

        /**
         * Look for preexisting entry for key. This will never happen for clone
         * or deserialize. It will only happen for construction if the input Map
         * is a sorted map whose ordering is inconsistent w/ equals.
         */
        for (Entry<K> e = table[i]; e != null; e = e.next) {
            Object k;
            if (e.hash == hash && ((k = e.key) == key || (key != null && key.equals(k)))) {
                e.value = value;
                return;
            }
        }

        createEntry(hash, key, value, i);
    }

    private void putAllForCreate(Map<? extends K, Double> m) {
        for (Map.Entry<? extends K, Double> e : m.entrySet())
            putForCreate(e.getKey(), e.getValue());
    }

    private void putAllForCreate(DoubleValueMap<? extends K> m) {
        for (DoubleValueMap.Entry<? extends K> e : m.entrySet())
            putForCreate(e.getKey(), e.getValue());
    }

    /**
     * Rehashes the contents of this map into a new array with a larger
     * capacity. This method is called automatically when the number of keys in
     * this map reaches its threshold.
     * <p>
     * If current capacity is MAXIMUM_CAPACITY, this method does not resize the
     * map, but sets threshold to Integer.MAX_VALUE. This has the effect of
     * preventing future calls.
     *
     * @param newCapacity the new capacity, MUST be a power of two; must be greater than
     *                    current capacity unless current capacity is MAXIMUM_CAPACITY
     *                    (in which case value is irrelevant).
     */
    void resize(int newCapacity) {
        Entry<K>[] oldTable = table;
        int oldCapacity = oldTable.length;
        if (oldCapacity == MAXIMUM_CAPACITY) {
            threshold = Integer.MAX_VALUE;
            return;
        }

        @SuppressWarnings("unchecked")
        Entry<K>[] newTable = new Entry[newCapacity];
        transfer(newTable);
        table = newTable;
        threshold = (int) Math.min(newCapacity * loadFactor, MAXIMUM_CAPACITY + 1);
    }

    /**
     * Transfers all entries from current table to newTable.
     */
    void transfer(Entry<K>[] newTable) {
        int newCapacity = newTable.length;
        for (Entry<K> e : table) {
            while (null != e) {
                Entry<K> next = e.next;
                int i = indexFor(e.hash, newCapacity);
                e.next = newTable[i];
                newTable[i] = e;
                e = next;
            }
        }
    }

    /**
     * Copies all of the mappings from the specified map to this map. These
     * mappings will replace any mappings that this map had for any of the keys
     * currently in the specified map.
     *
     * @param m mappings to be stored in this map
     * @throws NullPointerException if the specified map is null
     */
    public void putAll(Map<? extends K, Double> m) {
        int numKeysToBeAdded = m.size();
        if (numKeysToBeAdded == 0)
            return;

        if (table == EMPTY_TABLE) {
            inflateTable((int) Math.max(numKeysToBeAdded * loadFactor, threshold));
        }

        /*
         * Expand the map if the map if the number of mappings to be added is
         * greater than or equal to threshold. This is conservative; the obvious
         * condition is (m.size() + size) >= threshold, but this condition could
         * result in a map with twice the appropriate capacity, if the keys to
         * be added overlap with the keys already in this map. By using the
         * conservative calculation, we subject ourself to at most one extra
         * resize.
         */
        if (numKeysToBeAdded > threshold) {
            int targetCapacity = (int) (numKeysToBeAdded / loadFactor + 1);
            if (targetCapacity > MAXIMUM_CAPACITY)
                targetCapacity = MAXIMUM_CAPACITY;
            int newCapacity = table.length;
            while (newCapacity < targetCapacity)
                newCapacity <<= 1;
            if (newCapacity > table.length)
                resize(newCapacity);
        }

        for (Map.Entry<? extends K, Double> e : m.entrySet())
            put(e.getKey(), e.getValue());
    }

    public void putAll(DoubleValueMap<K> m) {
        int numKeysToBeAdded = m.size();
        if (numKeysToBeAdded == 0)
            return;

        if (table == EMPTY_TABLE) {
            inflateTable((int) Math.max(numKeysToBeAdded * loadFactor, threshold));
        }

        /*
         * Expand the map if the map if the number of mappings to be added is
         * greater than or equal to threshold. This is conservative; the obvious
         * condition is (m.size() + size) >= threshold, but this condition could
         * result in a map with twice the appropriate capacity, if the keys to
         * be added overlap with the keys already in this map. By using the
         * conservative calculation, we subject ourself to at most one extra
         * resize.
         */
        if (numKeysToBeAdded > threshold) {
            int targetCapacity = (int) (numKeysToBeAdded / loadFactor + 1);
            if (targetCapacity > MAXIMUM_CAPACITY)
                targetCapacity = MAXIMUM_CAPACITY;
            int newCapacity = table.length;
            while (newCapacity < targetCapacity)
                newCapacity <<= 1;
            if (newCapacity > table.length)
                resize(newCapacity);
        }

        for (DoubleValueMap.Entry<? extends K> e : m.entrySet())
            put(e.getKey(), e.getValue());
    }

    /**
     * Removes the mapping for the specified key from this map if present.
     *
     * @param key key whose mapping is to be removed from the map
     * @return the previous value associated with <tt>key</tt>, or <tt>null</tt>
     * if there was no mapping for <tt>key</tt>. (A <tt>null</tt> return
     * can also indicate that the map previously associated
     * <tt>null</tt> with <tt>key</tt>.)
     */
    public double remove(Object key) {
        return remove(key, DEFAULT_VALUE);
    }

    public double remove(Object key, double defaultRetVal) {
        Entry<K> e = removeEntryForKey(key);
        return (e == null ? defaultRetVal : e.value);
    }

    /**
     * Removes and returns the entry associated with the specified key in the
     * HashMap. Returns null if the HashMap contains no mapping for this key.
     */
    final Entry<K> removeEntryForKey(Object key) {
        if (size == 0) {
            return null;
        }
        int hash = (key == null) ? 0 : hash(key);
        int i = indexFor(hash, table.length);
        Entry<K> prev = table[i];
        Entry<K> e = prev;

        while (e != null) {
            Entry<K> next = e.next;
            Object k;
            if (e.hash == hash && ((k = e.key) == key || (key != null && key.equals(k)))) {
                modCount++;
                size--;
                if (prev == e)
                    table[i] = next;
                else
                    prev.next = next;
                e.recordRemoval(this);
                return e;
            }
            prev = e;
            e = next;
        }

        return e;
    }

    /**
     * Special version of remove for EntrySet using {@code Map.Entry.equals()}
     * for matching.
     */
    final Entry<K> removeMapping(Object o) {
        if (size == 0 || !(o instanceof DoubleValueMap.Entry))
            return null;

        @SuppressWarnings("unchecked")
        DoubleValueMap.Entry<K> entry = (DoubleValueMap.Entry<K>) o;
        Object key = entry.getKey();
        int hash = (key == null) ? 0 : hash(key);
        int i = indexFor(hash, table.length);
        Entry<K> prev = table[i];
        Entry<K> e = prev;

        while (e != null) {
            Entry<K> next = e.next;
            if (e.hash == hash && e.equals(entry)) {
                modCount++;
                size--;
                if (prev == e)
                    table[i] = next;
                else
                    prev.next = next;
                e.recordRemoval(this);
                return e;
            }
            prev = e;
            e = next;
        }

        return e;
    }

    /**
     * Removes all of the mappings from this map. The map will be empty after
     * this call returns.
     */
    public void clear() {
        modCount++;
        Arrays.fill(table, null);
        size = 0;
    }

    /**
     * Returns <tt>true</tt> if this map maps one or more keys to the specified
     * value.
     *
     * @param value value whose presence in this map is to be tested
     * @return <tt>true</tt> if this map maps one or more keys to the specified
     * value
     */
    public boolean containsValue(double value) {
        Entry<K>[] tab = table;
        for (int i = 0; i < tab.length; i++)
            for (Entry<K> e = tab[i]; e != null; e = e.next)
                if (value == e.value)
                    return true;
        return false;
    }

    /**
     * Returns a shallow copy of this <tt>HashMap</tt> instance: the keys and
     * values themselves are not cloned.
     *
     * @return a shallow copy of this map
     */
    @SuppressWarnings("unchecked")
    public Object clone() {
        DoubleValueHashMap<K> result = null;
        try {
            result = (DoubleValueHashMap<K>) super.clone();
        } catch (CloneNotSupportedException e) {
            // assert false;
        }
        if (result.table != EMPTY_TABLE) {
            result.inflateTable(Math.min((int) Math.min(size * Math.min(1 / loadFactor, 4.0f),
                    // we have limits...
                    DoubleValueHashMap.MAXIMUM_CAPACITY), table.length));
        }
        result.entrySet = null;
        result.modCount = 0;
        result.size = 0;
        result.init();
        result.putAllForCreate(this);

        return result;
    }

    static class Entry<K> implements DoubleValueMap.Entry<K> {
        final K key;
        double value;
        Entry<K> next;
        int hash;

        /**
         * Creates new entry.
         */
        Entry(int h, K k, double v, Entry<K> n) {
            value = v;
            next = n;
            key = k;
            hash = h;
        }

        public final K getKey() {
            return key;
        }

        public final double getValue() {
            return value;
        }

        public final double setValue(double newValue) {
            double oldValue = value;
            value = newValue;
            return oldValue;
        }

        public final boolean equals(Object o) {
            if (!(o instanceof DoubleValueMap.Entry))
                return false;
            @SuppressWarnings("rawtypes")
            DoubleValueMap.Entry e = (DoubleValueMap.Entry) o;
            Object k1 = getKey();
            Object k2 = e.getKey();
            if (k1 == k2 || (k1 != null && k1.equals(k2))) {
                double v1 = getValue();
                double v2 = e.getValue();
                if (v1 == v2)
                    return true;
            }
            return false;
        }

        public final int hashCode() {
            return Objects.hashCode(getKey()) ^ Helper.doubleHash(getValue());
        }

        public final String toString() {
            return getKey() + "=" + getValue();
        }

        /**
         * This method is invoked whenever the value in an entry is overwritten
         * by an invocation of put(k,v) for a key k that's already in the
         * HashMap.
         */
        void recordAccess(DoubleValueHashMap<K> m) {
        }

        /**
         * This method is invoked whenever the entry is removed from the table.
         */
        void recordRemoval(DoubleValueHashMap<K> m) {
        }
    }

    /**
     * Adds a new entry with the specified key, value and hash code to the
     * specified bucket. It is the responsibility of this method to resize the
     * table if appropriate.
     * <p>
     * Subclass overrides this to alter the behavior of put method.
     */
    void addEntry(int hash, K key, double value, int bucketIndex) {
        if ((size >= threshold) && (null != table[bucketIndex])) {
            resize(2 * table.length);
            hash = (null != key) ? hash(key) : 0;
            bucketIndex = indexFor(hash, table.length);
        }

        createEntry(hash, key, value, bucketIndex);
    }

    /**
     * Like addEntry except that this version is used when creating entries as
     * part of Map construction or "pseudo-construction" (cloning,
     * deserialization). This version needn't worry about resizing the table.
     * <p>
     * Subclass overrides this to alter the behavior of HashMap(Map), clone, and
     * readObject.
     */
    void createEntry(int hash, K key, double value, int bucketIndex) {
        Entry<K> e = table[bucketIndex];
        table[bucketIndex] = new Entry<>(hash, key, value, e);
        size++;
    }

    private abstract class HashIterator {
        Entry<K> next; // next entry to return
        int expectedModCount; // For fast-fail
        int index; // current slot
        Entry<K> current; // current entry

        HashIterator() {
            expectedModCount = modCount;
            if (size > 0) { // advance to first entry
                Entry<K>[] t = table;
                while (index < t.length && (next = t[index++]) == null)
                    ;
            }
        }

        public final boolean hasNext() {
            return next != null;
        }

        final Entry<K> nextEntry() {
            if (modCount != expectedModCount)
                throw new ConcurrentModificationException();
            Entry<K> e = next;
            if (e == null)
                throw new NoSuchElementException();

            if ((next = e.next) == null) {
                Entry<K>[] t = table;
                while (index < t.length && (next = t[index++]) == null)
                    ;
            }
            current = e;
            return e;
        }

        public void remove() {
            if (current == null)
                throw new IllegalStateException();
            if (modCount != expectedModCount)
                throw new ConcurrentModificationException();
            Object k = current.key;
            current = null;
            DoubleValueHashMap.this.removeEntryForKey(k);
            expectedModCount = modCount;
        }
    }

    private final class ValueIterator extends HashIterator implements DoubleIterator {
        public double next() {
            return nextEntry().value;
        }
    }

    private final class KeyIterator extends HashIterator implements Iterator<K> {
        public K next() {
            return nextEntry().getKey();
        }
    }

    private final class EntryIterator extends HashIterator
            implements Iterator<DoubleValueMap.Entry<K>> {
        public DoubleValueHashMap.Entry<K> next() {
            return nextEntry();
        }
    }

    // Subclass overrides these to alter behavior of views' iterator() method
    Iterator<K> newKeyIterator() {
        return new KeyIterator();
    }

    DoubleIterator newValueIterator() {
        return new ValueIterator();
    }

    Iterator<DoubleValueMap.Entry<K>> newEntryIterator() {
        return new EntryIterator();
    }

    // Views

    private transient Set<DoubleValueMap.Entry<K>> entrySet = null;
    private transient volatile Set<K> keySet = null;
    private transient volatile DoubleCollection values = null;

    /**
     * Returns a {@link Set} view of the keys contained in this map. The set is
     * backed by the map, so changes to the map are reflected in the set, and
     * vice-versa. If the map is modified while an iteration over the set is in
     * progress (except through the iterator's own <tt>remove</tt> operation),
     * the results of the iteration are undefined. The set supports element
     * removal, which removes the corresponding mapping from the map, via the
     * <tt>Iterator.remove</tt>, <tt>Set.remove</tt>, <tt>removeAll</tt>,
     * <tt>retainAll</tt>, and <tt>clear</tt> operations. It does not support
     * the <tt>add</tt> or <tt>addAll</tt> operations.
     */
    public Set<K> keySet() {
        Set<K> ks = keySet;
        return (ks != null ? ks : (keySet = new KeySet()));
    }

    private final class KeySet extends AbstractSet<K> {
        public Iterator<K> iterator() {
            return newKeyIterator();
        }

        public int size() {
            return size;
        }

        public boolean contains(Object o) {
            return containsKey(o);
        }

        public boolean remove(Object o) {
            return DoubleValueHashMap.this.removeEntryForKey(o) != null;
        }

        public void clear() {
            DoubleValueHashMap.this.clear();
        }
    }

    /**
     * Returns a {@link Collection} view of the values contained in this map.
     * The collection is backed by the map, so changes to the map are reflected
     * in the collection, and vice-versa. If the map is modified while an
     * iteration over the collection is in progress (except through the
     * iterator's own <tt>remove</tt> operation), the results of the iteration
     * are undefined. The collection supports element removal, which removes the
     * corresponding mapping from the map, via the <tt>Iterator.remove</tt>,
     * <tt>Collection.remove</tt>, <tt>removeAll</tt>, <tt>retainAll</tt> and
     * <tt>clear</tt> operations. It does not support the <tt>add</tt> or
     * <tt>addAll</tt> operations.
     */
    public DoubleCollection values() {
        DoubleCollection vs = values;
        return (vs != null ? vs : (values = new Values()));
    }

    private final class Values extends AbstractDoubleCollection {
        public DoubleIterator iterator() {
            return newValueIterator();
        }

        public int size() {
            return size;
        }

        public boolean contains(double o) {
            return containsValue(o);
        }

        public void clear() {
            DoubleValueHashMap.this.clear();
        }
    }

    /**
     * Returns a {@link Set} view of the mappings contained in this map. The set
     * is backed by the map, so changes to the map are reflected in the set, and
     * vice-versa. If the map is modified while an iteration over the set is in
     * progress (except through the iterator's own <tt>remove</tt> operation, or
     * through the <tt>setValue</tt> operation on a map entry returned by the
     * iterator) the results of the iteration are undefined. The set supports
     * element removal, which removes the corresponding mapping from the map,
     * via the <tt>Iterator.remove</tt>, <tt>Set.remove</tt>,
     * <tt>removeAll</tt>, <tt>retainAll</tt> and <tt>clear</tt> operations. It
     * does not support the <tt>add</tt> or <tt>addAll</tt> operations.
     *
     * @return a set view of the mappings contained in this map
     */
    public Set<DoubleValueMap.Entry<K>> entrySet() {
        return entrySet0();
    }

    private Set<DoubleValueMap.Entry<K>> entrySet0() {
        Set<DoubleValueMap.Entry<K>> es = entrySet;
        return es != null ? es : (entrySet = new EntrySet());
    }

    private final class EntrySet extends AbstractSet<DoubleValueMap.Entry<K>> {
        public Iterator<DoubleValueMap.Entry<K>> iterator() {
            return newEntryIterator();
        }

        public boolean contains(Object o) {
            if (!(o instanceof DoubleValueMap.Entry))
                return false;
            @SuppressWarnings("unchecked")
            DoubleValueMap.Entry<K> e = (DoubleValueMap.Entry<K>) o;
            Entry<K> candidate = getEntry(e.getKey());
            return candidate != null && candidate.equals(e);
        }

        public boolean remove(Object o) {
            return removeMapping(o) != null;
        }

        public int size() {
            return size;
        }

        public void clear() {
            DoubleValueHashMap.this.clear();
        }
    }

    /**
     * Save the state of the <tt>HashMap</tt> instance to a stream (i.e.,
     * serialize it).
     *
     * @serialData The <i>capacity</i> of the HashMap (the length of the bucket
     * array) is emitted (int), followed by the <i>size</i> (an int,
     * the number of key-value mappings), followed by the key
     * (Object) and value (Object) for each key-value mapping. The
     * key-value mappings are emitted in no particular order.
     */
    private void writeObject(java.io.ObjectOutputStream s) throws IOException {
        // Write out the threshold, loadfactor, and any hidden stuff
        s.defaultWriteObject();

        // Write out number of buckets
        if (table == EMPTY_TABLE) {
            s.writeInt(roundUpToPowerOf2(threshold));
        } else {
            s.writeInt(table.length);
        }

        // Write out size (number of Mappings)
        s.writeInt(size);

        // Write out keys and values (alternating)
        if (size > 0) {
            for (DoubleValueMap.Entry<K> e : entrySet0()) {
                s.writeObject(e.getKey());
                s.writeDouble(e.getValue());
            }
        }
    }

    private static final long serialVersionUID = 5099056333534961037L;

    /**
     * Reconstitute the {@code HashMap} instance from a stream (i.e.,
     * deserialize it).
     */
    @SuppressWarnings("unchecked")
    private void readObject(java.io.ObjectInputStream s)
            throws IOException, ClassNotFoundException {
        // Read in the threshold (ignored), loadfactor, and any hidden stuff
        s.defaultReadObject();
        if (loadFactor <= 0 || Float.isNaN(loadFactor)) {
            throw new InvalidObjectException("Illegal load factor: " + loadFactor);
        }

        // set other fields that need values
        table = (Entry<K>[]) EMPTY_TABLE;

        // Read in number of buckets
        s.readInt(); // ignored.

        // Read number of mappings
        int mappings = s.readInt();
        if (mappings < 0)
            throw new InvalidObjectException("Illegal mappings count: " + mappings);

        // capacity chosen by number of mappings and desired load (if >= 0.25)
        int capacity = (int) Math.min(mappings * Math.min(1 / loadFactor, 4.0f),
                // we have limits...
                IntValueHashMap.MAXIMUM_CAPACITY);

        // allocate the bucket array;
        if (mappings > 0) {
            inflateTable(capacity);
        } else {
            threshold = capacity;
        }

        init(); // Give subclass a chance to do its thing.

        // Read the keys and values, and put the mappings in the HashMap
        for (int i = 0; i < mappings; i++) {
            K key = (K) s.readObject();
            double value = s.readDouble();
            putForCreate(key, value);
        }
    }

    // These methods are used when serializing HashSets
    int capacity() {
        return table.length;
    }

    float loadFactor() {
        return loadFactor;
    }

}
