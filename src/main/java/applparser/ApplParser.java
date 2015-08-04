package applparser;


import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
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

    public String parseDate(String date) {
        if (date != null && date.length() > 1) {
            if (date.indexOf('T') > 0) {
                if (date.endsWith("GMT")) {
                    date = date.replace("GMT", "Z");
                } else if (!date.endsWith("Z")) {
                    date = date + "Z";
                }

                return date;
            }

            try {
                Date test = new SimpleDateFormat("yyyy-MM-dd").parse(date);
                return date + "T00:00:00Z";
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        return null;
    }
}
