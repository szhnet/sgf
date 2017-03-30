/**
 *
 */
package io.jpower.sgf.ser.test.model;

import io.jpower.sgf.ser.annotation.Field;
import io.jpower.sgf.ser.annotation.Serializable;

/**
 * @author zeusgooogle@gmail.com
 * @sine 2016年3月3日 上午11:14:34
 */
@Serializable
public class BasicData {

    @Field(value = 1)
    private byte bytee;

    @Field(value = 2)
    private char charr;

    @Field(value = 3)
    private short shortt;

    @Field(value = 4)
    private float floatt;

    @Field(value = 5)
    private int intt;

    @Field(value = 6)
    private double doublee;

    @Field(value = 7)
    private long longg;

    @Field(value = 8)
    private String string;

    @Field(value = 9)
    private boolean bool;

    public BasicData() {

    }

    public byte getBytee() {
        return bytee;
    }

    public void setBytee(byte bytee) {
        this.bytee = bytee;
    }

    public char getCharr() {
        return charr;
    }

    public void setCharr(char charr) {
        this.charr = charr;
    }

    public short getShortt() {
        return shortt;
    }

    public void setShortt(short shortt) {
        this.shortt = shortt;
    }

    public float getFloatt() {
        return floatt;
    }

    public void setFloatt(float floatt) {
        this.floatt = floatt;
    }

    public int getIntt() {
        return intt;
    }

    public void setIntt(int intt) {
        this.intt = intt;
    }

    public double getDoublee() {
        return doublee;
    }

    public void setDoublee(double doublee) {
        this.doublee = doublee;
    }

    public long getLongg() {
        return longg;
    }

    public void setLongg(long longg) {
        this.longg = longg;
    }

    public String getString() {
        return string;
    }

    public void setString(String string) {
        this.string = string;
    }

    public boolean isBool() {
        return bool;
    }

    public void setBool(boolean bool) {
        this.bool = bool;
    }

}
