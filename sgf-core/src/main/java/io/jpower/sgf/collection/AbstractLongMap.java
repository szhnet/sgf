package io.jpower.sgf.collection;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * @param <V>
 * @author zheng.sun
 */
public abstract class AbstractLongMap<V> implements LongMap<V> {

    // Comparison and hashing

    /**
     * Compares the specified object with this map for equality. Returns
     * <tt>true</tt> if the given object is also a map and the two maps
     * represent the same mappings. More formally, two maps <tt>m1</tt> and
     * <tt>m2</tt> represent the same mappings if
     * <tt>m1.entrySet().equals(m2.entrySet())</tt>. This ensures that the
     * <tt>equals</tt> method works properly across different implementations of
     * the <tt>Map</tt> interface.
     * <p>
     * <p>
     * This implementation first checks if the specified object is this map; if
     * so it returns <tt>true</tt>. Then, it checks if the specified object is a
     * map whose size is identical to the size of this map; if not, it returns
     * <tt>false</tt>. If so, it iterates over this map's <tt>entrySet</tt>
     * collection, and checks that the specified map contains each mapping that
     * this map contains. If the specified map fails to contain such a mapping,
     * <tt>false</tt> is returned. If the iteration completes, <tt>true</tt> is
     * returned.
     *
     * @param o object to be compared for equality with this map
     * @return <tt>true</tt> if the specified object is equal to this map
     */
    public boolean equals(Object o) {
        if (o == this)
            return true;

        if (!(o instanceof LongMap))
            return false;
        @SuppressWarnings("unchecked")
        LongMap<V> m = (LongMap<V>) o;
        if (m.size() != size())
            return false;

        try {
            Iterator<LongMap.Entry<V>> i = entrySet().iterator();
            while (i.hasNext()) {
                LongMap.Entry<V> e = i.next();
                long key = e.getKey();
                V value = e.getValue();
                if (value == null) {
                    if (!(m.get(key) == null && m.containsKey(key)))
                        return false;
                } else {
                    if (!value.equals(m.get(key)))
                        return false;
                }
            }
        } catch (ClassCastException unused) {
            return false;
        } catch (NullPointerException unused) {
            return false;
        }

        return true;
    }

    /**
     * Returns the hash code value for this map. The hash code of a map is
     * defined to be the sum of the hash codes of each entry in the map's
     * <tt>entrySet()</tt> view. This ensures that <tt>m1.equals(m2)</tt>
     * implies that <tt>m1.hashCode()==m2.hashCode()</tt> for any two maps
     * <tt>m1</tt> and <tt>m2</tt>, as required by the general contract of
     * {@link Object#hashCode}.
     * <p>
     * <p>
     * This implementation iterates over <tt>entrySet()</tt>, calling
     * {@link Map.Entry#hashCode hashCode()} on each element (entry) in the set,
     * and adding up the results.
     *
     * @return the hash code value for this map
     * @see Map.Entry#hashCode()
     * @see Object#equals(Object)
     * @see Set#equals(Object)
     */
    public int hashCode() {
        int h = 0;
        Iterator<LongMap.Entry<V>> i = entrySet().iterator();
        while (i.hasNext())
            h += i.next().hashCode();
        return h;
    }

    /**
     * Returns a string representation of this map. The string representation
     * consists of a list of key-value mappings in the order returned by the
     * map's <tt>entrySet</tt> view's iterator, enclosed in braces (
     * <tt>"{}"</tt>). Adjacent mappings are separated by the characters
     * <tt>", "</tt> (comma and space). Each key-value mapping is rendered as
     * the key followed by an equals sign (<tt>"="</tt>) followed by the
     * associated value. Keys and values are converted to strings as by
     * {@link String#valueOf(Object)}.
     *
     * @return a string representation of this map
     */
    public String toString() {
        Iterator<LongMap.Entry<V>> i = entrySet().iterator();
        if (!i.hasNext())
            return "{}";

        StringBuilder sb = new StringBuilder();
        sb.append('{');
        for (; ; ) {
            LongMap.Entry<V> e = i.next();
            long key = e.getKey();
            V value = e.getValue();
            sb.append(key);
            sb.append('=');
            sb.append(value == this ? "(this Map)" : value);
            if (!i.hasNext())
                return sb.append('}').toString();
            sb.append(',').append(' ');
        }
    }

    public static class SimpleEntry<V> implements Entry<V>, java.io.Serializable {

        private static final long serialVersionUID = 1967635978578091172L;

        private final long key;
        private V value;

        /**
         * Creates an entry representing a mapping from the specified key to the
         * specified value.
         *
         * @param key   the key represented by this entry
         * @param value the value represented by this entry
         */
        public SimpleEntry(long key, V value) {
            this.key = key;
            this.value = value;
        }

        /**
         * Creates an entry representing the same mapping as the specified
         * entry.
         *
         * @param entry the entry to copy
         */
        public SimpleEntry(Entry<? extends V> entry) {
            this.key = entry.getKey();
            this.value = entry.getValue();
        }

        /**
         * Returns the key corresponding to this entry.
         *
         * @return the key corresponding to this entry
         */
        public long getKey() {
            return key;
        }

        /**
         * Returns the value corresponding to this entry.
         *
         * @return the value corresponding to this entry
         */
        public V getValue() {
            return value;
        }

        /**
         * Replaces the value corresponding to this entry with the specified
         * value.
         *
         * @param value new value to be stored in this entry
         * @return the old value corresponding to the entry
         */
        public V setValue(V value) {
            V oldValue = this.value;
            this.value = value;
            return oldValue;
        }

        /**
         * Compares the specified object with this entry for equality. Returns
         * {@code true} if the given object is also a map entry and the two
         * entries represent the same mapping. More formally, two entries
         * {@code e1} and {@code e2} represent the same mapping if
         * <p>
         * <pre>
         * (e1.getKey() == null ? e2.getKey() == null : e1.getKey().equals(e2.getKey()))
         *         &amp;&amp; (e1.getValue() == null ? e2.getValue() == null
         *                 : e1.getValue().equals(e2.getValue()))
         * </pre>
         * <p>
         * This ensures that the {@code equals} method works properly across
         * different implementations of the {@code Map.Entry} interface.
         *
         * @param o object to be compared for equality with this map entry
         * @return {@code true} if the specified object is equal to this map
         * entry
         * @see #hashCode
         */
        public boolean equals(Object o) {
            if (!(o instanceof Entry))
                return false;
            @SuppressWarnings("unchecked")
            Entry<V> e = (Entry<V>) o;
            return key == e.getKey() && Helper.eq(value, e.getValue());
        }

        /**
         * Returns the hash code value for this map entry. The hash code of a
         * map entry {@code e} is defined to be:
         * <p>
         * <pre>
         * (e.getKey() == null ? 0 : e.getKey().hashCode())
         *         ^ (e.getValue() == null ? 0 : e.getValue().hashCode())
         * </pre>
         * <p>
         * This ensures that {@code e1.equals(e2)} implies that
         * {@code e1.hashCode()==e2.hashCode()} for any two Entries {@code e1}
         * and {@code e2}, as required by the general contract of
         * {@link Object#hashCode}.
         *
         * @return the hash code value for this map entry
         * @see #equals
         */
        public int hashCode() {
            int keyHash = Helper.longHash(key);
            return keyHash ^ (value == null ? 0 : value.hashCode());
        }

        /**
         * Returns a String representation of this map entry. This
         * implementation returns the string representation of this entry's key
         * followed by the equals character ("<tt>=</tt>") followed by the
         * string representation of this entry's value.
         *
         * @return a String representation of this map entry
         */
        public String toString() {
            return key + "=" + value;
        }

    }

}
