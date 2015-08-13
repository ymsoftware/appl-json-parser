package applparser;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.StringReader;
import java.util.*;
import java.util.stream.Stream;

/**
 * Created by ymetelkin on 7/27/15.
 */
public class DocumentParser {
    private static ObjectMapper mapper = new ObjectMapper();
    private static ObjectMapper prettyMapper = new ObjectMapper();

    static {
        // Non-standard JSON but we allow C style comments in our JSON
        //mapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);

        //prettyMapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
        prettyMapper.configure(SerializationFeature.INDENT_OUTPUT, true);

//        SimpleModule module = new SimpleModule();
//        module.addSerializer(JsonObject.class, new JsonObjectSerializer());
//        module.addSerializer(JsonArray.class, new JsonArraySerializer());
//        mapper.registerModule(module);
//        prettyMapper.registerModule(module);
    }

    public String parse(String appl, boolean pretty) throws JsonProcessingException, XMLStreamException {
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        map.put("representationversion", "1.0");
        map.put("representationtype", "full");

        XMLInputFactory factory = XMLInputFactory.newInstance();

        XMLStreamReader xmlr = factory.createXMLStreamReader(new StringReader(appl));

        ApplParser parser = null;

        while (xmlr.hasNext()) {
            xmlr.next();

            int eventType = xmlr.getEventType();

            if (eventType == XMLStreamReader.START_ELEMENT) {
                String name = xmlr.getLocalName();

                switch (name) {
                    case "Publication":
                        break; // ignore root
                    case "Identification":
                        parser = new IdentificationParser();
                        break;
                    case "PublicationManagement":
                        parser = new PublicationManagementParser();
                        break;
                    case "NewsLines":
                        parser = new NewsLinesParser(map);
                        break;
                    case "AdministrativeMetadata":
                        parser = new AdministrativeMetadataParser();
                        break;
                    case "RightsMetadata":
                        parser = new RightsMetadataParser();
                        break;
                    case "DescriptiveMetadata":
                        parser = new DescriptiveMetadataParser();
                        break;
                    case "FilingMetadata":
                        parser = new FilingMetadataParser();
                        break;
                    case "PublicationComponent":
                        String role = xmlr.getAttributeValue("", "Role");
                        if (role != null) {
                            String type = xmlr.getAttributeValue("", "MediaType");
                            if (type != null) {
                                parser = new PublicationComponentParser(role, type.toLowerCase(), map);
                            }
                        }
                        break;
                    default:
                        if (parser != null) {
                            parser.parse(name, xmlr, map);
                        }
                }
            } else if (eventType == XMLStreamReader.END_ELEMENT) {
                switch (xmlr.getLocalName()) {
                    case "Identification":
                    case "PublicationManagement":
                    case "NewsLines":
                    case "AdministrativeMetadata":
                    case "RightsMetadata":
                    case "DescriptiveMetadata":
                    case "FilingMetadata":
                    case "PublicationComponent":
                        parser.cleanup(map);
                        parser = null;
                        break;
                    case "Publication":
                        if (map.containsKey("addConsumerReady")) {
                            if (map.containsKey("signals")) {
                                List<String> signals = (List<String>) map.get("signals");
                                signals.add("consumerready");
                                map.replace("signals", signals);
                            } else {
                                map.put("signals", new String[]{"consumerready"});
                            }

                            map.remove("addConsumerReady");
                        }

                        for (String key : new String[]{"addStateAudienece", "totalduration"}) {
                            if (map.containsKey(key)) {
                                map.remove(key);
                            }
                        }

                        for (String key : new String[]{"copyrightholder", "copyrightdate"}) {
                            if (map.containsKey(key) && map.get(key) == null) {
                                map.remove(key);
                            }
                        }

                        if (map.containsKey("renditions")) {
                            map.replace("renditions", ((Map<String, Map<String, Object>>) map.get("renditions")).values());
                        }

                        break;
                }
            }
        }

        String json = pretty ? prettyMapper.writeValueAsString(map) : mapper.writeValueAsString(map);
        return json;
    }

    public String parse(String appl) throws JsonProcessingException, XMLStreamException {
        return parse(appl, true);
    }
}
