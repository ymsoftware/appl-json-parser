package applparser;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by ymetelkin on 7/29/15.
 */
public class PublicationComponentParser extends ApplParser {
    private String role;
    private String roleRaw;
    private String type;
    private String mediaType;
    private StringBuilder text;
    private Integer words;
    private boolean readText;
    private boolean readWords;
    private int blocks;
    private StringBuilder title;
    private boolean needsTitle;
    private boolean needsHeadline;
    private Map<String, Integer> counts;
    private Map<String, Map<String, Object>> renditions;

    public PublicationComponentParser(String role, String type, Map<String, Object> map) {
        this.roleRaw = role;
        this.role = role.toLowerCase();
        this.type = type;
        this.mediaType = map.containsKey("type") ? (String) map.get("type") : "text";

        boolean isMain = role.equals("main");
        boolean isCaption = role.equals("caption");

        if (isMain || isCaption) {
            if ((isMain && this.mediaType.equals("text")) || (isCaption && (this.mediaType.equals("photo") || this.mediaType.equals("video")))) {
                if (map.containsKey("title")) {
                    if (map.get("title") == null) {
                        this.needsTitle = true;
                    }
                } else {
                    map.put("title", null);
                    this.needsTitle = true;
                }

                if (map.containsKey("headline")) {
                    if (map.get("headline") == null) {
                        this.needsHeadline = true;
                    }
                } else {
                    map.put("headline", null);
                    this.needsHeadline = true;
                }
            }
        }

        if (this.needsTitle || this.needsHeadline) {
            this.title = new StringBuilder();
        }
    }

    @Override
    public void parse(String name, XMLStreamReader xmlr, Map<String, Object> map) throws XMLStreamException {
        if (name.equals("TextContentItem")) {
            if (!map.containsKey(this.role)) {
                setText(xmlr);
            }
        } else {
            if (!map.containsKey("renditions")) {
                map.put("renditions", new LinkedHashMap<String, Map<String, Object>>());
            }

            this.renditions = (Map<String, Map<String, Object>>) map.get("renditions");


            if (type.equals("photo")) {
                setPhoto(name, xmlr);

            } else if (type.equals("video")) {

            } else if (type.equals("graphic")) {

            } else if (type.equals("audio")) {
                setAudio(name, xmlr);
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

            if (this.title != null) {
                String title = this.title.toString().trim().replace("  ", " ");
                String[] tokens = title.split(" ");
                if (tokens.length > 10) {
                    title = Arrays.stream(tokens).limit(10).collect(Collectors.joining(" "));
                }

                if (this.needsTitle) map.replace("title", title);
                if (this.needsHeadline) map.replace("headline", title);
            }

            if (this.renditions != null) {
                map.replace("renditions", this.renditions);
            }
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

                    if (this.title != null) this.title.append(" ");
                }
            } else if (eventType == XMLStreamReader.CHARACTERS && this.readText) {
                String text = xmlr.getText();
                this.text.append(text);
                if (this.title != null) this.title.append(text);
            }
        }
    }

    private void setPhoto(String content, XMLStreamReader xmlr) throws XMLStreamException {
        if (this.role.equals("main")) {
            setRendition(this.role, getContent(content, "Full Resolution (JPG)", xmlr), !this.mediaType.equals("photo"));
        } else if (this.role.equals("preview")) {
            setPhoto("Preview", content, xmlr);
        } else if (this.role.equals("thumbnail")) {
            if (content.equals("PhotoCollectionContentItem")) {

            } else {
                setPhoto("Thumbnail", content, xmlr);
            }
        }
    }

    private void setPhoto(String title, String content, XMLStreamReader xmlr) throws XMLStreamException {
        if (this.mediaType.equals("video")) {
            Map<String, Object> photo = getContent(content, title, xmlr);
            String meta = getBinaryName(photo, null, true, true);
            String name = this.role + meta.replaceAll("-", "").replaceAll(" ", "").toLowerCase();
            title = String.format("%s (%s)", name, meta);
            photo.replace("title", title);
            setRendition(name, photo, true);
        } else {
            setRendition(this.role, getContent(content, title + " (JPG)", xmlr), false);
        }
    }

    private void setAudio(String content, XMLStreamReader xmlr) throws XMLStreamException {
        if (this.role.equals("main")) {
            Map<String, Object> audio = getContent(content, "Audio", xmlr);
            String meta = getBinaryName(audio, null, false, false);
            String name = "main" + meta.replaceAll("-", "").replaceAll(" ", "").toLowerCase();
            String title = String.format("Full Resolution (%s)", meta);
            audio.replace("title", title);
            setRendition(name, audio, false);
        }
    }

    private void setRendition(String name, Map<String, Object> rendition, boolean multiple) {
        if (multiple) {
            if (this.counts == null) {
                this.counts = new HashMap<String, Integer>();
            }

            Integer count = 1;
            if (this.counts.containsKey(name)) {
                count = (Integer) this.counts.get(name) + 1;
                this.counts.replace(name, count);
            } else {
                this.counts.put(name, 1);
            }

            this.renditions.put(String.format("%s%d", name, count), rendition);
        } else {
            if (!this.renditions.containsKey(name)) {
                this.renditions.put(name, rendition);
            }
        }
    }

    private Map<String, Object> getContent(String content, String title, XMLStreamReader xmlr) throws XMLStreamException {
        Map<String, Object> map = new LinkedHashMap<String, Object>();

        map.put("title", title);
        map.put("rel", this.role);
        map.put("type", this.type);

        setAttributes(xmlr, map);

        String tapenumber = null;

        while (xmlr.hasNext()) {
            xmlr.next();

            int eventType = xmlr.getEventType();

            if (eventType == XMLStreamReader.START_ELEMENT) {
                String name = xmlr.getLocalName();
                if (name.equals("ForeignKeys") && tapenumber == null && content.equals("VideoContentItem")) {
                    String system = xmlr.getAttributeValue("", "System");
                    if (system != null && system.equalsIgnoreCase("tape")) {
                        while (xmlr.hasNext() && tapenumber == null) {
                            xmlr.next();

                            eventType = xmlr.getEventType();
                            if (eventType == XMLStreamReader.START_ELEMENT) {
                                if (xmlr.getLocalName().equals("Keys")) {
                                    String field = xmlr.getAttributeValue("", "Field");
                                    if (field != null && field.equalsIgnoreCase("number")) {
                                        String id = xmlr.getAttributeValue("", "Id");
                                        if (id != null) {
                                            tapenumber = id;
                                            map.put("tapenumber", id);
                                            break;
                                        }
                                    }
                                }
                            } else if (eventType == XMLStreamReader.END_ELEMENT) {
                                if (xmlr.getLocalName().equals(name)) {
                                    break;
                                }
                            }
                        }
                    }
                } else if (name.equals("Presentation")) {
                    String system = xmlr.getAttributeValue("", "System");
                    if (system != null) {
                        map.put("presentationsystem", system);
                    }

                    while (xmlr.hasNext()) {
                        xmlr.next();

                        eventType = xmlr.getEventType();
                        if (eventType == XMLStreamReader.START_ELEMENT) {
                            if (xmlr.getLocalName().equals("Characteristics")) {
                                String presentationframe = xmlr.getAttributeValue("", "Frame");
                                if (presentationframe != null) map.put("presentationframe", presentationframe);

                                String presentationframelocation = xmlr.getAttributeValue("", "FrameLocation");
                                if (presentationframelocation != null)
                                    map.put("presentationframelocation", presentationframelocation);
                            }
                        } else if (eventType == XMLStreamReader.END_ELEMENT) {
                            if (xmlr.getLocalName().equals(name)) {
                                break;
                            }
                        }
                    }
                } else if (name.equals("Characteristics")) {
                    setCharacteristics(xmlr, map);
                }
            } else if (eventType == XMLStreamReader.END_ELEMENT) {
                String name = xmlr.getLocalName();

                if (name.equals(content)) {
                    break;
                }
            }
        }

        return map;
    }

    private void setCharacteristics(XMLStreamReader xmlr, Map<String, Object> map) throws XMLStreamException {
        setAttributes(xmlr, map);

        while (xmlr.hasNext()) {
            xmlr.next();

            int eventType = xmlr.getEventType();

            if (eventType == XMLStreamReader.START_ELEMENT) {
                String name = xmlr.getLocalName().toLowerCase();
                if (name.equals("scene")) {
                    String id = xmlr.getAttributeValue("", "Id");
                    if (id != null) map.put("sceneid", id);
                    String value = xmlr.getElementText();
                    if (value != null && value.length() > 0) map.put("scene", value);
                } else {
                    String value = xmlr.getElementText();
                    if (value != null && value.length() > 0) {
                        switch (name) {
                            case "totalduration":
                            case "resolution":
                            case "framerate":
                            case "width":
                            case "height":
                                Number number = Helpers.parseNumber(value);
                                if (number != null) {
                                    map.put(name, number);
                                }
                                break;
                            case "scenes":
                                break;
                            default:
                                map.put(name, value);
                                break;
                        }
                    }
                }
            } else if (eventType == XMLStreamReader.END_ELEMENT) {
                String name = xmlr.getLocalName();

                if (name.equals("Characteristics")) {
                    break;
                }
            }
        }
    }

    private void setAttributes(XMLStreamReader xmlr, Map<String, Object> map) throws XMLStreamException {
        for (int i = 0; i < xmlr.getAttributeCount(); i++) {
            String value = xmlr.getAttributeValue(i);
            String name = xmlr.getAttributeLocalName(i).toLowerCase();
            switch (name) {
                case "id":
                    map.put("code", value);
                    break;
                case "mediatype":
                    map.put("type", value);
                    break;
                case "sizeinbytes":
                    Number number = Helpers.parseNumber(value);
                    if (number != null) {
                        map.put(name, number);
                    }
                    break;
                default:
                    map.put(name, value);
                    break;
            }
        }
    }

    private String getBinaryName(Map<String, Object> map, String ext, boolean getWidth, boolean getHeight) {
        StringBuilder sb = new StringBuilder();

        if (ext == null && map.containsKey("fileextension")) {
            ext = (String) map.get("fileextension");
        }

        if (ext != null) {
            sb.append(ext.toUpperCase());
        }

        if (getWidth && map.containsKey("width")) {
            sb.append(" ").append(map.get("width"));
        }

        if (getHeight && map.containsKey("height")) {
            sb.append("x").append(map.get("height"));
        }

        return sb.toString().trim();
    }
}
