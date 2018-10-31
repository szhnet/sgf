package io.jpower.sgf.common.jackson;

import java.io.IOException;
import java.lang.reflect.Type;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer;
import io.jpower.sgf.enumtype.EnumUtils;
import io.jpower.sgf.enumtype.TagEnum;
import io.jpower.sgf.enumtype.Tag;

/**
 * 用来使jackson2支持{@link Tag}和{@link TagEnum}作为Map的key反序列化
 *
 * @author <a href="mailto:szhnet@gmail.com">szh</a>
 */
public class TagEnumKeySerializer extends StdScalarSerializer<Enum<?>> {

    /**  */
    private static final long serialVersionUID = 4436233251233107250L;

    public TagEnumKeySerializer(Class<?> enumClass) {
        super(enumClass, false);
    }

    @Override
    public void serialize(Enum<?> idEnum, JsonGenerator jgen, SerializerProvider provider)
            throws IOException, JsonGenerationException {
        int tag = EnumUtils.tagOf(idEnum);
        jgen.writeFieldName(Integer.toString(tag));
    }

    @Override
    public JsonNode getSchema(SerializerProvider provider, Type typeHint)
            throws JsonMappingException {
        return createSchemaNode("string", true);
    }

}
