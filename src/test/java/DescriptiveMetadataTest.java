import applparser.DocumentParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.io.IOException;

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
}
