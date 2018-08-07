package io.jpower.sgf.collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

/**
 * @author <a href="mailto:szhnet@gmail.com">szh</a>
 */
public class LongValueHashMapTest {

    @Test
    public void testLongValueHashMapMapOfQextendsKLong() {
        Map<String, Long> jm = new HashMap<>();
        jm.put("abc", 1L);
        jm.put("321", 2l);
        jm.put("szh", 3L);

        LongValueHashMap<String> m = new LongValueHashMap<>(jm);
        assertEquals(jm.size(), m.size());
        assertTrue(m.containsKey("abc"));
        assertTrue(m.containsKey("321"));
        assertTrue(m.containsKey("szh"));
        assertTrue(m.containsValue(1L));
        assertTrue(m.containsValue(2L));
        assertTrue(m.containsValue(3L));
    }

    @Test
    public void testLongValueHashMapLongValueMapOfK() {
        LongValueHashMap<String> m1 = new LongValueHashMap<>();
        m1.put("abc", 1L);
        m1.put("321", 2L);
        m1.put("szh", 3L);

        LongValueHashMap<String> m = new LongValueHashMap<>(m1);
        assertEquals(m1.size(), m.size());
        assertTrue(m.containsKey("abc"));
        assertTrue(m.containsKey("321"));
        assertTrue(m.containsKey("szh"));
        assertTrue(m.containsValue(1L));
        assertTrue(m.containsValue(2L));
        assertTrue(m.containsValue(3L));
    }

    @Test
    public void testGetObject() {
        LongValueHashMap<String> m = new LongValueHashMap<>();
        assertEquals(LongValueMap.DEFAULT_VALUE, m.get("abc"));
        long value = 987654321123L;
        m.put("abc", value);
        assertEquals(value, m.get("abc"));
        assertEquals(LongValueMap.DEFAULT_VALUE, m.get("321"));
    }

    @Test
    public void testGetObjectLong() {
        LongValueHashMap<String> m = new LongValueHashMap<>();
        long defaultValue = 1001L;
        assertEquals(defaultValue, m.get("abc", defaultValue));
        m.put("abc", 101);
        assertEquals(101, m.get("abc"));
        assertEquals(-1, m.get("321", -1));
    }

    @Test
    public void testContainsKey() {
        LongValueHashMap<String> m = new LongValueHashMap<>();
        assertFalse(m.containsKey("abc"));
        m.put("abc", 1);
        assertTrue(m.containsKey("abc"));
        assertFalse(m.containsKey("321"));
    }

    @Test
    public void testPutKLong() {
        LongValueHashMap<String> m = new LongValueHashMap<>();
        long exists = m.put("abc", Long.MIN_VALUE);
        assertEquals(LongValueMap.DEFAULT_VALUE, exists);
        assertEquals(1, m.size());
        assertEquals(Long.MIN_VALUE, m.get("abc"));

        // 测试一下key相同的情况
        exists = m.put("abc", 1);
        assertEquals(Long.MIN_VALUE, exists);
        assertEquals(1, m.size());
        assertEquals(1, m.get("abc"));
    }

    @Test
    public void testPutKLongLong() {
        LongValueHashMap<String> m = new LongValueHashMap<>();
        long exists = m.put("abc", Long.MAX_VALUE, -1);
        assertEquals(-1, exists);
        assertEquals(1, m.size());
        assertEquals(Long.MAX_VALUE, m.get("abc"));

        // 测试一下key相同的情况
        exists = m.put("abc", 1, -1);
        assertEquals(Long.MAX_VALUE, exists);
        assertEquals(1, m.size());
        assertEquals(1, m.get("abc"));
    }

    @Test
    public void testPutAllMapOfQextendsKLong() {
        Map<String, Long> jm = new HashMap<>();
        jm.put("abc", 1L);
        jm.put("321", 2L);
        jm.put("szh", 3L);

        LongValueHashMap<String> m = new LongValueHashMap<>();
        m.putAll(jm);
        assertEquals(jm.size(), m.size());
        assertTrue(m.containsKey("abc"));
        assertTrue(m.containsKey("321"));
        assertTrue(m.containsKey("szh"));
        assertTrue(m.containsValue(1L));
        assertTrue(m.containsValue(2L));
        assertTrue(m.containsValue(3L));
    }

    @Test
    public void testPutAllLongValueMapOfK() {
        LongValueHashMap<String> jm = new LongValueHashMap<>();
        jm.put("abc", 1L);
        jm.put("321", 2L);
        jm.put("szh", 3L);

        LongValueHashMap<String> m = new LongValueHashMap<>();
        m.putAll(jm);
        assertEquals(jm.size(), m.size());
        assertTrue(m.containsKey("abc"));
        assertTrue(m.containsKey("321"));
        assertTrue(m.containsKey("szh"));
        assertTrue(m.containsValue(1L));
        assertTrue(m.containsValue(2L));
        assertTrue(m.containsValue(3L));
    }

    @Test
    public void testRemoveObject() {
        LongValueHashMap<String> jm = new LongValueHashMap<>();
        long exists = jm.remove("abc");
        assertEquals(LongValueMap.DEFAULT_VALUE, exists);

        long value = 987654321123456789L;
        jm.put("abc", value);
        exists = jm.remove("abc");
        assertEquals(value, exists);
        exists = jm.remove("abc"); // 再删一次，应该没了
        assertEquals(LongValueMap.DEFAULT_VALUE, exists);
    }

    @Test
    public void testRemoveObjectLong() {
        LongValueHashMap<String> jm = new LongValueHashMap<>();
        long defaultVal = 12345678909876L;
        long exists = jm.remove("abc", defaultVal);
        assertEquals(defaultVal, exists);

        long value = 100;
        jm.put("abc", value);
        exists = jm.remove("abc", defaultVal);
        assertEquals(value, exists);
        exists = jm.remove("abc", defaultVal); // 再删一次，应该没了
        assertEquals(defaultVal, exists);
    }

    @Test
    public void testContainsValue() {
        LongValueHashMap<String> m = new LongValueHashMap<>();
        long value = 1000L;
        assertFalse(m.containsValue(value));
        m.put("abc", value);
        assertTrue(m.containsValue(value));
        assertFalse(m.containsValue(100L));
    }

    @Test
    public void testClone() {
        LongValueHashMap<String> m = new LongValueHashMap<>();
        @SuppressWarnings("unchecked")
        LongValueHashMap<String> cm = (LongValueHashMap<String>) m.clone();
        assertFalse(m == cm);

        assertTrue(m.equals(cm));
    }

    @Test
    public void testKeySet() {
        LongValueHashMap<String> m = new LongValueHashMap<>();
        Set<String> keySet = m.keySet();
        assertTrue(keySet.isEmpty());

        m.put("abc", 1L);
        m.put("321", 2L);

        keySet = m.keySet();
        assertEquals(2, keySet.size());
        assertTrue(keySet.contains("abc"));
        assertTrue(keySet.contains("321"));
        Iterator<String> keyItr = keySet.iterator();
        assertTrue(keyItr.hasNext());
        keyItr.next();
        assertTrue(keyItr.hasNext());
        keyItr.next();
        assertFalse(keyItr.hasNext());
    }

    @Test
    public void testValues() {
        LongValueHashMap<String> m = new LongValueHashMap<>();
        LongCollection values = m.values();
        assertTrue(values.isEmpty());

        m.put("abc", 1L);
        m.put("321", 2L);

        values = m.values();
        assertEquals(2, values.size());
        assertTrue(values.contains(1L));
        assertTrue(values.contains(2L));
        LongIterator valueItr = values.iterator();
        assertTrue(valueItr.hasNext());
        valueItr.next();
        assertTrue(valueItr.hasNext());
        valueItr.next();
        assertFalse(valueItr.hasNext());
    }

    @Test
    public void testEntrySet() {
        LongValueHashMap<String> m = new LongValueHashMap<>();
        Set<LongValueMap.Entry<String>> enSet = m.entrySet();
        assertTrue(enSet.isEmpty());

        m.put("abc", 1L);
        m.put("321", 2L);

        enSet = m.entrySet();
        assertEquals(2, enSet.size());
        Iterator<LongValueMap.Entry<String>> enItr = enSet.iterator();
        assertTrue(enItr.hasNext());
        enItr.next();
        assertTrue(enItr.hasNext());
        enItr.next();
        assertFalse(enItr.hasNext());
    }

    @Test
    public void testEqualsObject() {
        LongValueHashMap<String> x = new LongValueHashMap<>();

        x.put("szh", 1L);
        x.put("abc", 2L);
        x.put("321", 3L);

        assertTrue(x.equals(x)); // 自反性

        LongValueHashMap<String> y = new LongValueHashMap<>();
        y.put("szh", 1L);
        y.put("abc", 2L);

        assertFalse(x.equals(y));
        y.put("321", 3L);
        assertTrue(x.equals(y));
        assertTrue(x.equals(y)); // 一致性

        assertTrue(y.equals(x)); // 对称性

        LongValueHashMap<String> z = new LongValueHashMap<>();

        z.put("szh", 1L);
        z.put("abc", 2L);
        z.put("321", 3L);

        assertTrue(y.equals(z));
        assertTrue(x.equals(z)); // 传递性

        assertFalse(x.equals(null)); // null
    }

}
