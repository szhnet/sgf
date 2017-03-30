package io.jpower.sgf.ser.test;

import io.jpower.sgf.ser.Ser;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import io.jpower.sgf.ser.test.model.ExtendData;


/**
 * @author zeusgooogle@gmail.com
 * @sine 2016年3月10日 下午5:53:54
 */
public class ExtendTest {

    private String EXTEND_STRING = "extend";

    private ExtendData data;

    @Before
    public void init() {
        data = new ExtendData();
        data.setBytee(Byte.MAX_VALUE);
        data.setCharr(Character.MAX_VALUE);
        data.setShortt(Short.MAX_VALUE);
        data.setFloatt(Float.MAX_VALUE);
        data.setIntt(Integer.MAX_VALUE);
        data.setLongg(Long.MAX_VALUE);
        data.setDoublee(Double.MAX_VALUE);
        data.setBool(true);

        data.setExtend(EXTEND_STRING);
    }

    @Test
    public void extend() {
        Ser ser = Ser.ins();
        // serialize
        byte[] bytes = ser.serialize(data);

        // deserialize
        ExtendData data = ser.deserialize(bytes, ExtendData.class);

        Assert.assertTrue(data.getBytee() == Byte.MAX_VALUE);
        Assert.assertTrue(data.getCharr() == Character.MAX_VALUE);

        Assert.assertTrue(data.getExtend().equals(EXTEND_STRING));
    }
}
