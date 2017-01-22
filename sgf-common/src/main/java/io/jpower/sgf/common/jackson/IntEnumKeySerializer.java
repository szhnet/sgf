package io.jpower.sgf.common.jackson;

import java.io.IOException;
import java.lang.reflect.Type;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer;
import io.jpower.sgf.enumtype.IntEnum;

/**
 * 用来使jackson2支持{@link IntEnum}作为Map的key反序列化
 *
 * @author zheng.sun
 */
public class IntEnumKeySerializer extends StdScalarSerializer<IntEnum> {

    /**  */
    private static final long serialVersionUID = 4436233251233107250L;

    public IntEnumKeySerializer(Class<?> intEnumClass) {
        super(intEnumClass, false);
    }

    @Override
    public void serialize(IntEnum intEn, JsonGenerator jgen, SerializerProvider provider)
            throws IOException, JsonGenerationException {
        jgen.writeFieldName(Integer.toString(intEn.getId()));
    }

    @Override
    public JsonNode getSchema(SerializerProvider provider, Type typeHint)
            throws JsonMappingException {
        return createSchemaNode("string", true);
    }

}
