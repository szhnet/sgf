package io.jpower.sgf.collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

import org.junit.Test;

/**
 * @author zheng.sun
 */
public class SimpleCowArrayListTest {

    @Test
    public void testSimpleCowArrayListCollection() {
        List<String> jl = new ArrayList<String>();
        jl.add("abc");
        jl.add("321");
        jl.add("szh");
        SimpleCowArrayList<String> l = new SimpleCowArrayList<String>(jl);
        assertEquals(jl.size(), l.size());
        for (int i = 0; i < jl.size(); i++) {
            assertEquals(jl.get(i), l.get(i));
        }
    }

    @Test
    public void testAdd() {
        SimpleCowArrayList<String> l = new SimpleCowArrayList<String>();
        boolean add = l.add("szh");
        assertTrue(add);
        assertEquals(1, l.size());
        assertEquals("szh", l.get(0));
    }

    @Test
    public void testAddInt() {
        SimpleCowArrayList<String> l = new SimpleCowArrayList<String>();

        // 测一下异常
        try {
            l.add(1, "szh");
            fail("should throw exception");
        } catch (Exception e) {

        }

        // 正常添加
        l.add("abc");
        l.add("321");
        l.add(1, "szh");

        assertEquals(3, l.size());
        assertEquals("szh", l.get(1));
        assertEquals("321", l.get(2));
    }

    @Test
    public void testRemoveInt() {
        SimpleCowArrayList<String> l = new SimpleCowArrayList<String>();

        // 测一下异常
        try {
            l.remove(1);
            fail("should throw exception");
        } catch (Exception e) {

        }

        // 正常删除
        l.add("szh");
        String rmObj = l.remove(0);
        assertEquals("szh", rmObj);
        assertEquals(0, l.size());

        l.add("abc");
        l.add("321");
        l.add("szh");
        rmObj = l.remove(1);

        assertEquals("321", rmObj);
        assertEquals(2, l.size());
        assertEquals("abc", l.get(0));
        assertEquals("szh", l.get(1));
    }

    @Test
    public void testRemoveObject() {
        SimpleCowArrayList<String> l = new SimpleCowArrayList<String>();

        l.add("szh");
        boolean rm = l.remove("szh");
        assertTrue(rm);
        assertEquals(0, l.size());

        l.add("abc");
        l.add("321");
        l.add("szh");
        rm = l.remove("321");

        assertTrue(rm);
        assertEquals(2, l.size());
        assertEquals("abc", l.get(0));
        assertEquals("szh", l.get(1));
    }

    @Test
    public void testGet() {
        SimpleCowArrayList<String> l = new SimpleCowArrayList<String>();
        // 测一下异常
        try {
            l.get(0);
            fail("should throw exception");
        } catch (Exception e) {

        }

        l.add("lalala");
        assertEquals("lalala", l.get(0));
    }

    @Test
    public void testContains() {
        SimpleCowArrayList<Integer> l = new SimpleCowArrayList<Integer>();

        boolean contains = l.contains(123456);
        assertFalse(contains);

        l.add(123456);

        contains = l.contains(123456);
        assertTrue(contains);

        contains = l.contains(2);
        assertFalse(contains);
    }

    @Test
    public void testIterator() {
        SimpleCowArrayList<String> l = new SimpleCowArrayList<String>();
        Iterator<String> itr = l.iterator();
        assertFalse(itr.hasNext());

        // 测一下异常
        try {
            itr.next();
            fail("should throw exception");
        } catch (NoSuchElementException e) {

        }

        try {
            itr.remove();
            fail("should throw exception");
        } catch (UnsupportedOperationException e) {

        }

        l.add("abc");
        itr = l.iterator();
        assertTrue(itr.hasNext());

        assertEquals("abc", itr.next());
        assertFalse(itr.hasNext());
        // 只有一个元素，再次next应该抛异常
        try {
            itr.next();
            fail("should throw exception");
        } catch (NoSuchElementException e) {

        }
    }

    @Test
    public void testIteratorConcurrentModificationException() {
        List<String> l = new SimpleCowArrayList<String>();
        l.add("123");
        l.add("szh");
        l.add("abc");
        String rmStr = "123";
        try {
            for (String str : l) {
                if (str.equals(rmStr)) {
                    l.remove(rmStr);
                }
            }
        } catch (ConcurrentModificationException e) {
            fail("throw ConcurrentModificationException");
        }
    }

    @Test
    public void testToArray() {
        List<String> l = new SimpleCowArrayList<String>();
        l.add("123");
        l.add("szh");
        l.add("abc");
        Object[] array = l.toArray();
        assertEquals(l.size(), array.length);

        for (int i = 0; i < array.length; i++) {
            assertEquals(l.get(i), array[i]);
        }
    }

    @Test
    public void testToArrayTArray() {
        List<String> l = new SimpleCowArrayList<String>();
        l.add("123");
        l.add("szh");
        l.add("abc");

        int len = l.size();
        // 数组容量小情况
        String[] array = l.toArray(new String[0]);
        assertEquals(len, array.length);

        for (int i = 0; i < len; i++) {
            assertEquals(l.get(i), array[i]);
        }

        // 数组容量相等的情况
        String[] strs = new String[len];
        array = l.toArray(strs);
        assertTrue(strs == array); // 应该就是原来的数组
        for (int i = 0; i < len; i++) {
            assertEquals(l.get(i), array[i]);
        }

        // 数组容量大的情况
        strs = new String[len + 1];
        array = l.toArray(strs);
        assertTrue(strs == array); // 应该就是原来的数组
        for (int i = 0; i < len; i++) {
            assertEquals(l.get(i), array[i]);
        }
        assertNull(array[len]); // 紧随列表尾部的元素应该为null
    }

    @Test
    public void testContainsAll() {
        List<Integer> l = new SimpleCowArrayList<Integer>();

        List<Integer> cl = new ArrayList<Integer>();
        cl.add(1);
        cl.add(2);
        cl.add(100);

        List<Integer> cl1 = new ArrayList<Integer>();
        cl1.add(3);
        cl1.add(2);
        cl1.add(100);

        boolean contains = l.containsAll(cl);
        assertFalse(contains);

        l.add(1);
        l.add(2);
        l.add(100);

        contains = l.containsAll(cl);
        assertTrue(contains);

        contains = l.containsAll(cl1); // 不包含cl1
        assertFalse(contains);

        l.add(1010); // 再增加一个，应该还是包含
        contains = l.containsAll(cl);
        assertTrue(contains);

        cl.add(9); // cl再增加一个，就不包含了
        contains = l.containsAll(cl);
        assertFalse(contains);
    }

    @Test
    public void testAddAllCollection() {
        List<Integer> l = new SimpleCowArrayList<Integer>();

        List<Integer> cl = new ArrayList<Integer>();
        cl.add(1);
        cl.add(2);
        cl.add(100);

        l.addAll(cl);
        assertEquals(cl.size(), l.size());

        for (int i = 0; i < cl.size(); i++) {
            assertEquals(cl.get(i), l.get(i));
        }
    }

    @Test
    public void testAddAllIntCollection() {
        List<Integer> l = new SimpleCowArrayList<Integer>();

        List<Integer> cl = new ArrayList<Integer>();
        cl.add(1);
        cl.add(2);
        cl.add(100);

        l.addAll(0, cl);

        assertEquals(cl.size(), l.size());

        for (int i = 0; i < cl.size(); i++) {
            assertEquals(cl.get(i), l.get(i));
        }

        l.clear();

        l.add(1001);
        l.add(1002);
        l.addAll(1, cl);

        assertEquals(cl.size() + 2, l.size());

        for (int i = 0; i < cl.size(); i++) {
            assertEquals(cl.get(i), l.get(i + 1));
        }

        assertEquals(Integer.valueOf(1001), l.get(0));
        assertEquals(Integer.valueOf(1002), l.get(cl.size() + 1));
    }

    @Test
    public void testRemoveAll() {
        List<String> l = new SimpleCowArrayList<String>();

        l.add("szh");
        l.add("abc");
        l.add("lalala");

        List<String> cl = new ArrayList<String>();
        cl.add("szh");
        cl.add("abc");

        l.removeAll(cl);

        assertEquals(1, l.size());
        assertEquals("lalala", l.get(0));
    }

    @Test
    public void testRetainAll() {
        List<String> l = new SimpleCowArrayList<String>();

        l.add("szh");
        l.add("abc");
        l.add("lalala");

        List<String> cl = new ArrayList<String>();
        cl.add("szh");

        l.retainAll(cl);

        assertEquals(1, l.size());
        assertEquals("szh", l.get(0));
    }

    @Test
    public void testClear() {
        List<String> l = new SimpleCowArrayList<String>();

        l.add("szh");
        l.add("abc");
        l.add("321");

        l.clear();

        assertEquals(0, l.size());
    }

    @Test
    public void testEqualsObject() {
        List<String> x = new SimpleCowArrayList<String>();

        x.add("szh");
        x.add("abc");
        x.add("321");

        assertTrue(x.equals(x)); // 自反性

        List<String> y = new ArrayList<String>();
        y.add("szh");
        y.add("abc");

        assertFalse(x.equals(y));
        y.add("321");
        assertTrue(x.equals(y));
        assertTrue(x.equals(y)); // 一致性

        assertTrue(y.equals(x)); // 对称性

        List<String> z = new SimpleCowArrayList<String>();
        z.add("szh");
        z.add("abc");
        z.add("321");

        assertTrue(y.equals(z));
        assertTrue(x.equals(z)); // 传递性

        assertFalse(x.equals(null)); // null
    }

    @Test
    public void testSet() {
        List<String> l = new SimpleCowArrayList<String>();
        // 测一下异常
        try {
            l.set(0, "abc");
            fail("should throw exception");
        } catch (ArrayIndexOutOfBoundsException e) {

        }

        l.add("abc");
        l.add("321");
        l.set(0, "szh");

        assertEquals(2, l.size());
        assertEquals("szh", l.get(0));
        assertEquals("321", l.get(1));

        try {
            l.set(2, "lalala");
            fail("should throw exception");
        } catch (ArrayIndexOutOfBoundsException e) {

        }
    }

    @Test
    public void testIndexOf() {
        List<String> l = new SimpleCowArrayList<String>();

        l.add("abc");
        l.add("szh");
        l.add("321");
        l.add("szh");

        assertEquals(1, l.indexOf("szh"));
    }

    @Test
    public void testLastIndexOf() {
        List<String> l = new SimpleCowArrayList<String>();

        l.add("abc");
        l.add("szh");
        l.add("321");
        l.add("szh");

        assertEquals(3, l.lastIndexOf("szh"));
    }

    @Test
    public void testListIterator() {
        SimpleCowArrayList<String> l = new SimpleCowArrayList<String>();
        ListIterator<String> litr = l.listIterator();
        assertFalse(litr.hasNext());

        // 测一下异常
        try {
            litr.next();
            fail("should throw exception");
        } catch (NoSuchElementException e) {

        }

        assertFalse(litr.hasPrevious());

        try {
            litr.previous();
            fail("should throw exception");
        } catch (NoSuchElementException e) {

        }

        l.add("abc");
        litr = l.listIterator();

        assertEquals(0, litr.nextIndex());
        assertTrue(litr.hasNext());
        assertEquals("abc", litr.next());
        assertEquals(1, litr.nextIndex());
        assertFalse(litr.hasNext());
        // 只有一个元素，再次next应该抛异常
        try {
            litr.next();
            fail("should throw exception");
        } catch (NoSuchElementException e) {

        }

        assertEquals(0, litr.previousIndex());
        assertTrue(litr.hasPrevious());
        assertEquals("abc", litr.previous());
        assertFalse(litr.hasPrevious());
        assertEquals(-1, litr.previousIndex());
        try {
            litr.previous();
            fail("should throw exception");
        } catch (NoSuchElementException e) {

        }

        // UnsupportedOperation
        try {
            litr.remove();
            fail("should throw exception");
        } catch (UnsupportedOperationException e) {

        }

        try {
            litr.set("123");
            fail("should throw exception");
        } catch (UnsupportedOperationException e) {

        }

        try {
            litr.add("lalala");
            fail("should throw exception");
        } catch (UnsupportedOperationException e) {

        }
    }

    @Test
    public void testListIteratorInt() {
        SimpleCowArrayList<String> l = new SimpleCowArrayList<String>();

        l.add("szh");
        l.add("abc");

        ListIterator<String> litr = l.listIterator(1);
        assertTrue(litr.hasNext());

        assertEquals("abc", litr.next());
    }

    @Test
    public void testSubList() {
        List<String> l = new SimpleCowArrayList<String>();

        l.add("szh");
        l.add("abc");
        l.add("321");

        try {
            l.subList(1, 4);
            fail("should throw exception");
        } catch (IndexOutOfBoundsException e) {

        }

        List<String> subl = l.subList(1, 3);
        assertEquals(2, subl.size());
        assertEquals("abc", subl.get(0));
        assertEquals("321", subl.get(1));
    }

    /**
     * 测试一下Sub List迭代器
     */
    @Test
    public void testSubListIterator() {
        SimpleCowArrayList<String> l = new SimpleCowArrayList<String>();
        l.add("szh");
        l.add("abc");
        l.add("321");

        List<String> subl = l.subList(0, 0);

        Iterator<String> itr = subl.iterator();
        assertFalse(itr.hasNext());

        // 测一下异常
        try {
            itr.next();
            fail("should throw exception");
        } catch (NoSuchElementException e) {

        }

        try {
            itr.remove();
            fail("should throw exception");
        } catch (UnsupportedOperationException e) {

        }

        subl = l.subList(1, 3);
        itr = subl.iterator();
        assertTrue(itr.hasNext());

        assertEquals("abc", itr.next());
        assertTrue(itr.hasNext());
        assertEquals("321", itr.next());
        assertFalse(itr.hasNext());
        // 再次next应该抛异常
        try {
            itr.next();
            fail("should throw exception");
        } catch (NoSuchElementException e) {

        }
    }

    @Test
    public void testSubListListIterator() {
        SimpleCowArrayList<String> l = new SimpleCowArrayList<String>();
        l.add("szh");
        l.add("abc");

        List<String> subl = l.subList(0, 0);
        ListIterator<String> litr = subl.listIterator();
        assertFalse(litr.hasNext());

        // 测一下异常
        try {
            litr.next();
            fail("should throw exception");
        } catch (NoSuchElementException e) {

        }

        assertFalse(litr.hasPrevious());

        try {
            litr.previous();
            fail("should throw exception");
        } catch (NoSuchElementException e) {

        }

        subl = l.subList(1, 2);
        litr = subl.listIterator();

        assertEquals(0, litr.nextIndex());
        assertTrue(litr.hasNext());
        assertEquals("abc", litr.next());
        assertEquals(1, litr.nextIndex());
        assertFalse(litr.hasNext());
        // 只有一个元素，再次next应该抛异常
        try {
            litr.next();
            fail("should throw exception");
        } catch (NoSuchElementException e) {

        }

        assertEquals(0, litr.previousIndex());
        assertTrue(litr.hasPrevious());
        assertEquals("abc", litr.previous());
        assertFalse(litr.hasPrevious());
        assertEquals(-1, litr.previousIndex());
        try {
            litr.previous();
            fail("should throw exception");
        } catch (NoSuchElementException e) {

        }

        // UnsupportedOperation
        try {
            litr.remove();
            fail("should throw exception");
        } catch (UnsupportedOperationException e) {

        }

        try {
            litr.set("123");
            fail("should throw exception");
        } catch (UnsupportedOperationException e) {

        }

        try {
            litr.add("lalala");
            fail("should throw exception");
        } catch (UnsupportedOperationException e) {

        }
    }

    @Test
    public void testSubListListIteratorInt() {
        SimpleCowArrayList<String> l = new SimpleCowArrayList<String>();
        l.add("szh");
        l.add("321");
        l.add("abc");

        List<String> subl = l.subList(1, 3);

        ListIterator<String> litr = subl.listIterator(1);
        assertTrue(litr.hasNext());

        assertEquals("abc", litr.next());
    }

    @Test
    public void testToString() {
        List<String> l = new SimpleCowArrayList<String>();

        l.add("szh");
        l.add("abc");
        l.add("321");

        List<String> arrL = new ArrayList<String>();
        arrL.addAll(l);

        assertEquals(arrL.toString(), l.toString());
    }

    @Test
    public void testClone() {
        SimpleCowArrayList<String> l = new SimpleCowArrayList<String>();
        @SuppressWarnings("unchecked")
        SimpleCowArrayList<String> cl = (SimpleCowArrayList<String>) l.clone();
        assertFalse(l == cl);

        assertTrue(l.equals(cl));
    }

}
