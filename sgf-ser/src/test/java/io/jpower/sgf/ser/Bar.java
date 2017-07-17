package io.jpower.sgf.ser;

import io.jpower.sgf.ser.annotation.Field;
import io.jpower.sgf.ser.annotation.Serializable;

import java.util.List;

/**
 * @author <a href="mailto:szhnet@gmail.com">szh</a>
 */
@Serializable
public class Bar {

    @Field(1)
    private double dnum;

    @Field(2)
    private String name;

    @Field(3)
    private Ide ide;

    @Field(4)
    private List<String> strs;

    public Bar() {

    }

    public double getDnum() {
        return dnum;
    }

    public void setDnum(double dnum) {
        this.dnum = dnum;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Ide getIde() {
        return ide;
    }

    public void setIde(Ide ide) {
        this.ide = ide;
    }

    public List<String> getStrs() {
        return strs;
    }

    public void setStrs(List<String> strs) {
        this.strs = strs;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Bar bar = (Bar) o;

        if (Double.compare(bar.dnum, dnum) != 0) return false;
        if (name != null ? !name.equals(bar.name) : bar.name != null) return false;
        if (ide != bar.ide) return false;
        return strs != null ? strs.equals(bar.strs) : bar.strs == null;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(dnum);
        result = (int) (temp ^ (temp >>> 32));
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (ide != null ? ide.hashCode() : 0);
        result = 31 * result + (strs != null ? strs.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Bar(" +
                "dnum=" + dnum +
                ", name='" + name + '\'' +
                ", ide=" + ide +
                ", strs=" + strs +
                ')';
    }
}
