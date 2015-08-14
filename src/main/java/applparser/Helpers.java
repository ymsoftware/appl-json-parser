package applparser;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutionException;

/**
 * Created by ymetelkin on 8/4/15.
 */
public class Helpers {
    public static void safeAdd(String field, Object value, Map<String, Object> map) {
        if (value != null) {
            map.put(field, value);
        }
    }

    public static void safeAddString(String field, String value, Map<String, Object> map) {
        if (!isNullOrEmpty(value)) {
            map.put(field, value.trim());
        }
    }

    public static void safeAddStringId(String field, String value, Map<String, Object> map) {
        if (!isNullOrEmpty(value)) {
            map.put(field, value.trim().toLowerCase());
        }
    }

    public static void safeAddInteger(String field, String value, Map<String, Object> map) {
        if (!isNullOrEmpty(value)) {
            try {
                map.put(field, Integer.parseUnsignedInt(value));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static Integer parseInteger(String value) {
        try {
            if (value != null) {
                return Integer.parseUnsignedInt(value);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Long parseLong(String value) {
        try {
            if (value != null) {
                return Long.parseUnsignedLong(value);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Number parseNumber(String value) {
        try {
            if (value != null) {
                if (value.contains(".")) {
                    return value.length() > 7 ? Double.parseDouble(value) : Float.parseFloat(value);
                } else {
                    return value.length() > 9 ? Long.parseLong(value) : Integer.parseInt(value);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String parseDate(String date) {
        if (date != null && date.length() > 1) {
            if (date.indexOf('T') > 0) {
//                String[] tokens = date.split("T");
//                String time = tokens[1];
//
//                String[] signs = new String[]{"+", "-"};
//                for (String sign : signs) {
//                    if (time.contains(sign)) {
//                        int idx = time.indexOf(sign);
//                        time = String.format("%s%s%s", time.substring(0, idx), sign, time.substring(idx + 1).replace(":", ""));
//                        try {
//                            date = String.format("%sT%s", tokens[0], time);
//                            Date test = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").parse(date);
//                            date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").format(test);
//                            return date;
//                        } catch (ParseException e) {
//                            e.printStackTrace();
//                        }
//                        break;
//                    }
//                }

                String[] replaces = new String[]{"GMT", "+00:00", "-00:00"};
                for (String replace : replaces) {
                    if (date.endsWith(replace)) {
                        return date.replace(replace, "Z");
                    }
                }

                if (!date.endsWith("Z")) {
                    date = date + "Z";
                }

                return date;
            }

            try {
                Date test = new SimpleDateFormat("yyyy-MM-dd").parse(date);
                return date + "T00:00:00Z";
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    public static Map<String, Object> getCodeNameObject(String code, String name) {
        boolean hasCode = !isNullOrEmpty(code);
        boolean hasName = !isNullOrEmpty(name);
        if (hasCode || hasName) {
            Map<String, Object> map = new LinkedHashMap<String, Object>();
            if (hasCode) map.put("code", code);
            if (hasName) map.put("name", name);
            return map;
        } else {
            return null;
        }
    }

    public static void addStringToMapList(String value, String key, Map<String, Object> map) {
        if (value != null) {
            if (!map.containsKey(key)) {
                map.put(key, new ArrayList<String>());
            }

            List<String> list = (List<String>) map.get(key);
            if (!list.contains(value)) {
                list.add(value.trim());
            }
        }
    }

    public static void addListToMapList(List<String> values, String key, Map<String, Object> map) {
        if (values != null && values.size() > 0) {
            if (!map.containsKey(key)) {
                map.put(key, new ArrayList<String>());
            }

            List<String> list = (List<String>) map.get(key);

            for (String value : values) {
                if (!list.contains(value)) {
                    list.add(value.trim());
                }
            }
        }
    }

    public static void addSchemeAndCreatorToMap(String system, Map<String, Object> map) {
        if (!map.containsKey("scheme")) {
            map.put("scheme", "http://cv.ap.org/id/");
        }

        if (system != null) {
            if (map.containsKey("creator")) {
                if (system.equalsIgnoreCase("editorial")) {
                    map.replace("creator", system);
                }
            } else {
                map.put("creator", system);
            }
        }
    }

    public static List<Map<String, Object>> getForeignKeys(XMLStreamReader xmlr) throws XMLStreamException {
        List<Map<String, Object>> foreignkeys = null;

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

                                    if (foreignkeys == null) {
                                        foreignkeys = new ArrayList<Map<String, Object>>();
                                    }

                                    foreignkeys.add(fk);

                                } catch (UnsupportedEncodingException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }

                } else if (eventType == XMLStreamReader.END_ELEMENT) {
                    if (xmlr.getLocalName().equals("ForeignKeys")) {
                        break;
                    }
                }
            }
        }

        return foreignkeys;
    }

    public static boolean isNullOrEmpty(String test) {
        if (test == null) return true;
        if (test.length() == 0) return true;
        if (test.trim().length() == 0) return true;
        return false;
    }

    public static boolean isUUID(String test) {
        if (test == null) return false;
        if (test.equals("00000000000000000000000000000000")) return false;
        if (test.equals("00000000-0000-0000-0000-000000000000")) return false;

        if (test.contains("-")) {
            if (test.length() != 36) return false;
        } else if (test.length() == 32) {
            test = test.replaceFirst("([0-9a-fA-F]{8})([0-9a-fA-F]{4})([0-9a-fA-F]{4})([0-9a-fA-F]{4})([0-9a-fA-F]+)", "$1-$2-$3-$4-$5");
        }

        try {
            UUID uuid = UUID.fromString(test);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }
}
