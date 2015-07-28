package applparser;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import java.io.StringReader;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

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

    public String parse(String appl, boolean pretty) {
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        map.put("representationversion", "1.0");
        map.put("representationtype", "full");

        try {
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
                            parser = new NewsLinesParser();
                            break;
                        default:
                            if (parser != null) {
                                parser.parse(name, xmlr, map);
                            }
                    }
                } else if (eventType == XMLStreamReader.END_ELEMENT) {
                    switch (xmlr.getLocalName()) {
                        case "Publication":
                        case "Identification":
                        case "PublicationManagement":
                        case "NewsLines":
                            parser.cleanup(map);
                            parser = null;
                            break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        try {
            String json = pretty ? prettyMapper.writeValueAsString(map) : mapper.writeValueAsString(map);
            return json;
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return null;
    }

    public String parse(String appl) {
        return parse(appl, true);
    }
}
