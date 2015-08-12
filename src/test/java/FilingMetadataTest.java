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
public class FilingMetadataTest {
    @Test
    public void test() throws IOException, XMLStreamException {
        String appl = "<?xml version=\"1.0\" encoding=\"utf-8\" ?>"
                + "<Publication Version=\"4.4.0\" xmlns=\"http://ap.org/schemas/03/2005/appl\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">"
                + "<AdministrativeMetadata />"
                + "<NewsLines />"
                + "<DescriptiveMetadata />"
                + "<FilingMetadata>"
                + "<Id>f1</Id>"
                + "<Source>s1</Source>"
                + "<Category>c1</Category>"
                + "<Format>Text</Format>"
                + "<SlugLine>test</SlugLine>"
                + "<FilingCountry>Norway</FilingCountry>"
                + "<FilingCountry>Sweden</FilingCountry>"
                + "<FilingCountry>Finland</FilingCountry>"
                + "<FilingTopic>t1</FilingTopic>"
                + "<FilingTopic>t2</FilingTopic>"
                + "<FilingTopic>t1</FilingTopic>"
                + "<Products>"
                + "<Product>1</Product>"
                + "<Product>2</Product>"
                + "<Product>10001</Product>"
                + "<Product>1</Product>"
                + "</Products>"
                + "<FilingSubject>s1</FilingSubject>"
                + "<FilingSubject>s2</FilingSubject>"
                + "<FilingSubSubject>s3</FilingSubSubject>"
                + "<FilingSubSubject>s1</FilingSubSubject>"
                + "<ForeignKeys System=\"FK1\">"
                + "<Keys Id=\"k1\" Field=\"f1\" />"
                + "<Keys Id=\"k2\" Field=\"f2\" />"
                + "</ForeignKeys>"
                + "<ForeignKeys System=\"fk2\">"
                + "<Keys Id=\"k1\" Field=\"f1\" />"
                + "</ForeignKeys>"
                + "<Routing Type=\"Poem\" Expanded=\"false\" Outed=\"false\">exal greka cherez reku</Routing>"
                + "<Routing Type=\"Poem\" Expanded=\"true\" Outed=\"false\">vidit greka v reke rak</Routing>"
                + "<Routing Type=\"Poem\" Expanded=\"false\" Outed=\"true\">sunul greka v reku ruku</Routing>"
                + "<Routing Type=\"Poem\" Expanded=\"true\" Outed=\"true\">rak za ruku greka zap</Routing>"
                + "</FilingMetadata>"
                + "<FilingMetadata>"
                + "<Id>f2</Id>"
                + "<Source>s2</Source>"
                + "<Category>s</Category>"
                + "<Format>Photo</Format>"
                + "<SlugLine>test</SlugLine>"
                + "<FilingCountry>USA</FilingCountry>"
                + "<FilingCountry>Canada</FilingCountry>"
                + "<FilingCountry>Mexico</FilingCountry>"
                + "</FilingMetadata>"
                + "<FilingMetadata>"
                + "<Id>f3</Id>"
                + "<Source>nj</Source>"
                + "<Category>n</Category>"
                + "</FilingMetadata>"
                + "</Publication>";

        DocumentParser parser = new DocumentParser();
        String json = parser.parse(appl);

        ObjectMapper m = new ObjectMapper();
        JsonNode rootNode = m.readTree(json);
        JsonNode testNode = rootNode.path("filings");
        assertEquals(true, testNode.isArray());
        assertEquals(3, testNode.size());

        Iterator<JsonNode> elements = testNode.elements();

        JsonNode filing = elements.next();
        assertEquals("f1", filing.get("filingid").asText());
        assertEquals("s1", filing.get("filingsource").asText());
        assertEquals("c1", filing.get("filingcategory").asText());
        assertEquals("Text", filing.get("format").asText());
        assertEquals("test", filing.get("slugline").asText());
        JsonNode array = filing.get("filingcountries");
        assertEquals(true, array.isArray());
        assertEquals(3, array.size());
        assertEquals("Norway", array.elements().next().asText());
        array = filing.get("filingtopics");
        assertEquals(true, array.isArray());
        assertEquals(2, array.size());
        assertEquals("t1", array.elements().next().asText());
        array = filing.get("products");
        assertEquals(true, array.isArray());
        assertEquals(3, array.size());
        assertEquals(1, array.elements().next().asInt());
        array = filing.get("filingsubjects");
        assertEquals(true, array.isArray());
        assertEquals(3, array.size());
        assertEquals("s1", array.elements().next().asText());
        array = filing.get("foreignkeys");
        assertEquals(true, array.isArray());
        assertEquals(3, array.size());
        assertEquals("k1", array.elements().next().get("fk1f1").asText());

        JsonNode routings = filing.get("routings");
        array = routings.get("poemadds");
        assertEquals(true, array.isArray());
        assertEquals(4, array.size());
        assertEquals("exal", array.elements().next().asText());
        array = routings.get("expandedpoemadds");
        assertEquals(true, array.isArray());
        assertEquals(5, array.size());
        assertEquals("vidit", array.elements().next().asText());
        array = routings.get("poemouts");
        assertEquals(true, array.isArray());
        assertEquals(5, array.size());
        assertEquals("sunul", array.elements().next().asText());
        array = routings.get("expandedpoemouts");
        assertEquals(true, array.isArray());
        assertEquals(5, array.size());
        assertEquals("rak", array.elements().next().asText());

        filing = elements.next();
        assertEquals("f2", filing.get("filingid").asText());
        assertEquals("s2", filing.get("filingsource").asText());
        assertEquals("s", filing.get("filingcategory").asText());
        assertEquals("Photo", filing.get("format").asText());
        assertEquals("test", filing.get("slugline").asText());
        array = filing.get("filingcountries");
        assertEquals(true, array.isArray());
        assertEquals(3, array.size());
        assertEquals("USA", array.elements().next().asText());

        assertEquals("test", rootNode.get("title").asText());

        array = rootNode.get("signals");
        assertEquals(true, array.isArray());
        assertEquals(1, array.size());
        assertEquals("consumerready", array.elements().next().asText());

        array = rootNode.get("audiences");
        assertEquals(true, array.isArray());
        assertEquals(1, array.size());
        assertEquals("New Jersey", array.elements().next().get("name").asText());
    }
}
