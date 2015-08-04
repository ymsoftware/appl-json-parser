package applparser;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by ymetelkin on 8/4/15.
 */
public abstract class ObjectParser {
    abstract void add(Map<String, Object> map, XMLStreamReader xmlr) throws XMLStreamException;

    void cleanup(Map<String, Object> map) {
    }

    public Map<String, Object> parse(String parentName, XMLStreamReader xmlr) throws XMLStreamException {
        boolean end = false;
        Map<String, Object> map = new LinkedHashMap<String, Object>();

        while (!end && xmlr.hasNext()) {
            xmlr.next();

            int eventType = xmlr.getEventType();

            if (eventType == XMLStreamReader.START_ELEMENT) {
                add(map, xmlr);

            } else if (eventType == XMLStreamReader.END_ELEMENT) {
                if (xmlr.getLocalName() == parentName) {
                    end = true;
                    this.cleanup(map);
                }
            }
        }

        return map;
    }
}
