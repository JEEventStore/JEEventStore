package org.jeeventstore.serialization;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.List;
import javax.xml.bind.DatatypeConverter;
import org.jeeventstore.EventSerializer;

/**
 * A simple serializer for tests using the Object*Stream api.
 * @author Alexander Langer
 */
public class JavaSerializer implements EventSerializer {

    @Override
    public String serialize(List<? extends Serializable> events) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(events);
            oos.flush();
            return DatatypeConverter.printHexBinary(baos.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<? extends Serializable> deserialize(String body) {
        try {
            byte bodybytes[] = DatatypeConverter.parseHexBinary(body);
            ByteArrayInputStream bais = new ByteArrayInputStream(bodybytes);
            ObjectInputStream si = new ObjectInputStream(bais);
            return (List<? extends Serializable>) si.readObject();
        } catch (ClassNotFoundException | IOException e) {
            throw new RuntimeException(e);
        }
    }
    
}
