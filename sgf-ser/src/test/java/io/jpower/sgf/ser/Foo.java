package io.jpower.sgf.ser;

import java.util.List;
import java.util.Map;

import io.jpower.sgf.ser.annotation.Field;
import io.jpower.sgf.ser.annotation.Serializable;

/**
 * @author <a href="mailto:szhnet@gmail.com">szh</a>
 */
@Serializable
public class Foo {

    @Field(1)
    private int inum;

    @Field(2)
    private short snum;

    @Field(3)
    private long lnum;

    @Field(4)
    private Fruit fruit;

    private String desc;

    @Field(6)
    private Map<String, Bar> barMap;

    @Field(7)
    private List<Bar> barList;

    public Foo() {

    }

    public int getInum() {
        return inum;
    }

    public void setInum(int inum) {
        this.inum = inum;
    }

    public short getSnum() {
        return snum;
    }

    public void setSnum(short snum) {
        this.snum = snum;
    }

    public long getLnum() {
        return lnum;
    }

    public void setLnum(long lnum) {
        this.lnum = lnum;
    }

    public Fruit getFruit() {
        return fruit;
    }

    public void setFruit(Fruit fruit) {
        this.fruit = fruit;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public Map<String, Bar> getBarMap() {
        return barMap;
    }

    public void setBarMap(Map<String, Bar> barMap) {
        this.barMap = barMap;
    }

    public List<Bar> getBarList() {
        return barList;
    }

    public void setBarList(List<Bar> barList) {
        this.barList = barList;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Foo foo = (Foo) o;

        if (inum != foo.inum) return false;
        if (snum != foo.snum) return false;
        if (lnum != foo.lnum) return false;
        if (fruit != foo.fruit) return false;
        if (desc != null ? !desc.equals(foo.desc) : foo.desc != null) return false;
        if (barMap != null ? !barMap.equals(foo.barMap) : foo.barMap != null) return false;
        return barList != null ? barList.equals(foo.barList) : foo.barList == null;
    }

    @Override
    public int hashCode() {
        int result = inum;
        result = 31 * result + (int) snum;
        result = 31 * result + (int) (lnum ^ (lnum >>> 32));
        result = 31 * result + (fruit != null ? fruit.hashCode() : 0);
        result = 31 * result + (desc != null ? desc.hashCode() : 0);
        result = 31 * result + (barMap != null ? barMap.hashCode() : 0);
        result = 31 * result + (barList != null ? barList.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Foo(" +
                "inum=" + inum +
                ", snum=" + snum +
                ", lnum=" + lnum +
                ", fruit=" + fruit +
                ", desc='" + desc + '\'' +
                ", barMap=" + barMap +
                ", barList=" + barList +
                ')';
    }

}
