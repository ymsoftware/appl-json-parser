package applparser;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ymetelkin on 7/27/15.
 */
public class ParseHelper {
//    static List<String> getArray(String parentName, String name, XMLStreamReader xmlr) throws XMLStreamException {
//        boolean end = false;
//        List<String> list = new ArrayList<String>();
//
//        while (!end && xmlr.hasNext()) {
//            xmlr.next();
//
//            int eventType = xmlr.getEventType();
//
//            if (eventType == XMLStreamReader.START_ELEMENT) {
//                if (name == xmlr.getLocalName()) {
//                    list.add(xmlr.getElementText());
//                }
//
//            } else if (eventType == XMLStreamReader.END_ELEMENT) {
//                if (xmlr.getLocalName() == parentName) {
//                    end = true;
//                }
//            }
//        }
//
//        return list;
//    }
}
