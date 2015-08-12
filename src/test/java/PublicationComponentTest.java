import applparser.DocumentParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.util.Iterator;

import static org.junit.Assert.assertEquals;

/**
 * Created by ymetelkin on 8/7/15.
 */
public class PublicationComponentTest {
    @Test
    public void testText() throws IOException, XMLStreamException {
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
                + "<p>Exal greka cherez <a href=\"#\">reku</a></p>"
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

        testNode = rootNode.path("title");
        assertEquals("Exal greka cherez reku Vidit greka v reke rak", testNode.asText());

        testNode = rootNode.path("headline");
        assertEquals("Exal greka cherez reku Vidit greka v reke rak", testNode.asText());
    }

    @Test
    public void testPhoto() throws IOException, XMLStreamException {
        String appl = "<?xml version=\"1.0\" encoding=\"utf-8\" ?>"
                + "<Publication Version=\"4.4.0\" xmlns=\"http://ap.org/schemas/03/2005/appl\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">"
                + "<Identification>"
                + "<MediaType>Photo</MediaType>"
                + "</Identification>"
                + "<AdministrativeMetadata />"
                + "<NewsLines />"
                + "<DescriptiveMetadata />"
                + "<PublicationComponent Role=\"Main\" MediaType=\"Photo\">"
                + "<PhotoContentItem Id=\"p1\" Href=\"/p1\" BinaryPath=\"File\">"
                + "<BinaryLocation BinaryPath=\"Akamai\">b1</BinaryLocation>"
                + "<Presentations>"
                + "<Presentation System=\"s1\"><Characteristics Frame=\"f1\" FrameLocation=\"fl1\" /></Presentation>"
                + "</Presentations>"
                + "<Characteristics MimeType=\"image/jpeg\" Format=\"JPEG Baseline\" FileExtension=\"jpg\" SizeInBytes=\"8000\">"
                + "<Width>4000</Width>"
                + "<Height>2000</Height>"
                + "</Characteristics>"
                + "</PhotoContentItem>"
                + "</PublicationComponent>"
                + "<PublicationComponent Role=\"Preview\" MediaType=\"Photo\">"
                + "<PhotoContentItem Id=\"p2\" Href=\"/p2\" BinaryPath=\"File\">"
                + "<BinaryLocation BinaryPath=\"Akamai\">b1</BinaryLocation>"
                + "<Characteristics MimeType=\"image/jpeg\" Format=\"JPEG Baseline\" FileExtension=\"jpg\" SizeInBytes=\"4000\">"
                + "<Width>400</Width>"
                + "<Height>200</Height>"
                + "</Characteristics>"
                + "</PhotoContentItem>"
                + "</PublicationComponent>"
                + "<PublicationComponent Role=\"Thumbnail\" MediaType=\"Photo\">"
                + "<PhotoContentItem Id=\"p2\" Href=\"/p2\" BinaryPath=\"File\">"
                + "<BinaryLocation BinaryPath=\"Akamai\">b1</BinaryLocation>"
                + "<Characteristics MimeType=\"image/jpeg\" Format=\"JPEG Baseline\" FileExtension=\"jpg\" SizeInBytes=\"800\">"
                + "<Width>40</Width>"
                + "<Height>20</Height>"
                + "</Characteristics>"
                + "</PhotoContentItem>"
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
        JsonNode testNode = rootNode.path("renditions");
        assertEquals(true, testNode.isArray());
        assertEquals(3, testNode.size());

        Iterator<JsonNode> renditions = testNode.elements();

        JsonNode rendition = renditions.next();
        assertEquals("Full Resolution (JPG)", rendition.get("title").asText());
        assertEquals(4000, rendition.get("width").asInt());
        assertEquals(2000, rendition.get("height").asInt());
        assertEquals("s1", rendition.get("presentationsystem").asText());
        assertEquals("f1", rendition.get("presentationframe").asText());
        assertEquals("fl1", rendition.get("presentationframelocation").asText());

        rendition = renditions.next();
        assertEquals("Preview (JPG)", rendition.get("title").asText());
        assertEquals(400, rendition.get("width").asInt());
        assertEquals(200, rendition.get("height").asInt());

        rendition = renditions.next();
        assertEquals("Thumbnail (JPG)", rendition.get("title").asText());
        assertEquals(40, rendition.get("width").asInt());
        assertEquals(20, rendition.get("height").asInt());
    }

    @Test
    public void testShots() throws IOException, XMLStreamException {
        String appl = "<?xml version=\"1.0\" encoding=\"utf-8\" ?>"
                + "<Publication Version=\"4.4.0\" xmlns=\"http://ap.org/schemas/03/2005/appl\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">"
                + "<Identification>"
                + "<MediaType>Photo</MediaType>"
                + "</Identification>"
                + "<AdministrativeMetadata />"
                + "<NewsLines />"
                + "<DescriptiveMetadata />"
                + "<PublicationComponent Role=\"Thumbnail\" MediaType=\"Photo\">"
                + "<PhotoCollectionContentItem Id=\"p1\" Href=\"/p1\" BinaryPath=\"File\" BaseFileName=\"http://ap.org/thumbnails/1/1_index/0000.jpg\"  PrimaryFileName=\"http://ap.org/thumbnails/1/1_tmb/0000.jpg\">"
                + "<File TimeOffsetMilliseconds=\"360\"></File>"
                + "<File TimeOffsetMilliseconds=\"4360\"></File>"
                + "<File TimeOffsetMilliseconds=\"9000\"></File>"
                + "<File TimeOffsetMilliseconds=\"12300\"></File>"
                + "<Characteristics MimeType=\"image/jpeg\" Format=\"JPEG Baseline\" FileExtension=\"jpg\" SizeInBytes=\"800\">"
                + "<Width>40</Width>"
                + "<Height>20</Height>"
                + "<TotalDuration>15300</TotalDuration>"
                + "</Characteristics>"
                + "</PhotoCollectionContentItem>"
                + "</PublicationComponent>"
                + "</Publication>";

        DocumentParser parser = new DocumentParser();
        String json = parser.parse(appl);

        ObjectMapper m = new ObjectMapper();
        JsonNode rootNode = m.readTree(json);
        JsonNode testNode = rootNode.path("shots");
        assertEquals(true, testNode.isArray());
        assertEquals(4, testNode.size());

        Iterator<JsonNode> renditions = testNode.elements();

        JsonNode rendition = renditions.next();
        assertEquals(1, rendition.get("seq").asInt());
        assertEquals("http://ap.org/thumbnails/1/1_index/0000.jpg", rendition.get("href").asText());
        assertEquals(40, rendition.get("width").asInt());
        assertEquals(20, rendition.get("height").asInt());

        rendition = renditions.next();
        assertEquals(2, rendition.get("seq").asInt());
        assertEquals("http://ap.org/thumbnails/1/1_index/0001.jpg", rendition.get("href").asText());
        assertEquals(40, rendition.get("width").asInt());
        assertEquals(20, rendition.get("height").asInt());
    }
}
