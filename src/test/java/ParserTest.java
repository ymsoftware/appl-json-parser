

import applparser.DocumentParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.Test;

import javax.xml.stream.XMLStreamException;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static org.junit.Assert.assertEquals;

/**
 * Created by ymetelkin on 8/12/15.
 */
public class ParserTest {
    static final String PROTEUS_URL = "http://proteus.int.esclient.uno.proteusa.com:9200/appl/doc/_search?";
    static final String APPL_URL = "http://catalogapiqa.ap.org/AP.MessageRepository.APIHost/Services/MessageRepository.svc/documents/";

    @Test
    public void test() throws IOException {
        DocumentParser parser = new DocumentParser();
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.INDENT_OUTPUT, true);

        Map<String, Integer> queries = new LinkedHashMap<>();
        queries.put("q=type:text", 100);
        queries.put("q=type:photo", 100);
        queries.put("q=type:video", 100);
        queries.put("q=type:audio", 10);
        queries.put("q=type:graphic", 100);
        queries.put("q=type:complexdata", 10);
        queries.put("q=_exists_:itemid", 10000);

        int start = 0;

        for (String query : queries.keySet()) {
            for (int i = start; i < (Integer) queries.get(query); i++) {
                String q = i == 0 ? query : String.format("%s&from=%d", query, i * 10);
                System.out.println(String.format("\nTesting %s\n=====================================", q));
                String json = urlToString(PROTEUS_URL + q);

                JsonNode hits = mapper.readTree(json).path("hits").path("hits");
                Iterator<JsonNode> iterator = hits.elements();
                while (iterator.hasNext()) {
                    boolean success = true;

                    JsonNode hit = iterator.next().get("_source");
                    String itemId = hit.get("itemid").asText();
                    String appl = urlToString(APPL_URL + itemId);

                    System.out.println(String.format("Testing %s", itemId));

                    try {
                        json = parser.parse(appl);
                        JsonNode test = mapper.readTree(json);
                        if (!testNode(test, hit)) {
                            success = false;
                            System.out.println("test != hit");
                            saveError(itemId, hit, appl);
                        }
                        if (!testNode(hit, test)) {
                            success = false;
                            System.out.println("hit != test");
                            saveError(itemId, hit, appl);
                        }
                    } catch (Exception e) {
                        success = false;
                        saveError(itemId, hit, appl);
                    }

                    assertEquals(true, success);
                }
            }
        }
    }

    @Test
    public void testFiles() throws IOException, XMLStreamException {
        DocumentParser parser = new DocumentParser();
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.INDENT_OUTPUT, true);

        for (String file : new File("tests").list()) {
            if (file.endsWith(".xml")) {
                byte[] bytes = Files.readAllBytes(Paths.get("tests/" + file));
                String appl = new String(bytes);
                String json = parser.parse(appl);
                JsonNode test = mapper.readTree(json);
                JsonNode hit = mapper.readTree(new String(Files.readAllBytes(Paths.get("tests/" + file.replace(".xml", ".json")))));

                boolean success = true;

                if (!testNode(test, hit)) {
                    success = false;
                    System.out.println("test != hit");
                }

                if (!testNode(hit, test)) {
                    success = false;
                    System.out.println("hit != test");
                }

                assertEquals(true, success);
            }
        }
    }

    private boolean testNode(JsonNode node, JsonNode test, boolean output) {
        if (node == null || test == null) {
            return false;
        }

        if (node.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                String key = field.getKey();
                boolean isEqual = true;

                if (key.equals("nitf")) {
                    int l1 = field.getValue().asText().length();
                    int l2 = test.get(key).asText().length();
                    int diff = l1 > l2 ? l1 - l2 : l2 - l1;
                    isEqual = diff / l1 < 0.05;
                } else {
                    isEqual = testNode(field.getValue(), test.get(key));
                }

                if (!isEqual) {
                    if (output) {
                        System.out.println(String.format("%s: %s == %s => failure", key, field.getValue(), test.get(key)));
                    }
                    return false;
                }
            }
        } else if (node.isArray()) {
            if (test == null || !test.isArray()) {
                return false;
            }
            List<JsonNode> list1 = getList(node);
            List<JsonNode> list2 = getList(test);
            if (list1.size() != list2.size()) {
                return false;
            }

            for (JsonNode nd1 : list1) {
                boolean match = false;

                for (JsonNode nd2 : list2) {
                    if (testNode(nd1, nd2, false)) {
                        match = true;
                        break;
                    }
                }

                if (!match) {
                    return false;
                }
            }

        } else if (node.isTextual()) {
            String text1 = node.asText().replaceAll("\\n", "").trim();
            String text2 = test.asText().replaceAll("\\n", "").trim();
            return text1.equals(text2);
        } else if (node.isBoolean()) {
            return node.asBoolean() == test.asBoolean();
        } else if (node.isInt()) {
            return node.asInt() == test.asInt();
        } else if (node.isLong()) {
            return node.asLong() == test.asLong();
        } else if (node.isDouble() || node.isFloat()) {
            Double d1 = node.asDouble();
            Double d2 = test.asDouble();
            Double diff = d1 > d2 ? d1 - d2 : d2 - d1;
            return (d1 == 0 && d2 == 0) || diff / d1 < 0.01;
        } else {
            String debug = "WTF!";
        }

        return true;
    }

    private boolean testNode(JsonNode node, JsonNode test) {
        return testNode(node, test, true);
    }

    private List<JsonNode> getList(JsonNode node) {
        List<JsonNode> list = new ArrayList<>();
        Iterator<JsonNode> elements = node.elements();
        while (elements.hasNext()) {
            list.add(elements.next());
        }

        return list;
    }

    private void saveError(String itemId, JsonNode hit, String appl) throws FileNotFoundException, JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        String json = mapper.writeValueAsString(hit);

        PrintWriter out = new PrintWriter("tests/" + itemId + ".json");
        out.print(json);
        out.close();

        out = new PrintWriter("tests/" + itemId + ".xml");
        out.print(appl);
        out.close();
    }

    private String urlToString(String url) throws IOException {
        URL website = new URL(url);
        URLConnection connection = website.openConnection();
        BufferedReader in = new BufferedReader(
                new InputStreamReader(
                        connection.getInputStream()));

        StringBuilder response = new StringBuilder();
        String inputLine;

        while ((inputLine = in.readLine()) != null)
            response.append(inputLine);

        in.close();

        return response.toString();
    }
}
