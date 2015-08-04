package applparser;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.util.Map;

/**
 * Created by ymetelkin on 7/29/15.
 */
public class DescriptiveMetadataParser extends ApplParser {
    //addConsumerReady

    @Override
    public void parse(String name, XMLStreamReader xmlr, Map<String, Object> map) throws XMLStreamException {
        switch (name) {
            case "Description":
                //descriptions,function,array
                break;
            case "DateLineLocation":
                //datelinelocation,function,datelinelocation
                break;
            case "SubjectClassification":
                //classification,function,classification
                break;
            case "EntityClassification":
                //classification,function,classification
                break;
            case "AudienceClassification":
                //classification,function,classification
                break;
            case "SalesClassification":
                //classification,function,classification
                break;
            case "Comment":
                //classification,function,classification
                break;
            case "ThirdPartyMeta":
                //classification,function,classification
                break;
        }
    }
}
