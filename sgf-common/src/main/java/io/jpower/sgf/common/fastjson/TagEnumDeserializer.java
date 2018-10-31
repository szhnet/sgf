package io.jpower.sgf.common.fastjson;

import java.lang.reflect.Type;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.alibaba.fastjson.parser.JSONLexer;
import com.alibaba.fastjson.parser.JSONToken;
import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;
import io.jpower.sgf.enumtype.EnumUtils;
import io.jpower.sgf.enumtype.TagEnum;
import io.jpower.sgf.enumtype.Tag;

/**
 * 用来使fastjson支持{@link Tag}和{@link TagEnum}的反序列化
 *
 * @author <a href="mailto:szhnet@gmail.com">szh</a>
 */
public class TagEnumDeserializer implements ObjectDeserializer {

    private final Class<? extends Enum<?>> enumClass;

    public TagEnumDeserializer(Class<? extends Enum<?>> enumClass) {
        if (!enumClass.isEnum()) {
            throw new JSONException("The class is not Enum: " + enumClass);
        }
        this.enumClass = enumClass;
    }

    @Override
    public <T> T deserialze(DefaultJSONParser parser, Type type, Object fieldName) {
        try {
            Object value;
            final JSONLexer lexer = parser.getLexer();
            if (lexer.token() == JSONToken.LITERAL_INT) {
                int tag = lexer.intValue();
                lexer.nextToken(JSONToken.COMMA);

                return _deserialize(tag);
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

            throw new JSONException("parse Enum " + enumClass.getName()
                    + " error, value : " + value);
        } catch (JSONException e) {
            throw e;
        } catch (Throwable e) {
            throw new JSONException(e.getMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T _deserialize(int tag) throws IllegalArgumentException {
        T e = (T) EnumUtils.valueOf(enumClass, tag);
        if (e == null) {
            throw new JSONException("parse Enum " + enumClass
                    + " error, tag : " + tag);
        }
        return e;
    }

    @Override
    public int getFastMatchToken() {
        return JSONToken.LITERAL_INT;
    }

}
