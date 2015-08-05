package applparser;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.util.*;

/**
 * Created by ymetelkin on 7/29/15.
 */
public class DescriptiveMetadataParser extends ApplParser {
    private List<String> descriptions;
    private boolean addDescriptions;
    private Map<String, Map<String, Object>> generators;
    private boolean addGenerators;
    private List<Map<String, Object>> categories;
    private boolean addCategories;
    private List<Map<String, Object>> subcategories;
    private boolean addSubcategories;
    private List<String> alertcategories;
    private boolean addAlertcategories;
    private Map<String, Map<String, Object>> subjects;
    private boolean addSubjects;
    private String fixtureName;
    private Integer fixtureCode;
    private boolean calculate;

    public DescriptiveMetadataParser(Map<String, Object> map) {
        this.calculate = map.containsKey("addConsumerReady");
    }

    @Override
    public void parse(String name, XMLStreamReader xmlr, Map<String, Object> map) throws XMLStreamException {
        switch (name) {
            case "Description":
                setDescriptions(xmlr, map);
                break;
            case "DateLineLocation":
                ObjectParser parser = new DateLineLocationParser();
                Map<String, Object> datelinelocation = parser.parse(name, xmlr);
                if (datelinelocation.size() > 0) {
                    map.put("datelinelocation", datelinelocation);
                }
                break;
            case "SubjectClassification":
                String authority = xmlr.getAttributeValue("", "Authority");
                if (authority != null) {
                    setGenerator(authority, xmlr, map);
                    iterateOccurrences(name, authority, xmlr, map, (a, x, m) -> setSubjectClassification(authority, xmlr, map));
                }
                break;
            case "EntityClassification":
                authority = xmlr.getAttributeValue("", "Authority");
                if (authority != null) {
                    setGenerator(authority, xmlr, map);
                }
                break;
            case "AudienceClassification":
                //classification,function,classification
                break;
            case "SalesClassification":
                //classification,function,classification
                break;
            case "Comment":
                //classification,function,classification
                break;
            case "ThirdPartyMeta":
                //classification,function,classification
                break;
        }
    }

    @Override
    public void cleanup(Map<String, Object> map) {
        if (this.addDescriptions) {
            map.replace("descriptions", null, this.descriptions);
            this.addDescriptions = false;
        }
        if (this.addGenerators) {
            map.replace("generators", null, this.generators.values());
            this.addGenerators = false;
        }
        if (this.addCategories) {
            map.replace("categories", null, this.categories);
            this.addCategories = false;
        }
        if (this.addSubcategories) {
            map.replace("subcategories", null, this.subcategories);
            this.addSubcategories = false;
        }
        if (this.addAlertcategories) {
            map.replace("alertcategories", null, this.alertcategories);
            this.addAlertcategories = false;
        }
        if (this.addSubjects) {
            map.replace("subjects", null, this.subjects.values());
            this.addSubjects = false;
        }
        if (this.fixtureCode != null && this.fixtureCode >= 900) {
            Map<String, Object> fixture = new LinkedHashMap<String, Object>();
            fixture.put("code", String.valueOf(this.fixtureCode));
            if (this.fixtureName != null) fixture.put("name", this.fixtureName);
            map.put("fixture", fixture);
        }

        if (!this.calculate) {
            map.remove("addConsumerReady");
        }
    }

    private void setDescriptions(XMLStreamReader xmlr, Map<String, Object> map) throws XMLStreamException {
        if (this.descriptions == null) {
            this.descriptions = new ArrayList<String>();
        }

        String text = xmlr.getElementText();
        if (text != null) {
            if (!this.addDescriptions) {
                map.put("descriptions", null);
                this.addDescriptions = true;
            }
            if (!this.descriptions.contains(text)) this.descriptions.add(text);
        }
    }

    private void setGenerator(String authority, XMLStreamReader xmlr, Map<String, Object> map) throws XMLStreamException {
        String version = xmlr.getAttributeValue("", "AuthorityVersion");
        if (version != null) {
            if (!this.addGenerators) {
                this.addGenerators = true;
                this.generators = new LinkedHashMap<String, Map<String, Object>>();
                map.put("generators", null);
            }

            String key = version + authority;
            if (!this.generators.containsKey(key)) {
                Map<String, Object> generator = new LinkedHashMap<String, Object>();
                generator.put("name", authority);
                generator.put("version", version);
                this.generators.put(key, generator);
            }
        }
    }

    private void setSubjectClassification(String authority, XMLStreamReader xmlr, Map<String, Object> map) {
        String code = xmlr.getAttributeValue("", "Id");
        String name = xmlr.getAttributeValue("", "Value");

        if (authority.equalsIgnoreCase("AP Category Code")) {
            Map<String, Object> category = Helpers.getCodeNameObject(code, name);
            if (category != null) {
                if (!this.addCategories) {
                    this.addCategories = true;
                    this.categories = new ArrayList<Map<String, Object>>();
                    map.put("categories", null);
                }

                this.categories.add(category);

                if (this.fixtureName == null && name != null) {
                    this.fixtureName = name;
                }
            }
        } else if (authority.equalsIgnoreCase("AP Supplemental Category Code")) {
            Map<String, Object> category = Helpers.getCodeNameObject(code, name);
            if (category != null) {
                if (!this.addSubcategories) {
                    this.addSubcategories = true;
                    this.subcategories = new ArrayList<Map<String, Object>>();
                    map.put("subcategories", null);
                }

                this.subcategories.add(category);
            }
        } else if (authority.equalsIgnoreCase("AP Alert Category")) {
            if (code != null) {
                if (!this.addAlertcategories) {
                    this.addAlertcategories = true;
                    this.alertcategories = new ArrayList<String>();
                    map.put("alertcategories", null);
                }

                this.alertcategories.add(code);
            }
        } else if (authority.equalsIgnoreCase("AP Subject")) {
            if (this.subjects==null) this.subjects = new LinkedHashMap<String, Map<String, Object>>();
            Map<String, Object> subject = setSubject(code, name, this.subjects, xmlr, map);
            if (subject != null && !this.addSubjects) {
                this.addSubjects = true;
                map.put("subjects", null);
            }
        } else if (authority.equalsIgnoreCase("AP Audio Cut Number Code") && this.fixtureCode == null) {
            boolean hasFixture = false;
            if (map.containsKey("fixture")) {
                Map<String, Object> fixture = (Map<String, Object>) map.get("fixture");
                hasFixture = fixture.get("code") != null && fixture.get("name") != null;
            }

            if (hasFixture) {
                this.fixtureCode = 0;
            } else if (name != null) {
                this.fixtureCode = Helpers.parseInteger(name);
            }
        }
    }

    private Map<String, Object> setSubject(String code, String name, Map<String, Map<String, Object>> subjects, XMLStreamReader xmlr, Map<String, Object> map) {
        Map<String, Object> subject = Helpers.getCodeNameObject(code, name);

        if (subject != null) {
            String system = xmlr.getAttributeValue("", "System");

            String key = String.format("%s-%s", code, name);
            boolean exists = subjects.containsKey(key);
            if (exists) {
                subject = (Map<String, Object>) this.subjects.get(key);
            }

            String rel = null;
            if (system != null && system.equalsIgnoreCase("RTE")) {
                rel = "inferred";
            } else {
                String match = xmlr.getAttributeValue("", "ActualMatch");
                if (match != null) {
                    if (match.equalsIgnoreCase("true")) rel = "direct";
                    else if (match.equalsIgnoreCase("false")) rel = "ancestor";
                }
            }

            Helpers.addStringToMapList(rel, "rels", subject);
            Helpers.addSchemeAndCreatorToMap(system, subject);
            Helpers.addStringToMapList(xmlr.getAttributeValue("", "ParentId"), "parentids", subject);

            if (exists) {
                subjects.replace(key, subject);
            } else {
                String topparent = xmlr.getAttributeValue("", "TopParent");
                if (topparent != null) {
                    if (topparent.equalsIgnoreCase("true")) subject.put("topparent", true);
                    else if (topparent.equalsIgnoreCase("false")) subject.put("topparent", false);
                }

                subjects.put(key, subject);
            }
        }

        return subject;
    }

    private void iterateOccurrences(String parentName, String authority, XMLStreamReader xmlr, Map<String, Object> map, OccurrenceParser action) throws XMLStreamException {
        boolean end = false;

        while (!end && xmlr.hasNext()) {
            xmlr.next();

            int eventType = xmlr.getEventType();

            if (eventType == XMLStreamReader.START_ELEMENT) {
                if (xmlr.getLocalName().equals("Occurrence")) {
                    action.parse(authority, xmlr, map);
                }

            } else if (eventType == XMLStreamReader.END_ELEMENT) {
                if (xmlr.getLocalName() == parentName) {
                    end = true;
                }
            }
        }
    }

    private class DateLineLocationParser extends ObjectParser {
        private Number lon;
        private Number lat;

        @Override
        void add(Map<String, Object> map, XMLStreamReader xmlr) throws XMLStreamException {
            String name = xmlr.getLocalName();

            switch (name) {
                case "City":
                case "CountryAreaName":
                case "CountryName":
                    Helpers.safeAdd(name.toLowerCase(), xmlr.getElementText(), map);
                    break;
                case "CountryArea":
                    Helpers.safeAdd("countryareacode", xmlr.getElementText(), map);
                    break;
                case "Country":
                    Helpers.safeAdd("countrycode", xmlr.getElementText(), map);
                    break;
                case "LongitudeDD":
                    this.lon = Helpers.parseNumber(xmlr.getElementText());

                    break;
                case "LatitudeDD":
                    this.lat = Helpers.parseNumber(xmlr.getElementText());
                    break;
            }
        }

        @Override
        void cleanup(Map<String, Object> map) {
            if (this.lon != null && this.lat != null) {
                Map<String, Object> geo = new LinkedHashMap<String, Object>();
                geo.put("type", "Point");
                geo.put("coordinates", new Number[]{this.lon, this.lat});

                map.put("geometry_geojson", geo);
            }
        }
    }

    @FunctionalInterface
    private interface OccurrenceParser {
        void parse(String authority, XMLStreamReader xmlr, Map<String, Object> map);
    }
}
