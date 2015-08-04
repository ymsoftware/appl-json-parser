package applparser;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by ymetelkin on 7/29/15.
 */
public class DescriptiveMetadataParser extends ApplParser {
    private List<String> descriptions;
    private boolean addDescriptions;
    private boolean calculate;

    public DescriptiveMetadataParser(Map<String, Object> map) {
        this.calculate = map.containsKey("addConsumerReady");
    }

    @Override
    public void parse(String name, XMLStreamReader xmlr, Map<String, Object> map) throws XMLStreamException {
        switch (name) {
            case "Description":
                setDescriptions(xmlr, map);
                break;
            case "DateLineLocation":
                ObjectParser parser = new DateLineLocationParser();
                Map<String, Object> datelinelocation = parser.parse(name, xmlr);
                if (datelinelocation.size() > 0) {
                    map.put("datelinelocation", datelinelocation);
                }
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

    @Override
    public void cleanup(Map<String, Object> map) {
        if (this.addDescriptions) {
            map.replace("descriptions", null, this.descriptions);
            this.addDescriptions = false;
        }

        if (!this.calculate) {
            map.remove("addConsumerReady");
        }
    }

    private void setDescriptions(XMLStreamReader xmlr, Map<String, Object> map) throws XMLStreamException {
        if (this.descriptions == null) {
            this.descriptions = new ArrayList<String>();
        }

        String text = xmlr.getElementText();
        if (text != null) {
            if (!this.addDescriptions) {
                map.put("descriptions", null);
                this.addDescriptions = true;
            }
            if (!this.descriptions.contains(text)) this.descriptions.add(text);
        }
    }

    private class DateLineLocationParser extends ObjectParser {
        private Number lon;
        private Number lat;

        @Override
        void add(Map<String, Object> map, XMLStreamReader xmlr) throws XMLStreamException {
            String name = xmlr.getLocalName();

            switch (name) {
                case "City":
                case "CountryAreaName":
                case "CountryName":
                    Helpers.safeAdd(name.toLowerCase(), xmlr.getElementText(), map);
                    break;
                case "CountryArea":
                    Helpers.safeAdd("countryareacode", xmlr.getElementText(), map);
                    break;
                case "Country":
                    Helpers.safeAdd("countrycode", xmlr.getElementText(), map);
                    break;
                case "LongitudeDD":
                    this.lon = Helpers.parseNumber(xmlr.getElementText());

                    break;
                case "LatitudeDD":
                    this.lat = Helpers.parseNumber(xmlr.getElementText());
                    break;
            }
        }

        @Override
        void cleanup(Map<String, Object> map) {
            if (this.lon != null && this.lat != null) {
                Map<String, Object> geo = new LinkedHashMap<String, Object>();
                geo.put("type", "Point");
                geo.put("coordinates", new Number[]{this.lon, this.lat});

                map.put("geometry_geojson", geo);
            }
        }
    }
}
