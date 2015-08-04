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
}
