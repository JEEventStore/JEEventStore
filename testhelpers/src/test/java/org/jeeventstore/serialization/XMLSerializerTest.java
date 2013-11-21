package org.jeeventstore.serialization;

import org.jeeventstore.EventSerializer;

/**
 *
 * @author Alexander Langer
 */
public class XMLSerializerTest extends AbstractSerializerTest {

    @Override
    EventSerializer serializer() {
        return new XMLSerializer();
    }
    
}
