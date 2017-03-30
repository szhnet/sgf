package io.jpower.sgf.collection;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

/**
 * @author <a href="mailto:szhnet@gmail.com">szh</a>
 */
public class FloatValueHashMapTest {

    private static final float DELTA = 0.001f;

    @Test
    public void testFloatValueHashMapMapOfQextendsKFloat() {
        Map<String, Float> jm = new HashMap<>();
        jm.put("abc", 1.1f);
        jm.put("321", 2.2f);
        jm.put("szh", 3.3f);

        FloatValueHashMap<String> m = new FloatValueHashMap<>(jm);
        assertEquals(jm.size(), m.size());
        assertTrue(m.containsKey("abc"));
        assertTrue(m.containsKey("321"));
        assertTrue(m.containsKey("szh"));
        assertTrue(m.containsValue(1.1f));
        assertTrue(m.containsValue(2.2f));
        assertTrue(m.containsValue(3.3f));
    }

    @Test
    public void testFloatValueHashMapFloatValueMapOfK() {
        FloatValueHashMap<String> m1 = new FloatValueHashMap<>();
        m1.put("abc", 1.1f);
        m1.put("321", 2.2f);
        m1.put("szh", 3.3f);

        FloatValueHashMap<String> m = new FloatValueHashMap<>(m1);
        assertEquals(m1.size(), m.size());
        assertTrue(m.containsKey("abc"));
        assertTrue(m.containsKey("321"));
        assertTrue(m.containsKey("szh"));
        assertTrue(m.containsValue(1.1f));
        assertTrue(m.containsValue(2.2f));
        assertTrue(m.containsValue(3.3f));
    }

    @Test
    public void testGetObject() {
        FloatValueHashMap<String> m = new FloatValueHashMap<>();
        assertEquals(FloatValueMap.DEFAULT_VALUE, m.get("abc"), DELTA);
        float value = 101.21f;
        m.put("abc", value);
        assertEquals(value, m.get("abc"), DELTA);
        assertEquals(FloatValueMap.DEFAULT_VALUE, m.get("321"), DELTA);
    }

    @Test
    public void testGetObjectFloat() {
        FloatValueHashMap<String> m = new FloatValueHashMap<>();
        float defaultValue = 1001.0f;
        float value = 101.9f;
        assertEquals(defaultValue, m.get("abc", defaultValue), DELTA);
        m.put("abc", value);
        assertEquals(value, m.get("abc"), DELTA);
        assertEquals(-1.0f, m.get("321", -1.0f), DELTA);
    }

    @Test
    public void testContainsKey() {
        FloatValueHashMap<String> m = new FloatValueHashMap<>();
        assertFalse(m.containsKey("abc"));
        m.put("abc", 1.1f);
        assertTrue(m.containsKey("abc"));
        assertFalse(m.containsKey("321"));
    }

    @Test
    public void testPutKFloat() {
        FloatValueHashMap<String> m = new FloatValueHashMap<>();
        float value = 123.78f;
        float exists = m.put("abc", value);
        assertEquals(FloatValueMap.DEFAULT_VALUE, exists, DELTA);
        assertEquals(1, m.size());
        assertEquals(value, m.get("abc"), DELTA);

        // 测试一下key相同的情况
        float newValue = 1.1f;
        exists = m.put("abc", newValue);
        assertEquals(value, exists, DELTA);
        assertEquals(1, m.size());
        assertEquals(newValue, m.get("abc"), DELTA);
    }

    @Test
    public void testPutKFloatFloat() {
        FloatValueHashMap<String> m = new FloatValueHashMap<>();
        float value = 1.1f;
        float defaultValue = 2.2f;
        float exists = m.put("abc", value, defaultValue);
        assertEquals(defaultValue, exists, DELTA);
        assertEquals(1, m.size());
        assertEquals(value, m.get("abc"), DELTA);

        // 测试一下key相同的情况
        float newValue = 3.3f;
        exists = m.put("abc", newValue, defaultValue);
        assertEquals(value, exists, DELTA);
        assertEquals(1, m.size());
        assertEquals(newValue, m.get("abc"), DELTA);
    }

    @Test
    public void testPutAllMapOfQextendsKFloat() {
        Map<String, Float> jm = new HashMap<>();
        jm.put("abc", 1.1f);
        jm.put("321", 2.2f);
        jm.put("szh", 3.3f);

        FloatValueHashMap<String> m = new FloatValueHashMap<>();
        m.putAll(jm);
        assertEquals(jm.size(), m.size());
        assertTrue(m.containsKey("abc"));
        assertTrue(m.containsKey("321"));
        assertTrue(m.containsKey("szh"));
        assertTrue(m.containsValue(1.1f));
        assertTrue(m.containsValue(2.2f));
        assertTrue(m.containsValue(3.3f));
    }

    @Test
    public void testPutAllFloatValueMapOfK() {
        FloatValueHashMap<String> jm = new FloatValueHashMap<>();
        jm.put("abc", 1.1f);
        jm.put("321", 2.2f);
        jm.put("szh", 3.3f);

        FloatValueHashMap<String> m = new FloatValueHashMap<>();
        m.putAll(jm);
        assertEquals(jm.size(), m.size());
        assertTrue(m.containsKey("abc"));
        assertTrue(m.containsKey("321"));
        assertTrue(m.containsKey("szh"));
        assertTrue(m.containsValue(1.1f));
        assertTrue(m.containsValue(2.2f));
        assertTrue(m.containsValue(3.3f));
    }

    @Test
    public void testRemoveObject() {
        FloatValueHashMap<String> jm = new FloatValueHashMap<>();
        float exists = jm.remove("abc");
        assertEquals(FloatValueMap.DEFAULT_VALUE, exists, DELTA);

        float value = 100.1f;
        jm.put("abc", value);
        exists = jm.remove("abc");
        assertEquals(value, exists, DELTA);
        exists = jm.remove("abc"); // 再删一次，应该没了
        assertEquals(FloatValueMap.DEFAULT_VALUE, exists, DELTA);
    }

    @Test
    public void testRemoveObjectFloat() {
        FloatValueHashMap<String> jm = new FloatValueHashMap<>();
        float defaultVal = 109.9f;
        float exists = jm.remove("abc", defaultVal);
        assertEquals(defaultVal, exists, DELTA);

        float value = 999.9f;
        jm.put("abc", value);
        exists = jm.remove("abc", defaultVal);
        assertEquals(value, exists, DELTA);
        exists = jm.remove("abc", defaultVal); // 再删一次，应该没了
        assertEquals(defaultVal, exists, DELTA);
    }

    @Test
    public void testContainsValue() {
        FloatValueHashMap<String> m = new FloatValueHashMap<>();
        float value = 9999.9f;
        assertFalse(m.containsValue(value));
        m.put("abc", value);
        assertTrue(m.containsValue(value));
        assertFalse(m.containsValue(100.0f));
    }

    @Test
    public void testClone() {
        FloatValueHashMap<String> m = new FloatValueHashMap<>();
        @SuppressWarnings("unchecked")
        FloatValueHashMap<String> cm = (FloatValueHashMap<String>) m.clone();
        assertFalse(m == cm);

        assertTrue(m.equals(cm));
    }

    @Test
    public void testKeySet() {
        FloatValueHashMap<String> m = new FloatValueHashMap<>();
        Set<String> keySet = m.keySet();
        assertTrue(keySet.isEmpty());

        m.put("abc", 1.1f);
        m.put("321", 2.2f);

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
        FloatValueHashMap<String> m = new FloatValueHashMap<>();
        FloatCollection values = m.values();
        assertTrue(values.isEmpty());

        m.put("abc", 1.1f);
        m.put("321", 2.2f);

        values = m.values();
        assertEquals(2, values.size());
        assertTrue(values.contains(1.1f));
        assertTrue(values.contains(2.2f));
        FloatIterator valueItr = values.iterator();
        assertTrue(valueItr.hasNext());
        valueItr.next();
        assertTrue(valueItr.hasNext());
        valueItr.next();
        assertFalse(valueItr.hasNext());
    }

    @Test
    public void testEntrySet() {
        FloatValueHashMap<String> m = new FloatValueHashMap<>();
        Set<FloatValueMap.Entry<String>> enSet = m.entrySet();
        assertTrue(enSet.isEmpty());

        m.put("abc", 1.1f);
        m.put("321", 2.2f);

        enSet = m.entrySet();
        assertEquals(2, enSet.size());
        Iterator<FloatValueMap.Entry<String>> enItr = enSet.iterator();
        assertTrue(enItr.hasNext());
        enItr.next();
        assertTrue(enItr.hasNext());
        enItr.next();
        assertFalse(enItr.hasNext());
    }

    @Test
    public void testEqualsObject() {
        FloatValueHashMap<String> x = new FloatValueHashMap<>();

        x.put("szh", 1.1f);
        x.put("abc", 2.2f);
        x.put("321", 3.3f);

        assertTrue(x.equals(x)); // 自反性

        FloatValueHashMap<String> y = new FloatValueHashMap<>();
        y.put("szh", 1.1f);
        y.put("abc", 2.2f);

        assertFalse(x.equals(y));
        y.put("321", 3.3f);
        assertTrue(x.equals(y));
        assertTrue(x.equals(y)); // 一致性

        assertTrue(y.equals(x)); // 对称性

        FloatValueHashMap<String> z = new FloatValueHashMap<>();

        z.put("szh", 1.1f);
        z.put("abc", 2.2f);
        z.put("321", 3.3f);

        assertTrue(y.equals(z));
        assertTrue(x.equals(z)); // 传递性

        assertFalse(x.equals(null)); // null
    }

}
