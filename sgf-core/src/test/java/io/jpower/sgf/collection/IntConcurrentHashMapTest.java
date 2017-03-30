package io.jpower.sgf.collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.Test;

/**
 * @author <a href="mailto:szhnet@gmail.com">szh</a>
 */
public class IntConcurrentHashMapTest {

    @Test
    public void testIntConcurrentHashMapMapOfIntegerQextendsV() {
        Map<Integer, String> jm = new ConcurrentHashMap<>();
        jm.put(1, "abc");
        jm.put(2, "321");
        jm.put(3, "szh");

        IntConcurrentHashMap<String> m = new IntConcurrentHashMap<>(jm);
        assertEquals(jm.size(), m.size());
        Iterator<IntMap.Entry<String>> enItr = m.entrySet().iterator();
        while (enItr.hasNext()) {
            IntMap.Entry<String> en = enItr.next();
            assertEquals(jm.get(en.getKey()), en.getValue());
        }
    }

    @Test
    public void testIntConcurrentHashMapIntMapOfQextendsV() {
        IntHashMap<String> m1 = new IntHashMap<>();
        m1.put(1, "abc");
        m1.put(2, "321");
        m1.put(3, "szh");

        IntConcurrentHashMap<String> m = new IntConcurrentHashMap<>(m1);
        assertEquals(m1.size(), m.size());
        assertTrue(m.containsKey(1));
        assertTrue(m.containsKey(2));
        assertTrue(m.containsKey(3));
    }

    @Test
    public void testSize() {
        IntConcurrentHashMap<String> m = new IntConcurrentHashMap<>();
        assertEquals(0, m.size());
        m.put(1, "abc");
        assertEquals(1, m.size());
        m.put(2, "321");
        assertEquals(2, m.size());
        m.put(1, "szh");
        assertEquals(2, m.size());
    }

    @Test
    public void testIsEmpty() {
        IntConcurrentHashMap<String> m = new IntConcurrentHashMap<>();
        assertTrue(m.isEmpty());
        m.put(1, "abc");
        assertFalse(m.isEmpty());
    }

    @Test
    public void testGet() {
        IntConcurrentHashMap<String> m = new IntConcurrentHashMap<>();
        assertNull(m.get(1));
        m.put(1, "abc");
        assertEquals("abc", m.get(1));
        assertNull(m.get(2));
    }

    @Test
    public void testContainsKey() {
        IntConcurrentHashMap<String> m = new IntConcurrentHashMap<>();
        assertFalse(m.containsKey(1));
        m.put(1, "abc");
        assertTrue(m.containsKey(1));
        assertFalse(m.containsKey(2));
    }

    @Test
    public void testPut() {
        IntConcurrentHashMap<String> m = new IntConcurrentHashMap<>();
        String exists = m.put(1, "abc");
        assertNull(exists);
        assertEquals(1, m.size());
        assertEquals("abc", m.get(1));

        // 测试一下key相同的情况
        exists = m.put(1, "szh");
        assertEquals("abc", exists);
        assertEquals(1, m.size());
        assertEquals("szh", m.get(1));
    }

    @Test
    public void testPutAllMapOfIntegerQextendsV() {
        Map<Integer, String> jm = new ConcurrentHashMap<>();
        jm.put(1, "abc");
        jm.put(2, "321");
        jm.put(3, "szh");

        IntConcurrentHashMap<String> m = new IntConcurrentHashMap<>();
        m.putAll(jm);
        assertEquals(jm.size(), m.size());
        Iterator<IntMap.Entry<String>> enItr = m.entrySet().iterator();
        while (enItr.hasNext()) {
            IntMap.Entry<String> en = enItr.next();
            assertEquals(jm.get(en.getKey()), en.getValue());
        }
    }

    @Test
    public void testPutAllIntMapOfQextendsV() {
        IntMap<String> m1 = new IntConcurrentHashMap<>();
        m1.put(1, "abc");
        m1.put(2, "321");
        m1.put(3, "szh");

        IntConcurrentHashMap<String> m = new IntConcurrentHashMap<>(m1);
        assertEquals(m1.size(), m.size());
        Iterator<IntMap.Entry<String>> en1Itr = m1.entrySet().iterator();
        Iterator<IntMap.Entry<String>> enItr = m.entrySet().iterator();
        while (enItr.hasNext()) {
            IntMap.Entry<String> en1 = en1Itr.next();
            IntMap.Entry<String> en = enItr.next();
            assertEquals(en1.getKey(), en.getKey());
            assertEquals(en1.getValue(), en.getValue());
        }
    }

    @Test
    public void testClear() {
        IntMap<String> m = new IntConcurrentHashMap<>();
        m.put(1, "abc");
        m.put(2, "321");
        m.put(3, "szh");

        m.clear();
        assertEquals(0, m.size());
    }

    @Test
    public void testContainsValue() {
        IntConcurrentHashMap<String> m = new IntConcurrentHashMap<>();
        assertFalse(m.containsValue("abc"));
        m.put(1, "abc");
        assertTrue(m.containsValue("abc"));
        assertFalse(m.containsValue("321"));
    }

    @Test
    public void testKeySet() {
        IntConcurrentHashMap<String> m = new IntConcurrentHashMap<>();
        IntSet keySet = m.keySet();
        assertTrue(keySet.isEmpty());

        m.put(1, "abc");
        m.put(2, "321");

        keySet = m.keySet();
        assertEquals(2, keySet.size());
        assertTrue(keySet.contains(1));
        assertTrue(keySet.contains(2));
        IntIterator keyItr = keySet.iterator();
        assertTrue(keyItr.hasNext());
        keyItr.next();
        assertTrue(keyItr.hasNext());
        keyItr.next();
        assertFalse(keyItr.hasNext());
    }

    @Test
    public void testValues() {
        IntConcurrentHashMap<String> m = new IntConcurrentHashMap<>();
        Collection<String> values = m.values();
        assertTrue(values.isEmpty());

        m.put(1, "abc");
        m.put(2, "321");

        values = m.values();
        assertEquals(2, values.size());
        assertTrue(values.contains("abc"));
        assertTrue(values.contains("321"));
        Iterator<String> valueItr = values.iterator();
        assertTrue(valueItr.hasNext());
        valueItr.next();
        assertTrue(valueItr.hasNext());
        valueItr.next();
        assertFalse(valueItr.hasNext());
    }

    @Test
    public void testEntrySet() {
        IntConcurrentHashMap<String> m = new IntConcurrentHashMap<>();
        Set<IntMap.Entry<String>> enSet = m.entrySet();
        assertTrue(enSet.isEmpty());

        m.put(1, "abc");
        m.put(2, "321");

        enSet = m.entrySet();
        assertEquals(2, enSet.size());
        Iterator<IntMap.Entry<String>> enItr = enSet.iterator();
        assertTrue(enItr.hasNext());
        enItr.next();
        assertTrue(enItr.hasNext());
        enItr.next();
        assertFalse(enItr.hasNext());
    }

    @Test
    public void testEqualsObject() {
        IntConcurrentHashMap<String> x = new IntConcurrentHashMap<>();

        x.put(1, "szh");
        x.put(2, "abc");
        x.put(3, "321");

        assertTrue(x.equals(x)); // 自反性

        IntConcurrentHashMap<String> y = new IntConcurrentHashMap<>();
        y.put(1, "szh");
        y.put(2, "abc");

        assertFalse(x.equals(y));
        y.put(3, "321");
        assertTrue(x.equals(y));
        assertTrue(x.equals(y)); // 一致性

        assertTrue(y.equals(x)); // 对称性

        IntHashMap<String> z = new IntHashMap<>();

        z.put(1, "szh");
        z.put(2, "abc");
        z.put(3, "321");

        assertTrue(y.equals(z));
        assertTrue(x.equals(z)); // 传递性

        assertFalse(x.equals(null)); // null
    }

}
