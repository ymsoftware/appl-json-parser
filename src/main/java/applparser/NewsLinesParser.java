package applparser;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.util.Map;

/**
 * Created by ymetelkin on 7/28/15.
 */
public class NewsLinesParser extends ApplParser {

    @Override
    public void parse(String name, XMLStreamReader xmlr, Map<String, Object> map) throws XMLStreamException {
        switch (name) {
            case "DateLine":
            case "RightsLine":
            case "SeriesLine":
            case "OutCue":
            case "LocationLine":
                parseId(name.toLowerCase(), xmlr.getElementText(), map);
                break;
            case "ByLine":
            case "ByLineOriginal":
                break;
            case "OverLine":
                break;
            case "CreditLine":
                break;
            case "CopyrightLine":
                break;
            case "KeywordLine":
                break;
            case "NameLine":
                break;
        }
    }
}
