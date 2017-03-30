package io.jpower.sgf.collection;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * @param <K>
 * @author <a href="mailto:szhnet@gmail.com">szh</a>
 */
public abstract class AbstractIntValueMap<K> implements IntValueMap<K> {

    /**
     * Sole constructor. (For invocation by subclass constructors, typically
     * implicit.)
     */
    protected AbstractIntValueMap() {
    }

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

        if (!(o instanceof IntValueMap))
            return false;
        @SuppressWarnings("unchecked")
        IntValueMap<K> m = (IntValueMap<K>) o;
        if (m.size() != size())
            return false;

        try {
            Iterator<IntValueMap.Entry<K>> i = entrySet().iterator();
            while (i.hasNext()) {
                IntValueMap.Entry<K> e = i.next();
                K key = e.getKey();
                int value = e.getValue();
                if (value != m.get(key))
                    return false;
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
        Iterator<IntValueMap.Entry<K>> i = entrySet().iterator();
        while (i.hasNext())
            h += i.next().hashCode();
        return h;
    }

    /**
     * Returns a string representation of this map. The string representation
     * consists of a list of key-value mappings in the order returned by the
     * map's <tt>entrySet</tt> view's iterator, enclosed in braces
     * (<tt>"{}"</tt>). Adjacent mappings are separated by the characters
     * <tt>", "</tt> (comma and space). Each key-value mapping is rendered as
     * the key followed by an equals sign (<tt>"="</tt>) followed by the
     * associated value. Keys and values are converted to strings as by
     * {@link String#valueOf(Object)}.
     *
     * @return a string representation of this map
     */
    public String toString() {
        Iterator<IntValueMap.Entry<K>> i = entrySet().iterator();
        if (!i.hasNext())
            return "{}";

        StringBuilder sb = new StringBuilder();
        sb.append('{');
        for (; ; ) {
            IntValueMap.Entry<K> e = i.next();
            K key = e.getKey();
            int value = e.getValue();
            sb.append(key == this ? "(this Map)" : key);
            sb.append('=');
            sb.append(value);
            if (!i.hasNext())
                return sb.append('}').toString();
            sb.append(',').append(' ');
        }
    }

    public static class SimpleEntry<K> implements IntValueMap.Entry<K>, java.io.Serializable {

        private static final long serialVersionUID = 1885982405093681912L;

        private final K key;
        private int value;

        /**
         * Creates an entry representing a mapping from the specified key to the
         * specified value.
         *
         * @param key   the key represented by this entry
         * @param value the value represented by this entry
         */
        public SimpleEntry(K key, int value) {
            this.key = key;
            this.value = value;
        }

        /**
         * Creates an entry representing the same mapping as the specified
         * entry.
         *
         * @param entry the entry to copy
         */
        public SimpleEntry(Entry<? extends K> entry) {
            this.key = entry.getKey();
            this.value = entry.getValue();
        }

        /**
         * Returns the key corresponding to this entry.
         *
         * @return the key corresponding to this entry
         */
        public K getKey() {
            return key;
        }

        /**
         * Returns the value corresponding to this entry.
         *
         * @return the value corresponding to this entry
         */
        public int getValue() {
            return value;
        }

        /**
         * Replaces the value corresponding to this entry with the specified
         * value.
         *
         * @param value new value to be stored in this entry
         * @return the old value corresponding to the entry
         */
        public int setValue(int value) {
            int oldValue = this.value;
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
            Entry<K> e = (Entry<K>) o;
            return Helper.eq(key, e.getKey()) && value == e.getValue();
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
            return (key == null ? 0 : key.hashCode()) ^ Helper.intHash(value);
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
