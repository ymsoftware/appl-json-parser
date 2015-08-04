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
public class AdministrativeMetadataTest {
    @Test
    public void testProvider() throws IOException {
        String appl = "<?xml version=\"1.0\" encoding=\"utf-8\" ?>"
                + "<Publication Version=\"4.4.0\" xmlns=\"http://ap.org/schemas/03/2005/appl\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">"
                + "<AdministrativeMetadata>"
                + "<Provider Id=\"abc\" Type=\"type\" SubType=\"subtype\">YM</Provider>"
                + "</AdministrativeMetadata>"
                + "</Publication>";

        DocumentParser parser = new DocumentParser();
        String json = parser.parse(appl);

        ObjectMapper m = new ObjectMapper();
        JsonNode rootNode = m.readTree(json);
        JsonNode testNode = rootNode.path("provider");
        assertEquals("abc", testNode.get("code").asText());
        assertEquals("type", testNode.get("type").asText());
        assertEquals("subtype", testNode.get("subtype").asText());
        assertEquals("YM", testNode.get("name").asText());
    }

    @Test
    public void testSources() throws IOException {
        String appl = "<?xml version=\"1.0\" encoding=\"utf-8\" ?>"
                + "<Publication Version=\"4.4.0\" xmlns=\"http://ap.org/schemas/03/2005/appl\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">"
                + "<AdministrativeMetadata>"
                + "<Source City=\"Moscow\" Country=\"Russia\" CountryArea=\"RUS\" Id=\"abc\" Url=\"url\" Type=\"type\" SubType=\"subtype\">YM</Source>"
                + "</AdministrativeMetadata>"
                + "</Publication>";

        DocumentParser parser = new DocumentParser();
        String json = parser.parse(appl);

        ObjectMapper m = new ObjectMapper();
        JsonNode rootNode = m.readTree(json);
        JsonNode testNode = rootNode.path("sources");
        assertEquals(true, testNode.isArray());
        assertEquals(1, testNode.size());

        Iterator<JsonNode> elements = testNode.elements();

        JsonNode next = elements.next();
        assertEquals("Moscow", next.get("city").asText());
        assertEquals("Russia", next.get("country").asText());
        assertEquals("RUS", next.get("countryarea").asText());
        assertEquals("abc", next.get("code").asText());
        assertEquals("url", next.get("url").asText());
        assertEquals("type", next.get("type").asText());
        assertEquals("subtype", next.get("subtype").asText());
        assertEquals("YM", next.get("name").asText());
    }

    @Test
    public void testSourceMaterials() throws IOException {
        String appl = "<?xml version=\"1.0\" encoding=\"utf-8\" ?>"
                + "<Publication Version=\"4.4.0\" xmlns=\"http://ap.org/schemas/03/2005/appl\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">"
                + "<AdministrativeMetadata>"
                + "<SourceMaterial Name=\"Alternate\"><Url>canonicalurl</Url></SourceMaterial>"
                + "<SourceMaterial Name=\"Alternate\"><Url>url</Url></SourceMaterial>"
                + "<SourceMaterial Name=\"YM\" Id=\"abc\"><Url>url</Url><Type>type</Type><PermissionGranted>pg</PermissionGranted></SourceMaterial>"
                + "</AdministrativeMetadata>"
                + "</Publication>";

        DocumentParser parser = new DocumentParser();
        String json = parser.parse(appl);

        ObjectMapper m = new ObjectMapper();
        JsonNode rootNode = m.readTree(json);
        JsonNode testNode = rootNode.path("canonicallink");
        assertEquals("canonicalurl", testNode.asText());

        testNode = rootNode.path("sourcematerials");
        assertEquals(true, testNode.isArray());
        assertEquals(1, testNode.size());

        Iterator<JsonNode> elements = testNode.elements();

        JsonNode next = elements.next();
        assertEquals("abc", next.get("code").asText());
        assertEquals("url", next.get("url").asText());
        assertEquals("type", next.get("type").asText());
        assertEquals("YM", next.get("name").asText());
        assertEquals("pg", next.get("permissiongranted").asText());
    }
}
