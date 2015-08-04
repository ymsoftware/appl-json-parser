package applparser;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by ymetelkin on 7/29/15.
 */
public class AdministrativeMetadataParser extends ApplParser {
    private List<Map<String, Object>> sources;
    private boolean addSources;
    private List<Map<String, Object>> sourcematerials;
    private boolean addSourceMaterials;
    private boolean canonicallink;
    private List<String> transmissionsources;
    private boolean addTransmissionSources;
    private List<String> productsources;
    private boolean addProductSources;
    private List<String> distributionchannels;
    private boolean addDistributionChannels;
    private List<String> inpackages;
    private boolean addInPackages;
    private List<Map<String, Object>> ratings;
    private boolean addRating;
    private List<String> signals;
    private boolean addSignals;
    private boolean calculate = true;

    @Override
    public void parse(String name, XMLStreamReader xmlr, Map<String, Object> map) throws XMLStreamException {
        switch (name) {
            case "Creator":
            case "Contributor":
            case "WorkflowStatus":
            case "Workgroup":
                parse(name.toLowerCase(), xmlr.getElementText(), map);
                break;
            case "ContentElement":
                parse("editorialrole", xmlr.getElementText(), map);
                break;
            case "Provider":
                setProvider(name.toLowerCase(), xmlr, map);
                break;
            case "Source":
                setSource(xmlr, map);
                break;
            case "SourceMaterial":
                setSourceMaterial(name, xmlr, map);
                break;
            case "TransmissionSource":
                setTransmissionSource(xmlr, map);
                break;
            case "ProductSource":
                setProductSource(xmlr, map);
                break;
            case "ItemContentType":
                setItemContentType(name.toLowerCase(), xmlr, map);
                break;
            case "DistributionChannel":
                setDistributionChannel(xmlr, map);
                break;
            case "Fixture":
                setFixture(name.toLowerCase(), xmlr, map);
                break;
            case "InPackage":
                setInPackage(xmlr, map);
                break;
            case "Rating":
                setRating(xmlr, map);
                break;
            case "Reach":
                setReach(xmlr, map);
                break;
            case "Signal":
                setSignal(xmlr, map);
                break;
            case "ConsumerReady":
                setConsumerReady(xmlr, map);
                break;
        }
    }

    @Override
    public void cleanup(Map<String, Object> map) {
        if (this.addSources) {
            map.replace("sources", null, this.sources);
            this.addSources = false;
        }
        if (this.addSourceMaterials) {
            map.replace("sourcematerials", null, this.sourcematerials);
            this.addSourceMaterials = false;
        }
        if (this.addTransmissionSources) {
            map.replace("transmissionsources", null, this.transmissionsources);
            this.addTransmissionSources = false;
        }
        if (this.addProductSources) {
            map.replace("productsources", null, this.productsources);
            this.addProductSources = false;
        }
        if (this.addDistributionChannels) {
            map.replace("distributionchannels", null, this.distributionchannels);
            this.addDistributionChannels = false;
        }
        if (this.addInPackages) {
            map.replace("inpackages", null, this.inpackages);
            this.addInPackages = false;
        }
        if (this.addRating) {
            map.replace("ratings", null, this.ratings);
            this.addRating = false;
        }
        if (this.addSignals) {
            map.replace("signals", this.signals);
            this.addSignals = false;
        }
        if (this.calculate && map.containsKey("editorialtypes")) {
            List<String> editorialtypes = (List<String>) map.get("editorialtypes");
            String type = null;

            for (String editorialtype : editorialtypes) {
                if (editorialtype.equalsIgnoreCase("Advisory")
                        || editorialtype.equalsIgnoreCase("Disregard")
                        || editorialtype.equalsIgnoreCase("Elimination")
                        || editorialtype.equalsIgnoreCase("Withhold")) {
                    this.calculate = false;
                    break;
                } else if (editorialtype.equalsIgnoreCase("Correction")
                        || editorialtype.equalsIgnoreCase("Add")) {
                    if (type == null) {
                        type = map.containsKey("type") ? (String) map.get("type") : "";
                    }
                    if (type.equalsIgnoreCase("text")) {
                        this.calculate = false;
                        break;
                    }
                }
            }
        }

        if (this.calculate) {
            map.put("addConsumerReady", true);
        }
    }

    private void setProvider(String name, XMLStreamReader xmlr, Map<String, Object> map) throws XMLStreamException {
        Map<String, Object> provider = new LinkedHashMap<String, Object>();

        String code = xmlr.getAttributeValue("", "Id");
        if (code != null) provider.put("code", code);

        String type = xmlr.getAttributeValue("", "Type");
        if (type != null) provider.put("type", type);

        type = xmlr.getAttributeValue("", "SubType");
        if (type != null) provider.put("subtype", type);

        String text = xmlr.getElementText();
        if (text != null && text.length() > 0) provider.put("name", text);

        if (provider.size() > 0) {
            parse(name, provider, map);
        }
    }

    private void setSource(XMLStreamReader xmlr, Map<String, Object> map) throws XMLStreamException {
        Map<String, Object> source = new LinkedHashMap<String, Object>();

        String city = xmlr.getAttributeValue("", "City");
        if (city != null) source.put("city", city);

        String countryarea = xmlr.getAttributeValue("", "CountryArea");
        if (countryarea != null) source.put("countryarea", countryarea);

        String country = xmlr.getAttributeValue("", "Country");
        if (country != null) source.put("country", country);

        String county = xmlr.getAttributeValue("", "County");
        if (county != null) source.put("county", county);

        String url = xmlr.getAttributeValue("", "Url");
        if (url != null) source.put("url", url);

        String code = xmlr.getAttributeValue("", "Id");
        if (code != null) source.put("code", code);

        String type = xmlr.getAttributeValue("", "Type");
        if (type != null) source.put("type", type);

        type = xmlr.getAttributeValue("", "SubType");
        if (type != null) source.put("subtype", type);

        String text = xmlr.getElementText();
        if (text != null && text.length() > 0) source.put("name", text);

        if (source.size() > 0) {
            if (!this.addSources) {
                map.put("sources", null);
                this.addSources = true;
                this.sources = new ArrayList<Map<String, Object>>();
            }

            this.sources.add(source);
        }
    }

    private void setSourceMaterial(String parent, XMLStreamReader xmlr, Map<String, Object> map) throws XMLStreamException {
        String name = xmlr.getAttributeValue("", "Name");

        if (name != null && name.equalsIgnoreCase("alternate")) {
            if (!this.canonicallink) {
                ObjectParser parser = new SourceMaterialParser(this, true);
                Map<String, Object> meta = parser.parse(parent, xmlr);
                if (meta.size() > 0 && meta.containsKey("url")) {
                    map.put("canonicallink", meta.get("url"));
                    this.canonicallink = true;
                }
            }
        } else {
            Map<String, Object> source = new LinkedHashMap<String, Object>();

            if (name != null && name.length() > 0) source.put("name", name);

            String code = xmlr.getAttributeValue("", "Id");
            if (code != null) source.put("code", code);

            ObjectParser parser = new SourceMaterialParser(this, false);
            Map<String, Object> meta = parser.parse(parent, xmlr);
            if (meta.size() > 0) source.putAll(meta);

            if (source.size() > 0) {
                if (!this.addSourceMaterials) {
                    map.put("sourcematerials", null);
                    this.addSourceMaterials = true;
                    this.sourcematerials = new ArrayList<Map<String, Object>>();
                }

                this.sourcematerials.add(source);
            }
        }
    }

    private void setTransmissionSource(XMLStreamReader xmlr, Map<String, Object> map) throws XMLStreamException {
        if (this.transmissionsources == null) {
            this.transmissionsources = new ArrayList<String>();
        }

        String text = xmlr.getElementText();
        if (text != null) {
            if (!this.addTransmissionSources) {
                map.put("transmissionsources", null);
                this.addTransmissionSources = true;
            }
            if (!this.transmissionsources.contains(text)) this.transmissionsources.add(text);
        }
    }

    private void setProductSource(XMLStreamReader xmlr, Map<String, Object> map) throws XMLStreamException {
        if (this.productsources == null) {
            this.productsources = new ArrayList<String>();
        }

        String text = xmlr.getElementText();
        if (text != null) {
            if (!this.addProductSources) {
                map.put("productsources", null);
                this.addProductSources = true;
            }
            if (!this.productsources.contains(text)) this.productsources.add(text);
        }
    }

    private void setDistributionChannel(XMLStreamReader xmlr, Map<String, Object> map) throws XMLStreamException {
        if (this.distributionchannels == null) {
            this.distributionchannels = new ArrayList<String>();
        }

        String text = xmlr.getElementText();
        if (text != null) {
            if (!this.addDistributionChannels) {
                map.put("distributionchannels", null);
                this.addDistributionChannels = true;
            }
            if (!this.distributionchannels.contains(text)) this.distributionchannels.add(text);
        }
    }

    private void setItemContentType(String name, XMLStreamReader xmlr, Map<String, Object> map) throws XMLStreamException {
        Map<String, Object> itemcontenttype = new LinkedHashMap<String, Object>();

        String creator = xmlr.getAttributeValue("", "System");
        if (creator != null) itemcontenttype.put("creator", creator);

        String code = xmlr.getAttributeValue("", "Id");
        if (code != null) itemcontenttype.put("code", code);

        String text = xmlr.getElementText();
        if (text != null && text.length() > 0) {
            itemcontenttype.put("name", text);

            if (text.equalsIgnoreCase("Advisory")) {
                this.calculate = false;
            }
        }

        if (itemcontenttype.size() > 0) {
            parse(name, itemcontenttype, map);
        }
    }

    private void setFixture(String name, XMLStreamReader xmlr, Map<String, Object> map) throws XMLStreamException {
        Map<String, Object> provider = new LinkedHashMap<String, Object>();

        String code = xmlr.getAttributeValue("", "Id");
        if (code != null) provider.put("code", code);

        String text = xmlr.getElementText();
        if (text != null && text.length() > 0) provider.put("name", text);

        if (provider.size() > 0) {
            parse(name, provider, map);
        }
    }

    private void setInPackage(XMLStreamReader xmlr, Map<String, Object> map) throws XMLStreamException {
        if (this.inpackages == null) {
            this.inpackages = new ArrayList<String>();
        }

        String text = xmlr.getElementText();
        if (text != null) {
            if (!this.addInPackages) {
                map.put("inpackages", null);
                this.addInPackages = true;
            }

            for (String token : text.split(" ")) {
                if (!this.inpackages.contains(token)) this.inpackages.add(token);
            }
        }
    }

    private void setRating(XMLStreamReader xmlr, Map<String, Object> map) throws XMLStreamException {
        Map<String, Object> rating = new LinkedHashMap<String, Object>();

        Integer value = parseInteger("Value", xmlr);
        if (value != null) rating.put("rating", value);

        value = parseInteger("ScaleMin", xmlr);
        if (value != null) rating.put("scalemin", value);

        value = parseInteger("ScaleMax", xmlr);
        if (value != null) rating.put("scalemax", value);

        String scaleunit = xmlr.getAttributeValue("", "ScaleUnit");
        if (scaleunit != null) rating.put("scaleunit", scaleunit);

        value = parseInteger("Raters", xmlr);
        if (value != null) rating.put("raters", value);

        String ratertype = xmlr.getAttributeValue("", "RaterType");
        if (ratertype != null) rating.put("ratertype", ratertype);

        String creator = xmlr.getAttributeValue("", "Creator");
        if (creator != null) rating.put("creator", creator);

        if (rating.size() > 0) {
            if (!this.addRating) {
                map.put("ratings", null);
                this.addRating = true;
                this.ratings = new ArrayList<Map<String, Object>>();
            }

            this.ratings.add(rating);
        }
    }

    private void setReach(XMLStreamReader xmlr, Map<String, Object> map) throws XMLStreamException {
        String text = xmlr.getElementText();
        if (text != null && !text.equalsIgnoreCase("unknown")) {
            setSignals(map);
            this.signals.add(text);
        }
    }

    private void setSignal(XMLStreamReader xmlr, Map<String, Object> map) throws XMLStreamException {
        String text = xmlr.getElementText();
        if (text != null && text.length() > 0) {
            setSignals(map);
            this.signals.add(text);

            if (text.equalsIgnoreCase("test")) this.calculate = false;
        }
    }

    private void setConsumerReady(XMLStreamReader xmlr, Map<String, Object> map) throws XMLStreamException {
        String text = xmlr.getElementText();
        if (text != null) {
            if (text.equalsIgnoreCase("true") || text.equalsIgnoreCase("yes")) {
                setSignals(map);
                this.signals.add("consumerready");
                this.calculate = false;
            } else if (!text.equalsIgnoreCase("unknown")) {
                this.calculate = false;
            }
        }
    }

    private void setSignals(Map<String, Object> map) {
        if (!this.addSignals) {
            if (map.containsKey("signals")) {
                this.signals = (List<String>) map.get("signals");
            } else {
                map.put("signals", new ArrayList<String>());
                this.signals = new ArrayList<String>();
            }

            this.addSignals = true;
        }
    }

    private class SourceMaterialParser extends ObjectParser {
        private ApplParser parent;
        private boolean urlOnly;

        public SourceMaterialParser(ApplParser parent, boolean urlOnly) {
            this.parent = parent;
            this.urlOnly = urlOnly;
        }

        @Override
        void add(Map<String, Object> map, XMLStreamReader xmlr) throws XMLStreamException {
            String name = xmlr.getLocalName();

            if (this.urlOnly) {
                if (name.equalsIgnoreCase("url")) {
                    this.parent.parse(name.toLowerCase(), xmlr.getElementText(), map);
                }
            } else {
                switch (name) {
                    case "Type":
                    case "Url":
                    case "PermissionGranted":
                        this.parent.parse(name.toLowerCase(), xmlr.getElementText(), map);
                        break;
                }
            }
        }
    }
}
