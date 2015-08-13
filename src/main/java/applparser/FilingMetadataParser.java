package applparser;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by ymetelkin on 7/29/15.
 */
public class FilingMetadataParser extends ApplParser {
    private Map<String, Object> filing;
    private boolean calculate;
    private String slugline;
    private String category;
    private String selector;
    private String source;
    private List<String> filingcountries;
    private boolean addFilingCountries;
    private List<String> filingregions;
    private boolean addFilingRegions;
    private List<String> filingtopics;
    private boolean addFilingTopics;
    private List<Integer> products;
    private boolean addProducts;
    private List<String> filingsubjects;
    private boolean addFilingSubjects;
    private List<Map<String, Object>> foreignkeys;
    private boolean addForeignKeys;
    private Map<String, Object> routings;
    private boolean addRoutings;

    public FilingMetadataParser() {
        this.filing = new LinkedHashMap<String, Object>();
    }

    @Override
    public void parse(String name, XMLStreamReader xmlr, Map<String, Object> map) throws XMLStreamException {
        switch (name) {
            case "Cycle":
            case "TransmissionReference":
            case "TransmissionFilename":
            case "TransmissionContent":
            case "ServiceLevelDesignator":
            case "Format":
            case "OriginalMediaId":
            case "ImportFolder":
            case "ImportWarnings":
            case "LibraryTwinCheck":
            case "LibraryRequestId":
            case "SpecialFieldAttn":
            case "FeedLine":
            case "LibraryRequestLogin":
            case "PriorityLine":
            case "FilingOnlineCode":
            case "DistributionScope":
            case "BreakingNews":
            case "FilingStyle":
            case "JunkLine":
                Helpers.safeAddString(name.toLowerCase(), xmlr.getElementText(), this.filing);
                break;
            case "SlugLine":
                String text = xmlr.getElementText();
                if (!Helpers.isNullOrEmpty(text)) {
                    text = text.trim();
                    Helpers.safeAdd(name.toLowerCase(), text, this.filing);
                    this.slugline = text;
                }
                break;
            case "Selector":
                text = xmlr.getElementText();
                if (!Helpers.isNullOrEmpty(text)) {
                    text = text.trim();
                    Helpers.safeAdd(name.toLowerCase(), text, this.filing);
                    this.selector = text;
                }
                break;
            case "Id":
                Helpers.safeAddStringId("filingid", xmlr.getElementText(), this.filing);
                break;
            case "ArrivalDateTime":
                Helpers.safeAdd("filingarrivaldatetime", Helpers.parseDate(xmlr.getElementText()), this.filing);
                break;
            case "Source":
                text = xmlr.getElementText();
                if (!Helpers.isNullOrEmpty(text)) {
                    text = text.trim();
                    Helpers.safeAdd("filingsource", text, this.filing);
                    this.source = text;
                }
                break;
            case "Category":
                text = xmlr.getElementText();
                if (!Helpers.isNullOrEmpty(text)) {
                    text = text.trim();
                    Helpers.safeAdd("filingcategory", text, this.filing);
                    this.category = text;
                }
                break;
            case "FilingCountry":
                addCountry(xmlr.getElementText());
                break;
            case "FilingRegion":
                addRegion(xmlr.getElementText());
                break;
            case "FilingTopic":
                addTopic(xmlr.getElementText());
                break;
            case "Routing":
                addRouting(xmlr);
                break;
            case "Products":
                addProducts(xmlr);
                break;
            case "ForeignKeys":
                addForeignKeys(xmlr);
                break;
            case "FilingSubject":
            case "FilingSubSubject":
                addSubject(xmlr.getElementText());
                break;
        }
    }

    @Override
    public void cleanup(Map<String, Object> map) {
        if (this.addFilingCountries) {
            this.filing.replace("filingcountries", null, this.filingcountries);
            this.addFilingCountries = false;
        }
        if (this.addFilingRegions) {
            this.filing.replace("filingregions", null, this.filingregions);
            this.addFilingRegions = false;
        }
        if (this.addFilingTopics) {
            this.filing.replace("filingtopics", null, this.filingtopics);
            this.addFilingTopics = false;
        }
        if (this.addFilingSubjects) {
            this.filing.replace("filingsubjects", null, this.filingsubjects);
            this.addFilingSubjects = false;
        }
        if (this.addForeignKeys) {
            this.filing.replace("foreignkeys", null, this.foreignkeys);
            this.addForeignKeys = false;
        }
        if (this.addRoutings) {
            this.filing.replace("routings", null, this.routings);
            this.addRoutings = false;
        }

        if (this.filing.size() > 0) {
            if (map.containsKey("filings")) {
                ((List<Map<String, Object>>) map.get("filings")).add(this.filing);
            } else {
                List<Map<String, Object>> filings = new ArrayList<Map<String, Object>>();
                filings.add(this.filing);
                map.put("filings", filings);
            }
        }

        if (!map.containsKey("title") || map.get("title") == null) {
            if (this.slugline != null && this.category != null) {
                if (this.category.equalsIgnoreCase("l") || this.category.equalsIgnoreCase("s")) {
                    if (map.containsKey("title")) {
                        map.replace("title", this.slugline);
                    } else {
                        map.put("title", this.slugline);
                    }
                }
            }
        }

        if (map.containsKey("addConsumerReady") && this.category != null) {
            if (this.category.equalsIgnoreCase("v")) {
                map.remove("addConsumerReady");
            } else if (this.selector != null) {
                String selector = this.selector.toLowerCase();
                if (this.category.equalsIgnoreCase("r") && selector.startsWith("apr")) {
                    map.remove("addConsumerReady");
                } else if (this.category.equalsIgnoreCase("t") && selector.startsWith("1tv")) {
                    map.remove("addConsumerReady");
                }
            }
        }

        if (map.containsKey("addStateAudienece")) {
            if (this.category != null && this.category.equalsIgnoreCase("n") && this.source != null && this.source.length() > 1) {
                String code = this.source.toUpperCase();
                if (code.length() > 2) code = code.substring(0, 2);

                String id = null;
                String name = null;

                switch (code) {
                    case "AL":
                        id = "b8099e4881d610048a11df092526b43e";
                        name = "Alabama";
                        break;
                    case "AK":
                        id = "cbb727a881d610048a29df092526b43e";
                        name = "Alaska";
                        break;
                    case "AZ":
                        id = "e427079081d610048a4edf092526b43e";
                        name = "Arizona";
                        break;
                    case "AR":
                        id = "687e74a082af1004823adf092526b43e";
                        name = "Arkansas";
                        break;
                    case "CA":
                        id = "789fdd8882af10048263df092526b43e";
                        name = "California";
                        break;
                    case "CO":
                        id = "902a5eb082af1004828adf092526b43e";
                        name = "Colorado";
                        break;
                    case "CT":
                        id = "a42dc0a082af100482a7df092526b43e";
                        name = "Connecticut";
                        break;
                    case "DE":
                        id = "bcadd4f882af100482c9df092526b43e";
                        name = "Delaware";
                        break;
                    case "FL":
                        id = "cb06ab1082af100482f8df092526b43e";
                        name = "Florida";
                        break;
                    case "GA":
                        id = "dec1cce882af10048320df092526b43e";
                        name = "Georgia";
                        break;
                    case "HI":
                        id = "ee324cc082af10048342df092526b43e";
                        name = "Hawaii";
                        break;
                    case "ID":
                        id = "1885b7f082b01004835edf092526b43e";
                        name = "Idaho";
                        break;
                    case "IL":
                        id = "2c6a186082b010048379df092526b43e";
                        name = "Illinois";
                        break;
                    case "IN":
                        id = "0760000082b2100483c7df092526b43e";
                        name = "Indiana";
                        break;
                    case "IA":
                        id = "1608ba1082b310048433df092526b43e";
                        name = "Iowa";
                        break;
                    case "KS":
                        id = "1e8c5a7082b310048450df092526b43e";
                        name = "Kansas";
                        break;
                    case "KY":
                        id = "2f6e294082b310048474df092526b43e";
                        name = "Kentucky";
                        break;
                    case "LA":
                        id = "43fb970882b310048496df092526b43e";
                        name = "Louisiana";
                        break;
                    case "ME":
                        id = "8d2caa7082b3100484b8df092526b43e";
                        name = "Maine";
                        break;
                    case "MD":
                        id = "b0fa317082b3100484dbdf092526b43e";
                        name = "Maryland";
                        break;
                    case "MA":
                        id = "bed6942882b310048501df092526b43e";
                        name = "Massachusetts";
                        break;
                    case "MI":
                        id = "6bf49b4082c410048696df092526b43e";
                        name = "Michigan";
                        break;
                    case "MN":
                        id = "9f12355082c4100486addf092526b43e";
                        name = "Minnesota";
                        break;
                    case "MS":
                        id = "b3dfffa882c4100486c3df092526b43e";
                        name = "Mississippi";
                        break;
                    case "MO":
                        id = "bd8c35d082c4100486d5df092526b43e";
                        name = "Missouri";
                        break;
                    case "MT":
                        id = "6429117882c610048770df092526b43e";
                        name = "Montana";
                        break;
                    case "NE":
                        id = "808300b882c610048788df092526b43e";
                        name = "Nebraska";
                        break;
                    case "NV":
                        id = "8bb89dd082c61004879fdf092526b43e";
                        name = "Nevada";
                        break;
                    case "NH":
                        id = "9531546082c6100487b5df092526b43e";
                        name = "New Hampshire";
                        break;
                    case "NJ":
                        id = "a0eed68882c6100487cddf092526b43e";
                        name = "New Jersey";
                        break;
                    case "NM":
                        id = "aacce28082c6100487e4df092526b43e";
                        name = "New Mexico";
                        break;
                    case "NY":
                        id = "b58f18a082c6100487fbdf092526b43e";
                        name = "New York";
                        break;
                    case "NC":
                        id = "c01d179082c610048813df092526b43e";
                        name = "North Carolina";
                        break;
                    case "ND":
                        id = "cbaeb75882c61004882adf092526b43e";
                        name = "North Dakota";
                        break;
                    case "OH":
                        id = "dcb000c082c610048843df092526b43e";
                        name = "Ohio";
                        break;
                    case "OK":
                        id = "f142e8e082c610048858df092526b43e";
                        name = "Oklahoma";
                        break;
                    case "OR":
                        id = "fe016fe882c61004886adf092526b43e";
                        name = "Oregon";
                        break;
                    case "PA":
                        id = "0b394d7082c71004887fdf092526b43e";
                        name = "Pennsylvania";
                        break;
                    case "RI":
                        id = "1bf4bc0882c71004889cdf092526b43e";
                        name = "Rhode Island";
                        break;
                    case "SC":
                        id = "29d11ec082c7100488aedf092526b43e";
                        name = "South Carolina";
                        break;
                    case "SD":
                        id = "5578469882c7100488badf092526b43e";
                        name = "South Dakota";
                        break;
                    case "TN":
                        id = "62532b5882c7100488cedf092526b43e";
                        name = "Tennessee";
                        break;
                    case "TX":
                        id = "6e92d9b882c7100488e5df092526b43e";
                        name = "Texas";
                        break;
                    case "UT":
                        id = "c1dff44882c710048903df092526b43e";
                        name = "Utah";
                        break;
                    case "VT":
                        id = "d2f8d8a882c710048915df092526b43e";
                        name = "Vermont";
                        break;
                    case "VA":
                        id = "eaed376082c71004892cdf092526b43e";
                        name = "Virginia";
                        break;
                    case "WA":
                        id = "08a0a00882c810048942df092526b43e";
                        name = "Washington";
                        break;
                    case "WV":
                        id = "130bcce882c81004895adf092526b43e";
                        name = "West Virginia";
                        break;
                    case "WI":
                        id = "1bc1bc3082c81004896cdf092526b43e";
                        name = "Wisconsin";
                        break;
                    case "WY":
                        id = "2fb83d4082c810048984df092526b43e";
                        name = "Wyoming";
                        break;
                    case "DC":
                        id = "788b364882f110048df3df092526b43e";
                        name = "District of Columbia";
                        break;
                    case "NYC":
                        id = "b836d07082c610048807df092526b43e";
                        name = "New York City";
                        break;
                }

                if (id != null) {
                    if (map.containsKey("audiences")) {
                        List<Map<String, Object>> audiences = (List<Map<String, Object>>) map.get("audiences");
                        final String test = id;
                        if (audiences.stream().noneMatch(e -> ((String) e.get("code")).equalsIgnoreCase(test))) {
                            audiences.add(getState(id, name));
                        }
                    } else {
                        List<Map<String, Object>> audiences = new ArrayList<Map<String, Object>>();
                        audiences.add(getState(id, name));
                        map.put("audiences", audiences);
                    }

                    map.remove("addStateAudienece");
                }
            }
        }
    }

    private Map<String, Object> getState(String code, String name) {
        Map<String, Object> state = new LinkedHashMap<String, Object>();
        state.put("code", code);
        state.put("name", name);
        state.put("type", "AUDGEOGRAPHY");
        return state;
    }

    private void addCountry(String text) {
        if (!Helpers.isNullOrEmpty(text)) {
            if (!this.addFilingCountries) {
                this.addFilingCountries = true;
                this.filingcountries = new ArrayList<String>();
                this.filing.put("filingcountries", null);
            }

            if (!this.filingcountries.contains(text)) {
                this.filingcountries.add(text);
            }
        }
    }

    private void addRegion(String text) {
        if (!Helpers.isNullOrEmpty(text)) {
            if (!this.addFilingRegions) {
                this.addFilingRegions = true;
                this.filingregions = new ArrayList<String>();
                this.filing.put("filingregions", null);
            }

            if (!this.filingregions.contains(text)) {
                this.filingregions.add(text);
            }
        }
    }

    private void addTopic(String text) {
        if (!Helpers.isNullOrEmpty(text)) {
            if (!this.addFilingTopics) {
                this.addFilingTopics = true;
                this.filingtopics = new ArrayList<String>();
                this.filing.put("filingtopics", null);
            }

            if (!this.filingtopics.contains(text)) {
                this.filingtopics.add(text);
            }
        }
    }

    private void addProducts(XMLStreamReader xmlr) throws XMLStreamException {
        while (xmlr.hasNext()) {
            xmlr.next();

            int eventType = xmlr.getEventType();

            if (eventType == XMLStreamReader.START_ELEMENT) {
                if (xmlr.getLocalName().equals("Product")) {
                    Integer product = Helpers.parseInteger(xmlr.getElementText());
                    if (product != null) {
                        if (!this.addProducts) {
                            this.addProducts = true;
                            this.products = new ArrayList<Integer>();
                        }

                        if (!this.products.contains(product)) {
                            this.products.add(product);
                        }
                    }
                }

            } else if (eventType == XMLStreamReader.END_ELEMENT) {
                if (xmlr.getLocalName().equals("Products")) {
                    if (this.addProducts) {
                        this.filing.put("products", this.products);
                    }
                    break;
                }
            }
        }
    }

    private void addSubject(String text) {
        if (!Helpers.isNullOrEmpty(text)) {
            if (!this.addFilingSubjects) {
                this.addFilingSubjects = true;
                this.filingsubjects = new ArrayList<String>();
                this.filing.put("filingsubjects", null);
            }

            if (!this.filingsubjects.contains(text)) {
                this.filingsubjects.add(text);
            }
        }
    }

    private void addForeignKeys(XMLStreamReader xmlr) throws XMLStreamException {
        List<Map<String, Object>> foreignkeys = Helpers.getForeignKeys(xmlr);
        if (foreignkeys != null && foreignkeys.size() > 0) {
            if (this.addForeignKeys) {
                this.foreignkeys.addAll(foreignkeys);
            } else {
                this.addForeignKeys = true;
                this.foreignkeys = foreignkeys;
                this.filing.put("foreignkeys", null);
            }
        }
    }

    private void addRouting(XMLStreamReader xmlr) throws XMLStreamException {
        String type = xmlr.getAttributeValue("", "Type");
        if (type != null) {
            String expanded = xmlr.getAttributeValue("", "Expanded");
            String outed = xmlr.getAttributeValue("", "Outed");

            String text = xmlr.getElementText();
            if (!Helpers.isNullOrEmpty(text)) {
                if (!this.addRoutings) {
                    this.addRoutings = true;
                    this.routings = new LinkedHashMap<String, Object>();
                    this.filing.put("routings", null);
                }
                /*
                @Outed=true only, use {@Type}out;
                @Expanded=true and @Outed=true, use expanded{@Type}outs;
                @Expanded=true and @Outed=false, use expanded{@Type}adds.
                @Expanded=false and @Outed=true, use {@Type}outs;
                @Expanded=false and @Outed=false, use {@Type}adds;
                retrieve the value from Routing, treat the string as space delimited routing codes,
                and convert the string to an array of multiple unique strings.
                */
                expanded = expanded != null && expanded.equalsIgnoreCase("true") ? "expanded" : "";
                outed = outed != null && outed.equalsIgnoreCase("true") ? "out" : "add";
                type = String.format("%s%s%ss", expanded, type.toLowerCase(), outed);

                if (!this.routings.containsKey(type)) {
                    List<String> values = new ArrayList<String>();

                    for (String token : text.split(" ")) {
                        String value = token.trim();
                        if (value != "" && !values.contains(value)) {
                            values.add(value);
                        }
                    }

                    this.routings.put(type, values);
                }
            }
        }
    }
}
