package org.jeeventstore.serialization;

import org.jeeventstore.TestUTF8Utils;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import org.jeeventstore.EventSerializer;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 *
 * @author Alexander Langer
 */
public abstract class AbstractSerializerTest {

    abstract EventSerializer serializer();

    private List<Serializable> data() {
        List<Serializable> list = new ArrayList<>();
        String original = TestUTF8Utils.unicodeString();
        String encoded = TestUTF8Utils.utf8String();
        TestUTF8Utils.println("Output test encoded: " + encoded);
        TestUTF8Utils.println("Output test original: " + original);
        list.add(original);
        list.add(encoded);
        list.add("Hello, World - äöüÄÜÖß");
        list.add(12345L);
        return list;
    }

    @Test
    public void test_serialize() throws Exception {
        EventSerializer ser = serializer();
        List<? extends Serializable> list = data();
        String res = ser.serialize(list);
        TestUTF8Utils.println("Serialized Object:");
        TestUTF8Utils.println(res);
        List reconstructed = ser.deserialize(res);
        TestUTF8Utils.println("reconstructed: " + reconstructed.toString());
        Assert.assertEquals(reconstructed, list);
    }

}
