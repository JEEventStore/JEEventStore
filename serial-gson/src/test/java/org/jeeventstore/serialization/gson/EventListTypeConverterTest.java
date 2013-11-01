package org.jeeventstore.serialization.gson;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import static org.testng.Assert.*;
import org.testng.annotations.Test;

/**
 *
 * @author Alexander Langer
 */
public class EventListTypeConverterTest {
    
    private Gson gson() {
        return new GsonBuilder()
                .registerTypeAdapter(EventList.class, new EventListTypeConverter())
                .create();
    }

    private EventList createInstance() {
        List<Serializable> data = new ArrayList<>();
        data.add(new Integer(12345));
        data.add("Hello, World!");
        return new EventList(data);
    }

    public static String EXPECTED_RESULT = "{\"version\":1,\"events\":[{\"type\":\"java.lang.Integer\",\"body\":12345},{\"type\":\"java.lang.String\",\"body\":\"Hello, World!\"}]}";

    @Test
    public void testSerialize() {
        EventList el = createInstance();
        String res = gson().toJson(el);
        assertEquals(res, EXPECTED_RESULT);
    }

    @Test
    public void testDeserialize() {
        EventList el = gson().fromJson(EXPECTED_RESULT, EventList.class);
        List<? extends Serializable> events = el.events();
        assertEquals(events.get(0), 12345);
        assertEquals(events.get(1), "Hello, World!");
    }
    
}
