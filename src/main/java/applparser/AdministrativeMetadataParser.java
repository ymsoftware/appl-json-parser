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

    //signals

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
                //transmissionsources,function,array
                break;
            case "ProductSource":
                //productsources,function,array
                break;
            case "ItemContentType":
                //itemcontenttype,function,itemcontenttype
                break;
            case "DistributionChannel":
                //distributionchannels,function,array
                break;
            case "Fixture":
                //fixture,function,fixture
                break;
            case "InPackage":
                //inpackages,function,inpackages
                break;
            case "Rating":
                //ratings,function,ratings
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
