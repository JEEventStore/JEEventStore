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

package org.jeeventstore.util;

import java.util.ArrayList;
import java.util.List;
import static org.testng.Assert.*;
import org.testng.annotations.Test;

public class IteratorUtilsTest {
    
    @Test
    public void testToList() {
        List<Integer> source = new ArrayList<>();
        for (int i = 0; i < 145; i++)
            source.add((int) Math.random() * 1000);

        List<Integer> dest = IteratorUtils.toList(source.iterator());
        assertEquals(dest, source);
    }

    @Test
    public void testEmptyList() {
        List<Integer> source = new ArrayList<>();
        List<Integer> dest = IteratorUtils.toList(source.iterator());
        assertTrue(dest.isEmpty());
        assertEquals(dest, source);
    }

    @Test
    public void testNullArg() {
        try {
            List<Integer> dest = IteratorUtils.toList(null);
            fail("Should have failed by now.");
        } catch (Exception e) {
            // expected
        }
    }


    
}
