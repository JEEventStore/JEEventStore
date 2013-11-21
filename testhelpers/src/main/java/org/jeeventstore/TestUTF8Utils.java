package org.jeeventstore;

import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

/**
 *
 * @author Alexander Langer
 */
public class TestUTF8Utils {

    public static String unicodeString() {
        return "A" + "\u00ea" + "\u00f1" + "\u00fc" + "C";
    }

    public static String utf8String() {
        try {
            return new String(unicodeString().getBytes("UTF8"), "UTF8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public static void println(String output) {
        try {
            PrintStream out = new PrintStream(System.out, true, "UTF8");
            out.println(output);
            out.flush();
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
    
}
