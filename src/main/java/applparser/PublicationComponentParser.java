package applparser;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.text.SimpleDateFormat;
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
    private boolean hasText;
    private StringBuilder title;
    private boolean needsTitle;
    private boolean needsHeadline;
    private Map<String, Object> matte;
    private List<Map<String, Object>> parts;
    private List<Long> offsets;
    private List<Map<String, Object>> shots;
    private Map<String, Object> parent;

    public PublicationComponentParser(String role, String type, Map<String, Object> map) {
        this.roleRaw = role;
        this.role = role.toLowerCase();
        this.type = type;
        this.parent = map;
        this.mediaType = map.containsKey("type") ? (String) map.get("type") : "text";

        boolean isMain = this.role.equals("main");
        boolean isCaption = this.role.equals("caption");

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
            if (type.equals("photo")) {
                setPhoto(name, xmlr);
            } else if (type.equals("video")) {
                setVideo(name, xmlr);
            } else if (type.equals("graphic")) {
                setGraphic(name, xmlr);
            } else if (type.equals("audio")) {
                setAudio(name, xmlr);
            } else if (type.equals("complexdata")) {
                setComplexData(name, xmlr);
            }
        }
    }

    @Override
    public void cleanup(Map<String, Object> map) {
        if (this.hasText) {
            Map<String, Object> text = new LinkedHashMap<String, Object>();
            text.put("nitf", this.text.toString().trim());
            if (this.words != null) text.put("words", this.words);
            map.put(this.role, text);
        }

        if (this.title != null) {
            String title = this.title.toString().trim().replace("  ", " ");
            String[] tokens = title.split(" ");
            if (tokens.length > 10) {
                title = Arrays.stream(tokens).limit(10).collect(Collectors.joining(" "));
            }

            if (this.needsTitle) map.replace("title", title);
            if (this.needsHeadline) map.replace("headline", title);
        }

        if (this.parts != null) {
            if (map.containsKey("parts")) {
                ((List<Map<String, Object>>) map.get("parts")).addAll(this.parts);
            } else {
                map.put("parts", this.parts);
            }
        }

        if (this.shots != null) {
            if (map.containsKey("shots")) {
                ((List<Map<String, Object>>) map.get("shots")).addAll(this.shots);
            } else {
                map.put("shots", this.shots);
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
                    if (xmlr.isStandalone()) {
                        String debug = "";
                    }
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
                this.hasText = true;
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
                setShots(content, xmlr);
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
            title = String.format("%s (%s)", this.roleRaw, meta);
            photo.replace("title", title);
            setRendition(name, photo, true);
        } else {
            setRendition(this.role, getContent(content, title + " (JPG)", xmlr), false);
        }
    }

    private void setShots(String content, XMLStreamReader xmlr) throws XMLStreamException {
        this.offsets = null;

        Map<String, Object> photo = getContent(content, "Offset", xmlr);

        if (this.offsets != null && this.offsets.size() > 0) {
            this.shots = new ArrayList<Map<String, Object>>();

            String url = photo.containsKey("basefilename") ? (String) photo.get("basefilename") : null;
            String ext = null;
            String sep = "/";
            boolean isHref = false;
            int zeros = 0;

            if (url != null) {
                char[] chars = url.toCharArray();

                if (chars.length > 2) {
                    for (int i = chars.length - 3; i > 0; i--) {
                        if (chars[i] == '.') {
                            ext = url.substring(i + 1);
                            url = url.substring(0, i);
                            break;
                        }
                    }
                }

                isHref = !Helpers.isNullOrEmpty(ext);
                if (isHref) {
                    chars = url.toCharArray();
                    for (int i = chars.length - 1; i > 0; i--) {
                        if (chars[i] == '0') {
                            zeros += 1;
                        } else {
                            sep = url.substring(i, i + 1);
                            url = url.substring(0, i);
                            break;
                        }
                    }
                }
            }

            Long duration = this.parent.containsKey("totalduration") ? (Long) this.parent.get("totalduration") : 0l;

            String start = null, end = null;

            Long[] offsets = this.offsets.stream().sorted().toArray(size -> new Long[size]);
            for (int i = 0; i < offsets.length; i++) {
                Map<String, Object> shot = new LinkedHashMap<String, Object>();
                shot.put("seq", i + 1);

                String href = null;
                if (isHref) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(i);
                    while (sb.length() < zeros) {
                        sb.insert(0, "0");
                    }
                    shot.put("href", String.format("%s%s%s.%s", url, sep, sb, ext));
                }

                if (photo.containsKey("width")) {
                    shot.put("width", photo.get("width"));
                }

                if (photo.containsKey("height")) {
                    shot.put("height", photo.get("height"));
                }

                if (end == null) {
                    start = formatTime(offsets[i]);
                } else {
                    start = end;
                }
                end = i < offsets.length - 1 ? formatTime(offsets[i + 1]) : formatTime(duration);

                shot.put("start", start);
                shot.put("end", end);
                shot.put("timeunit", "normalplaytime");

                this.shots.add(shot);
            }
        }
    }

    private void setVideo(String content, XMLStreamReader xmlr) throws XMLStreamException {
        if (this.role.equals("main")) {
            if (content.equals("VideoContentItem")) {
                Map<String, Object> video = getContent(content, "Main", xmlr);
                if (video.containsKey("fileextension")) {
                    String ext = ((String) video.get("fileextension")).toUpperCase();
                    if (!ext.equals("TXT")) {
                        String file = "";
                        if (video.containsKey("originalfilename")) {
                            String url = (String) video.get("originalfilename");
                            String[] tokens = url.split("_");
                            file = tokens[tokens.length - 1].split("\\.")[0];
                        }

                        String meta = getBinaryName(video, null, true, true);
                        String name = String.format("main%s%s", meta.replaceAll("-", "").replaceAll(" ", "").toLowerCase(), file);
                        String title = String.format("Full Resolution (%s)", meta);
                        video.replace("title", title);
                        setRendition(name, video, true);
                    }
                }

                if (video.containsKey("totalduration") && !this.parent.containsKey("totalduration")) {
                    Long totalduration = (Long) video.get("totalduration");
                    this.parent.put("totalduration", totalduration);

                    if (this.parent.containsKey("shots")) {
                        List<Map<String, Object>> shots = (List<Map<String, Object>>) this.parent.get("shots");
                        Map<String, Object> shot = shots.get(shots.size() - 1);
                        shot.replace("end", formatTime(totalduration));
                    }
                }
            } else if (content.equals("WebPartContentItem")) {
                setRendition("mainweb", getContent(content, "Web", xmlr), true);
            }
        } else if (this.role.equals("physicalmain")) {
            Map<String, Object> video = getContent(content, "PhysicalMain", xmlr);
            if (video.containsKey("physicaltype")) {
                String physicaltype = (String)video.get("physicaltype");
                String name = physicaltype.replaceAll("-", "").replaceAll(" ", "").toLowerCase();
                video.replace("title", physicaltype);
                setRendition(name, video, true);
            }
        } else if (this.role.equals("preview")) {
            Map<String, Object> video = getContent(content, "Preview", xmlr);
            String meta = getBinaryName(video, null, false, false);
            String name = "preview" + meta.replaceAll("-", "").replaceAll(" ", "").toLowerCase();
            String title = String.format("Preview (%s)", meta);
            video.replace("title", title);
            setRendition(name, video, true);
        } else if (this.role.equals("thumbnail")) {
            Map<String, Object> video = getContent(content, "Thumbnail", xmlr);
            String meta = getBinaryName(video, null, true, true);
            String name = "thumbnail" + meta.replaceAll("-", "").replaceAll(" ", "").toLowerCase();
            String title = String.format("Thumbnail (%s)", meta);
            video.replace("title", title);
            setRendition(name, video, true);
        }
    }

    private void setAudio(String content, XMLStreamReader xmlr) throws XMLStreamException {
        if (this.role.equals("main")) {
            Map<String, Object> audio = getContent(content, "Main", xmlr);
            String meta = getBinaryName(audio, null, false, false);
            String name = "main" + meta.replaceAll("-", "").replaceAll(" ", "").toLowerCase();
            String title = String.format("Full Resolution (%s)", meta);
            audio.replace("title", title);
            setRendition(name, audio, false);
        }
    }

    private void setGraphic(String content, XMLStreamReader xmlr) throws XMLStreamException {
        if (this.role.equals("main")) {
            this.matte = null;

            Map<String, Object> graphic = getContent(content, "Main", xmlr);

            if (this.mediaType.equals("complexdata")) {
                if (graphic.containsKey("presentationframelocation")) {
                    String title = (String) graphic.get("presentationframelocation");
                    graphic.replace("title", title);

                    if (this.parts == null) {
                        this.parts = new ArrayList<Map<String, Object>>();
                    }
                    this.parts.add(graphic);
                }
            } else {
                String meta = getBinaryName(graphic, null, true, true);
                String name = meta.replaceAll("-", "").replaceAll(" ", "").toLowerCase();
                String title = String.format("Full Resolution (%s)", meta);
                graphic.replace("title", title);
                setRendition("main" + name, graphic, true);

                if (this.matte != null) {
                    title = String.format("Full Resolution Matte (%s)", meta);
                    this.matte.replace("title", title);
                    setRendition("mainmatte" + name, this.matte, true);
                }
            }
        } else if (this.role.equals("preview")) {
            setRendition(this.role, getContent(content, "Preview (JPG)", xmlr), false);
        } else if (this.role.equals("thumbnail")) {
            setRendition(this.role, getContent(content, "Thumbnail (JPG)", xmlr), false);
        }
    }

    private void setComplexData(String content, XMLStreamReader xmlr) throws XMLStreamException {
        if (this.role.equals("main")) {
            Map<String, Object> complexdata = getContent(content, "Main", xmlr);
            String meta = getBinaryName(complexdata, null, false, false);
            String name = meta.replaceAll("-", "").replaceAll(" ", "").toLowerCase();
            String title = String.format("Full Resolution (%s)", meta);
            complexdata.replace("title", title);
            setRendition(name, complexdata, false);
        } else if (this.role.equals("preview")) {
            setRendition(this.role, getContent(content, "Preview (JPG)", xmlr), false);
        } else if (this.role.equals("thumbnail")) {
            setRendition(this.role, getContent(content, "Thumbnail (JPG)", xmlr), false);
        }
    }

    private void setRendition(String name, Map<String, Object> rendition, boolean multiple) {
        if (this.parent.containsKey("renditions")) {
            Map<String, Map<String, Object>> renditions = (Map<String, Map<String, Object>>) this.parent.get("renditions");

            if (!renditions.containsKey(name)) {
                renditions.put(name, rendition);
            } else if (multiple) {
                renditions.put(String.format("%s%d", name, renditions.size() + 1), rendition);
            }
        } else {
            Map<String, Map<String, Object>> renditions = new LinkedHashMap<String, Map<String, Object>>();
            renditions.put(name, rendition);
            this.parent.put("renditions", renditions);
        }
    }

    private Map<String, Object> getContent(String content, String title, XMLStreamReader xmlr) throws XMLStreamException {
        Map<String, Object> map = new LinkedHashMap<String, Object>();

        map.put("title", title);
        map.put("rel", this.roleRaw);
        map.put("type", this.type);

        setAttributes(xmlr, map);

        String tapenumber = null;

        while (xmlr.hasNext()) {
            xmlr.next();

            int eventType = xmlr.getEventType();

            if (eventType == XMLStreamReader.START_ELEMENT) {
                String name = xmlr.getLocalName();
                if (name.equals("ForeignKeys")) {
                    if (content.equals("WebPartContentItem")) {
                        List<Map<String, Object>> foreignkeys = Helpers.getForeignKeys(xmlr);
                        if (foreignkeys != null && foreignkeys.size() > 0) {
                            map.put("foreignkeys", foreignkeys);
                        }
                    } else if (tapenumber == null && content.equals("VideoContentItem")) {
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
                } else if (name.equals("RelatedBinaries") && this.type.equals("graphic") && this.role.equals("main") && !this.mediaType.equals("complexdata")) {
                    String attr = xmlr.getAttributeValue("", "Name");
                    if (attr != null && attr.equalsIgnoreCase("MatteFileName")) {
                        Map<String, Object> matte = new LinkedHashMap<String, Object>();
                        matte.put("title", null);
                        setAttributes(xmlr, matte);
                        this.matte = matte;
                    }
                } else if (name.equals("File") && content.equals("PhotoCollectionContentItem") && this.type.equals("photo")) {
                    String attr = xmlr.getAttributeValue("", "TimeOffSetMilliseconds");
                    Long offset = attr == null ? 0 : Helpers.parseLong(attr);
                    if (this.offsets == null) {
                        this.offsets = new ArrayList<Long>();
                    }
                    if (!this.offsets.contains(offset)) {
                        this.offsets.add(offset);
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
                    if (!Helpers.isNullOrEmpty(value)) map.put("scene", value);
                } else if (!name.equals("scenes")) {
                    String value = xmlr.getElementText();
                    if (!Helpers.isNullOrEmpty(value)) {
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

    private String formatTime(Long ms) {
        long millis = ms;

        long second = (ms / 1000) % 60;
        if (second > 0) millis -= second * 1000;

        long minute = (ms / (1000 * 60)) % 60;
        if (minute > 0) millis -= minute * 60 * 1000;

        long hour = (ms / (1000 * 60 * 60)) % 24;
        if (hour > 0) millis -= hour * 24 * 60 * 1000;


        String time = String.format("%02d:%02d:%02d.%03d", hour, minute, second, millis);
        return time;
    }
}
