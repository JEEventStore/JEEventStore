/*
 * Copyright (c) 2013 Red Rainbow IT Solutions GmbH, Germany
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package org.jeeventstore.serialization.gson;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A custom Gson type converter for the {@link EventList}.
 * @author Alexander Langer
 */
public class EventListTypeConverter
        implements JsonSerializer<EventList>, JsonDeserializer<EventList> {

    @Override
    public JsonElement serialize(
	    EventList src,
	    Type srcType, JsonSerializationContext context) {
	JsonObject combined = new JsonObject();
        combined.add("version", new JsonPrimitive(1));
        JsonArray events = new JsonArray();
        for (Serializable s : src.events()) {
            JsonObject obj = new JsonObject();
	    obj.add("type", new JsonPrimitive(s.getClass().getCanonicalName()));
	    obj.add("body", context.serialize(s));
            events.add(obj);
        }
        combined.add("events", events);
	return combined;
    }

    @Override
    public EventList deserialize(JsonElement json, Type type, JsonDeserializationContext context)
	    throws JsonParseException {

	JsonObject obj = json.getAsJsonObject();
        Integer version = obj.getAsJsonPrimitive("version").getAsInt();
        if (version != 1)
            throw new JsonParseException("Unable to parse event of version " + version);

        Iterator<JsonElement> eit = obj.getAsJsonArray("events").iterator();
        List<Serializable> eventlist = new ArrayList<>();
        while (eit.hasNext()) {
            String clazz = null;
            try {
                JsonObject elem = eit.next().getAsJsonObject();
	        clazz = elem.getAsJsonPrimitive("type").getAsString();
	        Class<? extends Serializable> eventClass = (Class<? extends Serializable>) Class.forName(clazz);
	        Serializable s = context.deserialize(elem.get("body"), eventClass);
                eventlist.add(s);
            } catch (ClassNotFoundException e) {
                throw new JsonParseException("Cannot deserialize events of class " + clazz, e);
            }
        }
        return new EventList(eventlist);
    }

}