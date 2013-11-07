package org.jeeventstore.serialization.gson;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.ejb.EJB;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.testng.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jeeventstore.EventSerializer;
import org.jeeventstore.tests.DefaultDeployment;
import static org.testng.Assert.*;
import org.testng.annotations.Test;

/**
 *
 * @author Alexander Langer
 */
public class EventSerializerGsonTest extends Arquillian {

    private static final String EXPECTED = 
            "{\"version\":1,\"events\":[{\"type\":\"java.lang.String\","
            + "\"body\":\"Hello, World!\"},{\"type\":\"java.lang.String\","
            + "\"body\":\"Wonga wonga\"}]}";

    @Deployment
    public static EnterpriseArchive deployment() {
        EnterpriseArchive ear = ShrinkWrap.create(EnterpriseArchive.class, "testear.ear");
        DefaultDeployment.addDependencies(ear, "org.jeeventstore:jeeventstore-serialization-gson", false);
        ear.addAsModule(ShrinkWrap.create(JavaArchive.class, "ejb.jar")
                .addAsManifestResource(new File("src/test/resources/META-INF/beans.xml"))
                .addAsManifestResource(
                    new File("src/test/resources/META-INF/ejb-jar-EventSerializerGsonTest.xml"),
                             "ejb-jar.xml")
                .addPackage(EventSerializerGson.class.getPackage())
                );
        return ear;
    }

    @EJB(lookup = "java:global/testear/ejb/EventSerializer")
    private EventSerializer serializer;

    private List<String> instance() {
        List<String> objs = new ArrayList<>();
        objs.add("Hello, World!");
        objs.add("Wonga wonga");
        return objs;
    }
    
    @Test
    public void testSerialize() {
        List<String> objs = instance();
        String result = serializer.serialize(objs);
        assertEquals(result, EXPECTED);
    }

    @Test
    public void test_custom_converters() {
        List<TestObject> objs = new ArrayList<>();
        objs.add(new TestObject("DUMMY"));
        String result = serializer.serialize(objs);
        assertEquals(result, 
                "{\"version\":1,\"events\":[{\"type\":\""
                        + "org.jeeventstore.serialization.gson.TestObject\""
                        + ",\"body\":\"HELLO\"}]}");
    }

    @Test
    public void testDeserialize() {
        List<String> objs = instance();
        List<? extends Serializable> res = serializer.deserialize(EXPECTED);
        assertEquals(res, objs);
    }
    
}
