package io.jpower.sgf.common.fastjson;

import com.alibaba.fastjson.serializer.JSONSerializer;
import com.alibaba.fastjson.serializer.ObjectSerializer;
import com.alibaba.fastjson.serializer.SerializeWriter;
import io.jpower.sgf.enumtype.EnumUtils;
import io.jpower.sgf.enumtype.IntEnum;
import io.jpower.sgf.enumtype.Tag;

import java.io.IOException;
import java.lang.reflect.Type;

/**
 * 用来使fastjson支持{@link Tag}和{@link IntEnum}的序列化
 *
 * @author <a href="mailto:szhnet@gmail.com">szh</a>
 */
public class IdEnumSerializer implements ObjectSerializer {

    public final static IdEnumSerializer instance = new IdEnumSerializer();

    @Override
    public void write(JSONSerializer serializer, Object object, Object fieldName, Type fieldType,
                      int features) throws IOException {
        SerializeWriter out = serializer.getWriter();
        if (object == null) {
            serializer.getWriter().writeNull();
            return;
        }

        Enum<?> e = (Enum<?>) object;
        int id = EnumUtils.idOf(e);
        if (fieldName == null) { // 如果是作为map的key，则写入字符串，否则写入int
            out.writeString(Integer.toString(id));
        } else {
            out.writeInt(id);
        }
    }

}
