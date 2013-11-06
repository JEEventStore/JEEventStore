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
import org.jeeventstore.core.serialization.EventSerializer;

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
        EventList evlist = new EventList(events);
        return gson.toJson(evlist);
    }

    @Override
    @Lock(LockType.READ)
    public List<? extends Serializable> deserialize(String body) {
        return this.gson.fromJson(body, EventList.class).events();
    }

}
