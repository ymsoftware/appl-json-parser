package applparser;


import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.util.Map;

/**
 * Created by ymetelkin on 7/27/15.
 */
public abstract class ApplParser {
    public abstract void parse(String name, XMLStreamReader xmlr, Map<String, Object> map) throws XMLStreamException;

    public void cleanup(Map<String, Object> map) {
    }

    void parse(String field, Object value, Map<String, Object> map) {
        if (value != null) {
            map.put(field, value);
        }
    }

    void parseId(String field, String value, Map<String, Object> map) {
        if (value != null && value.length() > 0) {
            map.put(field, value.trim().toLowerCase());
        }
    }

    void parseInteger(String field, String value, Map<String, Object> map) {
        if (value != null && value.length() > 0) {
            try {
                map.put(field, Integer.parseUnsignedInt(value));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    Integer parseInteger(String attribute, XMLStreamReader xmlr) {
        try {
            String value = xmlr.getAttributeValue("", attribute);
            if (value != null) {
                return Integer.parseUnsignedInt(value);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
