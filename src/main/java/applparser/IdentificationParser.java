package applparser;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.util.Map;

/**
 * Created by ymetelkin on 7/27/15.
 */
public class IdentificationParser extends ApplParser {

    @Override
    public void parse(String name, XMLStreamReader xmlr, Map<String, Object> map) throws XMLStreamException {
        switch (name) {
            case "ItemId":
            case "RecordId":
            case "CompositeId":
                Helpers.safeAddStringId(name.toLowerCase(), xmlr.getElementText(), map);
                break;
            case "CompositionType":
            case "EditorialPriority":
            case "FriendlyKey":
                Helpers.safeAdd(name.toLowerCase(), xmlr.getElementText(), map);
                break;
            case "Priority":
            case "RecordSequenceNumber":
                Helpers.safeAddInteger(name.toLowerCase(), xmlr.getElementText(), map);
                break;
            case "MediaType":
                Helpers.safeAddStringId("type", xmlr.getElementText(), map);
                break;
            case "DefaultLanguage":
                setLanguage(xmlr, map);
                break;
        }
    }

    private void setLanguage(XMLStreamReader xmlr, Map<String, Object> map) throws XMLStreamException {
        String language = xmlr.getElementText();
        if (language != null && language.length() > 1) {
            if (language.length() > 2) {
                language = language.substring(0, 2);
            }
            map.put("language", language);
        }
    }
}
