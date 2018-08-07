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
public class DoubleValueHashMapTest {

    private static final double DELTA = 0.001;

    @Test
    public void testDoubleValueHashMapMapOfQextendsKDouble() {
        Map<String, Double> jm = new HashMap<>();
        jm.put("abc", 1.1);
        jm.put("321", 2.2);
        jm.put("szh", 3.3);

        DoubleValueHashMap<String> m = new DoubleValueHashMap<>(jm);
        assertEquals(jm.size(), m.size());
        assertTrue(m.containsKey("abc"));
        assertTrue(m.containsKey("321"));
        assertTrue(m.containsKey("szh"));
        assertTrue(m.containsValue(1.1));
        assertTrue(m.containsValue(2.2));
        assertTrue(m.containsValue(3.3));
    }

    @Test
    public void testDoubleValueHashMapDoubleValueMapOfK() {
        DoubleValueHashMap<String> m1 = new DoubleValueHashMap<>();
        m1.put("abc", 1.15);
        m1.put("321", 2.25);
        m1.put("szh", 3.35);

        DoubleValueHashMap<String> m = new DoubleValueHashMap<>(m1);
        assertEquals(m1.size(), m.size());
        assertTrue(m.containsKey("abc"));
        assertTrue(m.containsKey("321"));
        assertTrue(m.containsKey("szh"));
        assertTrue(m.containsValue(1.15));
        assertTrue(m.containsValue(2.25));
        assertTrue(m.containsValue(3.35));
    }

    @Test
    public void testGetObject() {
        DoubleValueHashMap<String> m = new DoubleValueHashMap<>();
        assertEquals(DoubleValueMap.DEFAULT_VALUE, m.get("abc"), DELTA);
        double value = 10781.21;
        m.put("abc", value);
        assertEquals(value, m.get("abc"), DELTA);
        assertEquals(DoubleValueMap.DEFAULT_VALUE, m.get("321"), DELTA);
    }

    @Test
    public void testGetObjectDouble() {
        DoubleValueHashMap<String> m = new DoubleValueHashMap<>();
        double defaultValue = 1001.0;
        double value = 123.9;
        assertEquals(defaultValue, m.get("abc", defaultValue), DELTA);
        m.put("abc", value);
        assertEquals(value, m.get("abc"), DELTA);
        assertEquals(-1.0, m.get("321", -1.0), DELTA);
    }

    @Test
    public void testContainsKey() {
        DoubleValueHashMap<String> m = new DoubleValueHashMap<>();
        assertFalse(m.containsKey("abc"));
        m.put("abc", 1.1);
        assertTrue(m.containsKey("abc"));
        assertFalse(m.containsKey("321"));
    }

    @Test
    public void testPutKDouble() {
        DoubleValueHashMap<String> m = new DoubleValueHashMap<>();
        double value = 123.78;
        double exists = m.put("abc", value);
        assertEquals(DoubleValueMap.DEFAULT_VALUE, exists, DELTA);
        assertEquals(1, m.size());
        assertEquals(value, m.get("abc"), DELTA);

        // 测试一下key相同的情况
        double newValue = 1.1;
        exists = m.put("abc", newValue);
        assertEquals(value, exists, DELTA);
        assertEquals(1, m.size());
        assertEquals(newValue, m.get("abc"), DELTA);
    }

    @Test
    public void testPutKDoubleDouble() {
        DoubleValueHashMap<String> m = new DoubleValueHashMap<>();
        double value = 1.1f;
        double defaultValue = 2.2f;
        double exists = m.put("abc", value, defaultValue);
        assertEquals(defaultValue, exists, DELTA);
        assertEquals(1, m.size());
        assertEquals(value, m.get("abc"), DELTA);

        // 测试一下key相同的情况
        double newValue = 3.3;
        exists = m.put("abc", newValue, defaultValue);
        assertEquals(value, exists, DELTA);
        assertEquals(1, m.size());
        assertEquals(newValue, m.get("abc"), DELTA);
    }

    @Test
    public void testPutAllMapOfQextendsKDouble() {
        Map<String, Double> jm = new HashMap<>();
        jm.put("abc", 1.15);
        jm.put("321", 2.25);
        jm.put("szh", 3.35);

        DoubleValueHashMap<String> m = new DoubleValueHashMap<>();
        m.putAll(jm);
        assertEquals(jm.size(), m.size());
        assertTrue(m.containsKey("abc"));
        assertTrue(m.containsKey("321"));
        assertTrue(m.containsKey("szh"));
        assertTrue(m.containsValue(1.15));
        assertTrue(m.containsValue(2.25));
        assertTrue(m.containsValue(3.35));
    }

    @Test
    public void testPutAllDoubleValueMapOfK() {
        DoubleValueHashMap<String> jm = new DoubleValueHashMap<>();
        jm.put("abc", 1.1);
        jm.put("321", 2.2);
        jm.put("szh", 3.3);

        DoubleValueHashMap<String> m = new DoubleValueHashMap<>();
        m.putAll(jm);
        assertEquals(jm.size(), m.size());
        assertTrue(m.containsKey("abc"));
        assertTrue(m.containsKey("321"));
        assertTrue(m.containsKey("szh"));
        assertTrue(m.containsValue(1.1));
        assertTrue(m.containsValue(2.2));
        assertTrue(m.containsValue(3.3));
    }

    @Test
    public void testRemoveObject() {
        DoubleValueHashMap<String> jm = new DoubleValueHashMap<>();
        double exists = jm.remove("abc");
        assertEquals(DoubleValueMap.DEFAULT_VALUE, exists, DELTA);

        double value = 100.1;
        jm.put("abc", value);
        exists = jm.remove("abc");
        assertEquals(value, exists, DELTA);
        exists = jm.remove("abc"); // 再删一次，应该没了
        assertEquals(DoubleValueMap.DEFAULT_VALUE, exists, DELTA);
    }

    @Test
    public void testRemoveObjectDouble() {
        DoubleValueHashMap<String> jm = new DoubleValueHashMap<>();
        double defaultVal = 109.9f;
        double exists = jm.remove("abc", defaultVal);
        assertEquals(defaultVal, exists, DELTA);

        double value = 999.9;
        jm.put("abc", value);
        exists = jm.remove("abc", defaultVal);
        assertEquals(value, exists, DELTA);
        exists = jm.remove("abc", defaultVal); // 再删一次，应该没了
        assertEquals(defaultVal, exists, DELTA);
    }

    @Test
    public void testContainsValue() {
        DoubleValueHashMap<String> m = new DoubleValueHashMap<>();
        double value = 9999.9;
        assertFalse(m.containsValue(value));
        m.put("abc", value);
        assertTrue(m.containsValue(value));
        assertFalse(m.containsValue(100.0));
    }

    @Test
    public void testClone() {
        DoubleValueHashMap<String> m = new DoubleValueHashMap<>();
        @SuppressWarnings("unchecked")
        DoubleValueHashMap<String> cm = (DoubleValueHashMap<String>) m.clone();
        assertFalse(m == cm);

        assertTrue(m.equals(cm));
    }

    @Test
    public void testKeySet() {
        DoubleValueHashMap<String> m = new DoubleValueHashMap<>();
        Set<String> keySet = m.keySet();
        assertTrue(keySet.isEmpty());

        m.put("abc", 1.1);
        m.put("321", 2.2);

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
        DoubleValueHashMap<String> m = new DoubleValueHashMap<>();
        DoubleCollection values = m.values();
        assertTrue(values.isEmpty());

        m.put("abc", 12345.67);
        m.put("321", 765643.21);

        values = m.values();
        assertEquals(2, values.size());
        assertTrue(values.contains(12345.67));
        assertTrue(values.contains(765643.21));
        DoubleIterator valueItr = values.iterator();
        assertTrue(valueItr.hasNext());
        valueItr.next();
        assertTrue(valueItr.hasNext());
        valueItr.next();
        assertFalse(valueItr.hasNext());
    }

    @Test
    public void testEntrySet() {
        DoubleValueHashMap<String> m = new DoubleValueHashMap<>();
        Set<DoubleValueMap.Entry<String>> enSet = m.entrySet();
        assertTrue(enSet.isEmpty());

        m.put("abc", 1.1);
        m.put("321", 2.2);

        enSet = m.entrySet();
        assertEquals(2, enSet.size());
        Iterator<DoubleValueMap.Entry<String>> enItr = enSet.iterator();
        assertTrue(enItr.hasNext());
        enItr.next();
        assertTrue(enItr.hasNext());
        enItr.next();
        assertFalse(enItr.hasNext());
    }

    @Test
    public void testEqualsObject() {
        DoubleValueHashMap<String> x = new DoubleValueHashMap<>();

        double v1 = 10009.16;
        double v2 = 20009.16;
        double v3 = 30009.16;
        x.put("szh", v1);
        x.put("abc", v2);
        x.put("321", v3);

        assertTrue(x.equals(x)); // 自反性

        DoubleValueHashMap<String> y = new DoubleValueHashMap<>();
        y.put("szh", v1);
        y.put("abc", v2);

        assertFalse(x.equals(y));
        y.put("321", v3);
        assertTrue(x.equals(y));
        assertTrue(x.equals(y)); // 一致性

        assertTrue(y.equals(x)); // 对称性

        DoubleValueHashMap<String> z = new DoubleValueHashMap<>();

        z.put("szh", v1);
        z.put("abc", v2);
        z.put("321", v3);

        assertTrue(y.equals(z));
        assertTrue(x.equals(z)); // 传递性

        assertFalse(x.equals(null)); // null
    }

}
