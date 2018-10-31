package io.jpower.sgf.common.fastjson;

import java.io.IOException;
import java.lang.reflect.Type;

import com.alibaba.fastjson.serializer.JSONSerializer;
import com.alibaba.fastjson.serializer.ObjectSerializer;
import com.alibaba.fastjson.serializer.SerializeWriter;
import io.jpower.sgf.enumtype.EnumUtils;
import io.jpower.sgf.enumtype.TagEnum;
import io.jpower.sgf.enumtype.Tag;

/**
 * 用来使fastjson支持{@link Tag}和{@link TagEnum}的序列化
 *
 * @author <a href="mailto:szhnet@gmail.com">szh</a>
 */
public class TagEnumSerializer implements ObjectSerializer {

    public final static TagEnumSerializer instance = new TagEnumSerializer();

    @Override
    public void write(JSONSerializer serializer, Object object, Object fieldName, Type fieldType,
                      int features) throws IOException {
        SerializeWriter out = serializer.getWriter();
        if (object == null) {
            serializer.getWriter().writeNull();
            return;
        }

        Enum<?> e = (Enum<?>) object;
        int tag = EnumUtils.tagOf(e);
        if (fieldName == null) { // 如果是作为map的key，则写入字符串，否则写入int
            out.writeString(Integer.toString(tag));
        } else {
            out.writeInt(tag);
        }
    }

}
