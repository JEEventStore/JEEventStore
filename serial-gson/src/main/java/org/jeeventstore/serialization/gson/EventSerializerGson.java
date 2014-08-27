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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import org.jeeventstore.EventSerializer;

/**
 * An EventSerializer implementation using Google's Gson library.
 * @author Alexander Langer
 */
public class EventSerializerGson implements EventSerializer {

    private static final Logger log = Logger.getLogger(EventSerializerGson.class.getName());

    private Gson gson;

    /*
     * This searches the class path and lists all custom converters.
     */
    @Inject
    private Instance<EventSerializerGsonTypeConverter> typeConverters;

    @PostConstruct
    public void init() {
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(EventList.class, new EventListTypeConverter());
	builder.serializeSpecialFloatingPointValues(); // required to serialize Double.POSITIVE_INFINITY and others
        builder.setDateFormat("yyyy-MM-dd HH:mm:ss.SSS"); // granularity of 1 ms

        // register client's type adapters
        Iterator<EventSerializerGsonTypeConverter> convit = typeConverters.iterator();
        while (convit.hasNext()) {
            EventSerializerGsonTypeConverter conv = convit.next();
            log.log(Level.INFO, "Registering type converter for class {0}",
                    conv.convertedType().getCanonicalName());
            builder.registerTypeAdapter(conv.convertedType(), conv);
        }
        this.gson = builder.create();
    }

    @Override
    @Lock(LockType.READ)
    public String serialize(List<? extends Serializable> events) {
        if (events == null)
            throw new IllegalArgumentException("events must not be null");
        EventList evlist = new EventList(events);
        return gson.toJson(evlist);
    }

    @Override
    @Lock(LockType.READ)
    public List<? extends Serializable> deserialize(String body) {
        if (body == null)
            throw new IllegalArgumentException("body must not be null");
        log.log(Level.FINER, "deserializing body: " + body);
        return this.gson.fromJson(body, EventList.class).events();
    }

}
