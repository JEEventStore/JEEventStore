package org.jeeventstore.serialization;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.jeeventstore.EventSerializer;
import org.jeeventstore.serialization.JavaSerializer;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 *
 * @author Alexander Langer
 */
public class JavaSerializerTest {

    private List<Serializable> data() {
        List<Serializable> list = new ArrayList<>();
        list.add("Hello, World");
        list.add(12345L);
        return list;
    }

    @Test
    public void test_serialize() throws Exception {
        EventSerializer sms = new JavaSerializer();
        List<? extends Serializable> list = data();
        String res = sms.serialize(list);

        EventSerializer dms = new JavaSerializer();
        List reconstructed = dms.deserialize(res);
        Assert.assertEquals(reconstructed, list);
    }
    
}
