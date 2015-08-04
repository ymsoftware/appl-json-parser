import applparser.DocumentParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.io.IOException;
import java.util.Iterator;

import static org.junit.Assert.assertEquals;

/**
 * Created by ymetelkin on 7/27/15.
 */
public class PublicationManagementTest {
    @Test
    public void test() throws IOException {
        String appl = "<?xml version=\"1.0\" encoding=\"utf-8\" ?>"
                + "<Publication Version=\"4.4.0\" xmlns=\"http://ap.org/schemas/03/2005/appl\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">"
                + "<Identification>"
                + "<ItemId>aBc</ItemId>"
                + "<MediaType>Text</MediaType>"
                + "</Identification>"
                + "<PublicationManagement>"
                + "<RecordType>Change</RecordType>"
                + "<FilingType>Text</FilingType>"
                + "<ChangeEvent>Event</ChangeEvent>"
                + "<ItemKey>Text|--|cwire|BC|EU</ItemKey>"
                + "<ArrivalDateTime>2015-07-11T16:20:05Z</ArrivalDateTime>"
                + "<ItemStartDateTime>2015-07-11T16:20:05</ItemStartDateTime>"
                + "<SearchDateTime>2015-07-11T16:20:05GMT</SearchDateTime>"
                + "<ReleaseDateTime>2015-07-11T16:20:05</ReleaseDateTime>"
                + "<ItemEndDateTime>2015-07-11</ItemEndDateTime>"
                + "<FirstCreated Year=\"2015\" Month=\"7\" Day=\"11\" Time=\"16:20:05\" />"
                + "<AssociatedWith CompositionType=\"StandardText\">AbC</AssociatedWith>"
                + "<AssociatedWith CompositionType=\"video\">cbS</AssociatedWith>"
                + "<AssociatedWith CompositionType=\"StandardText\">nBc</AssociatedWith>"
                + "<Editorial><Type>Advance</Type><Type>HoldForRelease</Type></Editorial>"
                + "<TimeRestrictions System=\"a\" Zone=\"b\" Include=\"true\"></TimeRestrictions>"
                + "<ExplicitWarning>1</ExplicitWarning>"
                + "<IsDigitized>false</IsDigitized>"
                + "<Instruction Type=\"Outing\">Include this</Instruction>"
                + "<Instruction Type=\"Test\">Exclude this</Instruction>"
                + "</PublicationManagement>"
                + "</Publication>";

        DocumentParser parser = new DocumentParser();
        String json = parser.parse(appl);
        json = parser.parse(appl, false);

        ObjectMapper m = new ObjectMapper();
        JsonNode rootNode = m.readTree(json);
        JsonNode testNode = rootNode.path("recordtype");
        assertEquals("Change", testNode.asText());

        testNode = rootNode.path("filingtype");
        assertEquals("Text", testNode.asText());

        testNode = rootNode.path("changeevent");
        assertEquals("Event", testNode.asText());

        testNode = rootNode.path("itemkey");
        assertEquals("Text|--|cwire|BC|EU", testNode.asText());

        testNode = rootNode.path("arrivaldatetime");
        assertEquals("2015-07-11T16:20:05Z", testNode.asText());

        testNode = rootNode.path("itemstartdatetime");
        assertEquals("2015-07-11T16:20:05Z", testNode.asText());

        testNode = rootNode.path("searchdatetime");
        assertEquals("2015-07-11T16:20:05Z", testNode.asText());

        testNode = rootNode.path("itemenddatetime");
        assertEquals("2015-07-11T00:00:00Z", testNode.asText());

        testNode = rootNode.path("firstcreated");
        assertEquals("2015-07-11T16:20:05Z", testNode.asText());

        testNode = rootNode.path("releasedatetime");
        assertEquals("2015-07-11T16:20:05Z", testNode.asText());

        testNode = rootNode.path("editorialtypes");
        assertEquals(true, testNode.isArray());
        assertEquals(2, testNode.size());
        assertEquals("Advance", testNode.elements().next().asText());

        testNode = rootNode.path("embargoed");
        assertEquals("2015-07-11T16:20:05Z", testNode.asText());

        testNode = rootNode.path("associations");
        assertEquals(true, testNode.isArray());
        assertEquals(3, testNode.size());
        assertEquals("abc", testNode.elements().next().get("itemid").asText());

        testNode = rootNode.path("outinginstructions");
        assertEquals(true, testNode.isArray());
        assertEquals(1, testNode.size());
        assertEquals("Include this", testNode.elements().next().asText());

        testNode = rootNode.path("timerestrictions");
        assertEquals(true, testNode.isArray());
        assertEquals(1, testNode.size());
        assertEquals(true, testNode.elements().next().get("ab").asBoolean());

        testNode = rootNode.path("signals");
        assertEquals(true, testNode.isArray());
        assertEquals(2, testNode.size());
        assertEquals("explicitcontent", testNode.elements().next().asText());
    }

    @Test
    public void testEditorilTypes() throws IOException {
        String appl = "<?xml version=\"1.0\" encoding=\"utf-8\" ?>"
                + "<Publication Version=\"4.4.0\" xmlns=\"http://ap.org/schemas/03/2005/appl\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">"
                + "<Identification>"
                + "<ItemId>aBc</ItemId>"
                + "</Identification>"
                + "<PublicationManagement>"
                + "<ReleaseDateTime>2015-07-11T16:20:05</ReleaseDateTime>"
                + "<Editorial><Type>Advance</Type><Type>HoldForRelease</Type></Editorial>"
                + "</PublicationManagement>"
                + "</Publication>";

        DocumentParser parser = new DocumentParser();
        String json = parser.parse(appl);

        ObjectMapper m = new ObjectMapper();
        JsonNode rootNode = m.readTree(json);
        JsonNode testNode = rootNode.path("releasedatetime");
        assertEquals("2015-07-11T16:20:05Z", testNode.asText());

        testNode = rootNode.path("editorialtypes");
        assertEquals(true, testNode.isArray());
        assertEquals(2, testNode.size());
        assertEquals("Advance", testNode.elements().next().asText());

        testNode = rootNode.path("embargoed");
        assertEquals("2015-07-11T16:20:05Z", testNode.asText());

        appl = "<?xml version=\"1.0\" encoding=\"utf-8\" ?>"
                + "<Publication Version=\"4.4.0\" xmlns=\"http://ap.org/schemas/03/2005/appl\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">"
                + "<Identification>"
                + "<ItemId>aBc</ItemId>"
                + "</Identification>"
                + "<PublicationManagement>"
                + "<Editorial><Type>Advance</Type><Type>HoldForRelease</Type></Editorial>"
                + "<ReleaseDateTime>2015-07-11T16:20:05</ReleaseDateTime>"
                + "</PublicationManagement>"
                + "</Publication>";

        parser = new DocumentParser();
        json = parser.parse(appl);

        m = new ObjectMapper();
        rootNode = m.readTree(json);
        testNode = rootNode.path("releasedatetime");
        assertEquals("2015-07-11T16:20:05Z", testNode.asText());

        testNode = rootNode.path("editorialtypes");
        assertEquals(true, testNode.isArray());
        assertEquals(2, testNode.size());
        assertEquals("Advance", testNode.elements().next().asText());

        testNode = rootNode.path("embargoed");
        assertEquals("2015-07-11T16:20:05Z", testNode.asText());

        appl = "<?xml version=\"1.0\" encoding=\"utf-8\" ?>"
                + "<Publication Version=\"4.4.0\" xmlns=\"http://ap.org/schemas/03/2005/appl\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">"
                + "<Identification>"
                + "<ItemId>aBc</ItemId>"
                + "</Identification>"
                + "<PublicationManagement>"
                + "<Editorial><Type>Test</Type><Type>AnotherTest</Type></Editorial>"
                + "<ReleaseDateTime>2015-07-11T16:20:05</ReleaseDateTime>"
                + "</PublicationManagement>"
                + "</Publication>";

        parser = new DocumentParser();
        json = parser.parse(appl);

        m = new ObjectMapper();
        rootNode = m.readTree(json);
        testNode = rootNode.path("releasedatetime");
        assertEquals("2015-07-11T16:20:05Z", testNode.asText());

        testNode = rootNode.path("editorialtypes");
        assertEquals(true, testNode.isArray());
        assertEquals(2, testNode.size());
        assertEquals("Test", testNode.elements().next().asText());

        testNode = rootNode.path("embargoed");
        assertEquals(true, testNode.isMissingNode());

        appl = "<?xml version=\"1.0\" encoding=\"utf-8\" ?>"
                + "<Publication Version=\"4.4.0\" xmlns=\"http://ap.org/schemas/03/2005/appl\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">"
                + "<Identification>"
                + "<ItemId>aBc</ItemId>"
                + "</Identification>"
                + "<PublicationManagement>"
                + "<Editorial><Type>Advance</Type><Type>HoldForRelease</Type></Editorial>"
                + "</PublicationManagement>"
                + "</Publication>";

        parser = new DocumentParser();
        json = parser.parse(appl);

        m = new ObjectMapper();
        rootNode = m.readTree(json);

        testNode = rootNode.path("editorialtypes");
        assertEquals(true, testNode.isArray());
        assertEquals(2, testNode.size());
        assertEquals("Advance", testNode.elements().next().asText());

        testNode = rootNode.path("embargoed");
        assertEquals(true, testNode.isMissingNode());
    }

    @Test
    public void testFirstCreated() throws IOException {
        String appl = "<?xml version=\"1.0\" encoding=\"utf-8\" ?>"
                + "<Publication Version=\"4.4.0\" xmlns=\"http://ap.org/schemas/03/2005/appl\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">"
                + "<PublicationManagement>"
                + "<FirstCreated Year=\"2015\" Month=\"7\" Day=\"11\" Time=\"16:20:05\" />"
                + "</PublicationManagement>"
                + "</Publication>";

        DocumentParser parser = new DocumentParser();
        String json = parser.parse(appl);

        ObjectMapper m = new ObjectMapper();
        JsonNode rootNode = m.readTree(json);
        JsonNode testNode = rootNode.path("firstcreated");
        assertEquals("2015-07-11T16:20:05Z", testNode.asText());

        appl = "<?xml version=\"1.0\" encoding=\"utf-8\" ?>"
                + "<Publication Version=\"4.4.0\" xmlns=\"http://ap.org/schemas/03/2005/appl\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">"
                + "<PublicationManagement>"
                + "<FirstCreated Year=\"2015\" Month=\"7\" Day=\"1\" />"
                + "</PublicationManagement>"
                + "</Publication>";

        parser = new DocumentParser();
        json = parser.parse(appl);

        m = new ObjectMapper();
        rootNode = m.readTree(json);
        testNode = rootNode.path("firstcreated");
        assertEquals("2015-07-01", testNode.asText());

        appl = "<?xml version=\"1.0\" encoding=\"utf-8\" ?>"
                + "<Publication Version=\"4.4.0\" xmlns=\"http://ap.org/schemas/03/2005/appl\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">"
                + "<PublicationManagement>"
                + "<FirstCreated Year=\"2015\" Month=\"7\" />"
                + "</PublicationManagement>"
                + "</Publication>";

        parser = new DocumentParser();
        json = parser.parse(appl);

        m = new ObjectMapper();
        rootNode = m.readTree(json);
        testNode = rootNode.path("firstcreated");
        assertEquals("2015-07", testNode.asText());

        appl = "<?xml version=\"1.0\" encoding=\"utf-8\" ?>"
                + "<Publication Version=\"4.4.0\" xmlns=\"http://ap.org/schemas/03/2005/appl\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">"
                + "<PublicationManagement>"
                + "<FirstCreated Year=\"2015\" />"
                + "</PublicationManagement>"
                + "</Publication>";

        parser = new DocumentParser();
        json = parser.parse(appl);

        m = new ObjectMapper();
        rootNode = m.readTree(json);
        testNode = rootNode.path("firstcreated");
        assertEquals("2015", testNode.asText());
    }

    @Test
    public void testTimeRestrictions() throws IOException {
        String appl = "<?xml version=\"1.0\" encoding=\"utf-8\" ?>"
                + "<Publication Version=\"4.4.0\" xmlns=\"http://ap.org/schemas/03/2005/appl\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">"
                + "<PublicationManagement>"
                + "<TimeRestrictions System=\"a\" Zone=\"b\" Include=\"true\"></TimeRestrictions>"
                + "<TimeRestrictions System=\"a\" Include=\"true\"></TimeRestrictions>"
                + "<TimeRestrictions Zone=\"b\" Include=\"true\"></TimeRestrictions>"
                + "<TimeRestrictions System=\"a\" Zone=\"b\" Include=\"false\"></TimeRestrictions>"
                + "<TimeRestrictions System=\"b\" Zone=\"a\" Include=\"false\"></TimeRestrictions>"
                + "</PublicationManagement>"
                + "</Publication>";

        DocumentParser parser = new DocumentParser();
        String json = parser.parse(appl);

        ObjectMapper m = new ObjectMapper();
        JsonNode rootNode = m.readTree(json);
        JsonNode testNode = rootNode.path("timerestrictions");
        assertEquals(true, testNode.isArray());
        assertEquals(4, testNode.size());

        Iterator<JsonNode> elements = testNode.elements();

        JsonNode time = elements.next().get("ab");
        assertEquals(true, time.asBoolean());

        time = elements.next().get("a");
        assertEquals(true, time.asBoolean());

        time = elements.next().get("b");
        assertEquals(true, time.asBoolean());

        time = elements.next().get("ba");
        assertEquals(false, time.asBoolean());
    }
}
