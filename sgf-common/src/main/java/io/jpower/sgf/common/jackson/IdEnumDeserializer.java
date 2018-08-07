package io.jpower.sgf.common.jackson;

import java.io.IOException;

import com.alibaba.fastjson.JSONException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer;
import io.jpower.sgf.enumtype.EnumUtils;
import io.jpower.sgf.enumtype.IntEnum;
import io.jpower.sgf.enumtype.Tag;

/**
 * 用来使jackson2支持{@link Tag}和{@link IntEnum}的反序列化
 *
 * @author <a href="mailto:szhnet@gmail.com">szh</a>
 */
public class IdEnumDeserializer<E extends Enum<E>> extends StdScalarDeserializer<E> {

    /**  */
    private static final long serialVersionUID = -6191443987036212089L;

    public IdEnumDeserializer(Class<E> enumClass) {
        super(enumClass);

        if (!enumClass.isEnum()) {
            throw new JSONException("The class is not Enum: " + enumClass);
        }
    }

    @Override
    public E deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException, JsonProcessingException {
        JsonToken curr = jp.getCurrentToken();
        if (curr == JsonToken.VALUE_NUMBER_INT) {
            int idx = jp.getIntValue();
            return _deserializeInt(idx, ctxt);
        } else if (curr == JsonToken.VALUE_STRING || curr == JsonToken.FIELD_NAME) {
            String idxStr = jp.getText();
            int idx = Integer.parseInt(idxStr);
            return _deserializeInt(idx, ctxt);
        }
        return _deserializeOther(jp, ctxt);
    }

    @SuppressWarnings("unchecked")
    private E _deserializeInt(int id, DeserializationContext ctxt) throws JsonProcessingException {
        E rst = (E) EnumUtils.valueOf((Class<? extends Enum<?>>) handledType(), id);
        if (rst == null
                && !ctxt.isEnabled(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL)) {
            throw ctxt.weirdNumberException(id, handledType(),
                    "value not one of declared Enum instance numberss: " + id);
        }
        return rst;
    }

    protected E _deserializeOther(JsonParser jp, DeserializationContext ctxt) throws IOException {
        JsonToken curr = jp.getCurrentToken();
        // Issue#381
        if (curr == JsonToken.START_ARRAY
                && ctxt.isEnabled(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS)) {
            jp.nextToken();
            final E parsed = deserialize(jp, ctxt);
            curr = jp.nextToken();
            if (curr != JsonToken.END_ARRAY) {
                throw ctxt.wrongTokenException(jp, JsonToken.END_ARRAY,
                        "Attempted to unwrap single value array for single '"
                                + handledType().getName()
                                + "' value but there was more than a single value in the array");
            }
            return parsed;
        }
        throw ctxt.mappingException(handledType());
    }

}
