import applparser.DocumentParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 * Created by ymetelkin on 7/28/15.
 */
public class NewsLinesTest {
    @Test
    public void test() throws IOException {
        String appl = "<?xml version=\"1.0\" encoding=\"utf-8\" ?>"
                + "<Publication>"
                + "<Identification>"
                + "<MediaType>Text</MediaType>"
                + "</Identification>"
                + "<NewsLines>"
                + "<ExtendedHeadLine>Extended HeadLine</ExtendedHeadLine>"
                + "<OriginalHeadLine>Original HeadLine</OriginalHeadLine>"
                + "<HeadLine>HeadLine</HeadLine>"
                + "<Title>Title</Title>"
                + "<ByLine Title=\"YM\">By Yuri Metelkin</ByLine>"
                + "<ByLine Title=\"AP\">By AP</ByLine>"
                + "<ByLineOriginal Title=\"AB\">By Yuri Metelkin</ByLineOriginal>"
                + "<DateLine>DateLine</DateLine>"
                + "<OverLine>OverLine 1</OverLine>"
                + "<OverLine>OverLine 2</OverLine>"
                + "<CreditLine Id=\"AP\" />"
                + "<CopyrightLine>Copyright notice</CopyrightLine>"
                + "<KeywordLine>KeywordLine 1</KeywordLine>"
                + "<KeywordLine>KeywordLine 2</KeywordLine>"
                + "<NameLine Parametric=\"PERSON_FEATURED\">YM</NameLine>"
                + "<NameLine>AB</NameLine>"
                + "</NewsLines>"
                + "</Publication>";

        DocumentParser parser = new DocumentParser();
        String json = parser.parse(appl);

        ObjectMapper m = new ObjectMapper();
        JsonNode rootNode = m.readTree(json);
        JsonNode testNode = rootNode.path("title");
        assertEquals("HeadLine", testNode.asText());

        testNode = rootNode.path("headline");
        assertEquals("Extended HeadLine", testNode.asText());

        testNode = rootNode.path("bylines");
        assertEquals(true, testNode.isArray());
        assertEquals(1, testNode.size());
        assertEquals("AB", testNode.elements().next().get("title").asText());

        testNode = rootNode.path("dateline");
        assertEquals("DateLine", testNode.asText());

        testNode = rootNode.path("overlines");
        assertEquals(true, testNode.isArray());
        assertEquals(2, testNode.size());
        assertEquals("OverLine 1", testNode.elements().next().asText());

        testNode = rootNode.path("creditlineid");
        assertEquals("AP", testNode.asText());

        testNode = rootNode.path("copyrightnotice");
        assertEquals("Copyright notice", testNode.asText());

        testNode = rootNode.path("keywordlines");
        assertEquals(true, testNode.isArray());
        assertEquals(2, testNode.size());
        assertEquals("KeywordLine 1", testNode.elements().next().asText());

        testNode = rootNode.path("persons");
        assertEquals(true, testNode.isArray());
        assertEquals(1, testNode.size());
        assertEquals("YM", testNode.elements().next().get("name").asText());
    }

    @Test
    public void testHeadlineAndTitle() throws IOException {
        String appl = "<?xml version=\"1.0\" encoding=\"utf-8\" ?>"
                + "<Publication>"
                + "<Identification>"
                + "<MediaType>Text</MediaType>"
                + "</Identification>"
                + "<NewsLines>"
                + "<ExtendedHeadLine>Extended HeadLine</ExtendedHeadLine>"
                + "<OriginalHeadLine>Original HeadLine</OriginalHeadLine>"
                + "<HeadLine>HeadLine</HeadLine>"
                + "<Title>Title</Title>"
                + "</NewsLines>"
                + "</Publication>";

        DocumentParser parser = new DocumentParser();
        String json = parser.parse(appl);

        ObjectMapper m = new ObjectMapper();
        JsonNode rootNode = m.readTree(json);
        JsonNode testNode = rootNode.path("title");
        assertEquals("HeadLine", testNode.asText());
        testNode = rootNode.path("headline");
        assertEquals("Extended HeadLine", testNode.asText());

        appl = "<?xml version=\"1.0\" encoding=\"utf-8\" ?>"
                + "<Publication>"
                + "<Identification>"
                + "<MediaType>Text</MediaType>"
                + "</Identification>"
                + "<NewsLines>"
                + "<OriginalHeadLine>Original HeadLine</OriginalHeadLine>"
                + "<HeadLine>HeadLine</HeadLine>"
                + "<Title>Title</Title>"
                + "</NewsLines>"
                + "</Publication>";

        parser = new DocumentParser();
        json = parser.parse(appl);

        m = new ObjectMapper();
        rootNode = m.readTree(json);
        testNode = rootNode.path("title");
        assertEquals("HeadLine", testNode.asText());

        testNode = rootNode.path("headline");
        assertEquals("Original HeadLine", testNode.asText());

        appl = "<?xml version=\"1.0\" encoding=\"utf-8\" ?>"
                + "<Publication>"
                + "<Identification>"
                + "<MediaType>Text</MediaType>"
                + "</Identification>"
                + "<NewsLines>"
                + "<HeadLine>HeadLine</HeadLine>"
                + "<Title>Title</Title>"
                + "</NewsLines>"
                + "</Publication>";

        parser = new DocumentParser();
        json = parser.parse(appl);

        m = new ObjectMapper();
        rootNode = m.readTree(json);
        testNode = rootNode.path("title");
        assertEquals("Title", testNode.asText());

        testNode = rootNode.path("headline");
        assertEquals("HeadLine", testNode.asText());

        appl = "<?xml version=\"1.0\" encoding=\"utf-8\" ?>"
                + "<Publication>"
                + "<Identification>"
                + "<MediaType>Video</MediaType>"
                + "</Identification>"
                + "<NewsLines>"
                + "<HeadLine>HeadLine</HeadLine>"
                + "<Title>Title</Title>"
                + "</NewsLines>"
                + "</Publication>";

        parser = new DocumentParser();
        json = parser.parse(appl);

        m = new ObjectMapper();
        rootNode = m.readTree(json);
        testNode = rootNode.path("title");
        assertEquals("Title", testNode.asText());

        testNode = rootNode.path("headline");
        assertEquals("HeadLine", testNode.asText());
    }

    @Test
    public void testBylines() throws IOException {
        String appl = "<?xml version=\"1.0\" encoding=\"utf-8\" ?>"
                + "<Publication>"
                + "<NewsLines>"
                + "<ByLine Title=\"YM\">By Yuri Metelkin</ByLine>"
                + "<ByLine Title=\"AP\">By AP</ByLine>"
                + "<ByLineOriginal Title=\"AB\">By Yuri Metelkin</ByLineOriginal>"
                + "</NewsLines>"
                + "</Publication>";

        DocumentParser parser = new DocumentParser();
        String json = parser.parse(appl);

        ObjectMapper m = new ObjectMapper();
        JsonNode rootNode = m.readTree(json);
        JsonNode testNode = rootNode.path("bylines");
        assertEquals(true, testNode.isArray());
        assertEquals(1, testNode.size());
        assertEquals("AB", testNode.elements().next().get("title").asText());

        appl = "<?xml version=\"1.0\" encoding=\"utf-8\" ?>"
                + "<Publication>"
                + "<NewsLines>"
                + "<ByLine Title=\"YM\">By Yuri Metelkin</ByLine>"
                + "<ByLine Title=\"AP\">By AP</ByLine>"
                + "<ByLineOriginal>By Yuri Metelkin</ByLineOriginal>"
                + "</NewsLines>"
                + "</Publication>";

        parser = new DocumentParser();
        json = parser.parse(appl);

        m = new ObjectMapper();
        rootNode = m.readTree(json);
        testNode = rootNode.path("bylines");
        assertEquals(true, testNode.isArray());
        assertEquals(1, testNode.size());
        assertEquals("YM", testNode.elements().next().get("title").asText());

        appl = "<?xml version=\"1.0\" encoding=\"utf-8\" ?>"
                + "<Publication>"
                + "<NewsLines>"
                + "<ByLine Title=\"EditedBy\" Parametric=\"Photographer\">Yuri Metelkin</ByLine>"
                + "<ByLine Title=\"NYC\">Yuri Metelkin</ByLine>"
                + "<ByLine Title=\"YM\" Parametric=\"Photographer\">By Yuri Metelkin</ByLine>"
                + "<ByLine Title=\"YM\" Parametric=\"CAPTIONWRITER\">By Yuri Metelkin</ByLine>"
                + "<ByLine Title=\"YM\" Parametric=\"editedby\">By Yuri Metelkin</ByLine>"
                + "<ByLine Title=\"AP\">By AP</ByLine>"
                + "<ByLineOriginal>By Yuri Metelkin</ByLineOriginal>"
                + "</NewsLines>"
                + "</Publication>";

        parser = new DocumentParser();
        json = parser.parse(appl);

        m = new ObjectMapper();
        rootNode = m.readTree(json);
        testNode = rootNode.path("bylines");
        assertEquals(true, testNode.isArray());
        assertEquals(1, testNode.size());
        assertEquals("NYC", testNode.elements().next().get("title").asText());

        testNode = rootNode.path("producer");
        assertEquals(true, testNode.isObject());
        assertEquals("Yuri Metelkin", testNode.get("name").asText());

        testNode = rootNode.path("photographer");
        assertEquals(true, testNode.isObject());
        assertEquals("YM", testNode.get("title").asText());

        testNode = rootNode.path("captionwriter");
        assertEquals(true, testNode.isObject());
        assertEquals("YM", testNode.get("title").asText());

        testNode = rootNode.path("editor");
        assertEquals(true, testNode.isObject());
        assertEquals("YM", testNode.get("title").asText());
    }
}
