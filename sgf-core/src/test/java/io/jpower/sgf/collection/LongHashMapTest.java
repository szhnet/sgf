package io.jpower.sgf.collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

/**
 * @author zheng.sun
 */
public class LongHashMapTest {

    private static final long K1 = 1010011000000001L;

    private static final long K2 = 1010011000000002L;

    private static final long K3 = 1010011000000003L;

    @Test
    public void testLongHashMapMapOfLongQextendsV() {
        Map<Long, String> jm = new HashMap<>();
        jm.put(K1, "abc");
        jm.put(K2, "321");
        jm.put(K3, "szh");

        LongHashMap<String> m = new LongHashMap<>(jm);
        assertEquals(jm.size(), m.size());
        assertTrue(m.containsKey(K1));
        assertTrue(m.containsKey(K2));
        assertTrue(m.containsKey(K3));
    }

    @Test
    public void testLongHashMapLongMapOfQextendsV() {
        LongHashMap<String> m1 = new LongHashMap<>();
        m1.put(K1, "abc");
        m1.put(K2, "321");
        m1.put(K3, "szh");

        LongHashMap<String> m = new LongHashMap<>(m1);
        assertEquals(m1.size(), m.size());
        assertTrue(m.containsKey(K1));
        assertTrue(m.containsKey(K2));
        assertTrue(m.containsKey(K3));
    }

    @Test
    public void testSize() {
        LongHashMap<String> m = new LongHashMap<>();
        assertEquals(0, m.size());
        m.put(K1, "abc");
        assertEquals(1, m.size());
        m.put(K2, "321");
        assertEquals(2, m.size());
        m.put(K1, "szh");
        assertEquals(2, m.size());
    }

    @Test
    public void testIsEmpty() {
        LongHashMap<String> m = new LongHashMap<>();
        assertTrue(m.isEmpty());
        m.put(1, "abc");
        assertFalse(m.isEmpty());
    }

    @Test
    public void testGet() {
        LongHashMap<String> m = new LongHashMap<>();
        assertNull(m.get(K1));
        m.put(K1, "abc");
        assertEquals("abc", m.get(K1));
        assertNull(m.get(K2));
    }

    @Test
    public void testContainsKey() {
        LongHashMap<String> m = new LongHashMap<>();
        assertFalse(m.containsKey(K1));
        m.put(K1, "abc");
        assertTrue(m.containsKey(K1));
        assertFalse(m.containsKey(K2));
    }

    @Test
    public void testPut() {
        LongHashMap<String> m = new LongHashMap<>();
        String exists = m.put(K1, "abc");
        assertNull(exists);
        assertEquals(1, m.size());
        assertEquals("abc", m.get(K1));

        // 测试一下key相同的情况
        exists = m.put(K1, "szh");
        assertEquals("abc", exists);
        assertEquals(1, m.size());
        assertEquals("szh", m.get(K1));
    }

    @Test
    public void testPutAllMapOfLongQextendsV() {
        Map<Long, String> jm = new HashMap<>();
        jm.put(K1, "abc");
        jm.put(K2, "321");
        jm.put(K3, "szh");

        LongHashMap<String> m = new LongHashMap<>();
        m.putAll(jm);
        assertEquals(jm.size(), m.size());
        assertTrue(m.containsKey(K1));
        assertTrue(m.containsKey(K2));
        assertTrue(m.containsKey(K3));
    }

    @Test
    public void testPutAllLongMapOfQextendsV() {
        LongHashMap<String> m1 = new LongHashMap<>();
        m1.put(K1, "abc");
        m1.put(K2, "321");
        m1.put(K3, "szh");

        LongHashMap<String> m = new LongHashMap<>(m1);
        assertEquals(m1.size(), m.size());
        assertTrue(m.containsKey(K1));
        assertTrue(m.containsKey(K2));
        assertTrue(m.containsKey(K3));
    }

    @Test
    public void testClear() {
        LongMap<String> m = new LongHashMap<>();
        m.put(K1, "abc");
        m.put(K2, "321");
        m.put(K3, "szh");

        m.clear();
        assertEquals(0, m.size());
    }

    @Test
    public void testContainsValue() {
        LongHashMap<String> m = new LongHashMap<>();
        assertFalse(m.containsValue("abc"));
        m.put(1, "abc");
        assertTrue(m.containsValue("abc"));
        assertFalse(m.containsValue("321"));
    }

    @Test
    public void testClone() {
        LongHashMap<String> m = new LongHashMap<>();
        @SuppressWarnings("unchecked")
        LongHashMap<String> cm = (LongHashMap<String>) m.clone();
        assertFalse(m == cm);

        assertTrue(m.equals(cm));
    }

    @Test
    public void testKeySet() {
        LongHashMap<String> m = new LongHashMap<>();
        LongSet keySet = m.keySet();
        assertTrue(keySet.isEmpty());

        m.put(K1, "abc");
        m.put(K2, "321");

        keySet = m.keySet();
        assertEquals(2, keySet.size());
        assertTrue(keySet.contains(K1));
        assertTrue(keySet.contains(K2));
        LongIterator keyItr = keySet.iterator();
        assertTrue(keyItr.hasNext());
        keyItr.next();
        assertTrue(keyItr.hasNext());
        keyItr.next();
        assertFalse(keyItr.hasNext());
    }

    @Test
    public void testValues() {
        LongHashMap<String> m = new LongHashMap<>();
        Collection<String> values = m.values();
        assertTrue(values.isEmpty());

        m.put(K1, "abc");
        m.put(K2, "321");

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
        LongHashMap<String> m = new LongHashMap<>();
        Set<LongMap.Entry<String>> enSet = m.entrySet();
        assertTrue(enSet.isEmpty());

        m.put(K1, "abc");
        m.put(K2, "321");

        enSet = m.entrySet();
        assertEquals(2, enSet.size());
        Iterator<LongMap.Entry<String>> enItr = enSet.iterator();
        assertTrue(enItr.hasNext());
        enItr.next();
        assertTrue(enItr.hasNext());
        enItr.next();
        assertFalse(enItr.hasNext());
    }

    @Test
    public void testEqualsObject() {
        LongHashMap<String> x = new LongHashMap<>();

        x.put(K1, "szh");
        x.put(K2, "abc");
        x.put(K3, "321");

        assertTrue(x.equals(x)); // 自反性

        LongHashMap<String> y = new LongHashMap<>();
        y.put(K1, "szh");
        y.put(K2, "abc");

        assertFalse(x.equals(y));
        y.put(K3, "321");
        assertTrue(x.equals(y));
        assertTrue(x.equals(y)); // 一致性

        assertTrue(y.equals(x)); // 对称性

        LongHashMap<String> z = new LongHashMap<>();

        z.put(K1, "szh");
        z.put(K2, "abc");
        z.put(K3, "321");

        assertTrue(y.equals(z));
        assertTrue(x.equals(z)); // 传递性

        assertFalse(x.equals(null)); // null
    }

}
