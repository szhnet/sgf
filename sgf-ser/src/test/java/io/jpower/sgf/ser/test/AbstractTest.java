/**
 *
 */
package io.jpower.sgf.ser.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;

import io.jpower.sgf.ser.test.model.BasicData;

/**
 * @author zeusgooogle@gmail.com
 * @sine 2016年3月3日 上午11:54:54
 */
public abstract class AbstractTest {

    public static final String STRING = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890~!@#$%^&*()_+";

    protected BasicData positive;

    protected BasicData negative;

    protected List<BasicData> list;

    protected Map<String, BasicData> map;

    @Before
    public void init() {
        positive = new BasicData();
        positive.setBytee(Byte.MAX_VALUE);
        positive.setCharr(Character.MAX_VALUE);
        positive.setShortt(Short.MAX_VALUE);
        positive.setFloatt(Float.MAX_VALUE);
        positive.setIntt(Integer.MAX_VALUE);
        positive.setLongg(Long.MAX_VALUE);
        positive.setDoublee(Double.MAX_VALUE);
        positive.setString(STRING);
        positive.setBool(true);

        negative = new BasicData();
        negative.setBytee(Byte.MIN_VALUE);
        negative.setCharr(Character.MIN_VALUE);
        negative.setShortt(Short.MIN_VALUE);
        negative.setFloatt(Float.MIN_VALUE);
        negative.setIntt(Integer.MIN_VALUE);
        negative.setLongg(Long.MIN_VALUE);
        negative.setDoublee(Double.MIN_VALUE);
        negative.setString(STRING);
        negative.setBool(true);

        list = new ArrayList<>();
        list.add(positive);
        list.add(negative);

        map = new HashMap<>();
        map.put("positive", positive);
        map.put("negative", negative);
    }

}
