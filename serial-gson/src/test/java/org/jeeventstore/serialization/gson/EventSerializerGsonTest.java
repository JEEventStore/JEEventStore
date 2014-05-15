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
