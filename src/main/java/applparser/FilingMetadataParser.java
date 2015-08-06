package applparser;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.util.Map;

/**
 * Created by ymetelkin on 7/29/15.
 */
public class FilingMetadataParser extends ApplParser {
    private boolean calculate;

    //title
    //geo

    public FilingMetadataParser(Map<String, Object> map) {
        this.calculate = map.containsKey("addConsumerReady");
    }

    @Override
    public void parse(String name, XMLStreamReader xmlr, Map<String, Object> map) throws XMLStreamException {

    }
}
