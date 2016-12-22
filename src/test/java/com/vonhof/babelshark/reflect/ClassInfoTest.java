package com.vonhof.babelshark.reflect;

import com.vonhof.babelshark.annotation.Name;
import com.vonhof.babelshark.node.SharkType;
import com.vonhof.babelshark.reflect.MethodInfo.Parameter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

import junit.framework.TestCase;

/**
 *
 * @author Henrik Hofmeister <@vonhofdk>
 */
public class ClassInfoTest extends TestCase {

    private final ClassInfo genericBeanInfo = ClassInfo.from(ExtendedGenericBean.class);;
    private final ClassInfo crazyBeanInfo = ClassInfo.from(CrazyBean.class);
    private final ClassInfo extendedCrazyBeanInfo = ClassInfo.from(ExtendedCrazyBean.class);

    public ClassInfoTest(String testName) {
        super(testName);
        
        SharkType<ExtendedGenericBean, ?> type = SharkType.get(ExtendedGenericBean.class);
        
    }

    public void test_can_read_generic_list() throws Exception {
        assertEquals(true, crazyBeanInfo.isBean());

        FieldInfo field = crazyBeanInfo.getField("someList");
        assertNotNull("Can find field", field);

        assertEquals("Can find generic type", String.class, field.getClassInfo().getCollectionType());
    }

    public void test_can_read_array_list() throws Exception {
        SimpleBean[] test = new SimpleBean[0];

        ClassInfo classInfo = ClassInfo.from(test.getClass());
        assertTrue(classInfo.isArray());
        assertTrue(classInfo.isCollection());

        assertEquals("Can find generic type", SimpleBean.class, classInfo.getCollectionType());
    }

    public void test_can_read_extended_list() throws Exception {
        SimpleBeanList test = new SimpleBeanList();

        ClassInfo classInfo = ClassInfo.from(test.getClass());
        assertFalse(classInfo.isArray());
        assertTrue(classInfo.isCollection());

        assertEquals("Can find generic type", SimpleBean.class, classInfo.getCollectionType());
    }

    public void test_can_read_generic_map() throws Exception {

        FieldInfo field = crazyBeanInfo.getField("someMap");
        assertNotNull("Can find field", field);

        assertEquals("Can find generic map key", String.class, field.getClassInfo().getMapKeyType());

        assertEquals("Can find generic map value", Integer.class, field.getClassInfo().getMapValueType());
    }

    public void test_can_read_extended_map() {
        ClassInfo<SimpleBeanMap> classInfo = ClassInfo.from(SimpleBeanMap.class);

        assertTrue(classInfo.isMap());

        assertEquals("Can find generic map key", String.class, classInfo.getMapKeyType());

        assertEquals("Can find generic map value", SimpleBean.class, classInfo.getMapValueType());
    }

    public void test_can_read_generic_parm() throws Exception {
        MethodInfo method = crazyBeanInfo.getMethodByClassParms("setSomeList", List.class);
        assertNotNull("Can find method", method);
        assertEquals("Can find parm", 1, method.getParameters().size());
        Parameter p = method.getParameter(0);
        assertEquals("Can find parm generic type", String.class, p.getClassInfo().getGenericTypes()[0]);
    }

    public void test_can_read_parm_names() throws Exception {
        MethodInfo method = crazyBeanInfo.getMethodByClassParms("setSomeList", List.class, boolean.class);
        assertNotNull("Can find method", method);
        assertEquals("Can find parms", 2, method.getParameters().size());
        assertEquals("Can find parm name", "someList", method.getParameter(0).getName());
        assertEquals("Can find parm name", "other", method.getParameter(1).getName());
    }

    public void test_can_read_generic_return_type() throws Exception {
        MethodInfo method = crazyBeanInfo.getMethodByClassParms("getSomeList");
        assertNotNull("Can find method", method);
        assertEquals("Can find generic return type", List.class, method.getReturnClassInfo().getType());
        assertEquals("Can find generic return type class", String.class, method.getReturnClassInfo().getGenericTypes()[0]);
    }

    public void test_can_read_array_return_type() throws Exception {
        MethodInfo method = crazyBeanInfo.getMethodByClassParms("getStringArray");
        assertNotNull("Can find method", method);
        assertTrue("Can find array return type", method.getReturnClassInfo().isArray());
        assertEquals("Can find array return type class", String.class, method.getReturnClassInfo().getComponentType());
    }

    public void test_can_read_generic_field_type() throws Exception {
        FieldInfo field = genericBeanInfo.getField("genField");
        assertNotNull("Can find field", field);
        assertEquals("Can determine type of generic field", ExtendedCrazyBean.class, field.getClassInfo().getType());
    }
    
    public void test_can_read_generic_parameterized_field_type() throws Exception {
        FieldInfo field = genericBeanInfo.getField("genMap");
        assertNotNull("Can find field", field);
        assertEquals("Can determine type of generic field", ExtendedCrazyBean.class, field.getClassInfo().getGenericTypes()[1]);
    }

    public void test_can_read_generic_method() throws Exception {
        MethodInfo method = genericBeanInfo.getMethodByClassParms("doSomething", ExtendedCrazyBean.class);
        assertNotNull("Can find method", method);
        assertEquals("Can determine type of generic parm", ExtendedCrazyBean.class, method.getParameter("val").getClassInfo().getType());
        assertEquals("Can determine type of generic return type", ExtendedCrazyBean.class, method.getReturnClassInfo().getType());
    }
    
    public void test_can_read_generic_paremeterized_method() throws Exception {
        MethodInfo method = genericBeanInfo.getMethodByClassParms("doSomething", Map.class);
        assertNotNull("Can find method", method);
        assertEquals("Can determine type of generic parm", Map.class, method.getParameter("val").getClassInfo().getType());
        assertEquals("Can determine type of generic return type", Map.class, method.getReturnClassInfo().getType());
        
        assertEquals("Can determine type of generic parameter in return type", ExtendedCrazyBean.class, 
                        method.getReturnClassInfo().getGenericTypes()[1]);
        assertEquals("Can determine type of generic parameter in parm type", ExtendedCrazyBean.class, 
                        method.getParameter("val").getClassInfo().getGenericTypes()[1]);
        
    }
    
    
    public void test_can_read_inherited_annotations() throws Exception {
        MethodInfo getSomeList = extendedCrazyBeanInfo.getMethodByClassParms("getSomeList");
        assertNotNull("Can find method", getSomeList);
        Name nameAnno = getSomeList.getAnnotation(Name.class);
        assertNotNull("Has method annotation",nameAnno);
        assertEquals("Has method annotation value","crazyName",nameAnno.value());
        
        
        MethodInfo setSomeList = extendedCrazyBeanInfo.getMethodByClassParms("setSomeList",List.class);
        assertNotNull("Can find method", setSomeList);
        nameAnno = setSomeList.getParameter("someList").getAnnotation(Name.class);
        assertNotNull("Has parm annotation",nameAnno);
        assertEquals("Has parm annotation value","someparmname",nameAnno.value());
        
        
        FieldInfo stringArray = extendedCrazyBeanInfo.getField("stringArray");
        assertNotNull("Can find field", stringArray);
        nameAnno = stringArray.getAnnotation(Name.class);
        assertNotNull("Has field annotation",nameAnno);
        assertEquals("Has field annotation value","someProperty",nameAnno.value());
    }
    
    public void test_ignores_overridden_methods() {
        assertEquals(2,genericBeanInfo.getMethods().size());
    }
    
    
    public void test_concurrent_reading() throws InterruptedException {
        final ConcurrentLinkedQueue<String> results = new ConcurrentLinkedQueue<String>();
        Runnable r1 = new Runnable() {
            @Override
            public void run() {
                ClassInfo<SimpleBean> info = ClassInfo.from(SimpleBean.class);
                assertNotNull(info.getField("property"));
                results.add("result");
            }
        };
        
        Thread t1 = new Thread(r1);
        Thread t2 = new Thread(r1);
        Thread t3 = new Thread(r1);
        t1.start();
        t2.start();
        t3.start();
        
        t1.join();
        t2.join();
        t3.join();
        
        assertEquals("Can call ClassInfo.from concurrently",3, results.size());
    }

    public static class GenericBean<T extends CrazyBean> {

        public T genField;
        
        public Map<String,T> genMap; 

        public T doSomething(T val) {
            return val;
        }
        
        public Map<String,T> doSomething(Map<String,T> val) {
            return val;
        }
    }
    
    public static class ExtendedGenericBean extends GenericBean<ExtendedCrazyBean> {

        @Override
        public ExtendedCrazyBean doSomething(ExtendedCrazyBean val) {
            return super.doSomething(val);
        }
    }

    public static class CrazyBean {

        private List<String> someList = new ArrayList<String>();
        private Map<String, Integer> someMap = new HashMap<String, Integer>();
        private int[] intArray = {1, 2, 3, 4, 5};
        
        @Name("someProperty")
        private String[] stringArray = {"sdf", "sdgfhtd"};
        private String someHiddenField = "test";

        public int[] getIntArray() {
            return intArray;
        }

        public void setIntArray(int[] intArray) {
            this.intArray = intArray;
        }

        public String getSomeHiddenField() {
            return someHiddenField;
        }

        public void setSomeHiddenField(String someHiddenField) {
            this.someHiddenField = someHiddenField;
        }

        @Name("crazyName")
        public List<String> getSomeList() {
            return someList;
        }

        public List<String> getSomeList(boolean some) {
            return someList;
        }

        public void setSomeList(List<String> someList, boolean other) {
            this.someList = someList;
        }

        public void setSomeList(@Name("someparmname") List<String> someList) {
            this.someList = someList;
        }

        public Map<String, Integer> getSomeMap() {
            return someMap;
        }

        public void setSomeMap(Map<String, Integer> someMap) {
            this.someMap = someMap;
        }

        public String[] getStringArray() {
            return stringArray;
        }
    }
    
    public static class ExtendedCrazyBean extends CrazyBean {

        @Override
        public List<String> getSomeList() {
            return super.getSomeList();
        }

        @Override
        public void setSomeList(List<String> someList) {
            super.setSomeList(someList);
        }
    }
    
    public static class SimpleBean {
        private String property;

        public String getProperty() {
            return property;
        }

        public void setProperty(String property) {
            this.property = property;
        }
    }

    public static class SimpleBeanMap extends HashMap<String, SimpleBean> {

    }

    public static class SimpleBeanList extends ArrayList<SimpleBean> {

    }

}
