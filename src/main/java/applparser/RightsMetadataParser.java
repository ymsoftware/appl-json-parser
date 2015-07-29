package applparser;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.time.LocalDate;
import java.util.Date;
import java.util.Map;

/**
 * Created by ymetelkin on 7/29/15.
 */
public class RightsMetadataParser extends ApplParser {
    @Override
    public void parse(String name, XMLStreamReader xmlr, Map<String, Object> map) throws XMLStreamException {
        switch (name) {
            case "Copyright":
                setCopyright(name, xmlr, map);
                break;
            case "UsageRights":
                //usagerights,function,usagerights
                break;
        }
    }

    @Override
    public void cleanup(Map<String, Object> map) {
//        if (this.addPersons) {
//            map.replace("persons", null, this.persons);
//            this.addPersons = false;
//        }
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
}
