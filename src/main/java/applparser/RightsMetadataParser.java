package applparser;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.util.Map;

/**
 * Created by ymetelkin on 7/29/15.
 */
public class RightsMetadataParser extends ApplParser {
    @Override
    public void parse(String name, XMLStreamReader xmlr, Map<String, Object> map) throws XMLStreamException {
        switch (name) {
            case "Copyright":
                //Holder="copyrightholder,function,copyright" Date="copyrightdate,function,copyright"
                break;
            case "UsageRights":
                //usagerights,function,usagerights
                break;
        }
    }
}
