import applparser.DocumentParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.io.IOException;
import java.util.Iterator;

import static org.junit.Assert.assertEquals;

/**
 * Created by ymetelkin on 8/7/15.
 */
public class PublicationComponentTest {
    @Test
    public void testText() throws IOException {
        String appl = "<?xml version=\"1.0\" encoding=\"utf-8\" ?>"
                + "<Publication Version=\"4.4.0\" xmlns=\"http://ap.org/schemas/03/2005/appl\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">"
                + "<AdministrativeMetadata />"
                + "<NewsLines />"
                + "<DescriptiveMetadata />"
                + "<PublicationComponent Role=\"Main\" MediaType=\"Text\">"
                + "<TextContentItem>"
                + "<DataContent>"
                + "<nitf>"
                + "<body.content>"
                + "<block>"
                + "<p>Exal greak cherez <a href=\"#\">reku</a></p>"
                + "<p>Vidit greka v reke <block>rak</block></p>"
                + "</block>"
                + "</body.content>"
                + "</nitf>"
                + "</DataContent>"
                + "<Characteristics><Words>9</Words></Characteristics>"
                + "</TextContentItem>"
                + "</PublicationComponent>"
                + "<PublicationComponent Role=\"Caption\" MediaType=\"Text\">"
                + "<TextContentItem>"
                + "<DataContent>"
                + "<nitf>"
                + "<body.content>"
                + "<block>Exal greaka cherez reku</block>"
                + "</body.content>"
                + "</nitf>"
                + "</DataContent>"
                + "<Characteristics><Words>4</Words></Characteristics>"
                + "</TextContentItem>"
                + "</PublicationComponent>"
                + "<PublicationComponent Role=\"Caption\" MediaType=\"Text\">"
                + "<TextContentItem>"
                + "<DataContent>"
                + "<nitf>"
                + "<body.content>"
                + "<block>second caption</block>"
                + "</body.content>"
                + "</nitf>"
                + "</DataContent>"
                + "<Characteristics><Words>2</Words></Characteristics>"
                + "</TextContentItem>"
                + "</PublicationComponent>"
                + "</Publication>";

        DocumentParser parser = new DocumentParser();
        String json = parser.parse(appl);

        ObjectMapper m = new ObjectMapper();
        JsonNode rootNode = m.readTree(json);
        JsonNode testNode = rootNode.path("main");
        assertEquals(true, testNode.get("nitf").asText().length()>0);
        assertEquals(9, testNode.get("words").asInt());

        testNode = rootNode.path("caption");
        assertEquals("Exal greaka cherez reku", testNode.get("nitf").asText());
        assertEquals(4, testNode.get("words").asInt());
    }
}
