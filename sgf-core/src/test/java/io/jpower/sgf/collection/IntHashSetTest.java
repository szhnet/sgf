package io.jpower.sgf.collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;

/**
 * @author <a href="mailto:szhnet@gmail.com">szh</a>
 */
public class IntHashSetTest {

    @Test
    public void testIsEmpty() {
        IntHashSet s = new IntHashSet();
        assertTrue(s.isEmpty());
        s.add(1);
        assertFalse(s.isEmpty());
    }

    @Test
    public void testAdd() {
        IntHashSet s = new IntHashSet();
        boolean rst = s.add(1);
        assertTrue(rst);
        assertEquals(1, s.size());

        // 测试一下值相同的情况
        rst = s.add(1);
        assertFalse(rst);
        assertEquals(1, s.size());
    }

    @Test
    public void testIntHashSetCollectionOfInteger() {
        Collection<Integer> c = new ArrayList<>();
        c.add(1);
        c.add(234534098);
        c.add(2);

        IntHashSet s = new IntHashSet(c);
        assertEquals(c.size(), s.size());
        assertTrue(s.contains(1));
        assertTrue(s.contains(2));
        assertTrue(s.contains(234534098));
    }

    @Test
    public void testIntHashSetIntArray() {
        int[] a = {1, 987654321, 2};

        IntHashSet s = new IntHashSet(a);
        assertEquals(a.length, s.size());
        assertTrue(s.contains(1));
        assertTrue(s.contains(2));
        assertTrue(s.contains(987654321));
    }

    @Test
    public void testIntHashSetIntSet() {
        IntSet os = new IntHashSet();
        os.add(1);
        os.add(987654321);
        os.add(2);

        IntHashSet s = new IntHashSet(os);
        assertEquals(os.size(), s.size());
        assertTrue(s.contains(1));
        assertTrue(s.contains(2));
        assertTrue(s.contains(987654321));
    }

    @Test
    public void testIterator() {
        IntHashSet s = new IntHashSet();
        s.add(1);
        s.add(987654321);
        s.add(2);

        IntIterator itr = s.iterator();
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
        IntHashSet s = new IntHashSet();
        s.add(100);
        assertEquals(1, s.size());
        s.remove(100);
        assertEquals(0, s.size());
    }

    @Test
    public void testContains() {
        IntHashSet s = new IntHashSet();
        s.add(100);
        assertTrue(s.contains(100));
        assertFalse(s.contains(1));
    }

    @Test
    public void testRemove() {
        IntHashSet s = new IntHashSet();
        s.add(1);
        s.add(2);

        assertTrue(s.contains(1));
        assertTrue(s.contains(2));
        assertFalse(s.contains(3));
    }

    @Test
    public void testClear() {
        IntHashSet s = new IntHashSet();
        s.add(1);
        s.add(2);
        s.clear();
        assertTrue(s.isEmpty());
    }

    @Test
    public void testClone() {
        IntHashSet s = new IntHashSet();
        IntHashSet cs = (IntHashSet) s.clone();
        assertFalse(s == cs);

        assertTrue(s.equals(cs));
    }

    @Test
    public void testEqualsObject() {
        IntHashSet x = new IntHashSet();
        x.add(1);
        x.add(2);
        x.add(3);
        assertTrue(x.equals(x)); // 自反性

        IntHashSet y = new IntHashSet();
        y.add(1);
        y.add(2);

        assertFalse(x.equals(y));
        y.add(3);
        assertTrue(x.equals(y));
        assertTrue(x.equals(y)); // 一致性

        assertTrue(y.equals(x)); // 对称性

        IntHashSet z = new IntHashSet();
        z.add(1);
        z.add(2);
        z.add(3);

        assertTrue(y.equals(z));
        assertTrue(x.equals(z)); // 传递性

        assertFalse(x.equals(null)); // null
    }

    @Test
    public void testAddAllCollectionOfInteger() {
        Collection<Integer> c = new ArrayList<>();
        c.add(1);
        c.add(234534098);
        c.add(2);

        IntHashSet s = new IntHashSet();
        s.addAll(c);
        assertEquals(c.size(), s.size());
        assertTrue(s.contains(1));
        assertTrue(s.contains(2));
        assertTrue(s.contains(234534098));
    }

    @Test
    public void testAddAllIntArray() {
        int[] a = {1, 987654321, 2};

        IntHashSet s = new IntHashSet();
        s.addAll(a);
        assertEquals(a.length, s.size());
        assertTrue(s.contains(1));
        assertTrue(s.contains(2));
        assertTrue(s.contains(987654321));
    }

    @Test
    public void testAddAllIntSet() {
        IntSet os = new IntHashSet();
        os.add(1);
        os.add(987654321);
        os.add(2);

        IntHashSet s = new IntHashSet();
        s.addAll(os);
        assertEquals(os.size(), s.size());
        assertTrue(s.contains(1));
        assertTrue(s.contains(2));
        assertTrue(s.contains(987654321));
    }

    @Test
    public void testRemoveAllCollectionOfInteger() {
        IntHashSet s = new IntHashSet();
        s.add(1);
        s.add(2);
        s.add(987654321);

        Collection<Integer> c = new ArrayList<>();
        c.add(1);
        c.add(2);

        s.removeAll(c);
        assertEquals(1, s.size());
        assertFalse(s.contains(1));
        assertFalse(s.contains(2));
        assertTrue(s.contains(987654321));
    }

    @Test
    public void testRemoveAllIntArray() {
        IntHashSet s = new IntHashSet();
        s.add(1);
        s.add(2);
        s.add(987654321);

        int[] a = {1, 2};

        s.removeAll(a);
        assertEquals(1, s.size());
        assertFalse(s.contains(1));
        assertFalse(s.contains(2));
        assertTrue(s.contains(987654321));
    }

    @Test
    public void testRemoveAllIntSet() {
        IntHashSet s = new IntHashSet();
        s.add(1);
        s.add(2);
        s.add(987654321);

        IntSet os = new IntHashSet();
        os.add(1);
        os.add(2);

        s.removeAll(os);
        assertEquals(1, s.size());
        assertFalse(s.contains(1));
        assertFalse(s.contains(2));
        assertTrue(s.contains(987654321));
    }

    @Test
    public void testToArray() {
        IntHashSet s = new IntHashSet();
        s.add(1);
        s.add(2);
        s.add(987654321);

        int[] intArr = s.toArray();
        Arrays.sort(intArr);
        assertEquals(3, s.size());
        assertTrue(Arrays.binarySearch(intArr, 1) >= 0);
        assertTrue(Arrays.binarySearch(intArr, 2) >= 0);
        assertTrue(Arrays.binarySearch(intArr, 987654321) >= 0);
        assertFalse(Arrays.binarySearch(intArr, 3) >= 0);
    }

    @Test
    public void testContainsAllCollectionOfInteger() {
        IntHashSet s = new IntHashSet();
        s.add(1);
        s.add(2);
        s.add(987654321);

        Collection<Integer> c = new ArrayList<>();
        c.add(1);
        c.add(2);
        assertTrue(s.containsAll(c));

        c.add(3);
        assertFalse(s.containsAll(c));
    }

    @Test
    public void testContainsAllIntArray() {
        IntHashSet s = new IntHashSet();
        s.add(1);
        s.add(2);
        s.add(987654321);

        int[] a = {1, 2};
        assertTrue(s.containsAll(a));

        a = new int[]{1, 2, 3};
        assertFalse(s.containsAll(a));
    }

    @Test
    public void testContainsAllIntSet() {
        IntHashSet s = new IntHashSet();
        s.add(1);
        s.add(2);
        s.add(987654321);

        IntSet os = new IntHashSet();
        os.add(1);
        os.add(2);
        assertTrue(s.containsAll(os));

        os.add(3);
        assertFalse(s.containsAll(os));
    }

    @Test
    public void testRetainAllCollectionOfInteger() {
        IntHashSet s = new IntHashSet();
        s.add(1);
        s.add(2);
        s.add(987654321);

        Collection<Integer> c = new ArrayList<>();
        c.add(1);
        c.add(987654321);

        s.retainAll(c);

        assertEquals(2, s.size());
        assertTrue(s.contains(1));
        assertTrue(s.contains(987654321));
        assertFalse(s.contains(2));

    }

    @Test
    public void testRetainAllIntArray() {
        IntHashSet s = new IntHashSet();
        s.add(1);
        s.add(2);
        s.add(987654321);

        int[] a = {1, 987654321};

        s.retainAll(a);

        assertEquals(2, s.size());
        assertTrue(s.contains(1));
        assertTrue(s.contains(987654321));
        assertFalse(s.contains(2));
    }

    @Test
    public void testRetainAllIntSet() {
        IntHashSet s = new IntHashSet();
        s.add(1);
        s.add(2);
        s.add(987654321);

        IntSet os = new IntHashSet();
        os.add(1);
        os.add(987654321);

        s.retainAll(os);

        assertEquals(2, s.size());
        assertTrue(s.contains(1));
        assertTrue(s.contains(987654321));
        assertFalse(s.contains(2));
    }

}
