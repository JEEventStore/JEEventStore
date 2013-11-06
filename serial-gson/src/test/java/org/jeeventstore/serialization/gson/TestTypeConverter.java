package org.jeeventstore.serialization.gson;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import java.lang.reflect.Type;

/**
 *
 * @author Alexander Langer
 */
public class TestTypeConverter implements EventSerializerGsonTypeConverter<TestObject> {

    @Override
    public Class<TestObject> convertedType() {
        return TestObject.class;
    }

    @Override
    public JsonElement serialize(TestObject t, Type type, JsonSerializationContext jsc) {
        return new JsonPrimitive("HELLO");
    }

    @Override
    public TestObject deserialize(JsonElement je, Type type, JsonDeserializationContext jdc) throws JsonParseException {
        return jdc.deserialize(je, type);
    }

}
