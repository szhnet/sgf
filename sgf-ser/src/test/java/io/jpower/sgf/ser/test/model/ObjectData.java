/**
 *
 */
package io.jpower.sgf.ser.test.model;

import java.util.List;
import java.util.Map;

import io.jpower.sgf.ser.annotation.Field;
import io.jpower.sgf.ser.annotation.Serializable;

/**
 * @author zeusgooogle@gmail.com
 * @sine 2016年3月3日 上午11:50:36
 */
@Serializable
public class ObjectData {

    @Field(value = 1)
    private List<BasicData> list;

    @Field(value = 2)
    private Map<String, BasicData> map;

    @Field(value = 3)
    private TestCurrency currency;

    public ObjectData() {

    }

    public List<BasicData> getList() {
        return list;
    }

    public void setList(List<BasicData> list) {
        this.list = list;
    }

    public Map<String, BasicData> getMap() {
        return map;
    }

    public void setMap(Map<String, BasicData> map) {
        this.map = map;
    }

    public TestCurrency getCurrency() {
        return currency;
    }

    public void setCurrency(TestCurrency currency) {
        this.currency = currency;
    }

}
