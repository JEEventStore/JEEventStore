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
