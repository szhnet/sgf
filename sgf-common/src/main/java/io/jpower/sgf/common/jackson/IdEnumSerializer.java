package io.jpower.sgf.common.jackson;

import java.io.IOException;
import java.lang.reflect.Type;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonIntegerFormatVisitor;
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer;
import io.jpower.sgf.enumtype.EnumUtils;
import io.jpower.sgf.enumtype.IntEnum;
import io.jpower.sgf.enumtype.Tag;

/**
 * 用来使jackson2支持{@link Tag}和{@link IntEnum}的序列化
 *
 * @author <a href="mailto:szhnet@gmail.com">szh</a>
 */
public class IdEnumSerializer extends StdScalarSerializer<Enum<?>> {

    /**  */
    private static final long serialVersionUID = -2571982673299899055L;

    public IdEnumSerializer(Class<?> enumClass) {
        super(enumClass, false);
    }

    @Override
    public void serialize(Enum<?> idEnum, JsonGenerator jgen, SerializerProvider provider)
            throws IOException, JsonGenerationException {
        int id = EnumUtils.idOf(idEnum);
        jgen.writeNumber(id);
    }

    @Override
    public JsonNode getSchema(SerializerProvider provider, Type typeHint)
            throws JsonMappingException {
        return createSchemaNode("integer", true);
    }

    @Override
    public void acceptJsonFormatVisitor(JsonFormatVisitorWrapper visitor, JavaType typeHint)
            throws JsonMappingException {
        JsonIntegerFormatVisitor v2 = visitor.expectIntegerFormat(typeHint);
        if (v2 != null) { // typically serialized as a small number (byte or int)
            v2.numberType(JsonParser.NumberType.INT);
        }
    }

}
