package io.jpower.sgf.common.jackson;

import java.io.IOException;
import java.lang.reflect.Method;

import com.alibaba.fastjson.JSONException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer;
import io.jpower.sgf.enumtype.EnumUtils;
import io.jpower.sgf.enumtype.IntEnum;

/**
 * 用来使jackson2支持{@link IntEnum}的反序列化
 *
 * @author zheng.sun
 */
public class IntEnumDeserializer<T extends IntEnum> extends StdScalarDeserializer<T> {

    /**  */
    private static final long serialVersionUID = -6191443987036212089L;

    private final Method findByIdMethod;

    private final T[] intEnums;

    public IntEnumDeserializer(Class<T> intEnumClass) {
        super(intEnumClass);

        if (!intEnumClass.isEnum()) {
            throw new JSONException("The class is not Enum: " + intEnumClass);
        }

        // 优先使用findById方法
        this.findByIdMethod = findFindByIdMethod(intEnumClass);

        if (this.findByIdMethod == null) {
            // 没有findById，就用数组
            T[] enumConstants = (T[]) intEnumClass.getEnumConstants();
            this.intEnums = EnumUtils.toArray(enumConstants);
        } else {
            this.intEnums = null;
        }
    }

    private Method findFindByIdMethod(Class<?> c) {
        try {
            return c.getMethod("findById", int.class);
        } catch (Exception e) {
            // ignore
        }
        return null;
    }

    @Override
    public T deserialize(JsonParser jp, DeserializationContext ctxt)
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
    private T _deserializeInt(int id, DeserializationContext ctxt) throws JsonProcessingException {
        T rst = null;
        if (this.findByIdMethod != null) {
            try {
                rst = (T) findByIdMethod.invoke(null, id);
            } catch (Exception e) {
                throw JsonMappingException.from(ctxt.getParser(),
                        "invoke findById method error, problem: " + e.getMessage(), e);
            }
        } else {
            if (id >= 0 && id < intEnums.length) {
                rst = intEnums[id];
            }
        }
        if (rst == null
                && !ctxt.isEnabled(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL)) {
            throw ctxt.weirdNumberException(id, handledType(),
                    "value not one of declared IntEnum instance numberss: " + id);
        }
        return rst;
    }

    protected T _deserializeOther(JsonParser jp, DeserializationContext ctxt) throws IOException {
        JsonToken curr = jp.getCurrentToken();
        // Issue#381
        if (curr == JsonToken.START_ARRAY
                && ctxt.isEnabled(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS)) {
            jp.nextToken();
            final T parsed = deserialize(jp, ctxt);
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
