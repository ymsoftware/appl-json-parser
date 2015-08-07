package applparser;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by ymetelkin on 7/29/15.
 */
public class PublicationComponentParser extends ApplParser {
    private String role;
    private String type;
    private StringBuilder text;
    private Integer words;
    private boolean readText;
    private boolean readWords;
    private int blocks;

    public PublicationComponentParser(String role, String type) {
        this.role = role;
        this.type = type;
    }
    //title: caption and main

    @Override
    public void parse(String name, XMLStreamReader xmlr, Map<String, Object> map) throws XMLStreamException {
        if (name.equals("TextContentItem")) {
            if (!map.containsKey(this.role)) {
                setText(xmlr);
            }
        } else {
            if (type.equals("photo")) {

            } else if (type.equals("video")) {

            } else if (type.equals("graphic")) {

            } else if (type.equals("audio")) {

            } else if (type.equals("complexdata")) {

            }
        }
    }

    @Override
    public void cleanup(Map<String, Object> map) {
        if (this.text != null) {
            Map<String, Object> text = new LinkedHashMap<String, Object>();
            text.put("nitf", this.text.toString());
            if (this.words != null) text.put("words", this.words);
            map.put(this.role, text);
        }
    }

    private void setText(XMLStreamReader xmlr) throws XMLStreamException {
        while (xmlr.hasNext()) {
            xmlr.next();

            int eventType = xmlr.getEventType();

            if (eventType == XMLStreamReader.START_ELEMENT) {
                String name = xmlr.getLocalName();

                if (this.readText) {
                    this.text.append("<").append(name);

                    for (int i = 0; i < xmlr.getAttributeCount(); i++) {
                        this.text
                                .append(" ")
                                .append(xmlr.getAttributeLocalName(i))
                                .append("=\"")
                                .append(xmlr.getAttributeValue(i))
                                .append("\"");
                    }

                    this.text.append(">");

                    if (name.equals("block")) {
                        this.blocks += 1;
                    }
                } else {
                    if (name.equals("block")) {
                        this.readText = true;
                        this.blocks = 0;

                        if (this.text == null) {
                            this.text = new StringBuilder();
                        }
                    } else if (name.equals("Characteristics")) {
                        this.readWords = true;
                    } else if (name.equals("Words")) {
                        this.words = Helpers.parseInteger(xmlr.getElementText());
                    }
                }
            } else if (eventType == XMLStreamReader.END_ELEMENT) {
                String name = xmlr.getLocalName();

                if (name.equals("TextContentItem")) {
                    break;
                } else if (name.equals("block")) {
                    if (this.blocks == 0) {
                        this.readText = false;
                    } else {
                        this.text.append("</block>");
                        this.blocks -= 1;
                    }
                } else if (name.equals("Characteristics")) {
                    this.readWords = false;
                } else if (this.readText) {
                    this.text
                            .append("</")
                            .append(name)
                            .append(">");
                }
            } else if (eventType == XMLStreamReader.CHARACTERS && this.readText) {
                this.text.append(xmlr.getText());
            }
        }
    }
}
