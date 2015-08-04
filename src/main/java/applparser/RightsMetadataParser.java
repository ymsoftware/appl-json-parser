package applparser;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.time.LocalDate;
import java.util.*;

/**
 * Created by ymetelkin on 7/29/15.
 */
public class RightsMetadataParser extends ApplParser {
    private List<Map<String, Object>> usagerights;
    private boolean addUsageRights;

    @Override
    public void parse(String name, XMLStreamReader xmlr, Map<String, Object> map) throws XMLStreamException {
        switch (name) {
            case "Copyright":
                setCopyright(name, xmlr, map);
                break;
            case "UsageRights":
                ObjectParser parser = new UsageRightsParser(this);
                Map<String, Object> right = parser.parse(name, xmlr);
                if (map.size()>0){
                    if (!this.addUsageRights) {
                        map.put("usagerights", null);
                        this.addUsageRights = true;
                        this.usagerights = new ArrayList<Map<String, Object>>();
                    }

                    this.usagerights.add(right);
                }
                break;
        }
    }

    @Override
    public void cleanup(Map<String, Object> map) {
        if (this.addUsageRights) {
            map.replace("usagerights", null, this.usagerights);
            this.addUsageRights = false;
        }
    }

    private void setCopyright(String name, XMLStreamReader xmlr, Map<String, Object> map) throws XMLStreamException {
        boolean exists = map.containsKey("copyrightnotice");
        String copyrightholder = xmlr.getAttributeValue("", "Holder");
        String copyrightdate = xmlr.getAttributeValue("", "Date");
        String copyrightnotice = exists ? (String) map.get("copyrightnotice") : null;

        int year = 0;
        try {
            year = Integer.parseUnsignedInt(copyrightdate);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (copyrightholder == null) {
            if (exists) {
                map.remove("copyrightholder");

                if (copyrightnotice == null) {
                    map.remove("copyrightnotice");
                }
            }
        } else {
            if (copyrightnotice == null) {
                int date = year == 0 ? LocalDate.now().getYear() : year;

                copyrightnotice = String.format("Copyright %d %s. All rights reserved. This material may not be published, broadcast, rewritten or redistributed.", date, copyrightholder);

                if (exists) {
                    map.replace("copyrightnotice", null, copyrightnotice);
                } else {
                    map.put("copyrightnotice", copyrightnotice);
                }
            }

            if (exists) {
                map.replace("copyrightholder", null, copyrightholder);
            } else {
                map.put("copyrightholder", copyrightholder);
            }
        }

        if (year > 0) {
            if (exists) {
                map.replace("copyrightdate", null, year);
            } else {
                map.put("copyrightdate", year);
            }
        } else {
            if (exists) {
                map.remove("copyrightdate");
            }
        }
    }

    private class UsageRightsParser extends ObjectParser {
        private ApplParser parent;
        private List<String> geography;
        private boolean addGeography;
        private List<String> limitations;
        private boolean addLimitations;
        private List<Map<String, Object>> groups;
        private boolean addGroups;

        public UsageRightsParser(ApplParser parent) {
            this.parent = parent;
        }

        @Override
        void add(Map<String, Object> map, XMLStreamReader xmlr) throws XMLStreamException {
            String name = xmlr.getLocalName();

            switch (name) {
                case "UsageType":
                case "RightsHolder":
                    this.parent.parse(name.toLowerCase(), xmlr.getElementText(), map);
                    break;
                case "StartDate":
                case "EndDate":
                    String date = parseDate(xmlr.getElementText());
                    this.parent.parse(name.toLowerCase(), date, map);
                    break;
                case "Geography":
                    String geo = xmlr.getElementText();
                    if (geo != null && geo.length() > 0) {
                        if (!this.addGeography) {
                            map.put("geography", null);
                            this.addGeography = true;
                            this.geography = new ArrayList<String>();
                        }

                        this.geography.add(geo);
                    }
                    break;
                case "Limitations":
                    String limit = xmlr.getElementText();
                    if (limit != null && limit.length() > 0) {
                        if (!this.addLimitations) {
                            map.put("limitations", null);
                            this.addLimitations = true;
                            this.limitations = new ArrayList<String>();
                        }

                        this.limitations.add(limit);
                    }
                    break;
                case "Group":
                    Map<String, Object> group = new LinkedHashMap<String, Object>();

                    String type = xmlr.getAttributeValue("", "Type");
                    if (type != null) group.put("type", type);

                    String code = xmlr.getAttributeValue("", "Id");
                    if (code != null) group.put("code", code);

                    String text = xmlr.getElementText();
                    if (text != null && text.length() > 0) group.put("name", text);

                    if (group.size() > 0) {
                        if (!this.addGroups) {
                            map.put("groups", null);
                            this.addGroups = true;
                            this.groups = new ArrayList<Map<String, Object>>();
                        }

                        this.groups.add(group);
                    }

                    break;
            }

        }

        @Override
        void cleanup(Map<String, Object> map) {
            if (this.addGeography) {
                map.replace("geography", null, this.geography);
                this.addGeography = false;
            }
            if (this.addLimitations) {
                map.replace("limitations", null, this.limitations);
                this.addLimitations = false;
            }
            if (this.addGroups) {
                map.replace("groups", null, this.groups);
                this.addGroups = false;
            }
        }
    }
}

