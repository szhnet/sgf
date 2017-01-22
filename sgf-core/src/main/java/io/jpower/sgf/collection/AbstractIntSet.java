package io.jpower.sgf.collection;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

/**
 * @author zheng.sun
 */
public abstract class AbstractIntSet extends AbstractIntCollection implements IntSet {

    /**
     * Sole constructor. (For invocation by subclass constructors, typically
     * implicit.)
     */
    protected AbstractIntSet() {
    }

    // Comparison and hashing

    /**
     * Compares the specified object with this set for equality. Returns
     * <tt>true</tt> if the given object is also a set, the two sets have the
     * same size, and every member of the given set is contained in this set.
     * This ensures that the <tt>equals</tt> method works properly across
     * different implementations of the <tt>Set</tt> interface.
     * <p>
     * <p>
     * This implementation first checks if the specified object is this set; if
     * so it returns <tt>true</tt>. Then, it checks if the specified object is a
     * set whose size is identical to the size of this set; if not, it returns
     * false. If so, it returns <tt>containsAll((Collection) o)</tt>.
     *
     * @param o object to be compared for equality with this set
     * @return <tt>true</tt> if the specified object is equal to this set
     */
    public boolean equals(Object o) {
        if (o == this)
            return true;

        if (!(o instanceof IntSet))
            return false;
        IntSet c = (IntSet) o;
        if (c.size() != size())
            return false;
        try {
            return containsAll(c);
        } catch (ClassCastException unused) {
            return false;
        } catch (NullPointerException unused) {
            return false;
        }
    }

    /**
     * Returns the hash code value for this set. The hash code of a set is
     * defined to be the sum of the hash codes of the elements in the set, where
     * the hash code of a <tt>null</tt> element is defined to be zero. This
     * ensures that <tt>s1.equals(s2)</tt> implies that
     * <tt>s1.hashCode()==s2.hashCode()</tt> for any two sets <tt>s1</tt> and
     * <tt>s2</tt>, as required by the general contract of
     * {@link Object#hashCode}.
     * <p>
     * <p>
     * This implementation iterates over the set, calling the <tt>hashCode</tt>
     * method on each element in the set, and adding up the results.
     *
     * @return the hash code value for this set
     * @see Object#equals(Object)
     * @see Set#equals(Object)
     */
    public int hashCode() {
        int h = 0;
        IntIterator i = iterator();
        while (i.hasNext()) {
            int e = i.next();
            h += Helper.intHash(e);
        }
        return h;
    }

    @Override
    public boolean removeAll(Collection<Integer> c) {
        boolean modified = false;

        if (size() > c.size()) {
            for (Iterator<Integer> i = c.iterator(); i.hasNext(); ) {
                modified |= remove(i.next());
            }
        } else {
            for (IntIterator i = iterator(); i.hasNext(); ) {
                if (c.contains(i.next())) {
                    i.remove();
                    modified = true;
                }
            }
        }
        return modified;
    }

    @Override
    public boolean removeAll(int[] a) {
        boolean modified = false;

        for (int e : a) {
            modified |= remove(e);
        }
        return modified;
    }

    @Override
    public boolean removeAll(IntCollection s) {
        boolean modified = false;

        if (size() > s.size()) {
            for (IntIterator i = s.iterator(); i.hasNext(); )
                modified |= remove(i.next());
        } else {
            for (IntIterator i = iterator(); i.hasNext(); ) {
                if (s.contains(i.next())) {
                    i.remove();
                    modified = true;
                }
            }
        }
        return modified;
    }

}
