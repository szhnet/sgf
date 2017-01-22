package io.jpower.sgf.collection;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;

/**
 * @author zheng.sun
 */
public class LongHashSetTest {

    @Test
    public void testIsEmpty() {
        LongHashSet s = new LongHashSet();
        assertTrue(s.isEmpty());
        s.add(1);
        assertFalse(s.isEmpty());
    }

    @Test
    public void testAdd() {
        LongHashSet s = new LongHashSet();
        boolean rst = s.add(1);
        assertTrue(rst);
        assertEquals(1, s.size());

        // 测试一下值相同的情况
        rst = s.add(1);
        assertFalse(rst);
        assertEquals(1, s.size());
    }

    @Test
    public void testLongHashSetCollectionOfLong() {
        Collection<Long> c = new ArrayList<>();
        c.add(1L);
        c.add(987654321123456789L);
        c.add(2L);

        LongHashSet s = new LongHashSet(c);
        assertEquals(c.size(), s.size());
        assertTrue(s.contains(1));
        assertTrue(s.contains(2));
        assertTrue(s.contains(987654321123456789L));

    }

    @Test
    public void testLongHashSetLongArray() {
        long[] a = {1, 987654321123456789L, 2};

        LongHashSet s = new LongHashSet(a);
        assertEquals(a.length, s.size());
        assertTrue(s.contains(1));
        assertTrue(s.contains(2));
        assertTrue(s.contains(987654321123456789L));
    }

    @Test
    public void testLongHashSetLongSet() {
        LongSet os = new LongHashSet();
        os.add(1);
        os.add(987654321123456789L);
        os.add(2);

        LongHashSet s = new LongHashSet(os);
        assertEquals(os.size(), s.size());
        assertTrue(s.contains(1));
        assertTrue(s.contains(2));
        assertTrue(s.contains(987654321123456789L));
    }

    @Test
    public void testIterator() {
        LongHashSet s = new LongHashSet();
        s.add(1);
        s.add(987654321123456789L);
        s.add(2);

        LongIterator itr = s.iterator();
        assertTrue(itr.hasNext());
        itr.next();
        assertTrue(itr.hasNext());
        itr.next();
        itr.remove(); // 测试一下删除
        assertTrue(itr.hasNext());
        itr.next();
        assertFalse(itr.hasNext());
        assertEquals(2, s.size());
    }

    @Test
    public void testSize() {
        LongHashSet s = new LongHashSet();
        s.add(100);
        assertEquals(1, s.size());
        s.remove(100);
        assertEquals(0, s.size());
    }

    @Test
    public void testContains() {
        LongHashSet s = new LongHashSet();
        s.add(100);
        assertTrue(s.contains(100));
        assertFalse(s.contains(1));
    }

    @Test
    public void testRemove() {
        LongHashSet s = new LongHashSet();
        s.add(1);
        s.add(2);

        assertTrue(s.contains(1));
        assertTrue(s.contains(2));
        assertFalse(s.contains(3));
    }

    @Test
    public void testClear() {
        LongHashSet s = new LongHashSet();
        s.add(1);
        s.add(2);
        s.clear();
        assertTrue(s.isEmpty());
    }

    @Test
    public void testClone() {
        LongHashSet s = new LongHashSet();
        LongHashSet cs = (LongHashSet) s.clone();
        assertFalse(s == cs);

        assertTrue(s.equals(cs));
    }

    @Test
    public void testEqualsObject() {
        LongHashSet x = new LongHashSet();
        x.add(1);
        x.add(2);
        x.add(987654321123456789L);
        assertTrue(x.equals(x)); // 自反性

        LongHashSet y = new LongHashSet();
        y.add(1);
        y.add(2);

        assertFalse(x.equals(y));
        y.add(987654321123456789L);
        assertTrue(x.equals(y));
        assertTrue(x.equals(y)); // 一致性

        assertTrue(y.equals(x)); // 对称性

        LongHashSet z = new LongHashSet();
        z.add(1);
        z.add(2);
        z.add(987654321123456789L);

        assertTrue(y.equals(z));
        assertTrue(x.equals(z)); // 传递性

        assertFalse(x.equals(null)); // null
    }

    @Test
    public void testAddAllCollectionOfLong() {
        Collection<Long> c = new ArrayList<>();
        c.add(1L);
        c.add(987654321123456789L);
        c.add(2L);

        LongHashSet s = new LongHashSet();
        s.addAll(c);
        assertEquals(c.size(), s.size());
        assertTrue(s.contains(1));
        assertTrue(s.contains(2));
        assertTrue(s.contains(987654321123456789L));
    }

    @Test
    public void testAddAllLongArray() {
        long[] a = {1, 987654321123456789L, 2};

        LongHashSet s = new LongHashSet();
        s.addAll(a);
        assertEquals(a.length, s.size());
        assertTrue(s.contains(1));
        assertTrue(s.contains(2));
        assertTrue(s.contains(987654321123456789L));
    }

    @Test
    public void testAddAllLongSet() {
        LongSet os = new LongHashSet();
        os.add(1);
        os.add(987654321123456789L);
        os.add(2);

        LongHashSet s = new LongHashSet();
        s.addAll(os);
        assertEquals(os.size(), s.size());
        assertTrue(s.contains(1));
        assertTrue(s.contains(2));
        assertTrue(s.contains(987654321123456789L));
    }

    @Test
    public void testRemoveAllCollectionOfLong() {
        LongHashSet s = new LongHashSet();
        s.add(1);
        s.add(2);
        s.add(987654321123456789L);

        Collection<Long> c = new ArrayList<>();
        c.add(1L);
        c.add(987654321123456789L);

        s.removeAll(c);
        assertEquals(1, s.size());
        assertFalse(s.contains(1));
        assertFalse(s.contains(987654321123456789L));
        assertTrue(s.contains(2));
    }

    @Test
    public void testRemoveAllLongArray() {
        LongHashSet s = new LongHashSet();
        s.add(1);
        s.add(2);
        s.add(987654321123456789L);

        long[] a = {1, 987654321123456789L};

        s.removeAll(a);
        assertEquals(1, s.size());
        assertFalse(s.contains(1));
        assertFalse(s.contains(987654321123456789L));
        assertTrue(s.contains(2));
    }

    @Test
    public void testRemoveAllLongSet() {
        LongHashSet s = new LongHashSet();
        s.add(1);
        s.add(2);
        s.add(987654321123456789L);

        LongSet os = new LongHashSet();
        os.add(1);
        os.add(987654321123456789L);

        s.removeAll(os);
        assertEquals(1, s.size());
        assertFalse(s.contains(1));
        assertFalse(s.contains(987654321123456789L));
        assertTrue(s.contains(2));
    }

    @Test
    public void testToArray() {
        LongHashSet s = new LongHashSet();
        s.add(1);
        s.add(2);
        s.add(987654321123456789L);

        long[] longArr = s.toArray();
        Arrays.sort(longArr);
        assertEquals(3, s.size());
        assertTrue(Arrays.binarySearch(longArr, 1) >= 0);
        assertTrue(Arrays.binarySearch(longArr, 2) >= 0);
        assertTrue(binarySearch(longArr, 987654321123456789L) >= 0);
        assertFalse(Arrays.binarySearch(longArr, 3) >= 0);
    }

    @Test
    public void testContainsAllCollectionOfLong() {
        LongHashSet s = new LongHashSet();
        s.add(1);
        s.add(2);
        s.add(987654321123456789L);

        Collection<Long> c = new ArrayList<>();
        c.add(1L);
        c.add(987654321123456789L);
        assertTrue(s.containsAll(c));

        c.add(3L);
        assertFalse(s.containsAll(c));
    }

    @Test
    public void testContainsAllLongArray() {
        LongHashSet s = new LongHashSet();
        s.add(1);
        s.add(2);
        s.add(987654321123456789L);

        long[] a = {1, 987654321123456789L};
        assertTrue(s.containsAll(a));

        a = new long[]{1, 2, 3};
        assertFalse(s.containsAll(a));
    }

    @Test
    public void testContainsAllLongSet() {
        LongHashSet s = new LongHashSet();
        s.add(1);
        s.add(2);
        s.add(987654321123456789L);

        LongSet os = new LongHashSet();
        os.add(1);
        os.add(987654321123456789L);
        assertTrue(s.containsAll(os));

        os.add(3);
        assertFalse(s.containsAll(os));
    }

    @Test
    public void testRetainAllCollectionOfLong() {
        LongHashSet s = new LongHashSet();
        s.add(1);
        s.add(2);
        s.add(987654321123456789L);

        Collection<Long> c = new ArrayList<>();
        c.add(1L);
        c.add(987654321123456789L);

        s.retainAll(c);

        assertEquals(2, s.size());
        assertTrue(s.contains(1));
        assertTrue(s.contains(987654321123456789L));
        assertFalse(s.contains(2));
    }

    @Test
    public void testRetainAllLongArray() {
        LongHashSet s = new LongHashSet();
        s.add(1);
        s.add(2);
        s.add(987654321123456789L);

        long[] a = {1, 987654321123456789L};

        s.retainAll(a);

        assertEquals(2, s.size());
        assertTrue(s.contains(1));
        assertTrue(s.contains(987654321123456789L));
        assertFalse(s.contains(2));
    }

    @Test
    public void testRetainAllLongSet() {
        LongHashSet s = new LongHashSet();
        s.add(1);
        s.add(2);
        s.add(987654321123456789L);

        LongSet os = new LongHashSet();
        os.add(1);
        os.add(987654321123456789L);

        s.retainAll(os);

        assertEquals(2, s.size());
        assertTrue(s.contains(1));
        assertTrue(s.contains(987654321123456789L));
        assertFalse(s.contains(2));
    }

    public static int binarySearch(long[] a, long key) {
        return binarySearch0(a, 0, a.length, key);
    }

    private static int binarySearch0(long[] a, int fromIndex, int toIndex, long key) {
        int low = fromIndex;
        int high = toIndex - 1;

        while (low <= high) {
            int mid = (low + high) >>> 1;
            long midVal = a[mid];

            if (midVal < key)
                low = mid + 1;
            else if (midVal > key)
                high = mid - 1;
            else
                return mid; // key found
        }
        return -(low + 1); // key not found.
    }

}
