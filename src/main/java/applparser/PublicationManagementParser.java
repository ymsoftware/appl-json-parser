package applparser;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by ymetelkin on 7/27/15.
 */
public class PublicationManagementParser extends ApplParser {
    private String releaseDateTime;
    private boolean embargoed;
    private List<Map<String, Object>> associations;
    private Map<String, Integer> types;
    private int rank;
    private boolean addAssociations;
    private List<String> instructions;
    private boolean addInstructions;
    private List<Map<String, Object>> timeRestrictions;
    private Map<String, Boolean> include;
    private boolean addTimeRestrictions;
    private List<String> signals;
    private boolean addSignals;

    @Override
    public void parse(String name, XMLStreamReader xmlr, Map<String, Object> map) throws XMLStreamException {
        switch (name) {
            case "RecordType":
            case "FilingType":
            case "ChangeEvent":
            case "ItemKey":
            case "SpecialInstructions":
            case "EditorialId":
            case "Function":
                parse(name.toLowerCase(), xmlr.getElementText(), map);
                break;
            case "ArrivalDateTime":
            case "LastModifiedDateTime":
            case "ReleaseDateTime":
            case "ItemStartDateTime":
            case "ItemStartDateTimeActual":
            case "ItemExpireDateTime":
            case "SearchDateTime":
            case "ItemEndDateTime":
                setDate(name.toLowerCase(), xmlr, map);
                break;
            case "FirstCreated":
                setFirstCreated(xmlr, map);
                break;
            case "Status":
                setStatus(xmlr, map);
                break;
            case "AssociatedWith":
                setAssociation(name, xmlr, map);
                break;
            case "Editorial":
                setEditorialTypes(name, xmlr, map);
                break;
            case "Instruction":
                setInstructions(name, xmlr, map);
                break;
            case "TimeRestrictions":
                setTimeRestrictions(name, xmlr, map);
                break;
            case "ExplicitWarning":
                setSignal("explicitcontent", "1", xmlr, map);
                break;
            case "IsDigitized":
                setSignal("isnotdigitized", "false", xmlr, map);
                break;
        }
    }

    @Override
    public void cleanup(Map<String, Object> map) {
        if (this.addAssociations) {
            map.replace("associations", null, this.associations);
            this.addAssociations = false;
        }

        if (this.addInstructions) {
            map.replace("outinginstructions", null, this.instructions);
            this.addInstructions = false;
        }

        if (this.addTimeRestrictions) {
            map.replace("timerestrictions", null, this.timeRestrictions);
            this.addTimeRestrictions = false;
        }

        if (this.addSignals) {
            map.replace("signals", null, this.signals);
            this.addSignals = false;
        }
    }

    private void setDate(String name, XMLStreamReader xmlr, Map<String, Object> map) throws XMLStreamException {
        String date = parseDate(xmlr.getElementText());
        if (date != null) {
            if (name.equals("releasedatetime")) {
                if (this.embargoed) {
                    map.put("embargoed", date);
                } else {
                    this.releaseDateTime = date;
                }
            }

            parse(name, date, map);
        }
    }

    private void setFirstCreated(XMLStreamReader xmlr, Map<String, Object> map) throws XMLStreamException {
        Integer year = parseInteger("Year", xmlr);
        if (year != null) {
            Integer month = parseInteger("Month", xmlr);
            Integer day = parseInteger("Day", xmlr);
            String value = null;

            if (month == null) {
                value = String.valueOf(year);
            } else {
                String m = String.valueOf(month);
                if (month < 10) {
                    m = "0" + m;
                }

                if (day == null) {
                    value = String.format("%d-%s", year, m);
                } else {
                    String d = String.valueOf(day);
                    if (day < 10) {
                        d = "0" + d;
                    }

                    String time = xmlr.getAttributeValue("", "Time");

                    if (time == null) {
                        value = String.format("%d-%s-%s", year, m, d);
                    } else {
                        value = String.format("%d-%s-%sT%sZ", year, m, d, time);

                    }
                }
            }

            map.put("firstcreated", value);
        }
    }

    private void setStatus(XMLStreamReader xmlr, Map<String, Object> map) throws XMLStreamException {
        String status = xmlr.getElementText();
        if (status != null && status.length() > 1) {
            if (!(status.equalsIgnoreCase("withheld") || status.equalsIgnoreCase("canceled"))) {
                status = "usable";
            }
        } else {
            status = "usable";
        }

        map.put("pubstatus", status);
    }

    private void setAssociation(String name, XMLStreamReader xmlr, Map<String, Object> map) throws XMLStreamException {
         /*
            each object $.associations[i] has five name/value pairs, $.associations[i].type, $.associations[i].itemid, $.associations[i].representationtype, $.associations[i].associationrank and $associations[i].typerank;
            retrieve the value from AssociatedWith/@CompositionType, use Appendix III: CompositionType/Type Lookup Table to derive the value for $.associations.association{i}.type;
            test the value of AssociatedWith, if its not all zeros, load as is to  $.associations[i].itemid;
            hardcode partial for $.associations[i].representationtype;
            load the sequence number of the AssociatedWith node (a number starting at 1) to $.associations[i].associationrank as a number;
            load the sequence number of the AssociatedWith node by @CompositionType (a number starting at 1) to $.associations[i].typerank as a number, note that CompositionType may be absent OR StandardIngestedContent and any such AssociatedWith nodes should be ranked on their own;
            */

        if (this.associations == null) {
            this.associations = new ArrayList<Map<String, Object>>();
            this.types = new HashMap<String, Integer>();
            this.rank = 0;
        }

        String type = getCompositionType(xmlr.getAttributeValue("", "CompositionType"));

        String id = xmlr.getElementText();
        if (id != null) {
            if (!this.addAssociations) {
                map.put("associations", null);
                this.addAssociations = true;
            }

            Map<String, Object> ass = new LinkedHashMap<String, Object>();

            if (type != "notype") {
                ass.put("type", type);
            }

            ass.put("itemid", id.toLowerCase());
            ass.put("represantationtype", "partial");

            this.rank += 1;
            ass.put("associationrank", this.rank);

            Integer typerank = this.types.getOrDefault(type, 0);
            typerank += 1;
            this.types.put(type, typerank);
            ass.put("typerank", typerank);

            this.associations.add(ass);
        }
    }

    private String getCompositionType(String type) {
        if (type == null) return "notype";

        if (type.equalsIgnoreCase("text")) return "text";
        if (type.equalsIgnoreCase("photo")) return "photo";
        if (type.equalsIgnoreCase("video")) return "video";
        if (type.equalsIgnoreCase("audio")) return "audio";
        if (type.equalsIgnoreCase("graphic")) return "graphic";
        if (type.equalsIgnoreCase("complexdata")) return "complexdata";

        if (type.equalsIgnoreCase("StandardText")) return "text";
        if (type.equalsIgnoreCase("StandardPrintPhoto")) return "photo";
        if (type.equalsIgnoreCase("StandardOnlinePhoto")) return "photo";
        if (type.equalsIgnoreCase("StandardPrintGraphic")) return "graphic";
        if (type.equalsIgnoreCase("StandardOnlineGraphic")) return "graphic";
        if (type.equalsIgnoreCase("StandardBroadcastVideo")) return "video";
        if (type.equalsIgnoreCase("StandardOnlineVideo")) return "video";
        if (type.equalsIgnoreCase("StandardBroadcastAudio")) return "audio";
        if (type.equalsIgnoreCase("StandardOnlineAudio")) return "audio";
        if (type.equalsIgnoreCase("StandardLibraryVideo")) return "video";
        if (type.equalsIgnoreCase("StandardInteractive")) return "complexdata";
        if (type.equalsIgnoreCase("StandardBroadcastGraphic")) return "graphic";
        if (type.equalsIgnoreCase("StandardBroadcastPhoto")) return "photo";

        return "notype";
    }

    private void setInstructions(String name, XMLStreamReader xmlr, Map<String, Object> map) throws XMLStreamException {
        if (this.instructions == null) {
            this.instructions = new ArrayList<String>();
        }

        String type = xmlr.getAttributeValue("", "Type");
        if (type.equalsIgnoreCase("Outing")) {
            String text = xmlr.getElementText();
            if (text != null) {
                if (!this.addInstructions) {
                    map.put("outinginstructions", null);
                    this.addInstructions = true;
                }
                this.instructions.add(text);
            }
        }
    }

    private void setTimeRestrictions(String name, XMLStreamReader xmlr, Map<String, Object> map) throws XMLStreamException {
        if (this.timeRestrictions == null) {
            this.timeRestrictions = new ArrayList<Map<String, Object>>();
            this.include = new HashMap<String, Boolean>();
        }

        String system = xmlr.getAttributeValue("", "System");
        if (system == null) system = "";

        String zone = xmlr.getAttributeValue("", "Zone");
        if (zone == null) zone = "";

        String key = String.format("%s%s", system, zone).toLowerCase();
        if (key.length() > 0) {
            if (!this.addTimeRestrictions) {
                map.put("timerestrictions", null);
                this.addTimeRestrictions = true;
            }

            if (!this.include.containsKey(key)) {
                String include = xmlr.getAttributeValue("", "Include");
                Boolean isInclude = include.equalsIgnoreCase("true");
                this.include.put(key, isInclude);

                Map<String, Object> times = new LinkedHashMap<String, Object>();
                times.put(key, isInclude);
                this.timeRestrictions.add(times);
            }
        }
    }

    private void setSignal(String signal, String test, XMLStreamReader xmlr, Map<String, Object> map) throws XMLStreamException {
        if (this.signals == null) {
            this.signals = new ArrayList<String>();
        }

        String text = xmlr.getElementText();
        if (text != null && text.equalsIgnoreCase(test)) {
            if (!this.addSignals) {
                map.put("signals", null);
                this.addSignals = true;
            }
            this.signals.add(signal);
        }
    }

    private void setEditorialTypes(String name, XMLStreamReader xmlr, Map<String, Object> map) throws XMLStreamException {
        EditorialTypesParser parser = new EditorialTypesParser();
        List<String> list = parser.parse(name, xmlr);
        map.put("editorialtypes", list);

        if (parser.isEmbargoed()) {
            if (this.releaseDateTime == null) {
                this.embargoed = true;
            } else {
                map.put("embargoed", this.releaseDateTime);
            }
        }
    }

    private class EditorialTypesParser extends ListParser<String> {
        private boolean embargoed;

        public boolean isEmbargoed() {
            return this.embargoed;
        }

        @Override
        void add(List<String> list, XMLStreamReader xmlr) throws XMLStreamException {
            if ("Type".equals(xmlr.getLocalName())) {
                String text = xmlr.getElementText();
                list.add(text);

                if (!this.embargoed && (text.equalsIgnoreCase("Advance") || text.equalsIgnoreCase("HoldForRelease"))) {
                    this.embargoed = true;
                }
            }
        }
    }
}
