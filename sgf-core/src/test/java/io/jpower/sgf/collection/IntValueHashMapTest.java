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
public class IntValueHashMapTest {

    @Test
    public void testIntValueHashMapMapOfQextendsKInteger() {
        Map<String, Integer> jm = new HashMap<>();
        jm.put("abc", 1);
        jm.put("321", 2);
        jm.put("szh", 3);

        IntValueHashMap<String> m = new IntValueHashMap<>(jm);
        assertEquals(jm.size(), m.size());
        assertTrue(m.containsKey("abc"));
        assertTrue(m.containsKey("321"));
        assertTrue(m.containsKey("szh"));
        assertTrue(m.containsValue(1));
        assertTrue(m.containsValue(2));
        assertTrue(m.containsValue(3));
    }

    @Test
    public void testIntValueHashMapIntValueMapOfK() {
        IntValueHashMap<String> m1 = new IntValueHashMap<>();
        m1.put("abc", 1);
        m1.put("321", 2);
        m1.put("szh", 3);

        IntValueHashMap<String> m = new IntValueHashMap<>(m1);
        assertEquals(m1.size(), m.size());
        assertTrue(m.containsKey("abc"));
        assertTrue(m.containsKey("321"));
        assertTrue(m.containsKey("szh"));
        assertTrue(m.containsValue(1));
        assertTrue(m.containsValue(2));
        assertTrue(m.containsValue(3));
    }

    @Test
    public void testGetObject() {
        IntValueHashMap<String> m = new IntValueHashMap<>();
        assertEquals(IntValueMap.DEFAULT_VALUE, m.get("abc"));
        m.put("abc", 101);
        assertEquals(101, m.get("abc"));
        assertEquals(IntValueMap.DEFAULT_VALUE, m.get("321"));
    }

    @Test
    public void testGetObjectInt() {
        IntValueHashMap<String> m = new IntValueHashMap<>();
        assertEquals(1001, m.get("abc", 1001));
        m.put("abc", 101);
        assertEquals(101, m.get("abc"));
        assertEquals(-1, m.get("321", -1));
    }

    @Test
    public void testContainsKey() {
        IntValueHashMap<String> m = new IntValueHashMap<>();
        assertFalse(m.containsKey("abc"));
        m.put("abc", 1);
        assertTrue(m.containsKey("abc"));
        assertFalse(m.containsKey("321"));
    }

    @Test
    public void testPutKInt() {
        IntValueHashMap<String> m = new IntValueHashMap<>();
        int exists = m.put("abc", Integer.MAX_VALUE);
        assertEquals(IntValueMap.DEFAULT_VALUE, exists);
        assertEquals(1, m.size());
        assertEquals(Integer.MAX_VALUE, m.get("abc"));

        // 测试一下key相同的情况
        exists = m.put("abc", 1);
        assertEquals(Integer.MAX_VALUE, exists);
        assertEquals(1, m.size());
        assertEquals(1, m.get("abc"));
    }

    @Test
    public void testPutKIntInt() {
        IntValueHashMap<String> m = new IntValueHashMap<>();
        int exists = m.put("abc", Integer.MAX_VALUE, -1);
        assertEquals(-1, exists);
        assertEquals(1, m.size());
        assertEquals(Integer.MAX_VALUE, m.get("abc"));

        // 测试一下key相同的情况
        exists = m.put("abc", 1, -1);
        assertEquals(Integer.MAX_VALUE, exists);
        assertEquals(1, m.size());
        assertEquals(1, m.get("abc"));
    }

    @Test
    public void testPutAllMapOfQextendsKInteger() {
        Map<String, Integer> jm = new HashMap<>();
        jm.put("abc", 1);
        jm.put("321", 2);
        jm.put("szh", 3);

        IntValueHashMap<String> m = new IntValueHashMap<>();
        m.putAll(jm);
        assertEquals(jm.size(), m.size());
        assertTrue(m.containsKey("abc"));
        assertTrue(m.containsKey("321"));
        assertTrue(m.containsKey("szh"));
        assertTrue(m.containsValue(1));
        assertTrue(m.containsValue(2));
        assertTrue(m.containsValue(3));
    }

    @Test
    public void testPutAllIntValueMapOfK() {
        IntValueHashMap<String> jm = new IntValueHashMap<>();
        jm.put("abc", 1);
        jm.put("321", 2);
        jm.put("szh", 3);

        IntValueHashMap<String> m = new IntValueHashMap<>();
        m.putAll(jm);
        assertEquals(jm.size(), m.size());
        assertTrue(m.containsKey("abc"));
        assertTrue(m.containsKey("321"));
        assertTrue(m.containsKey("szh"));
        assertTrue(m.containsValue(1));
        assertTrue(m.containsValue(2));
        assertTrue(m.containsValue(3));
    }

    @Test
    public void testRemoveObject() {
        IntValueHashMap<String> jm = new IntValueHashMap<>();
        int exists = jm.remove("abc");
        assertEquals(IntValueMap.DEFAULT_VALUE, exists);

        int value = 100;
        jm.put("abc", value);
        exists = jm.remove("abc");
        assertEquals(value, exists);
        exists = jm.remove("abc"); // 再删一次，应该没了
        assertEquals(IntValueMap.DEFAULT_VALUE, exists);
    }

    @Test
    public void testRemoveObjectInt() {
        IntValueHashMap<String> jm = new IntValueHashMap<>();
        int defaultVal = 100;
        int exists = jm.remove("abc", defaultVal);
        assertEquals(defaultVal, exists);

        int value = 100;
        jm.put("abc", value);
        exists = jm.remove("abc", defaultVal);
        assertEquals(value, exists);
        exists = jm.remove("abc", defaultVal); // 再删一次，应该没了
        assertEquals(defaultVal, exists);
    }

    @Test
    public void testContainsValue() {
        IntValueHashMap<String> m = new IntValueHashMap<>();
        int value = 1000;
        assertFalse(m.containsValue(value));
        m.put("abc", value);
        assertTrue(m.containsValue(value));
        assertFalse(m.containsValue(100));
    }

    @Test
    public void testClone() {
        IntValueHashMap<String> m = new IntValueHashMap<>();
        @SuppressWarnings("unchecked")
        IntValueHashMap<String> cm = (IntValueHashMap<String>) m.clone();
        assertFalse(m == cm);

        assertTrue(m.equals(cm));
    }

    @Test
    public void testKeySet() {
        IntValueHashMap<String> m = new IntValueHashMap<>();
        Set<String> keySet = m.keySet();
        assertTrue(keySet.isEmpty());

        m.put("abc", 1);
        m.put("321", 2);

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
        IntValueHashMap<String> m = new IntValueHashMap<>();
        IntCollection values = m.values();
        assertTrue(values.isEmpty());

        m.put("abc", 1);
        m.put("321", 2);

        values = m.values();
        assertEquals(2, values.size());
        assertTrue(values.contains(1));
        assertTrue(values.contains(2));
        IntIterator valueItr = values.iterator();
        assertTrue(valueItr.hasNext());
        valueItr.next();
        assertTrue(valueItr.hasNext());
        valueItr.next();
        assertFalse(valueItr.hasNext());
    }

    @Test
    public void testEntrySet() {
        IntValueHashMap<String> m = new IntValueHashMap<>();
        Set<IntValueMap.Entry<String>> enSet = m.entrySet();
        assertTrue(enSet.isEmpty());

        m.put("abc", 1);
        m.put("321", 2);

        enSet = m.entrySet();
        assertEquals(2, enSet.size());
        Iterator<IntValueMap.Entry<String>> enItr = enSet.iterator();
        assertTrue(enItr.hasNext());
        enItr.next();
        assertTrue(enItr.hasNext());
        enItr.next();
        assertFalse(enItr.hasNext());
    }

    @Test
    public void testEqualsObject() {
        IntValueHashMap<String> x = new IntValueHashMap<>();

        x.put("szh", 1);
        x.put("abc", 2);
        x.put("321", 3);

        assertTrue(x.equals(x)); // 自反性

        IntValueHashMap<String> y = new IntValueHashMap<>();
        y.put("szh", 1);
        y.put("abc", 2);

        assertFalse(x.equals(y));
        y.put("321", 3);
        assertTrue(x.equals(y));
        assertTrue(x.equals(y)); // 一致性

        assertTrue(y.equals(x)); // 对称性

        IntValueHashMap<String> z = new IntValueHashMap<>();

        z.put("szh", 1);
        z.put("abc", 2);
        z.put("321", 3);

        assertTrue(y.equals(z));
        assertTrue(x.equals(z)); // 传递性

        assertFalse(x.equals(null)); // null
    }

}
