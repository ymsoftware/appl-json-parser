package applparser;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

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
}
