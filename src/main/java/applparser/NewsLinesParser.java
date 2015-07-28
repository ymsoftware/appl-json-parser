package applparser;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by ymetelkin on 7/28/15.
 */
public class NewsLinesParser extends ApplParser {
    private List<Map<String, String>> bylines;
    private List<Map<String, String>> originalBylines;
    private boolean addBylines;
    private boolean missingOriginalBylineTitles;
    private String defaultBylineTitle;
    private boolean isPhotographer;
    private boolean isCaptionwriter;
    private boolean isEditor;

    @Override
    public void parse(String name, XMLStreamReader xmlr, Map<String, Object> map) throws XMLStreamException {
        switch (name) {
            case "DateLine":
            case "RightsLine":
            case "SeriesLine":
            case "OutCue":
            case "LocationLine":
                parseId(name.toLowerCase(), xmlr.getElementText(), map);
                break;
            case "ByLine":
                setBylines(name, xmlr, map);
                break;
            case "ByLineOriginal":
                setOriginalBylines(name, xmlr, map);
                break;
            case "OverLine":
                break;
            case "CreditLine":
                break;
            case "CopyrightLine":
                break;
            case "KeywordLine":
                break;
            case "NameLine":
                break;
        }
    }

    @Override
    public void cleanup(Map<String, Object> map) {
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

//        if (this.addInstructions) {
//            map.replace("outinginstructions", null, this.instructions);
//            this.addInstructions = false;
//        }
    }

    private void setOriginalBylines(String name, XMLStreamReader xmlr, Map<String, Object> map) throws XMLStreamException {
        if (this.originalBylines == null) {
            this.originalBylines = new ArrayList<Map<String, String>>();
        }

        String title = xmlr.getAttributeValue("", "Title");
        String by = xmlr.getElementText();
        if (by != null) {
            if (!this.addBylines) {
                map.put("bylines", null);
                this.addBylines = true;
            }

            Map<String, String> byline = new LinkedHashMap<String, String>();
            byline.put("by", by);

            if (title == null) {
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
        boolean producer = title.equalsIgnoreCase("EditedBy");

        String parametric = xmlr.getAttributeValue("", "Parametric");
        boolean photographer = parametric != null && parametric.equalsIgnoreCase("photographer");
        boolean captionwriter = !photographer && parametric != null && parametric.equalsIgnoreCase("captionwriter");
        boolean editor = !photographer && !captionwriter && parametric != null && parametric.equalsIgnoreCase("editedby");

        String id = xmlr.getAttributeValue("", "Id");
        String by = xmlr.getElementText();

        if (producer || photographer || captionwriter || editor) {
            Map<String, String> add = new LinkedHashMap<String, String>();
            add.put("name", by);

            if (id != null) {
                add.put("code", id);
            }

            if (producer) {
                map.put("producer", add);
                return;
            }

            if (title != null) {
                add.put("title", title);
            }

            if (photographer) {
                if (!this.isPhotographer) {
                    this.isPhotographer = true;
                    map.put("photographer", add);
                }
                return;
            } else if (captionwriter) {
                if (!this.isCaptionwriter) {
                    this.isCaptionwriter = true;
                    map.put("captionwriter", add);
                }
                return;
            } else if (editor) {
                if (!this.isEditor) {
                    this.isEditor = true;
                    map.put("editor", add);
                }
                return;
            }
        } else {
            if (title != null && this.defaultBylineTitle == null) {
                this.defaultBylineTitle = title;
            }

            if (this.originalBylines == null) {
                Map<String, String> add = new LinkedHashMap<String, String>();
                add.put("by", by);

                if (id != null) {
                    add.put("code", id);
                }

                if (title != null) {
                    add.put("title", title);
                }

                if (parametric != null) {
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
    }
}
