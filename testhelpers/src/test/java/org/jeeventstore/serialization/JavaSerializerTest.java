package org.jeeventstore.serialization;

import org.jeeventstore.EventSerializer;

/**
 *
 * @author Alexander Langer
 */
public class JavaSerializerTest extends AbstractSerializerTest {

    @Override
    EventSerializer serializer() {
        return new JavaSerializer();
    }
    
}
