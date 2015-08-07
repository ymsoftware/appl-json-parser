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

    //title
    //geo

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
                Helpers.safeAdd(name.toLowerCase(), xmlr.getElementText(), this.filing);
                break;
            case "SlugLine":
                String text = xmlr.getElementText();
                if (text != null && text.length() > 0) {
                    Helpers.safeAdd(name.toLowerCase(), text, this.filing);
                    this.slugline = text;
                }
                break;
            case "Selector":
                text = xmlr.getElementText();
                if (text != null && text.length() > 0) {
                    Helpers.safeAdd(name.toLowerCase(), text, this.filing);
                    this.selector = text;
                }
                break;
            case "Id":
            case "ArrivalDateTime":
            case "Source":
                Helpers.safeAdd("filing" + name.toLowerCase(), xmlr.getElementText(), this.filing);
                break;
            case "Category":
                text = xmlr.getElementText();
                if (text != null && text.length() > 0) {
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
    }

    private void addCountry(String text) {
        if (text != null && text.length() > 0) {
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
        if (text != null && text.length() > 0) {
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
        if (text != null && text.length() > 0) {
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
                if (xmlr.getLocalName() == "Products") {
                    if (this.addProducts) {
                        this.filing.put("products", this.products);
                    }
                    break;
                }
            }
        }
    }

    private void addSubject(String text) {
        if (text != null && text.length() > 0) {
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
        String system = xmlr.getAttributeValue("", "System");
        if (system != null) {
            while (xmlr.hasNext()) {
                xmlr.next();

                int eventType = xmlr.getEventType();

                if (eventType == XMLStreamReader.START_ELEMENT) {
                    if (xmlr.getLocalName().equals("Keys")) {
                        String id = xmlr.getAttributeValue("", "Id");
                        if (id != null) {
                            String field = xmlr.getAttributeValue("", "Field");
                            if (field != null) {
                                String name = (system + field).replaceAll(" ", "").toLowerCase();
                                try {
                                    name = URLEncoder.encode(name, "utf-8");

                                    Map<String, Object> fk = new LinkedHashMap<String, Object>();
                                    fk.put(name, id);

                                    if (!this.addForeignKeys) {
                                        this.addForeignKeys = true;
                                        this.foreignkeys = new ArrayList<Map<String, Object>>();
                                        this.filing.put("foreignkeys", null);
                                    }

                                    this.foreignkeys.add(fk);

                                } catch (UnsupportedEncodingException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }

                } else if (eventType == XMLStreamReader.END_ELEMENT) {
                    if (xmlr.getLocalName() == "ForeignKeys") {
                        break;
                    }
                }
            }
        }
    }

    private void addRouting(XMLStreamReader xmlr) throws XMLStreamException {
        String type = xmlr.getAttributeValue("", "Type");
        if (type != null) {
            String expanded = xmlr.getAttributeValue("", "Expanded");
            String outed = xmlr.getAttributeValue("", "Outed");

            String text = xmlr.getElementText();
            if (text != null && text.length() > 0) {
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
