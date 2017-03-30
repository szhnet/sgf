package io.jpower.sgf.common.fastjson;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.alibaba.fastjson.parser.JSONLexer;
import com.alibaba.fastjson.parser.JSONToken;
import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;
import io.jpower.sgf.enumtype.EnumUtils;
import io.jpower.sgf.enumtype.IntEnum;

/**
 * 用来使fastjson支持{@link IntEnum}的反序列化
 *
 * @author <a href="mailto:szhnet@gmail.com">szh</a>
 */
public class IntEnumDeserializer implements ObjectDeserializer {

    private final Class<?> intEnumClass;

    private final Method findByIdMethod;

    private final IntEnum[] intEnums;

    public IntEnumDeserializer(Class<?> intEnumClass) {
        if (!intEnumClass.isEnum()) {
            throw new JSONException("The class is not Enum: " + intEnumClass);
        }
        this.intEnumClass = intEnumClass;

        // 优先使用findById方法
        this.findByIdMethod = findFindByIdMethod(intEnumClass);

        if (this.findByIdMethod == null) {
            // 没有findById，就用数组
            IntEnum[] enumConstants = (IntEnum[]) intEnumClass.getEnumConstants();
            this.intEnums = EnumUtils.toArray(enumConstants);
        } else {
            this.intEnums = null;
        }
    }

    private Method findFindByIdMethod(Class<?> c) {
        try {
            return c.getMethod("findById", int.class);
        } catch (Exception ignored) {
            // ignore
        }
        return null;
    }

    @Override
    public <T> T deserialze(DefaultJSONParser parser, Type type, Object fieldName) {
        try {
            Object value;
            final JSONLexer lexer = parser.getLexer();
            if (lexer.token() == JSONToken.LITERAL_INT) {
                int id = lexer.intValue();
                lexer.nextToken(JSONToken.COMMA);

                return _deserialize(id);
            } else if (lexer.token() == JSONToken.LITERAL_STRING) {
                String strVal = lexer.stringVal();
                lexer.nextToken(JSONToken.COMMA);

                if (strVal.length() == 0) {
                    return (T) null;
                }
                int id = Integer.parseInt(strVal);

                return _deserialize(id);
            } else if (lexer.token() == JSONToken.NULL) {
                lexer.nextToken(JSONToken.COMMA);

                return null;
            } else {
                value = parser.parse();
            }

            throw new JSONException("parse IntEnum " + findByIdMethod.getDeclaringClass().getName()
                    + " error, value : " + value);
        } catch (JSONException e) {
            throw e;
        } catch (Throwable e) {
            throw new JSONException(e.getMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T _deserialize(int id)
            throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        T e = null;
        if (this.findByIdMethod != null) {
            e = (T) findByIdMethod.invoke(null, id);
        } else {
            if (id < 0 || id >= intEnums.length) {
                return null;
            }
            e = (T) intEnums[id];
        }
        if (e == null) {
            throw new JSONException("parse IntEnum " + intEnumClass
                    + " error, index : " + id);
        }
        return e;
    }

    @Override
    public int getFastMatchToken() {
        return JSONToken.LITERAL_INT;
    }

}
