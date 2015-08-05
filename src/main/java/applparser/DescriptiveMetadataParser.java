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
    private Map<String, Map<String, Object>> events;
    private boolean addEvents;
    private Map<String, Map<String, Object>> organizations;
    private boolean addOrganizations;
    private Map<String, Map<String, Object>> companies;
    private boolean addCompanies;
    private Map<String, Map<String, Object>> symbols;
    private Map<String, Map<String, Object>> industries;
    private List<Map<String, Object>> tickers;
    private List<Map<String, Object>> exchanges;
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
                    iterateOccurrences(name, authority, xmlr, map, this::setSubjectClassification);
                }
                break;
            case "EntityClassification":
                authority = xmlr.getAttributeValue("", "Authority");
                if (authority != null) {
                    setGenerator(authority, xmlr, map);
                    iterateOccurrences(name, authority, xmlr, map, this::setEntityClassification);
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
        if (this.addEvents) {
            map.replace("events", null, this.events.values());
            this.addEvents = false;
        }
        if (this.addOrganizations) {
            map.replace("organizations", null, this.organizations.values());
            this.addOrganizations = false;
        }
        if (this.addCompanies) {
            map.replace("companies", null, this.companies.values());
            this.addCompanies = false;
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
            if (this.subjects == null) this.subjects = new LinkedHashMap<String, Map<String, Object>>();
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

    private void setEntityClassification(String authority, XMLStreamReader xmlr, Map<String, Object> map) throws XMLStreamException {
        String code = xmlr.getAttributeValue("", "Id");
        String name = xmlr.getAttributeValue("", "Value");
        if (code != null && name != null) {
            if (authority.equalsIgnoreCase("AP Event")) {
                Map<String, Object> event = Helpers.getCodeNameObject(code, name);
                if (!this.addEvents) {
                    this.addEvents = true;
                    this.events = new LinkedHashMap<String, Map<String, Object>>();
                    map.put("events", null);
                }

                if (this.events.containsKey(code)) {
                    this.events.replace(code, event);
                } else {
                    this.events.put(code, event);
                }
            } else {
                String system = xmlr.getAttributeValue("", "System");

                if (authority.equalsIgnoreCase("AP Party")) {

                } else if (authority.equalsIgnoreCase("AP Organization")) {
                    if (this.organizations == null)
                        this.organizations = new LinkedHashMap<String, Map<String, Object>>();
                    Map<String, Object> subject = setSubject(code, name, this.organizations, xmlr, map);
                    if (subject != null && !this.addOrganizations) {
                        this.addOrganizations = true;
                        map.put("organizations", null);
                    }
                } else if (authority.equalsIgnoreCase("AP Company")) {
                    if (this.companies == null)
                        this.companies = new LinkedHashMap<String, Map<String, Object>>();
                    Map<String, Object> company = setCompany(code, name, system, this.companies, xmlr, map);
                    if (company != null && !this.addCompanies) {
                        this.addCompanies = true;
                        map.put("companies", null);
                    }
                } else if (authority.equalsIgnoreCase("AP Geography")
                        || authority.equalsIgnoreCase("AP Country")
                        || authority.equalsIgnoreCase("AP Region")) {

                }
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
                subject = (Map<String, Object>) subjects.get(key);
            }

            Helpers.addSchemeAndCreatorToMap(system, subject);

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

    private Map<String, Object> setCompany(String code, String name, String system, Map<String, Map<String, Object>> companies, XMLStreamReader xmlr, Map<String, Object> map) throws XMLStreamException {
        Map<String, Object> company = Helpers.getCodeNameObject(code, name);

        String key = String.format("%s-%s", code, name);
        boolean exists = companies.containsKey(key);
        if (exists) {
            company = (Map<String, Object>) companies.get(key);
        }

        Helpers.addSchemeAndCreatorToMap(system, company);

        if (!exists) {
            company.put("rels", new String[]{"direct"});
        }

        this.symbols = new HashMap<String, Map<String, Object>>();
        this.industries = new HashMap<String, Map<String, Object>>();
        this.tickers = new ArrayList<Map<String, Object>>();
        this.exchanges = new ArrayList<Map<String, Object>>();
        iterateOccurrenceProperties(xmlr, map, (n, v, x, m) -> setCompanyProperties(n, v, x, m));

        if (this.tickers.size() > 0 && this.exchanges.size() > 0) {
            for (Map<String, Object> ticker : this.tickers) {
                String parentid = (String) ticker.get("parentid");
                Map<String, Object> exchange = this.exchanges.get(0);
                if (parentid != null) {
                    for (Map<String, Object> exch : this.exchanges) {
                        if (parentid.equalsIgnoreCase((String) exch.get("id"))) {
                            exchange = exch;
                            break;
                        }
                    }
                }

                String e = ((String) exchange.get("value")).toUpperCase();
                String t = ((String) ticker.get("value")).toUpperCase();
                String k = String.format("%s:%s", e, t);
                if (!this.symbols.containsKey(k)) {
                    Map<String, Object> symbol = new LinkedHashMap<String, Object>();
                    symbol.put("instrument", k);
                    symbol.put("exchange", e);
                    symbol.put("ticker", t);
                    this.symbols.put(key, symbol);
                }
            }
        }

        if (this.symbols.size() > 0) {
            company.put("symbols", this.symbols);
        }
        if (this.industries.size() > 0) {
            company.put("industries", this.industries);
        }

        if (exists) {
            companies.replace(key, company);
        } else {
            companies.put(key, company);
        }

        return company;
    }

    private void setCompanyProperties(String name, String value, XMLStreamReader xmlr, Map<String, Object> map) {
        if (name.equalsIgnoreCase("Instrument")) {
            String key = value.toUpperCase();
            String[] tokens = key.split(":");
            if (tokens.length > 1 && !this.symbols.containsKey(key)) {
                Map<String, Object> symbol = new LinkedHashMap<String, Object>();
                symbol.put("instrument", key);
                symbol.put("exchange", tokens[0]);
                symbol.put("ticker", tokens[1]);
                this.symbols.put(key, symbol);
            }
        } else if (name.equalsIgnoreCase("PrimaryTicker")
                || name.equalsIgnoreCase("Ticker")) {
            String parentid = xmlr.getAttributeValue("", "ParentId");
            Map<String, Object> ticker = new LinkedHashMap<String, Object>();
            ticker.put("parentid", parentid);
            ticker.put("value", value);
            this.tickers.add(ticker);
        } else if (name.equalsIgnoreCase("APIndustry")) {
            String id = xmlr.getAttributeValue("", "Id");
            if (id != null && !this.industries.containsKey(id)) {
                Map<String, Object> industry = new LinkedHashMap<String, Object>();
                industry.put("code", id);
                industry.put("name", value);
                this.industries.put(id, industry);
            }
        } else if (name.equalsIgnoreCase("Exchange")) {
            String id = xmlr.getAttributeValue("", "Id");
            if (id != null) {
                Map<String, Object> exchange = new LinkedHashMap<String, Object>();
                exchange.put("id", id);
                exchange.put("value", value);
                this.exchanges.add(exchange);
            }
        }
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

    private void iterateOccurrenceProperties(XMLStreamReader xmlr, Map<String, Object> map, OccurrencePropertyParser action) throws XMLStreamException {
        boolean end = false;

        while (!end && xmlr.hasNext()) {
            xmlr.next();

            int eventType = xmlr.getEventType();

            if (eventType == XMLStreamReader.START_ELEMENT) {
                if (xmlr.getLocalName().equals("Property")) {
                    String name = xmlr.getAttributeValue("", "Name");
                    String value = xmlr.getAttributeValue("", "Value");
                    if (name != null && value != null) {
                        action.parse(name, value, xmlr, map);
                    }
                }

            } else if (eventType == XMLStreamReader.END_ELEMENT) {
                if (xmlr.getLocalName() == "Occurrence") {
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
        void parse(String authority, XMLStreamReader xmlr, Map<String, Object> map) throws XMLStreamException;
    }

    @FunctionalInterface
    private interface OccurrencePropertyParser {
        void parse(String name, String value, XMLStreamReader xmlr, Map<String, Object> map);
    }
}
