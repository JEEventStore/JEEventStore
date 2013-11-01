package org.jeeventstore.serialization.gson;

import com.google.gson.JsonDeserializer;
import com.google.gson.JsonSerializer;
import java.io.Serializable;

/**
 * Provides the ability to add custom serializers for events.
 * @author Alexander Langer
 */
public interface EventSerializerGsonTypeConverter<T extends Serializable>
    extends JsonSerializer<T>, JsonDeserializer<T> {

    Class<T> convertedType();
    
}
