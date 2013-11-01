package org.jeeventstore.serialization.gson;

import java.io.Serializable;

/**
 *
 * @author Alexander Langer
 */
public class TestObject implements Serializable {

    private String field;

    public TestObject(String field) {
        this.field = field;
    }

    public String getField() {
        return field;
    }
    
}
