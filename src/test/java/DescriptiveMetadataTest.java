import applparser.DocumentParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.io.IOException;
import java.util.Iterator;

import static org.junit.Assert.assertEquals;

/**
 * Created by ymetelkin on 8/4/15.
 */
public class DescriptiveMetadataTest {
    @Test
    public void testDateLineLocation() throws IOException {
        String appl = "<?xml version=\"1.0\" encoding=\"utf-8\" ?>"
                + "<Publication Version=\"4.4.0\" xmlns=\"http://ap.org/schemas/03/2005/appl\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">"
                + "<DescriptiveMetadata>"
                + "<DateLineLocation>"
                + "<City>Tula</City>"
                + "<CountryArea>TU</CountryArea>"
                + "<CountryAreaName>Tula Region</CountryAreaName>"
                + "<Country>RUS</Country>"
                + "<CountryName>Russian Federation</CountryName>"
                + "<LatitudeDD>54.2044444</LatitudeDD>"
                + "<LongitudeDD>37.6111111</LongitudeDD>"
                + "</DateLineLocation>"
                + "</DescriptiveMetadata>"
                + "</Publication>";

        DocumentParser parser = new DocumentParser();
        String json = parser.parse(appl);

        ObjectMapper m = new ObjectMapper();
        JsonNode rootNode = m.readTree(json);
        JsonNode testNode = rootNode.path("datelinelocation");
        assertEquals("Tula", testNode.get("city").asText());
        assertEquals("TU", testNode.get("countryareacode").asText());
        assertEquals("Tula Region", testNode.get("countryareaname").asText());
        assertEquals("RUS", testNode.get("countrycode").asText());
        assertEquals("Russian Federation", testNode.get("countryname").asText());

        testNode = testNode.get("geometry_geojson").get("coordinates");
        assertEquals(true, testNode.isArray());
        assertEquals(2, testNode.size());
        assertEquals(37.6111111d, testNode.elements().next().numberValue());
    }

    @Test
    public void testGenerators() throws IOException {
        String appl = "<?xml version=\"1.0\" encoding=\"utf-8\" ?>"
                + "<Publication Version=\"4.4.0\" xmlns=\"http://ap.org/schemas/03/2005/appl\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">"
                + "<DescriptiveMetadata>"
                + "<SubjectClassification Authority=\"AP\" AuthorityVersion=\"v1\" />"
                + "<SubjectClassification Authority=\"AP\" AuthorityVersion=\"v2\" />"
                + "<SubjectClassification Authority=\"YM\" AuthorityVersion=\"v1\" />"
                + "<EntityClassification Authority=\"YM\" AuthorityVersion=\"v1\" />"
                + "<EntityClassification Authority=\"YM\" AuthorityVersion=\"v3\" />"
                + "</DescriptiveMetadata>"
                + "</Publication>";

        DocumentParser parser = new DocumentParser();
        String json = parser.parse(appl);

        ObjectMapper m = new ObjectMapper();
        JsonNode rootNode = m.readTree(json);
        JsonNode testNode = rootNode.path("generators");
        assertEquals(true, testNode.isArray());
        assertEquals(4, testNode.size());

        Iterator<JsonNode> elements = testNode.elements();

        JsonNode next = elements.next();
        assertEquals("v1", next.get("version").asText());
        assertEquals("AP", next.get("name").asText());

        next = elements.next();
        assertEquals("v2", next.get("version").asText());
        assertEquals("AP", next.get("name").asText());

        next = elements.next();
        assertEquals("v1", next.get("version").asText());
        assertEquals("YM", next.get("name").asText());

        next = elements.next();
        assertEquals("v3", next.get("version").asText());
        assertEquals("YM", next.get("name").asText());
    }

    @Test
    public void testSubjects() throws IOException {
        String appl = "<?xml version=\"1.0\" encoding=\"utf-8\" ?>"
                + "<Publication Version=\"4.4.0\" xmlns=\"http://ap.org/schemas/03/2005/appl\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">"
                + "<DescriptiveMetadata>"
                + "<SubjectClassification Authority=\"AP Category Code\">"
                + "<Occurrence Id=\"a1\" Value=\"v1\" />"
                + "<Occurrence Id=\"a2\" />"
                + "</SubjectClassification>"
                + "<SubjectClassification Authority=\"AP Supplemental Category Code\">"
                + "<Occurrence Id=\"a1\" Value=\"v1\" />"
                + "<Occurrence Id=\"a2\" />"
                + "</SubjectClassification>"
                + "<SubjectClassification Authority=\"AP Alert Category\">"
                + "<Occurrence Id=\"a1\" />"
                + "<Occurrence Id=\"a2\" />"
                + "</SubjectClassification>"
                + "<SubjectClassification Authority=\"AP Subject\" System=\"RTE\" >"
                + "<Occurrence Id=\"a1\" Value=\"v1\" ParentId=\"p1\" TopParent=\"true\" />"
                + "<Occurrence Id=\"a2\" ActualMatch=\"true\" />"
                + "<Occurrence Id=\"a1\" Value=\"v1\" ActualMatch=\"true\" ParentId=\"p2\" TopParent=\"false\" />"
                + "</SubjectClassification>"
                + "<SubjectClassification Authority=\"AP Subject\" >"
                + "<Occurrence Id=\"a1\" Value=\"v1\" ActualMatch=\"true\" ParentId=\"p2\" TopParent=\"false\" />"
                + "</SubjectClassification>"
                + "<SubjectClassification Authority=\"AP Audio Cut Number Code\">"
                + "<Occurrence Value=\"901\" />"
                + "</SubjectClassification>"
                + "</DescriptiveMetadata>"
                + "</Publication>";


        DocumentParser parser = new DocumentParser();
        String json = parser.parse(appl);

        ObjectMapper m = new ObjectMapper();
        JsonNode rootNode = m.readTree(json);
        JsonNode testNode = rootNode.path("categories");
        assertEquals(true, testNode.isArray());
        assertEquals(2, testNode.size());

        Iterator<JsonNode> elements = testNode.elements();

        JsonNode next = elements.next();
        assertEquals("v1", next.get("name").asText());
        assertEquals("a1", next.get("code").asText());

        next = elements.next();
        assertEquals("a2", next.get("code").asText());

        testNode = rootNode.path("subcategories");
        assertEquals(true, testNode.isArray());
        assertEquals(2, testNode.size());

        elements = testNode.elements();

        next = elements.next();
        assertEquals("v1", next.get("name").asText());
        assertEquals("a1", next.get("code").asText());

        next = elements.next();
        assertEquals("a2", next.get("code").asText());

        testNode = rootNode.path("alertcategories");
        assertEquals(true, testNode.isArray());
        assertEquals(2, testNode.size());

        elements = testNode.elements();

        next = elements.next();
        assertEquals("a1", next.asText());

        next = elements.next();
        assertEquals("a2", next.asText());

        testNode = rootNode.path("subjects");
        assertEquals(true, testNode.isArray());
        assertEquals(2, testNode.size());

        elements = testNode.elements();

        next = elements.next();
        assertEquals("v1", next.get("name").asText());
        assertEquals("a1", next.get("code").asText());
        JsonNode array = next.get("rels");
        assertEquals(true, array.isArray());
        assertEquals(2, array.size());
        assertEquals("inferred", array.elements().next().asText());
        array=next.get("parentids");
        assertEquals(true, array.isArray());
        assertEquals(2, array.size());
        assertEquals("p1", array.elements().next().asText());
        assertEquals(true, next.get("topparent").asBoolean());

        next = elements.next();
        assertEquals("a2", next.get("code").asText());
        array = next.get("rels");
        assertEquals(true, array.isArray());
        assertEquals(1, array.size());
        assertEquals("inferred", array.elements().next().asText());

        testNode = rootNode.path("fixture");
        assertEquals("v1", testNode.get("name").asText());
        assertEquals("901", testNode.get("code").asText());
    }

    @Test
    public void testEntitiess() throws IOException {
        String appl = "<?xml version=\"1.0\" encoding=\"utf-8\" ?>"
                + "<Publication Version=\"4.4.0\" xmlns=\"http://ap.org/schemas/03/2005/appl\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">"
                + "<DescriptiveMetadata>"
                + "<EntityClassification Authority=\"AP Event\">"
                + "<Occurrence Id=\"a1\" Value=\"v1\" />"
                + "<Occurrence Id=\"a2\" />"
                + "</EntityClassification>"
                + "<EntityClassification Authority=\"AP Organization\" System=\"Teragram\">"
                + "<Occurrence Id=\"a1\" Value=\"v1\" ParentId=\"p1\" ActualMatch=\"true\" />"
                + "<Occurrence Id=\"a2\" />"
                + "</EntityClassification>"
                + "<EntityClassification Authority=\"AP Company\" System=\"Teragram\">"
                + "<Occurrence Id=\"MSFT\" Value=\"Microsoft\" />"
                + "</EntityClassification>"
                + "<EntityClassification Authority=\"AP Company\" System=\"Teragram\">"
                + "<Occurrence Id=\"a1\" Value=\"Microsoft\">"
                + "<Property Name=\"Instrument\" Value=\"NASDAQ:MSFT\" />"
                + "<Property Name=\"Instrument\" Value=\"NASDAQ:APPL\" />"
                + "<Property Name=\"APIndustry\" Value=\"Software\" Id=\"a1\" />"
                + "<Property Name=\"Ticker\" Value=\"XYZ\" ParentId=\"a1\" />"
                + "<Property Name=\"PrimaryTicker\" Value=\"XYZ\" ParentId=\"b1\" />"
                + "<Property Name=\"PrimaryTicker\" Value=\"ABC\" ParentId=\"a1\" />"
                + "<Property Name=\"Exchange\" Value=\"NYCE\" Id=\"a1\" />"
                + "<Property Name=\"Exchange\" Value=\"SPIDER\" Id=\"b1\" />"
                + "</Occurrence>"
                + "<Occurrence Id=\"a1\" Value=\"Microsoft\">"
                + "<Property Name=\"Instrument\" Value=\"RXE:MSFT\" />"
                + "<Property Name=\"APIndustry\" Value=\"Hardware\" Id=\"b1\" />"
                + "<Property Name=\"Ticker\" Value=\"XYZ\" ParentId=\"c1\" />"
                + "<Property Name=\"Exchange\" Value=\"RXE\" Id=\"c1\" />"
                + "</Occurrence>"
                + "</EntityClassification>"
                + "</DescriptiveMetadata>"
                + "</Publication>";


        DocumentParser parser = new DocumentParser();
        String json = parser.parse(appl);

        ObjectMapper m = new ObjectMapper();
        JsonNode rootNode = m.readTree(json);
        JsonNode testNode = rootNode.path("events");
        assertEquals(true, testNode.isArray());
        assertEquals(1, testNode.size());

        Iterator<JsonNode> elements = testNode.elements();

        JsonNode next = elements.next();
        assertEquals("v1", next.get("name").asText());
        assertEquals("a1", next.get("code").asText());

        testNode = rootNode.path("organizations");
        assertEquals(true, testNode.isArray());
        assertEquals(1, testNode.size());

        elements = testNode.elements();

        next = elements.next();
        assertEquals("v1", next.get("name").asText());
        assertEquals("a1", next.get("code").asText());
        assertEquals("Teragram", next.get("creator").asText());
        JsonNode array = next.get("rels");
        assertEquals(true, array.isArray());
        assertEquals(1, array.size());
        assertEquals("direct", array.elements().next().asText());
        array=next.get("parentids");
        assertEquals(true, array.isArray());
        assertEquals(1, array.size());
        assertEquals("p1", array.elements().next().asText());

        testNode = rootNode.path("companies");
        assertEquals(true, testNode.isArray());
        assertEquals(2, testNode.size());

        elements = testNode.elements();

        next = elements.next();
        assertEquals("Microsoft", next.get("name").asText());
        assertEquals("MSFT", next.get("code").asText());
        assertEquals("Teragram", next.get("creator").asText());
        array = next.get("rels");
        assertEquals(true, array.isArray());
        assertEquals(1, array.size());
        assertEquals("direct", array.elements().next().asText());

        next = elements.next();
        assertEquals("Microsoft", next.get("name").asText());
        assertEquals("a1", next.get("code").asText());
        assertEquals("Teragram", next.get("creator").asText());
        array = next.get("rels");
        assertEquals(true, array.isArray());
        assertEquals(1, array.size());
        assertEquals("direct", array.elements().next().asText());

        array = next.get("symbols");
        assertEquals(true, array.isArray());
        assertEquals(7, array.size());
        assertEquals("NASDAQ:MSFT", array.elements().next().get("instrument").asText());

        array = next.get("industries");
        assertEquals(true, array.isArray());
        assertEquals(2, array.size());
        assertEquals("Software", array.elements().next().get("name").asText());
    }
}
