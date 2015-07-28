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
