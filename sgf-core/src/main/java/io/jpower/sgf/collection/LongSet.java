package io.jpower.sgf.collection;

import java.util.Collection;
import java.util.Set;

/**
 * long set
 *
 * @author zheng.sun
 */
public interface LongSet extends LongCollection {

    // Query Operations

    /**
     * Returns the number of elements in this set (its cardinality). If this set
     * contains more than <tt>Integer.MAX_VALUE</tt> elements, returns
     * <tt>Integer.MAX_VALUE</tt>.
     *
     * @return the number of elements in this set (its cardinality)
     */
    int size();

    /**
     * Returns <tt>true</tt> if this set contains no elements.
     *
     * @return <tt>true</tt> if this set contains no elements
     */
    boolean isEmpty();

    /**
     * Returns <tt>true</tt> if this set contains the specified element. More
     * formally, returns <tt>true</tt> if and only if this set contains an
     * element <tt>e</tt> such that
     * <tt>(o==null&nbsp;?&nbsp;e==null&nbsp;:&nbsp;o.equals(e))</tt>.
     *
     * @param o element whose presence in this set is to be tested
     * @return <tt>true</tt> if this set contains the specified element
     * @throws ClassCastException   if the type of the specified element is incompatible with
     *                              this set (
     *                              <a href="Collection.html#optional-restrictions">optional</a>)
     * @throws NullPointerException if the specified element is null and this set does not permit
     *                              null elements (
     *                              <a href="Collection.html#optional-restrictions">optional</a>)
     */
    boolean contains(long o);

    /**
     * Returns an iterator over the elements in this set. The elements are
     * returned in no particular order (unless this set is an instance of some
     * class that provides a guarantee).
     *
     * @return an iterator over the elements in this set
     */
    LongIterator iterator();

    /**
     * Returns an array containing all of the elements in this set. If this set
     * makes any guarantees as to what order its elements are returned by its
     * iterator, this method must return the elements in the same order.
     * <p>
     * <p>
     * The returned array will be "safe" in that no references to it are
     * maintained by this set. (In other words, this method must allocate a new
     * array even if this set is backed by an array). The caller is thus free to
     * modify the returned array.
     * <p>
     * <p>
     * This method acts as bridge between array-based and collection-based APIs.
     *
     * @return an array containing all the elements in this set
     */
    long[] toArray();

    // Modification Operations

    /**
     * Adds the specified element to this set if it is not already present
     * (optional operation). More formally, adds the specified element
     * <tt>e</tt> to this set if the set contains no element <tt>e2</tt> such
     * that <tt>(e==null&nbsp;?&nbsp;e2==null&nbsp;:&nbsp;e.equals(e2))</tt>. If
     * this set already contains the element, the call leaves the set unchanged
     * and returns <tt>false</tt>. In combination with the restriction on
     * constructors, this ensures that sets never contain duplicate elements.
     * <p>
     * <p>
     * The stipulation above does not imply that sets must accept all elements;
     * sets may refuse to add any particular element, including <tt>null</tt>,
     * and throw an exception, as described in the specification for
     * {@link Collection#add Collection.add}. Individual set implementations
     * should clearly document any restrictions on the elements that they may
     * contain.
     *
     * @param e element to be added to this set
     * @return <tt>true</tt> if this set did not already contain the specified
     * element
     * @throws UnsupportedOperationException if the <tt>add</tt> operation is not supported by this set
     * @throws ClassCastException            if the class of the specified element prevents it from being
     *                                       added to this set
     * @throws NullPointerException          if the specified element is null and this set does not permit
     *                                       null elements
     * @throws IllegalArgumentException      if some property of the specified element prevents it from
     *                                       being added to this set
     */
    boolean add(long e);

    /**
     * Removes the specified element from this set if it is present (optional
     * operation). More formally, removes an element <tt>e</tt> such that
     * <tt>(o==null&nbsp;?&nbsp;e==null&nbsp;:&nbsp;o.equals(e))</tt>, if this
     * set contains such an element. Returns <tt>true</tt> if this set contained
     * the element (or equivalently, if this set changed as a result of the
     * call). (This set will not contain the element once the call returns.)
     *
     * @param o object to be removed from this set, if present
     * @return <tt>true</tt> if this set contained the specified element
     * @throws ClassCastException            if the type of the specified element is incompatible with
     *                                       this set (
     *                                       <a href="Collection.html#optional-restrictions">optional</a>)
     * @throws NullPointerException          if the specified element is null and this set does not permit
     *                                       null elements (
     *                                       <a href="Collection.html#optional-restrictions">optional</a>)
     * @throws UnsupportedOperationException if the <tt>remove</tt> operation is not supported by this set
     */
    boolean remove(long o);

    // Bulk Operations

    /**
     * Returns <tt>true</tt> if this set contains all of the elements of the
     * specified collection. If the specified collection is also a set, this
     * method returns <tt>true</tt> if it is a <i>subset</i> of this set.
     *
     * @param c collection to be checked for containment in this set
     * @return <tt>true</tt> if this set contains all of the elements of the
     * specified collection
     * @throws ClassCastException   if the types of one or more elements in the specified
     *                              collection are incompatible with this set (
     *                              <a href="Collection.html#optional-restrictions">optional</a>)
     * @throws NullPointerException if the specified collection contains one or more null
     *                              elements and this set does not permit null elements (
     *                              <a href="Collection.html#optional-restrictions">optional</a>
     *                              ), or if the specified collection is null
     * @see #contains(Object)
     */
    boolean containsAll(Collection<Long> c);

    boolean containsAll(long[] a);

    boolean containsAll(LongCollection s);

    /**
     * Adds all of the elements in the specified collection to this set if
     * they're not already present (optional operation). If the specified
     * collection is also a set, the <tt>addAll</tt> operation effectively
     * modifies this set so that its value is the <i>union</i> of the two sets.
     * The behavior of this operation is undefined if the specified collection
     * is modified while the operation is in progress.
     *
     * @param c collection containing elements to be added to this set
     * @return <tt>true</tt> if this set changed as a result of the call
     * @throws UnsupportedOperationException if the <tt>addAll</tt> operation is not supported by this set
     * @throws ClassCastException            if the class of an element of the specified collection
     *                                       prevents it from being added to this set
     * @throws NullPointerException          if the specified collection contains one or more null
     *                                       elements and this set does not permit null elements, or if
     *                                       the specified collection is null
     * @throws IllegalArgumentException      if some property of an element of the specified collection
     *                                       prevents it from being added to this set
     * @see #add(Object)
     */
    boolean addAll(Collection<Long> c);

    boolean addAll(long[] a);

    boolean addAll(LongCollection s);

    /**
     * Retains only the elements in this set that are contained in the specified
     * collection (optional operation). In other words, removes from this set
     * all of its elements that are not contained in the specified collection.
     * If the specified collection is also a set, this operation effectively
     * modifies this set so that its value is the <i>intersection</i> of the two
     * sets.
     *
     * @param c collection containing elements to be retained in this set
     * @return <tt>true</tt> if this set changed as a result of the call
     * @throws UnsupportedOperationException if the <tt>retainAll</tt> operation is not supported by this
     *                                       set
     * @throws ClassCastException            if the class of an element of this set is incompatible with
     *                                       the specified collection (
     *                                       <a href="Collection.html#optional-restrictions">optional</a>)
     * @throws NullPointerException          if this set contains a null element and the specified
     *                                       collection does not permit null elements (
     *                                       <a href="Collection.html#optional-restrictions">optional</a>
     *                                       ), or if the specified collection is null
     * @see #remove(Object)
     */
    boolean retainAll(Collection<Long> c);

    boolean retainAll(long[] a);

    boolean retainAll(LongCollection s);

    /**
     * Removes from this set all of its elements that are contained in the
     * specified collection (optional operation). If the specified collection is
     * also a set, this operation effectively modifies this set so that its
     * value is the <i>asymmetric set difference</i> of the two sets.
     *
     * @param c collection containing elements to be removed from this set
     * @return <tt>true</tt> if this set changed as a result of the call
     * @throws UnsupportedOperationException if the <tt>removeAll</tt> operation is not supported by this
     *                                       set
     * @throws ClassCastException            if the class of an element of this set is incompatible with
     *                                       the specified collection (
     *                                       <a href="Collection.html#optional-restrictions">optional</a>)
     * @throws NullPointerException          if this set contains a null element and the specified
     *                                       collection does not permit null elements (
     *                                       <a href="Collection.html#optional-restrictions">optional</a>
     *                                       ), or if the specified collection is null
     * @see #remove(Object)
     * @see #contains(Object)
     */
    boolean removeAll(Collection<Long> c);

    boolean removeAll(long[] a);

    boolean removeAll(LongCollection s);

    /**
     * Removes all of the elements from this set (optional operation). The set
     * will be empty after this call returns.
     *
     * @throws UnsupportedOperationException if the <tt>clear</tt> method is not supported by this set
     */
    void clear();

    // Comparison and hashing

    /**
     * Compares the specified object with this set for equality. Returns
     * <tt>true</tt> if the specified object is also a set, the two sets have
     * the same size, and every member of the specified set is contained in this
     * set (or equivalently, every member of this set is contained in the
     * specified set). This definition ensures that the equals method works
     * properly across different implementations of the set interface.
     *
     * @param o object to be compared for equality with this set
     * @return <tt>true</tt> if the specified object is equal to this set
     */
    boolean equals(Object o);

    /**
     * Returns the hash code value for this set. The hash code of a set is
     * defined to be the sum of the hash codes of the elements in the set, where
     * the hash code of a <tt>null</tt> element is defined to be zero. This
     * ensures that <tt>s1.equals(s2)</tt> implies that
     * <tt>s1.hashCode()==s2.hashCode()</tt> for any two sets <tt>s1</tt> and
     * <tt>s2</tt>, as required by the general contract of
     * {@link Object#hashCode}.
     *
     * @return the hash code value for this set
     * @see Object#equals(Object)
     * @see Set#equals(Object)
     */
    int hashCode();

}
