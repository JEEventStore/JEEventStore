/*
 * Copyright (c) 2013-2014 Red Rainbow IT Solutions GmbH, Germany
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
import java.util.ArrayList;
import java.util.List;
import static org.testng.Assert.*;
import org.testng.annotations.Test;

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
