import applparser.DocumentParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import static org.junit.Assert.*;

/**
 * Created by ymetelkin on 7/27/15.
 */
public class IdentificationTest {
    @Test
    public void test() throws IOException, XMLStreamException {
        String appl = "<?xml version=\"1.0\" encoding=\"utf-8\" ?>"
                + "<Publication Version=\"4.4.0\" xmlns=\"http://ap.org/schemas/03/2005/appl\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">"
                + "<Identification>"
                + "<ItemId>aBc</ItemId>"
                + "<RecordId>aBc</RecordId>"
                + "<CompositeId>aBc</CompositeId>"
                + "<CompositionType>StandardText</CompositionType>"
                + "<MediaType>Text</MediaType>"
                + "<Priority>4</Priority>"
                + "<EditorialPriority>r</EditorialPriority>"
                + "<DefaultLanguage>en-us</DefaultLanguage>"
                + "<RecordSequenceNumber>1</RecordSequenceNumber>"
                + "<FriendlyKey>X:1234-YZ</FriendlyKey>"
                + "</Identification>"
                + "</Publication>";

        DocumentParser parser = new DocumentParser();
        String json = parser.parse(appl);
        json = parser.parse(appl, false);

        ObjectMapper m = new ObjectMapper();
        JsonNode rootNode = m.readTree(json);
        JsonNode testNode = rootNode.path("itemid");
        assertEquals("abc", testNode.asText());

        testNode = rootNode.path("recordid");
        assertEquals("abc", testNode.asText());

        testNode = rootNode.path("compositeid");
        assertEquals("abc", testNode.asText());

        testNode = rootNode.path("compositiontype");
        assertEquals("StandardText", testNode.asText());

        testNode = rootNode.path("type");
        assertEquals("text", testNode.asText());

        testNode = rootNode.path("priority");
        assertEquals(4, testNode.asInt());

        testNode = rootNode.path("editorialpriority");
        assertEquals("r", testNode.asText());

        testNode = rootNode.path("language");
        assertEquals("en", testNode.asText());

        testNode = rootNode.path("recordsequencenumber");
        assertEquals(1, testNode.asInt());

        testNode = rootNode.path("friendlykey");
        assertEquals("X:1234-YZ", testNode.asText());
    }
}
