import applparser.DocumentParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.util.Iterator;

import static org.junit.Assert.assertEquals;

/**
 * Created by ymetelkin on 8/4/15.
 */
public class AdministrativeMetadataTest {
    @Test
    public void testProvider() throws IOException, XMLStreamException {
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
    public void testSources() throws IOException, XMLStreamException {
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
    public void testSourceMaterials() throws IOException, XMLStreamException {
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

    @Test
    public void testArrays() throws IOException, XMLStreamException {
        String appl = "<?xml version=\"1.0\" encoding=\"utf-8\" ?>"
                + "<Publication Version=\"4.4.0\" xmlns=\"http://ap.org/schemas/03/2005/appl\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">"
                + "<AdministrativeMetadata>"
                + "<TransmissionSource>T1</TransmissionSource>"
                + "<TransmissionSource>T2</TransmissionSource>"
                + "<TransmissionSource>T1</TransmissionSource>"
                + "<ProductSource>P1</ProductSource>"
                + "<ProductSource>P2</ProductSource>"
                + "<ProductSource>P2</ProductSource>"
                + "<DistributionChannel>DC1</DistributionChannel>"
                + "<DistributionChannel>DC2</DistributionChannel>"
                + "<DistributionChannel>DC1</DistributionChannel>"
                + "</AdministrativeMetadata>"
                + "</Publication>";

        DocumentParser parser = new DocumentParser();
        String json = parser.parse(appl);

        ObjectMapper m = new ObjectMapper();
        JsonNode rootNode = m.readTree(json);
        JsonNode testNode = rootNode.path("transmissionsources");
        assertEquals(true, testNode.isArray());
        assertEquals(2, testNode.size());
        assertEquals("T1", testNode.elements().next().asText());

        testNode = rootNode.path("productsources");
        assertEquals(true, testNode.isArray());
        assertEquals(2, testNode.size());
        assertEquals("P1", testNode.elements().next().asText());

        testNode = rootNode.path("distributionchannels");
        assertEquals(true, testNode.isArray());
        assertEquals(2, testNode.size());
        assertEquals("DC1", testNode.elements().next().asText());
    }

    @Test
    public void testInPackages() throws IOException, XMLStreamException {
        String appl = "<?xml version=\"1.0\" encoding=\"utf-8\" ?>"
                + "<Publication Version=\"4.4.0\" xmlns=\"http://ap.org/schemas/03/2005/appl\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">"
                + "<AdministrativeMetadata>"
                + "<InPackage>a</InPackage>"
                + "<InPackage>b c</InPackage>"
                + "<InPackage>c d</InPackage>"
                + "</AdministrativeMetadata>"
                + "</Publication>";

        DocumentParser parser = new DocumentParser();
        String json = parser.parse(appl);

        ObjectMapper m = new ObjectMapper();
        JsonNode rootNode = m.readTree(json);
        JsonNode testNode = rootNode.path("inpackages");
        assertEquals(true, testNode.isArray());
        assertEquals(4, testNode.size());
        assertEquals("a", testNode.elements().next().asText());
    }

    @Test
    public void testRatings() throws IOException, XMLStreamException {
        String appl = "<?xml version=\"1.0\" encoding=\"utf-8\" ?>"
                + "<Publication Version=\"4.4.0\" xmlns=\"http://ap.org/schemas/03/2005/appl\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">"
                + "<AdministrativeMetadata>"
                + "<Rating Value=\"10\" ScaleMin=\"1\" ScaleMax=\"10\" ScaleUnit=\"digit\" Raters=\"5\" RaterType=\"expert\" Creator=\"creator\" />"
                + "<Rating Value=\"7\" ScaleMin=\"1\" ScaleMax=\"10\" ScaleUnit=\"digit\" Raters=\"15\" RaterType=\"expert\" />"
                + "</AdministrativeMetadata>"
                + "</Publication>";

        DocumentParser parser = new DocumentParser();
        String json = parser.parse(appl);

        ObjectMapper m = new ObjectMapper();
        JsonNode rootNode = m.readTree(json);
        JsonNode testNode = rootNode.path("ratings");
        assertEquals(true, testNode.isArray());
        assertEquals(2, testNode.size());

        Iterator<JsonNode> elements = testNode.elements();

        JsonNode next = elements.next();
        assertEquals(10, next.get("rating").asInt());
        assertEquals(1, next.get("scalemin").asInt());
        assertEquals(10, next.get("scalemax").asInt());
        assertEquals(5, next.get("raters").asInt());
        assertEquals("expert", next.get("ratertype").asText());
        assertEquals("creator", next.get("creator").asText());

        next = elements.next();
        assertEquals(7, next.get("rating").asInt());
    }

    @Test
    public void testSignals() throws IOException, XMLStreamException {
        String appl = "<?xml version=\"1.0\" encoding=\"utf-8\" ?>"
                + "<Publication Version=\"4.4.0\" xmlns=\"http://ap.org/schemas/03/2005/appl\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">"
                + "<AdministrativeMetadata>"
                + "<Reach>reach</Reach>"
                + "<Reach>unknown</Reach>"
                + "<Signal>S1</Signal>"
                + "<Signal>S2</Signal>"
                + "</AdministrativeMetadata>"
                + "</Publication>";

        DocumentParser parser = new DocumentParser();
        String json = parser.parse(appl);

        ObjectMapper m = new ObjectMapper();
        JsonNode rootNode = m.readTree(json);
        JsonNode testNode = rootNode.path("signals");
        assertEquals(true, testNode.isArray());
        assertEquals(4, testNode.size());

        Iterator<JsonNode> elements = testNode.elements();

        JsonNode next = elements.next();
        assertEquals("reach", next.asText());

        next = elements.next();
        assertEquals("S1", next.asText());

        next = elements.next();
        assertEquals("S2", next.asText());

        next = elements.next();
        assertEquals("consumerready", next.asText());

        appl = "<?xml version=\"1.0\" encoding=\"utf-8\" ?>"
                + "<Publication Version=\"4.4.0\" xmlns=\"http://ap.org/schemas/03/2005/appl\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">"
                + "<AdministrativeMetadata>"
                + "<Reach>reach</Reach>"
                + "<Reach>unknown</Reach>"
                + "<Signal>S1</Signal>"
                + "<Signal>S2</Signal>"
                + "<ConsumerReady>S2</ConsumerReady>"
                + "</AdministrativeMetadata>"
                + "</Publication>";

        parser = new DocumentParser();
        json = parser.parse(appl);

        m = new ObjectMapper();
        rootNode = m.readTree(json);
        testNode = rootNode.path("signals");
        assertEquals(true, testNode.isArray());
        assertEquals(3, testNode.size());

        elements = testNode.elements();

        next = elements.next();
        assertEquals("reach", next.asText());

        next = elements.next();
        assertEquals("S1", next.asText());

        next = elements.next();
        assertEquals("S2", next.asText());

        appl = "<?xml version=\"1.0\" encoding=\"utf-8\" ?>"
                + "<Publication Version=\"4.4.0\" xmlns=\"http://ap.org/schemas/03/2005/appl\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">"
                + "<PublicationManagement>"
                + "<ExplicitWarning>1</ExplicitWarning>"
                + "<IsDigitized>false</IsDigitized>"
                + "</PublicationManagement>"
                + "<AdministrativeMetadata>"
                + "<Reach>reach</Reach>"
                + "<Reach>unknown</Reach>"
                + "<Signal>S1</Signal>"
                + "<Signal>S2</Signal>"
                + "<ConsumerReady>yes</ConsumerReady>"
                + "</AdministrativeMetadata>"
                + "</Publication>";

        parser = new DocumentParser();
        json = parser.parse(appl);

        m = new ObjectMapper();
        rootNode = m.readTree(json);
        testNode = rootNode.path("signals");
        assertEquals(true, testNode.isArray());
        assertEquals(6, testNode.size());

        elements = testNode.elements();

        next = elements.next();
        assertEquals("explicitcontent", next.asText());

        next = elements.next();
        assertEquals("isnotdigitized", next.asText());

        next = elements.next();
        assertEquals("reach", next.asText());

        next = elements.next();
        assertEquals("S1", next.asText());

        next = elements.next();
        assertEquals("S2", next.asText());

        next = elements.next();
        assertEquals("consumerready", next.asText());
    }
}
