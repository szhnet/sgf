/**
 *
 */
package io.jpower.sgf.ser.test;

import io.jpower.sgf.ser.Ser;
import io.jpower.sgf.ser.test.model.ObjectData;
import org.junit.Assert;
import org.junit.Test;

import io.jpower.sgf.ser.test.model.BasicData;
import io.jpower.sgf.ser.test.model.TestCurrency;


/**
 * @author zeusgooogle@gmail.com
 * @sine 2016年3月3日 上午10:25:44
 */
public class BasicTest extends AbstractTest {
    /**
     * 正值 测试
     */
    @Test
    public void positiveNumber() {
        Ser ser = Ser.ins();
        // serialize
        byte[] bytes = ser.serialize(positive);

        // deserialize
        BasicData data = ser.deserialize(bytes, BasicData.class);

        Assert.assertTrue(data.getBytee() == Byte.MAX_VALUE);
        Assert.assertTrue(data.getCharr() == Character.MAX_VALUE);
        Assert.assertTrue(data.getShortt() == Short.MAX_VALUE);
        Assert.assertTrue(data.getFloatt() == Float.MAX_VALUE);
        Assert.assertTrue(data.getIntt() == Integer.MAX_VALUE);
        Assert.assertTrue(data.getLongg() == Long.MAX_VALUE);
        Assert.assertTrue(data.getDoublee() == Double.MAX_VALUE);
        Assert.assertTrue(data.getString().equals(STRING));
        Assert.assertTrue(data.isBool());
    }

    /**
     * 负值 测试
     */
    @Test
    public void negativeNumber() {
        Ser ser = Ser.ins();
        // serialize
        byte[] bytes = ser.serialize(negative);

        // deserialize
        BasicData data = ser.deserialize(bytes, BasicData.class);

        Assert.assertTrue(data.getBytee() == Byte.MIN_VALUE);
        Assert.assertTrue(data.getCharr() == Character.MIN_VALUE);
        Assert.assertTrue(data.getShortt() == Short.MIN_VALUE);
        Assert.assertTrue(data.getFloatt() == Float.MIN_VALUE);
        Assert.assertTrue(data.getIntt() == Integer.MIN_VALUE);
        Assert.assertTrue(data.getLongg() == Long.MIN_VALUE);
        Assert.assertTrue(data.getDoublee() == Double.MIN_VALUE);
        Assert.assertTrue(data.getString().equals(STRING));
        Assert.assertTrue(data.isBool());
    }

    @Test
    public void collection() {
        ObjectData data = new ObjectData();
        data.setList(list);
        data.setMap(map);

        Ser ser = Ser.ins();
        // serialize
        byte[] bytes = ser.serialize(data);

        // deserialize
        ObjectData d = ser.deserialize(bytes, ObjectData.class);

        Assert.assertTrue(d.getList().size() == list.size());
        Assert.assertTrue(d.getMap().size() == map.size());
    }

    @Test
    public void intenum() {
        ObjectData data = new ObjectData();
        data.setCurrency(TestCurrency.GOLD);

        Ser ser = Ser.ins();
        // serialize
        byte[] bytes = ser.serialize(data);

        // deserialize
        ObjectData d = ser.deserialize(bytes, ObjectData.class);

        Assert.assertTrue(d.getCurrency() == TestCurrency.GOLD);
    }
}
