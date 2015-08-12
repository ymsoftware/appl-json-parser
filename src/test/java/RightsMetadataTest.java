import applparser.DocumentParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Iterator;

import static org.junit.Assert.assertEquals;

/**
 * Created by ymetelkin on 7/29/15.
 */
public class RightsMetadataTest {
    @Test
    public void testCopyrights() throws IOException, XMLStreamException {
        String appl = "<?xml version=\"1.0\" encoding=\"utf-8\" ?>"
                + "<Publication Version=\"4.4.0\" xmlns=\"http://ap.org/schemas/03/2005/appl\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">"
                + "<NewsLines>"
                + "<CopyrightLine>Copyright notice</CopyrightLine>"
                + "</NewsLines>"
                + "<RightsMetadata>"
                + "<Copyright Holder=\"AP\" Date=\"2014\" />"
                + "</RightsMetadata>"
                + "</Publication>";

        DocumentParser parser = new DocumentParser();
        String json = parser.parse(appl);

        ObjectMapper m = new ObjectMapper();
        JsonNode rootNode = m.readTree(json);
        JsonNode testNode = rootNode.path("copyrightnotice");
        assertEquals("Copyright notice", testNode.asText());
        testNode = rootNode.path("copyrightholder");
        assertEquals("AP", testNode.asText());
        testNode = rootNode.path("copyrightdate");
        assertEquals(2014, testNode.asInt());

        appl = "<?xml version=\"1.0\" encoding=\"utf-8\" ?>"
                + "<Publication Version=\"4.4.0\" xmlns=\"http://ap.org/schemas/03/2005/appl\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">"
                + "<NewsLines>"
                + "</NewsLines>"
                + "<RightsMetadata>"
                + "<Copyright Holder=\"AP\" Date=\"2014\" />"
                + "</RightsMetadata>"
                + "</Publication>";

        parser = new DocumentParser();
        json = parser.parse(appl);

        m = new ObjectMapper();
        rootNode = m.readTree(json);
        testNode = rootNode.path("copyrightnotice");
        assertEquals("Copyright 2014 AP. All rights reserved. This material may not be published, broadcast, rewritten or redistributed.", testNode.asText());
        testNode = rootNode.path("copyrightholder");
        assertEquals("AP", testNode.asText());
        testNode = rootNode.path("copyrightdate");
        assertEquals(2014, testNode.asInt());

        appl = "<?xml version=\"1.0\" encoding=\"utf-8\" ?>"
                + "<Publication Version=\"4.4.0\" xmlns=\"http://ap.org/schemas/03/2005/appl\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">"
                + "<NewsLines>"
                + "</NewsLines>"
                + "<RightsMetadata>"
                + "<Copyright Holder=\"AP\" />"
                + "</RightsMetadata>"
                + "</Publication>";

        parser = new DocumentParser();
        json = parser.parse(appl);

        m = new ObjectMapper();
        rootNode = m.readTree(json);
        testNode = rootNode.path("copyrightnotice");
        assertEquals(String.format("Copyright %d AP. All rights reserved. This material may not be published, broadcast, rewritten or redistributed.", LocalDate.now().getYear()), testNode.asText());
        testNode = rootNode.path("copyrightholder");
        assertEquals("AP", testNode.asText());
        testNode = rootNode.path("copyrightdate");
        assertEquals(true, testNode.isMissingNode());

        appl = "<?xml version=\"1.0\" encoding=\"utf-8\" ?>"
                + "<Publication Version=\"4.4.0\" xmlns=\"http://ap.org/schemas/03/2005/appl\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">"
                + "<NewsLines>"
                + "</NewsLines>"
                + "<RightsMetadata>"
                + "<Copyright Date=\"2014\" />"
                + "</RightsMetadata>"
                + "</Publication>";

        parser = new DocumentParser();
        json = parser.parse(appl);

        m = new ObjectMapper();
        rootNode = m.readTree(json);
        testNode = rootNode.path("copyrightnotice");
        assertEquals(true, testNode.isMissingNode());
        testNode = rootNode.path("copyrightholder");
        assertEquals(true, testNode.isMissingNode());
        testNode = rootNode.path("copyrightdate");
        assertEquals(2014, testNode.asInt());
    }

    @Test
    public void testUsageRights() throws IOException, XMLStreamException {
        String appl = "<?xml version=\"1.0\" encoding=\"utf-8\" ?>"
                + "<Publication Version=\"4.4.0\" xmlns=\"http://ap.org/schemas/03/2005/appl\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">"
                + "<RightsMetadata>"
                + "<UsageRights>"
                + "<UsageType>MobileNews</UsageType>"
                + "</UsageRights>"
                + "<UsageRights>"
                + "<UsageType>Marketplace</UsageType>"
                + "<Geography>us</Geography>"
                + "<RightsHolder>AP</RightsHolder>"
                + "<Limitations>nj</Limitations>"
                + "<Group Type=\"Corporate\" Id=\"abc\">YM</Group>"
                + "</UsageRights>"
                + "</RightsMetadata>"
                + "</Publication>";

        DocumentParser parser = new DocumentParser();
        String json = parser.parse(appl);

        ObjectMapper m = new ObjectMapper();
        JsonNode rootNode = m.readTree(json);
        JsonNode testNode = rootNode.path("usagerights");
        assertEquals(true, testNode.isArray());
        assertEquals(2, testNode.size());

        Iterator<JsonNode> elements = testNode.elements();

        JsonNode first = elements.next();
        assertEquals("MobileNews", first.get("usagetype").asText());

        JsonNode last = elements.next();
        assertEquals("Marketplace", last.get("usagetype").asText());
        assertEquals("AP", last.get("rightsholder").asText());

        testNode = last.get("geography");
        assertEquals(true, testNode.isArray());
        assertEquals(1, testNode.size());

        testNode = last.get("limitations");
        assertEquals(true, testNode.isArray());
        assertEquals(1, testNode.size());

        testNode = last.get("groups");
        assertEquals(true, testNode.isArray());
        assertEquals(1, testNode.size());
    }
}
