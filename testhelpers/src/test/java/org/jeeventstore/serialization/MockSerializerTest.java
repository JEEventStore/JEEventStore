package org.jeeventstore.serialization;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.jeeventstore.EventSerializer;
import org.jeeventstore.serialization.MockSerializer;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 *
 * @author Alexander Langer
 */
public class MockSerializerTest {

    private final static String RESULT 
            = "ACED0005737200136A6176612E7574696C2E41727261794C6973747881D21D"
            + "99C7619D03000149000473697A6578700000000277040000000274000C4865"
            + "6C6C6F2C20576F726C647372000E6A6176612E6C616E672E4C6F6E673B8BE4"
            + "90CC8F23DF0200014A000576616C7565787200106A6176612E6C616E672E4E"
            + "756D62657286AC951D0B94E08B0200007870000000000000303978";

    private List<Serializable> data() {
        List<Serializable> list = new ArrayList<>();
        list.add("Hello, World");
        list.add(12345L);
        return list;
    }

    @Test
    public void test_serialize() throws Exception {
        EventSerializer ms = new MockSerializer();
        List<? extends Serializable> list = data();
        String res = ms.serialize(list);
        Assert.assertEquals(res, RESULT);
    }

    @Test
    public void test_deserialize() throws Exception {
        EventSerializer ms = new MockSerializer();
        List<? extends Serializable> list = data();
        List reconstructed = ms.deserialize(RESULT);
        Assert.assertEquals(reconstructed, list);
    }
    
}
