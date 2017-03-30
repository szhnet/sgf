package io.jpower.sgf.collection;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.AbstractList;
import java.util.Arrays;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.RandomAccess;

/**
 * 一个简单的copy on write list
 * <p>
 * <ul>
 * <li>不是线程安全的。但是可以解决循环内改变list，导致迭代器抛出ConcurrentModificationException的问题。</li>
 * <li>当增加和删除操作时，会对内部的数组进行复制。<strong>注意</strong>，修改操作不会。</li>
 * </ul>
 *
 * @author <a href="mailto:szhnet@gmail.com">szh</a>
 */
public class SimpleCowArrayList<E> implements List<E>, RandomAccess, Cloneable, Serializable {

    private static final long serialVersionUID = 5629444416377947590L;

    private static final Object[] NULL_ARRAY = new Object[0];

    private transient Object[] elements = NULL_ARRAY;

    public SimpleCowArrayList() {

    }

    public SimpleCowArrayList(Collection<? extends E> c) {
        Object[] elements = c.toArray();
        int len = elements.length;
        if (len != 0) {
            if (elements.getClass() != Object[].class) {
                elements = Arrays.copyOf(elements, len, Object[].class);
            }
            this.elements = elements;
        }
    }

    public Object[] getArray() {
        return elements;
    }

    @Override
    public boolean add(E e) {
        if (elements == NULL_ARRAY) {
            elements = new Object[]{e};
        } else {
            int len = elements.length;
            Object[] newElements = new Object[len + 1];
            System.arraycopy(elements, 0, newElements, 0, len);

            newElements[len] = e;

            elements = newElements;
        }
        return true;
    }

    @Override
    public void add(int index, E e) {
        int len = elements.length;
        if (index > len || index < 0) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + len);
        }

        Object[] newElements = new Object[len + 1];
        int numMoved = len - index;
        if (numMoved == 0) {
            System.arraycopy(elements, 0, newElements, 0, len);
        } else {
            System.arraycopy(elements, 0, newElements, 0, index);
            System.arraycopy(elements, index, newElements, index + 1, numMoved);
        }
        newElements[index] = e;

        elements = newElements;
    }

    @SuppressWarnings("unchecked")
    @Override
    public E remove(int index) {
        int len = elements.length;
        if (index >= len || index < 0) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + len);
        }
        Object oldValue = elements[index];
        int newLen = len - 1;
        if (newLen == 0) {
            elements = NULL_ARRAY;
        } else {
            Object[] newElements = new Object[newLen];
            int numMoved = len - index - 1;
            if (numMoved == 0) {
                System.arraycopy(elements, 0, newElements, 0, newLen);
            } else {
                System.arraycopy(elements, 0, newElements, 0, index);
                System.arraycopy(elements, index + 1, newElements, index, numMoved);
            }

            elements = newElements;
        }
        return (E) oldValue;
    }

    @Override
    public boolean remove(Object o) {
        int len = elements.length;
        if (len == 0) {
            return false;
        }

        int index = -1;
        for (int i = 0; i < len; i++) {
            if (eq(elements[i], o)) {
                index = i;
                break;
            }
        }

        if (index != -1) {
            int newLen = elements.length - 1;
            if (newLen == 0) {
                elements = NULL_ARRAY;
            } else {
                Object[] newElements = new Object[newLen];
                int numMoved = len - index - 1;
                if (numMoved == 0) {
                    System.arraycopy(elements, 0, newElements, 0, newLen);
                } else {
                    System.arraycopy(elements, 0, newElements, 0, index);
                    System.arraycopy(elements, index + 1, newElements, index, numMoved);
                }

                elements = newElements;
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * 区域删除
     *
     * @param fromIndex
     * @param toIndex
     */
    private void removeRange(int fromIndex, int toIndex) {
        int len = elements.length;
        if (fromIndex < 0 || fromIndex >= len || toIndex > len || fromIndex > toIndex) {
            throw new IndexOutOfBoundsException();
        }
        int newLen = len - (toIndex - fromIndex);
        if (newLen == 0) {
            elements = NULL_ARRAY;
        } else {
            Object[] newElements = new Object[newLen];
            int numMoved = len - toIndex;
            if (numMoved == 0) {
                System.arraycopy(elements, 0, newElements, 0, newLen);
            } else {
                System.arraycopy(elements, 0, newElements, 0, fromIndex);
                System.arraycopy(elements, toIndex, newElements, fromIndex, numMoved);
            }

            elements = newElements;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public E get(int index) {
        return (E) elements[index];
    }

    @Override
    public int size() {
        return elements.length;
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public boolean contains(Object o) {
        return indexOf(o, elements, 0, elements.length) >= 0;
    }

    @Override
    public Iterator<E> iterator() {
        return new SimpleCowIterator<E>(this.elements, 0);
    }

    @Override
    public Object[] toArray() {
        return Arrays.copyOf(elements, elements.length);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T[] toArray(T[] a) {
        int len = elements.length;
        if (a.length < len) {
            return (T[]) Arrays.copyOf(elements, len, a.getClass());
        } else {
            System.arraycopy(elements, 0, a, 0, len);
            if (a.length > len) {
                a[len] = null;
            }
            return a;
        }
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        int len = elements.length;
        for (Object e : c) {
            if (indexOf(e, elements, 0, len) < 0) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        Object[] cs = c.toArray();
        int csLen = cs.length;
        if (csLen == 0) {
            return false;
        }

        int len = elements.length;
        Object[] newElements = new Object[len + csLen];
        System.arraycopy(elements, 0, newElements, 0, len);
        System.arraycopy(cs, 0, newElements, len, csLen);

        elements = newElements;
        return true;
    }

    @Override
    public boolean addAll(int index, Collection<? extends E> c) {
        int len = elements.length;
        if (index > len || index < 0) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + len);
        }

        Object[] cs = c.toArray();
        int csLen = cs.length;
        if (csLen == 0) {
            return false;
        }
        Object[] newElements = new Object[len + csLen];
        int numMoved = len - index;
        if (numMoved == 0) {
            System.arraycopy(elements, 0, newElements, 0, len);
        } else {
            System.arraycopy(elements, 0, newElements, 0, index);
            System.arraycopy(elements, index, newElements, index + csLen, numMoved);
        }
        System.arraycopy(cs, 0, newElements, index, csLen);

        elements = newElements;
        return true;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        int len = elements.length;
        if (len == 0) {
            return false;
        }

        int newLen = 0;
        Object[] temp = new Object[len];
        for (int i = 0; i < len; i++) {
            Object e = elements[i];
            if (!c.contains(e)) {
                temp[newLen++] = e;
            }
        }
        if (newLen != len) {
            if (newLen == 0) {
                elements = NULL_ARRAY;
            } else {
                Object[] newElements = new Object[newLen];
                System.arraycopy(temp, 0, newElements, 0, newLen);
                elements = newElements;
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        int len = elements.length;
        if (len == 0) {
            return false;
        }

        int newLen = 0;
        Object[] temp = new Object[len];
        for (int i = 0; i < len; i++) {
            Object e = elements[i];
            if (c.contains(e)) {
                temp[newLen++] = e;
            }
        }
        if (newLen != len) {
            if (newLen == 0) {
                elements = NULL_ARRAY;
            } else {
                Object[] newElements = new Object[newLen];
                System.arraycopy(temp, 0, newElements, 0, newLen);
                elements = newElements;
            }
            return true;
        }
        return false;
    }

    @Override
    public void clear() {
        elements = NULL_ARRAY;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof List)) {
            return false;
        }

        List<?> list = (List<?>) (o);
        Iterator<?> itr = list.iterator();
        int len = elements.length;
        for (int i = 0; i < len; i++) {
            if (!itr.hasNext() || !eq(elements[i], itr.next())) {
                return false;
            }
        }
        if (itr.hasNext()) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hashCode = 1;
        int len = elements.length;
        for (int i = 0; i < len; i++) {
            Object e = elements[i];
            hashCode = 31 * hashCode + (e == null ? 0 : e.hashCode());
        }
        return hashCode;
    }

    @Override
    public E set(int index, E e) {
        @SuppressWarnings("unchecked")
        E oldValue = (E) elements[index];
        elements[index] = e;
        return oldValue;
    }

    @Override
    public int indexOf(Object o) {
        return indexOf(o, elements, 0, elements.length);
    }

    @Override
    public int lastIndexOf(Object o) {
        return lastIndexOf(o, elements, elements.length - 1);
    }

    @Override
    public ListIterator<E> listIterator() {
        return new SimpleCowIterator<E>(elements, 0);
    }

    @Override
    public ListIterator<E> listIterator(int index) {
        int len = elements.length;
        if (index > len || index < 0) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + len);
        }
        return new SimpleCowIterator<E>(elements, index);
    }

    @Override
    public List<E> subList(int fromIndex, int toIndex) {
        int len = elements.length;
        if (fromIndex < 0 || toIndex > len || fromIndex > toIndex) {
            throw new IndexOutOfBoundsException();
        }
        return new SimpleCowSubList<E>(this, fromIndex, toIndex);
    }

    @Override
    public String toString() {
        return Arrays.toString(this.elements);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    protected Object clone() {
        try {
            SimpleCowArrayList l = (SimpleCowArrayList) (super.clone());
            l.elements = Arrays.copyOf(this.elements, this.elements.length); // 这里需要复制一个新的内部数组（与CopyOnWriteArrayList不同），因为修改操作是不复制内部数组的，所以clone的对象不能用原来的数组，需要复制一份。
            return l;
        } catch (CloneNotSupportedException e) {
            throw new InternalError();
        }
    }

    private void writeObject(ObjectOutputStream s) throws IOException {
        s.defaultWriteObject();

        int len = elements.length;
        s.writeInt(len);
        for (int i = 0; i < len; i++) {
            s.writeObject(elements[i]);
        }
    }

    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        s.defaultReadObject();

        int len = s.readInt();
        Object[] elements = new Object[len];
        for (int i = 0; i < len; i++) {
            elements[i] = s.readObject();
        }
        this.elements = elements;
    }

    private static int indexOf(Object o, Object[] elements, int index, int fence) {
        if (o == null) {
            for (int i = index; i < fence; i++) {
                if (elements[i] == null) {
                    return i;
                }
            }
        } else {
            for (int i = index; i < fence; i++) {
                if (o.equals(elements[i])) {
                    return i;
                }
            }
        }
        return -1;
    }

    private static int lastIndexOf(Object o, Object[] elements, int index) {
        if (o == null) {
            for (int i = index; i >= 0; i--) {
                if (elements[i] == null) {
                    return i;
                }
            }
        } else {
            for (int i = index; i >= 0; i--) {
                if (o.equals(elements[i])) {
                    return i;
                }
            }
        }
        return -1;
    }

    private static boolean eq(Object o1, Object o2) {
        return o1 == o2 || (o1 != null && o1.equals(o2));
    }

    private static class SimpleCowIterator<E> implements ListIterator<E> {

        private Object[] snapshot;

        private int cursor = 0;

        private SimpleCowIterator(Object[] snapshot, int initCursor) {
            this.cursor = initCursor;
            this.snapshot = snapshot;
        }

        @Override
        public boolean hasNext() {
            return cursor < snapshot.length;
        }

        @SuppressWarnings("unchecked")
        @Override
        public E next() {
            if (cursor >= snapshot.length) {
                throw new NoSuchElementException();
            }
            return (E) snapshot[cursor++];
        }

        @Override
        public boolean hasPrevious() {
            return cursor > 0;
        }

        @SuppressWarnings("unchecked")
        @Override
        public E previous() {
            if (cursor <= 0) {
                throw new NoSuchElementException();
            }
            return (E) snapshot[--cursor];
        }

        @Override
        public int nextIndex() {
            return cursor;
        }

        @Override
        public int previousIndex() {
            return cursor - 1;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void set(E e) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void add(E e) {
            throw new UnsupportedOperationException();
        }

    }

    private static class SimpleCowSubList<E> extends AbstractList<E> {

        private final SimpleCowArrayList<E> l;

        private int offset;

        private int size;

        private Object[] expectedArray;

        public SimpleCowSubList(SimpleCowArrayList<E> list, int fromIndex, int toIndex) {
            this.l = list;
            expectedArray = l.getArray();
            offset = fromIndex;
            size = toIndex - fromIndex;
        }

        private void checkForComodification() {
            if (l.getArray() != expectedArray) {
                throw new ConcurrentModificationException();
            }
        }

        private void rangeCheck(int index) {
            if (index < 0 || index >= size) {
                throw new IndexOutOfBoundsException("Index: " + index + ",Size: " + size);
            }
        }

        public E set(int index, E e) {
            checkForComodification();
            rangeCheck(index);
            E x = l.set(index + offset, e);
            return x;
        }

        @Override
        public E get(int index) {
            checkForComodification();
            rangeCheck(index);
            return l.get(index + offset);
        }

        @Override
        public int size() {
            checkForComodification();
            return size;
        }

        @Override
        public void add(int index, E e) {
            checkForComodification();
            if (index < 0 || index > size) {
                throw new IndexOutOfBoundsException("Index: " + index + ",Size: " + size);
            }
            l.add(index + offset, e);
            expectedArray = l.getArray();
            size++;
        }

        @Override
        public void clear() {
            checkForComodification();
            l.removeRange(offset, offset + size);
            expectedArray = l.getArray();
            size = 0;
        }

        @Override
        public E remove(int index) {
            checkForComodification();
            rangeCheck(index);
            E result = l.remove(index + offset);
            expectedArray = l.getArray();
            size--;
            return result;
        }

        @Override
        public Iterator<E> iterator() {
            checkForComodification();
            return new SimpleCowSubListIterator<E>(l, 0, offset, size);
        }

        @Override
        public ListIterator<E> listIterator(int index) {
            checkForComodification();
            if (index < 0 || index > size) {
                throw new IndexOutOfBoundsException("Index: " + index + ",Size: " + size);
            }
            return new SimpleCowSubListIterator<E>(l, index, offset, size);
        }

        @Override
        public List<E> subList(int fromIndex, int toIndex) {
            checkForComodification();
            if (fromIndex < 0 || toIndex > size || fromIndex > toIndex) {
                throw new IndexOutOfBoundsException();
            }
            return new SimpleCowSubList<E>(l, fromIndex + offset, toIndex + offset);
        }

    }

    private static class SimpleCowSubListIterator<E> implements ListIterator<E> {

        private final ListIterator<E> i;

        @SuppressWarnings("unused")
        private final int index;

        private final int offset;

        private final int size;

        private SimpleCowSubListIterator(List<E> l, int index, int offset, int size) {
            this.index = index;
            this.offset = offset;
            this.size = size;
            this.i = l.listIterator(index + offset);
        }

        @Override
        public boolean hasNext() {
            return nextIndex() < size;
        }

        @Override
        public E next() {
            if (hasNext()) {
                return i.next();
            } else {
                throw new NoSuchElementException();
            }
        }

        @Override
        public boolean hasPrevious() {
            return previousIndex() >= 0;
        }

        @Override
        public E previous() {
            if (hasPrevious()) {
                return i.previous();
            } else {
                throw new NoSuchElementException();
            }
        }

        @Override
        public int nextIndex() {
            return i.nextIndex() - offset;
        }

        @Override
        public int previousIndex() {
            return i.previousIndex() - offset;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void set(E e) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void add(E e) {
            throw new UnsupportedOperationException();
        }

    }

}
