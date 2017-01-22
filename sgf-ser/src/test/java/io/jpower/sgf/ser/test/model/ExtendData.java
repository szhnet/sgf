package io.jpower.sgf.ser.test.model;

import io.jpower.sgf.ser.annotation.Field;
import io.jpower.sgf.ser.annotation.Serializable;

/**
 * @author zeusgooogle@gmail.com
 * @sine 2016年3月10日 下午5:50:57
 */
@Serializable
public class ExtendData extends BasicData {

    @Field(20)
    private String extend;

    public String getExtend() {
        return extend;
    }

    public void setExtend(String extend) {
        this.extend = extend;
    }

}
