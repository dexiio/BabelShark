package com.vonhof.babelshark;

import java.sql.Time;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;

public class ConvertUtils {
    /**
     * Converts string to primitives (boolean,enum,class etc.) 
     * @param <T>
     * @param str
     * @param type
     * @return 
     */
    public static <T> T convert(String str,Class<T> type) {
        if (str == null)
            return null;
        if (!ReflectUtils.isPrimitive(type))
            throw new RuntimeException(String.format("Not a primitive: %s",type.getName()));
        if (String.class.equals(type))
            return (T) str;
        if (boolean.class.equals(type) || Boolean.class.equals(type))
            return (T) Boolean.valueOf(str);
        if (int.class.equals(type) || Integer.class.equals(type))
            return (T) Integer.valueOf(str);
        if (float.class.equals(type) || Float.class.equals(type))
            return (T) Float.valueOf(str);
        if (double.class.equals(type) || Double.class.equals(type))
            return (T) Double.valueOf(str);
        if (long.class.equals(type) || Long.class.equals(type))
            return (T) Long.valueOf(str);
        if (Class.class.equals(type))
            try {
            return (T) Class.forName(str);
        } catch (ClassNotFoundException ex) {
            throw new RuntimeException(String.format("Could not read class name from %s",type.getName(),str), ex);
        }
        if (Enum.class.equals(type)) {
            try {
                return (T) type.getMethod("valueOf",String.class).invoke(null,str);
            } catch (Exception ex) {
                throw new RuntimeException(String.format("Could not read enum value in %s: %s",type.getName(),str), ex);
            }
        }
        if (Date.class.equals(type)) {
            try {
                return (T) DateFormat.getDateTimeInstance().parse(str);
            } catch (ParseException ex) {
                throw new RuntimeException(String.format("Could not read date string: %s",str), ex);
            }
        }
        if (Timestamp.class.equals(type)) {
            try {
                Date time = DateFormat.getTimeInstance().parse(str);
                return (T) new Timestamp(time.getTime());
            } catch (ParseException ex) {
                throw new RuntimeException(String.format("Could not read date string: %s",str), ex);
            }
        }
        if (Time.class.equals(type)) {
            try {
                Date time = DateFormat.getTimeInstance().parse(str);
                return (T) new Time(time.getTime());
            } catch (ParseException ex) {
                throw new RuntimeException(String.format("Could not read date string: %s",str), ex);
            }
        }
        
        throw new RuntimeException(String.format("Did not recognize type: %s",type.getName()));
    }
    public static <T extends Number> T convert(Number number,Class<T> type) {
        if (Integer.class.equals(type) || int.class.equals(type))
            return (T) new Integer(number.intValue());
        if (Float.class.equals(type) || float.class.equals(type))
            return (T) new Float(number.floatValue());
        if (Long.class.equals(type) || long.class.equals(type))
            return (T) new Long(number.longValue());
        return (T) new Double(number.doubleValue());
    }
}
