package org.jeeventstore.util;

import java.util.ArrayList;
import java.util.List;
import static org.testng.Assert.*;
import org.testng.annotations.Test;

/**
 *
 * @author Alexander Langer
 */
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
