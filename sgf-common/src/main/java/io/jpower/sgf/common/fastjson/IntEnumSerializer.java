package io.jpower.sgf.common.fastjson;

import java.io.IOException;
import java.lang.reflect.Type;

import com.alibaba.fastjson.serializer.JSONSerializer;
import com.alibaba.fastjson.serializer.ObjectSerializer;
import com.alibaba.fastjson.serializer.SerializeWriter;
import io.jpower.sgf.enumtype.IntEnum;

/**
 * 用来使fastjson支持{@link IntEnum}的序列化
 *
 * @author zheng.sun
 */
public class IntEnumSerializer implements ObjectSerializer {

    public final static IntEnumSerializer instance = new IntEnumSerializer();

    @Override
    public void write(JSONSerializer serializer, Object object, Object fieldName, Type fieldType,
                      int features) throws IOException {
        SerializeWriter out = serializer.getWriter();
        if (object == null) {
            serializer.getWriter().writeNull();
            return;
        }

        if (fieldName == null) { // 如果是作为map的key，则写入字符串，否则写入int
            IntEnum e = (IntEnum) object;
            out.writeString(Integer.toString(e.getId()));
        } else {
            IntEnum e = (IntEnum) object;
            out.writeInt(e.getId());
        }
    }

}
