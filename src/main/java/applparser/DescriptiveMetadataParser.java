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
    private Map<String, Map<String, Map<String, Object>>> symbols;
    private Map<String, Map<String, Map<String, Object>>> industries;
    private Map<String, List<Map<String, Object>>> tickers;
    private Map<String, List<Map<String, Object>>> exchanges;
    private String companyKey;
    private Map<String, Map<String, Object>> persons;
    private boolean addPersons;
    private Map<String, Map<String, Map<String, Object>>> teams;
    private Map<String, Map<String, Map<String, Object>>> associatedevents;
    private Map<String, Map<String, Map<String, Object>>> associatedstates;
    private Map<String, List<String>> rels;
    private Map<String, List<String>> types;
    private Map<String, List<String>> extids;
    private String creator;
    private String personKey;
    private Map<String, Map<String, Object>> places;
    private boolean addPlaces;
    private Map<String, Object> locationtype;
    private Number lat;
    private Number lon;
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

                    String system = xmlr.getAttributeValue("", "System");
                    iterateOccurrences(name, system, authority, xmlr, map, this::setSubjectClassification);
                }
                break;
            case "EntityClassification":
                authority = xmlr.getAttributeValue("", "Authority");
                if (authority != null) {
                    setGenerator(authority, xmlr, map);

                    String system = xmlr.getAttributeValue("", "System");
                    iterateOccurrences(name, system, authority, xmlr, map, this::setEntityClassification);
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
        if (this.addPersons) {
            map.replace("persons", null, this.persons.values());
            this.addPersons = false;
        }
        if (this.addPlaces) {
            map.replace("places", null, this.places.values());
            this.addPlaces = false;
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

    private void setSubjectClassification(String authority, String system, XMLStreamReader xmlr, Map<String, Object> map) {
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
            Map<String, Object> subject = setSubject(code, name, system, this.subjects, xmlr, map);
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

    private void setEntityClassification(String authority, String system, XMLStreamReader xmlr, Map<String, Object> map) throws XMLStreamException {
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
                if (authority.equalsIgnoreCase("AP Party")) {
                    if (this.persons == null)
                        this.persons = new LinkedHashMap<String, Map<String, Object>>();

                    Map<String, Object> person = setPerson(code, name, system, this.persons, xmlr, map);

                    if (person != null && !this.addPersons) {
                        this.addPersons = true;
                        map.put("persons", null);
                    }
                } else if (authority.equalsIgnoreCase("AP Organization")) {
                    if (this.organizations == null)
                        this.organizations = new LinkedHashMap<String, Map<String, Object>>();
                    Map<String, Object> subject = setSubject(code, name, system, this.organizations, xmlr, map);
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
                    if (this.places == null)
                        this.places = new LinkedHashMap<String, Map<String, Object>>();

                    Map<String, Object> place = setPlace(code, name, system, this.places, xmlr, map);

                    if (place != null && !this.addPlaces) {
                        this.addPlaces = true;
                        map.put("places", null);
                    }
                }
            }
        }
    }

    private Map<String, Object> setSubject(String code, String name, String system, Map<String, Map<String, Object>> subjects, XMLStreamReader xmlr, Map<String, Object> map) {
        Map<String, Object> subject = Helpers.getCodeNameObject(code, name);

        if (subject != null) {
            String key = String.format("%s-%s", code, name);
            boolean exists = subjects.containsKey(key);
            if (exists) {
                subject = (Map<String, Object>) subjects.get(key);
            }

            String rel = calculateRel(system, xmlr);

            Helpers.addSchemeAndCreatorToMap(system, subject);
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

    private Map<String, Object> setPerson(String code, String name, String system, Map<String, Map<String, Object>> persons, XMLStreamReader xmlr, Map<String, Object> map) throws XMLStreamException {
        Map<String, Object> person = Helpers.getCodeNameObject(code, name);

        String key = String.format("%s-%s", code, name);
        boolean exists = persons.containsKey(key);
        if (exists) {
            person = (Map<String, Object>) persons.get(key);
        }

        if (this.teams == null) this.teams = new HashMap<String, Map<String, Map<String, Object>>>();
        if (this.associatedevents == null)
            this.associatedevents = new HashMap<String, Map<String, Map<String, Object>>>();
        if (this.associatedstates == null)
            this.associatedstates = new HashMap<String, Map<String, Map<String, Object>>>();
        if (this.rels == null) this.rels = new HashMap<String, List<String>>();
        if (this.types == null) this.types = new HashMap<String, List<String>>();
        if (this.extids == null) this.extids = new HashMap<String, List<String>>();
        this.creator = null;
        this.personKey = key;

        iterateOccurrenceProperties(xmlr, map, this::setPersonProperties);

        if (this.creator == null) this.creator = system;
        Helpers.addSchemeAndCreatorToMap(this.creator, person);

        if (!exists) {
            List<String> rels = new ArrayList<String>();
            rels.add("direct");
            person.put("rels", rels);
        }

        Helpers.addListToMapList(this.rels.get(key), "rels", person);
        Helpers.addListToMapList(this.types.get(key), "types", person);

        Map<String, Map<String, Object>> teams = this.teams.containsKey(key) ? this.teams.get(key) : null;
        if (teams != null && teams.size() > 0) {
            person.put("teams", teams.values());
        }

        Map<String, Map<String, Object>> associatedstates = this.associatedstates.containsKey(key) ? this.associatedstates.get(key) : null;
        if (associatedstates != null && associatedstates.size() > 0) {
            person.put("associatedstates", associatedstates.values());
        }

        Map<String, Map<String, Object>> associatedevents = this.associatedevents.containsKey(key) ? this.associatedevents.get(key) : null;
        if (associatedevents != null && associatedevents.size() > 0) {
            person.put("associatedevents", associatedevents.values());
        }

        Helpers.addListToMapList(this.extids.get(key), "extids", person);

        if (exists) {
            persons.replace(key, person);
        } else {
            persons.put(key, person);
        }

        return person;
    }

    private void setPersonProperties(String name, String value, XMLStreamReader xmlr, Map<String, Object> map) {
        if (name.equalsIgnoreCase("PartyType")) {
            if (value.equalsIgnoreCase("PERSON_FEATURED")) {
                if (!this.rels.containsKey(this.personKey)) {
                    this.rels.put(this.personKey, new ArrayList<String>());
                }
                List<String> rels = this.rels.get(this.personKey);
                if (!rels.contains(value)) rels.add(value);

                if (this.creator == null) this.creator = "Editorial";
            } else {
                if (!this.types.containsKey(this.personKey)) {
                    this.types.put(this.personKey, new ArrayList<String>());
                }
                List<String> types = this.types.get(this.personKey);
                if (!types.contains(value)) types.add(value);
            }
        } else if (name.equalsIgnoreCase("Team")) {
            String id = xmlr.getAttributeValue("", "Id");
            if (id != null) {
                String key = id + value;

                if (!this.teams.containsKey(this.personKey)) {
                    this.teams.put(this.personKey, new LinkedHashMap<String, Map<String, Object>>());
                }
                Map<String, Map<String, Object>> teams = this.teams.get(this.personKey);
                if (!teams.containsKey(key)) {
                    teams.put(key, Helpers.getCodeNameObject(id, value));
                }
            }
        } else if (name.equalsIgnoreCase("AssociatedEvent")) {
            String id = xmlr.getAttributeValue("", "Id");
            if (id != null) {
                String key = id + value;

                if (!this.associatedevents.containsKey(this.personKey)) {
                    this.associatedevents.put(this.personKey, new LinkedHashMap<String, Map<String, Object>>());
                }
                Map<String, Map<String, Object>> events = this.associatedevents.get(this.personKey);
                if (!events.containsKey(key)) {
                    events.put(key, Helpers.getCodeNameObject(id, value));
                }
            }
        } else if (name.equalsIgnoreCase("AssociatedState")) {
            String id = xmlr.getAttributeValue("", "Id");
            if (id != null) {
                String key = id + value;

                if (!this.associatedstates.containsKey(this.personKey)) {
                    this.associatedstates.put(this.personKey, new LinkedHashMap<String, Map<String, Object>>());
                }
                Map<String, Map<String, Object>> states = this.associatedstates.get(this.personKey);
                if (!states.containsKey(key)) {
                    states.put(key, Helpers.getCodeNameObject(id, value));
                }
            }
        } else if (name.equalsIgnoreCase("ExtId")) {
            if (!this.extids.containsKey(this.personKey)) {
                this.extids.put(this.personKey, new ArrayList<String>());
            }
            List<String> ids = this.extids.get(this.personKey);
            if (!ids.contains(value)) ids.add(value);
        }
    }

    private Map<String, Object> setPlace(String code, String name, String system, Map<String, Map<String, Object>> places, XMLStreamReader xmlr, Map<String, Object> map) throws XMLStreamException {
        Map<String, Object> place = Helpers.getCodeNameObject(code, name);

        String key = String.format("%s-%s", code, name);
        boolean exists = places.containsKey(key);
        if (exists) {
            place = (Map<String, Object>) places.get(key);
        }

        String rel = calculateRel(system, xmlr);

        Helpers.addSchemeAndCreatorToMap(system, place);
        Helpers.addStringToMapList(rel, "rels", place);
        Helpers.addStringToMapList(xmlr.getAttributeValue("", "ParentId"), "parentids", place);

        String topparent = xmlr.getAttributeValue("", "TopParent");
        if (topparent != null && !place.containsKey("topparent")) {
            if (topparent.equalsIgnoreCase("true")) {
                place.put("topparent", true);
            } else if (topparent.equalsIgnoreCase("false")) {
                place.put("topparent", false);
            }
        }

        if (!exists) {
            this.locationtype = null;
            this.lat = null;
            this.lon = null;

            iterateOccurrenceProperties(xmlr, map, this::setPlaceProperties);

            if (this.locationtype != null) {
                place.put("locationtype", this.locationtype);
            }

            if (this.lon != null && this.lat != null) {
                Map<String, Object> geo = new LinkedHashMap<String, Object>();
                geo.put("type", "Point");
                geo.put("coordinates", new Number[]{this.lon, this.lat});
                place.put("geometry_geojson", geo);
            }
        }

        if (exists) {
            places.replace(key, place);
        } else {
            places.put(key, place);
        }

        return place;
    }

    private void setPlaceProperties(String name, String value, XMLStreamReader xmlr, Map<String, Object> map) {
        if (name.equalsIgnoreCase("LocationType")) {
            if (this.locationtype == null) {
                String id = xmlr.getAttributeValue("", "Id");
                this.locationtype = Helpers.getCodeNameObject(id, value);
            }

        } else if (name.equalsIgnoreCase("CentroidLongitude")) {
            if (this.lon == null) {
                this.lon = Helpers.parseNumber(value);
            }
        } else if (name.equalsIgnoreCase("CentroidLatitude")) {
            if (this.lat == null) {
                this.lat = Helpers.parseNumber(value);
            }
        }
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
            List<String> rels = new ArrayList<String>();
            rels.add("direct");
            company.put("rels", rels);
        }

        if (this.symbols == null) this.symbols = new HashMap<String, Map<String, Map<String, Object>>>();
        if (this.industries == null) this.industries = new HashMap<String, Map<String, Map<String, Object>>>();
        if (this.tickers == null) this.tickers = new HashMap<String, List<Map<String, Object>>>();
        if (this.exchanges == null) this.exchanges = new HashMap<String, List<Map<String, Object>>>();
        this.companyKey = key;

        iterateOccurrenceProperties(xmlr, map, this::setCompanyProperties);

        List<Map<String, Object>> tickers = this.tickers.containsKey(key) ? this.tickers.get(key) : null;
        List<Map<String, Object>> exchanges = this.exchanges.containsKey(key) ? this.exchanges.get(key) : null;

        if (tickers != null && tickers.size() > 0 && exchanges != null && exchanges.size() > 0) {
            for (Map<String, Object> ticker : tickers) {
                String parentid = (String) ticker.get("parentid");
                Map<String, Object> exchange = exchanges.get(0);
                if (parentid != null && !parentid.equalsIgnoreCase((String) exchange.get("id"))) {
                    for (Map<String, Object> exch : exchanges) {
                        if (parentid.equalsIgnoreCase((String) exch.get("id"))) {
                            exchange = exch;
                            break;
                        }
                    }
                }

                if (!this.symbols.containsKey(key)) {
                    this.symbols.put(key, new LinkedHashMap<String, Map<String, Object>>());
                }
                Map<String, Map<String, Object>> symbols = this.symbols.get(key);

                String e = ((String) exchange.get("value")).toUpperCase();
                String t = ((String) ticker.get("value")).toUpperCase();
                String k = String.format("%s:%s", e, t);
                if (!symbols.containsKey(k)) {
                    Map<String, Object> symbol = new LinkedHashMap<String, Object>();
                    symbol.put("instrument", k);
                    symbol.put("exchange", e);
                    symbol.put("ticker", t);
                    symbols.put(k, symbol);
                }
            }
        }

        Map<String, Map<String, Object>> symbols = this.symbols.containsKey(key) ? this.symbols.get(key) : null;
        if (symbols != null && symbols.size() > 0) {
            company.put("symbols", symbols.values());
        }
        Map<String, Map<String, Object>> industries = this.industries.containsKey(key) ? this.industries.get(key) : null;
        if (symbols != null && industries.size() > 0) {
            company.put("industries", industries.values());
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

            if (!this.symbols.containsKey(this.companyKey)) {
                this.symbols.put(this.companyKey, new LinkedHashMap<String, Map<String, Object>>());
            }

            if (tokens.length > 1 && !this.symbols.containsKey(key)) {
                Map<String, Object> symbol = new LinkedHashMap<String, Object>();
                symbol.put("instrument", key);
                symbol.put("exchange", tokens[0]);
                symbol.put("ticker", tokens[1]);
                this.symbols.get(this.companyKey).put(key, symbol);
            }
        } else if (name.equalsIgnoreCase("PrimaryTicker")
                || name.equalsIgnoreCase("Ticker")) {
            String parentid = xmlr.getAttributeValue("", "ParentId");
            Map<String, Object> ticker = new LinkedHashMap<String, Object>();
            ticker.put("parentid", parentid);
            ticker.put("value", value);

            if (!this.tickers.containsKey(this.companyKey)) {
                this.tickers.put(this.companyKey, new ArrayList<Map<String, Object>>());
            }
            this.tickers.get(this.companyKey).add(ticker);
        } else if (name.equalsIgnoreCase("APIndustry")) {
            String id = xmlr.getAttributeValue("", "Id");

            if (!this.industries.containsKey(this.companyKey)) {
                this.industries.put(this.companyKey, new LinkedHashMap<String, Map<String, Object>>());
            }

            if (id != null && !this.industries.containsKey(id)) {
                Map<String, Object> industry = new LinkedHashMap<String, Object>();
                industry.put("code", id);
                industry.put("name", value);
                this.industries.get(this.companyKey).put(id, industry);
            }
        } else if (name.equalsIgnoreCase("Exchange")) {
            String id = xmlr.getAttributeValue("", "Id");
            if (id != null) {
                Map<String, Object> exchange = new LinkedHashMap<String, Object>();
                exchange.put("id", id);
                exchange.put("value", value);

                if (!this.exchanges.containsKey(this.companyKey)) {
                    this.exchanges.put(this.companyKey, new ArrayList<Map<String, Object>>());
                }
                this.exchanges.get(this.companyKey).add(exchange);
            }
        }
    }

    private void iterateOccurrences(String parentName, String system, String authority, XMLStreamReader xmlr, Map<String, Object> map, OccurrenceParser action) throws XMLStreamException {
        boolean end = false;

        while (!end && xmlr.hasNext()) {
            xmlr.next();

            int eventType = xmlr.getEventType();

            if (eventType == XMLStreamReader.START_ELEMENT) {
                if (xmlr.getLocalName().equals("Occurrence")) {
                    action.parse(authority, system, xmlr, map);
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

    private String calculateRel(String system, XMLStreamReader xmlr) {
        if (system != null && system.equalsIgnoreCase("RTE")) {
            return "inferred";
        } else {
            String match = xmlr.getAttributeValue("", "ActualMatch");
            if (match != null) {
                if (match.equalsIgnoreCase("true")) return "direct";
                else if (match.equalsIgnoreCase("false")) return "ancestor";
            }
        }
        return null;
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
        void parse(String authority, String system, XMLStreamReader xmlr, Map<String, Object> map) throws XMLStreamException;
    }

    @FunctionalInterface
    private interface OccurrencePropertyParser {
        void parse(String name, String value, XMLStreamReader xmlr, Map<String, Object> map);
    }
}
