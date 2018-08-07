package io.jpower.sgf.ser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

/**
 * @author <a href="mailto:szhnet@gmail.com">szh</a>
 */
public class SerTest {

    @Test
    public void testSerAndDeser() {
        Foo foo = new Foo();
        foo.setInum(123456789);
        foo.setSnum((short) 10);
        foo.setLnum(12345678987654321L);
        foo.setFruit(Fruit.ORANGE);

        Bar bar1 = bar(123456.123456, "bar1", Ide.IDEA, new String[]{"bar1", "hehe"});

        Bar bar2 = bar(234567.234567, "bar2", Ide.ECLIPSE, new String[]{"bar2", "lalala"});

        Map<String, Bar> barMap = new HashMap<>();
        barMap.put("bar1", bar1);
        barMap.put("bar2", bar2);
        foo.setBarMap(barMap);

        Bar bar3 = bar(345678.345678, "bar3", Ide.IDEA, new String[]{"bar3", "hehe"});

        Bar bar4 = bar(456789.456789, "bar4", Ide.ECLIPSE, new String[]{"bar4", "lalala"});

        List<Bar> barList = new ArrayList<>();
        barList.add(bar3);
        barList.add(bar4);
        foo.setBarList(barList);

        Ser ser = Ser.ins();

        byte[] data = ser.serialize(foo);
        Foo deserFoo = ser.deserialize(data, Foo.class);
        assertEquals(foo, deserFoo);

        foo.setDesc("desc");

        data = ser.serialize(foo);
        deserFoo = ser.deserialize(data, Foo.class);
        assertNotEquals(foo, deserFoo);
    }

    private Bar bar(double dnum, String name, Ide ide, String[] strs) {
        Bar bar = new Bar();
        bar.setDnum(dnum);
        bar.setName(name);
        bar.setIde(ide);
        bar.setStrs(Arrays.asList(strs));

        return bar;
    }

}