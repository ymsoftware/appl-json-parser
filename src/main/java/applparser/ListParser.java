package applparser;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ymetelkin on 7/28/15.
 */
public abstract class ListParser<T> {
    abstract void add(List<T> list, XMLStreamReader xmlr) throws XMLStreamException;

    public List<T> parse(String parentName, XMLStreamReader xmlr) throws XMLStreamException {
        boolean end = false;
        List<T> list = new ArrayList<T>();

        while (!end && xmlr.hasNext()) {
            xmlr.next();

            int eventType = xmlr.getEventType();

            if (eventType == XMLStreamReader.START_ELEMENT) {
                add(list, xmlr);

            } else if (eventType == XMLStreamReader.END_ELEMENT) {
                if (xmlr.getLocalName() == parentName) {
                    end = true;
                }
            }
        }

        return list;
    }
}
