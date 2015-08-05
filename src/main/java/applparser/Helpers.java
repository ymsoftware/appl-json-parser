package applparser;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by ymetelkin on 8/4/15.
 */
public class Helpers {
    public static void safeAdd(String field, Object value, Map<String, Object> map) {
        if (value != null) {
            map.put(field, value);
        }
    }

    public static void safeAddStringId(String field, String value, Map<String, Object> map) {
        if (value != null && value.length() > 0) {
            map.put(field, value.trim().toLowerCase());
        }
    }

    public static void safeAddInteger(String field, String value, Map<String, Object> map) {
        if (value != null && value.length() > 0) {
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

    public static Number parseNumber(String value) {
        try {
            if (value != null) {
                if (value.contains(".")) {
                    return value.length() > 7 ? Double.parseDouble(value) : Float.parseFloat(value);
                } else {
                    return value.length() > 9 ? Long.parseUnsignedLong(value) : Integer.parseUnsignedInt(value);
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
                if (date.endsWith("GMT")) {
                    date = date.replace("GMT", "Z");
                } else if (!date.endsWith("Z")) {
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
        boolean hasCode = code != null;
        boolean hasName = name != null;
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
            boolean hasRels = map.containsKey(key);
            List<String> rels = hasRels ? (List<String>) map.get(key) : new ArrayList<String>();
            if (!rels.contains(value)) {
                rels.add(value);

                if (hasRels) {
                    map.replace(key, rels);
                } else {
                    map.put(key, rels);
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
}
