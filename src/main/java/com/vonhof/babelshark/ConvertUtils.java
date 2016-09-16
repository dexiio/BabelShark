package com.vonhof.babelshark;

import com.vonhof.babelshark.converter.SimpleConverter;
import com.vonhof.babelshark.reflect.ClassInfo;
import java.lang.reflect.Array;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class ConvertUtils {

    private static final DateFormat java8Format = new SimpleDateFormat("EEE MMM dd kk:mm:ss z yyyy");
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
        if (!ReflectUtils.isSimple(type))
            throw new RuntimeException(String.format("Not a primitive: %s",type.getName()));
        if (String.class.equals(type))
            return (T) str;
        if (boolean.class.equals(type) || Boolean.class.equals(type))
            return (T) Boolean.valueOf(str);

        if (int.class.equals(type) || Integer.class.equals(type)) {
            if (str.isEmpty())
                str = "0";
            return (T) Integer.valueOf(str);
        }

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
        
        if (UUID.class.equals(type)) {
            if (str.isEmpty())
                return null;
            return (T) UUID.fromString(str);
        }

        if (Enum.class.isAssignableFrom(type)) {
            try {
                return (T) Enum.valueOf((Class<Enum>)type, str);
            } catch (Exception ex) {
                throw new RuntimeException(String.format("Could not read enum value in %s: %s",type.getName(),str), ex);
            }
        }
        if (Date.class.equals(type)) {
            if (str.trim().isEmpty()) {
                return null;
            }

            if (isNumberString(str)) {
                return (T) new Date(Long.valueOf(str));
            }

            return (T) parseDate(str);
        }
        
        if (Calendar.class.isAssignableFrom(type)) {
            Date date = parseDate(str);
            GregorianCalendar calendar = new GregorianCalendar();
            calendar.setTime(date);
            return (T) calendar;
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

    public static Date parseDate(String dateStr) {
        try {
            return DateFormat.getDateTimeInstance().parse(dateStr);
        } catch (ParseException e) {
            try {
                return java8Format.parse(dateStr);
            } catch (ParseException e1) {
                throw new RuntimeException(String.format("Could not read date string: %s", dateStr), e1);
            }
        }
    }

    public static <T> T convert(Number number,Class<T> type) {
        if (Integer.class.equals(type) || int.class.equals(type))
            return (T) new Integer(number.intValue());
        if (Float.class.equals(type) || float.class.equals(type))
            return (T) new Float(number.floatValue());
        if (Long.class.equals(type) || long.class.equals(type))
            return (T) new Long(number.longValue());
        if (Double.class.equals(type) || double.class.equals(type))
            return (T) new Double(number.doubleValue());
        if (Date.class.equals(type))
            return (T) new Date(number.longValue());
        return null;
    }

    public static Object convertCollection(ClassInfo type, String[] values) throws InstantiationException, IllegalAccessException {
        if (values == null 
                || values.length == 0)
            return null;
        
        if (type.isCollection()) {
            if (type.isArray()) {
                Class arrayType = type.getComponentType();
                Object arrayValues = Array.newInstance(arrayType,values.length);
                for (int x = 0; x < values.length; x++) {
                    Object value = ConvertUtils.convert(values[x], arrayType);
                    Array.set(arrayValues, x, value);
                }
                return arrayValues;
            } else {
                Collection list = null;
                if (type.isInstantiatable()) {
                    list = (Collection) type.newInstance();
                } else {
                    if (type.isA(List.class) || type.isA(Collection.class)) {
                        list = new ArrayList();
                    } else if (type.isA(Deque.class)) {
                        list = new LinkedList();
                    } else if (type.isA(Stack.class)) {
                        list = new Stack();
                    } else if (type.isA(Set.class)) {
                        list = new HashSet();
                    } else if (type.isA(Queue.class)) {
                        list = new PriorityQueue();
                    }
                }

                Class valueType = type.getGenericTypes().length > 0 ? (Class) type.getGenericTypes()[0] : Object.class;

                if (values.length == 1 && values[0].contains(",")) {
                    values = values[0].split(",");
                }

                for(String val : values) {
                    if (val == null || val.isEmpty()) {
                        continue;
                    }
                    list.add(ConvertUtils.convert(val, valueType));
                }
                return list;
            }

        } else if (type.isPrimitive()) {
            return ConvertUtils.convert(values[0], type.getType());
        }
        if (values.length > 0)
            return values[0];
        return null;
    }



    public static boolean isNumberString(String o) {
        return o.matches("^[1-9][0-9]*$");
    }
}
