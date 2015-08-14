package applparser;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.util.*;

/**
 * Created by ymetelkin on 7/28/15.
 */
public class NewsLinesParser extends ApplParser {
    private String type;
    private String title;
    private String headline;
    private String extendedHeadline;
    private String originalHeadline;
    private List<Map<String, String>> bylines;
    private List<Map<String, String>> originalBylines;
    private boolean addBylines;
    private boolean missingOriginalBylineTitles;
    private String defaultBylineTitle;
    private boolean isPhotographer;
    private boolean isCaptionwriter;
    private boolean isEditor;
    private List<String> overlines;
    private boolean addOverlines;
    private List<String> keywordlines;
    private boolean addKeywordlines;
    private List<Map<String, Object>> persons;
    private boolean addPersons;
    private boolean summary;

    public NewsLinesParser(Map<String, Object> map) {
        this.type = map.containsKey("type") ? (String) map.get("type") : "text";
        map.put("headline", null);
        map.put("title", null);
    }

    @Override
    public void parse(String name, XMLStreamReader xmlr, Map<String, Object> map) throws XMLStreamException {
        switch (name) {
            case "BodySubHeader":
                if (!this.summary) {
                    map.put("summary", xmlr.getElementText());
                    this.summary = true;
                }
            case "ExtendedHeadLine":
                this.extendedHeadline = xmlr.getElementText();
                break;
            case "OriginalHeadLine":
                this.originalHeadline = xmlr.getElementText();
                break;
            case "HeadLine":
                String text = xmlr.getElementText();
                this.headline = Helpers.isNullOrEmpty(text) ? null : text.trim();
                break;
            case "Title":
                text = xmlr.getElementText();
                this.title = Helpers.isNullOrEmpty(text) ? null : text.trim();
                break;
            case "DateLine":
            case "RightsLine":
            case "SeriesLine":
            case "OutCue":
            case "LocationLine":
                Helpers.safeAddString(name.toLowerCase(), xmlr.getElementText(), map);
                break;
            case "ByLine":
                setBylines(name, xmlr, map);
                break;
            case "ByLineOriginal":
                setOriginalBylines(name, xmlr, map);
                break;
            case "OverLine":
                setOverlines(name, xmlr, map);
                break;
            case "CreditLine":
                String id = xmlr.getAttributeValue("", "Id");
                if (id != null) {
                    map.put("creditlineid", id);
                }
                text = xmlr.getElementText();
                if (!Helpers.isNullOrEmpty(text)) {
                    map.put("creditline", text);
                }
                break;
            case "CopyrightLine":
                map.put("copyrightnotice", xmlr.getElementText());
                map.put("copyrightholder", null);
                map.put("copyrightdate", null);
                break;
            case "KeywordLine":
                setKeywordlines(name, xmlr, map);
                break;
            case "NameLine":
                setPerson(name, xmlr, map);
                break;
        }
    }

    @Override
    public void cleanup(Map<String, Object> map) {
        setTitle(map);
        setHeadline(map);

        if (this.addBylines) {
            if (this.originalBylines == null) {
                map.replace("bylines", null, this.bylines);
            } else {
                if (this.missingOriginalBylineTitles && this.defaultBylineTitle != null) {
                    for (Map<String, String> byline : this.originalBylines) {
                        if (!byline.containsKey("title")) {
                            byline.put("title", this.defaultBylineTitle);
                        }
                    }
                }

                map.replace("bylines", null, this.originalBylines);
            }

            this.addBylines = false;
        }

        if (this.addOverlines) {
            map.replace("overlines", null, this.overlines);
            this.addOverlines = false;
        }

        if (this.addKeywordlines) {
            map.replace("keywordlines", null, this.keywordlines);
            this.addKeywordlines = false;
        }

        if (this.addPersons) {
            if (map.containsKey("persons")) {
                ((Collection<Map<String, Object>>) map.get("person")).addAll(this.persons);
            } else {
                map.put("persons", this.persons);
            }
            this.addPersons = false;
        }
    }

    private void setTitle(Map<String, Object> map) {
        if (this.type.equals("text") || this.type.equals("complexdata")) {
            //If NewsLines/ExtendedHeadLine AND NewsLines/HeadLine exist use NewsLines/HeadLine
            //If NewsLines/OriginalHeadLine AND NewsLines/HeadLine exist use NewsLines/HeadLine
            //If NewsLines/HeadLine AND NewsLines/Title exist use NewsLines/Title
            //FilingMetadata[1]/SlugLine (only when FilingMetadata[1]/Category="l" or "s")
            //First ten words from PublicationComponent[@Role="Main" and @MediaType="Text"]/TextContentItem/DataContent/text()

            if (this.headline == null) {
                this.title = null;
            } else {
                if (this.extendedHeadline != null || this.originalHeadline != null) {
                    this.title = this.headline;
                }
            }
        }

        if (this.title != null) {
            map.replace("title", null, this.title);
        }
    }

    private void setHeadline(Map<String, Object> map) {
        String title = this.title;

        if (this.type.equals("text") || this.type.equals("complexdata")) {
            if (this.extendedHeadline != null) {
                this.headline = this.extendedHeadline;
            } else if (this.originalHeadline != null) {
                this.headline = this.originalHeadline;
            } else if (this.headline == null && this.title != null) {
                this.headline = this.title;
            }
        } else if (this.type.equals("photo") || this.type.equals("graphic")) {
            if (this.title != null) {
                this.headline = this.title;
            }
        } else if (this.type.equals("video")) {
            if (map.containsKey("function") && ((String) map.get("function")).equalsIgnoreCase("APTNLibrary")) {
                if (this.title != null) {
                    this.headline = this.title;
                }
            } else {
                if (this.headline == null && this.title != null) {
                    this.headline = this.title;
                }
            }
        } else if (this.type.equals("audio")) {
            if (this.headline == null) {
                this.headline = this.title;
            }
        }

        if (this.headline != null) {
            map.replace("headline", null, this.headline);
        }
    }

    private void setOriginalBylines(String name, XMLStreamReader xmlr, Map<String, Object> map) throws XMLStreamException {
        if (this.originalBylines == null) {
            this.originalBylines = new ArrayList<Map<String, String>>();
        }

        String title = xmlr.getAttributeValue("", "Title");
        String by = xmlr.getElementText();
        if (!Helpers.isNullOrEmpty(by)) {
            if (!this.addBylines) {
                map.put("bylines", null);
                this.addBylines = true;
            }

            Map<String, String> byline = new LinkedHashMap<String, String>();
            byline.put("by", by);

            if (Helpers.isNullOrEmpty(title)) {
                this.missingOriginalBylineTitles = true;
            } else {
                byline.put("title", title);
            }

            this.originalBylines.add(byline);
        }
    }

    private void setBylines(String name, XMLStreamReader xmlr, Map<String, Object> map) throws XMLStreamException {
        if (this.originalBylines != null && this.defaultBylineTitle != null) {
            return;
        }

        String title = xmlr.getAttributeValue("", "Title");
        boolean producer = title != null && title.equalsIgnoreCase("EditedBy");

        boolean photographer = false;
        boolean captionwriter = false;
        boolean editor = false;

        String parametric = xmlr.getAttributeValue("", "Parametric");
        if (parametric != null) {
            if (parametric.equalsIgnoreCase("photographer")) {
                photographer = !this.isPhotographer;
            } else if (parametric.equalsIgnoreCase("captionwriter")) {
                captionwriter = !this.isCaptionwriter;
            } else if (parametric.equalsIgnoreCase("editedby")) {
                editor = !this.isEditor;
            }
        }

        String id = xmlr.getAttributeValue("", "Id");
        String by = xmlr.getElementText();

        if (producer || photographer || captionwriter || editor) {
            Map<String, String> add = new LinkedHashMap<String, String>();
            add.put("name", by);

            if (!Helpers.isNullOrEmpty(id)) {
                add.put("code", id);
            }

            if (producer) {
                map.put("producer", add);
                return;
            }

            if (!Helpers.isNullOrEmpty(title)) {
                add.put("title", title);
            }

            if (photographer) {
                this.isPhotographer = true;
                map.put("photographer", add);
                return;
            } else if (captionwriter) {
                this.isCaptionwriter = true;
                map.put("captionwriter", add);
                return;
            } else if (editor) {
                this.isEditor = true;
                map.put("editor", add);
                return;
            }
        }

        if (title != null && this.defaultBylineTitle == null) {
            this.defaultBylineTitle = title;
        }

        if (this.originalBylines == null) {
            Map<String, String> add = new LinkedHashMap<String, String>();
            add.put("by", by);

            if (!Helpers.isNullOrEmpty(id)) {
                add.put("code", id);
            }

            if (!Helpers.isNullOrEmpty(title)) {
                add.put("title", title);
            }

            if (!Helpers.isNullOrEmpty(parametric)) {
                add.put("parametric", parametric);
            }

            if (!this.addBylines) {
                map.put("bylines", null);
                this.addBylines = true;
            }

            if (this.bylines == null) {
                this.bylines = new ArrayList<Map<String, String>>();
            }

            this.bylines.add(add);
        }
    }

    private void setOverlines(String name, XMLStreamReader xmlr, Map<String, Object> map) throws XMLStreamException {
        if (this.overlines == null) {
            this.overlines = new ArrayList<String>();
        }

        String text = xmlr.getElementText();
        if (text != null) {
            if (!this.addOverlines) {
                map.put("overlines", null);
                this.addOverlines = true;
            }
            text = text.trim();
            if (!this.overlines.contains(text)) this.overlines.add(text);
        }
    }

    private void setKeywordlines(String name, XMLStreamReader xmlr, Map<String, Object> map) throws XMLStreamException {
        if (this.keywordlines == null) {
            this.keywordlines = new ArrayList<String>();
        }

        String text = xmlr.getElementText();
        if (text != null) {
            if (!this.addKeywordlines) {
                map.put("keywordlines", null);
                this.addKeywordlines = true;
            }
            text = text.trim();
            if (!this.keywordlines.contains(text)) this.keywordlines.add(text);
        }
    }

    private void setPerson(String name, XMLStreamReader xmlr, Map<String, Object> map) throws XMLStreamException {
        if (this.persons == null) {
            this.persons = new ArrayList<Map<String, Object>>();
        }

        String parametric = xmlr.getAttributeValue("", "Parametric");
        if (parametric != null && parametric.equalsIgnoreCase("PERSON_FEATURED")) {
            String text = xmlr.getElementText();
            if (text != null) {
                if (!this.addPersons) {
                    this.addPersons = true;
                }

                Map<String, Object> person = new LinkedHashMap<String, Object>();
                person.put("name", text.trim());
                person.put("rels", new String[]{"personfeatured"});
                person.put("creator", "Editorial");

                this.persons.add(person);
            }
        }
    }
}
