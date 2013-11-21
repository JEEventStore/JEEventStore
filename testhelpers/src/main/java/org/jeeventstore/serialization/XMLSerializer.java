package org.jeeventstore.serialization;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import org.jeeventstore.EventSerializer;

/**
 *
 * @author Alexander Langer
 */
public class XMLSerializer implements EventSerializer {

    @Override
    public String serialize(List<? extends Serializable> events) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            XMLEncoder encoder = new XMLEncoder(baos);
            encoder.writeObject(events);
            encoder.close();
            String res = baos.toString("UTF-8");
            baos.close();
            return res;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<? extends Serializable> deserialize(String body) {
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(body.getBytes("UTF-8"));
            XMLDecoder decoder = new XMLDecoder(bais);
            List<? extends Serializable> res = (List<? extends Serializable>) decoder.readObject();
            decoder.close();
            bais.close();
            return res;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
}
