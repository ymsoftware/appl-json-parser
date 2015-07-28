package applparser;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.util.List;

/**
 * Created by ymetelkin on 7/28/15.
 */
public class StringListParser extends ListParser<String> {
    private String name;

    public StringListParser(String name) {
        this.name = name;
    }

    @Override
    void add(List<String> list, XMLStreamReader xmlr) throws XMLStreamException {
        if (name.equals(xmlr.getLocalName())) {
            list.add(xmlr.getElementText());
        }
    }
}
