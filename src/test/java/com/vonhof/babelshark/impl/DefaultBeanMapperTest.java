package com.vonhof.babelshark.impl;

import com.vonhof.babelshark.MappedBean;
import com.vonhof.babelshark.MappedBean.ObjectField;
import com.vonhof.babelshark.annotation.Ignore;
import com.vonhof.babelshark.annotation.Name;
import com.vonhof.babelshark.node.SharkType;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import junit.framework.TestCase;

/**
 *
 * @author Henrik Hofmeister <hh@cphse.com>
 */
public class DefaultBeanMapperTest extends TestCase {
    
    public DefaultBeanMapperTest(String testName) {
        super(testName);
    }
    
    private final Class type = TestClass.class;
    private final DefaultBeanMapper instance = new DefaultBeanMapper();
    
    private MappedBean expResult;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        expResult = new MappedBean(type);
        expResult.addField("_id", type.getDeclaredField("id"),type.getMethod("getId"), type.getMethod("setId",String.class));
        expResult.addField("name", type.getDeclaredField("name"),type.getMethod("getName"), type.getMethod("setName",String.class));
        expResult.addField("subs", type.getDeclaredField("children"),null,null);
        expResult.addField("fields", type.getDeclaredField("fields"),type.getMethod("getFields"), type.getMethod("setFields",Map.class));
        
    }

    public void testIgnoreField() throws Exception {
        Field ignoredField = type.getDeclaredField("ignoredField");
        boolean result = instance.ignoreField(type, ignoredField);
        assertEquals(true, result);
    }
    
    public void testCanGetFieldName() throws Exception {
        Field nameField = type.getDeclaredField("name");
        String result = instance.getFieldName(type, nameField);
        assertEquals("name", result);
    }
    
    public void testCanOverrideFieldName() throws Exception {
        Field idField = type.getDeclaredField("id");
        String result = instance.getFieldName(type, idField);
        assertEquals("_id", result);
    }
    
    public void testCanGetListType() throws Exception {
        MappedBean map = instance.getMap(type);
        ObjectField subsField = map.getField("subs");
        assertEquals(SharkType.forCollection(List.class,TestClass.class),subsField.getType());
    }
    
    public void testCanGetMapType() throws Exception {
        MappedBean map = instance.getMap(type);
        ObjectField subsField = map.getField("fields");
        assertEquals(SharkType.forMap(Map.class,TestClass.class),subsField.getType());
    }

    public void testCanGetBeanGetter() throws Exception {
        Field idField = type.getDeclaredField("id");
        Method expGetter = type.getMethod("getId");
        Method getter = instance.getGetter(type, idField);
        assertEquals(expGetter, getter);
    }
    
    public void testCanGetPublicGetter() throws Exception {
        Field childrenField = type.getDeclaredField("children");
        Method getter = instance.getGetter(type, childrenField);
        assertNull(getter);
    }
    
    public void testCanGetBeanSetter() throws Exception {
        Field idField = type.getDeclaredField("id");
        Method expSetter = type.getMethod("setId",String.class);
        Method setter = instance.getSetter(type, idField);
        assertEquals(expSetter, setter);
    }
    
    public void testCanGetPublicSetter() throws Exception {
        Field childrenField = type.getDeclaredField("children");
        Method setter = instance.getSetter(type, childrenField);
        assertNull(setter);
    }
    
    public void testMapping() throws Exception {
        MappedBean result = instance.getMap(type);
        assertEquals(expResult, result);
    }


    
    @Name("test")
    public static class TestClass {
        private String name;
        
        @Name("_id")
        private String id;
        
        @Ignore
        private String ignoredField = "ignored value";
        
        @Name(value="subs")
        public List<TestClass> children = new ArrayList<DefaultBeanMapperTest.TestClass>();
        
        private Map<String,TestClass> fields = new HashMap<String, TestClass>();

        public TestClass() {
            children.add(new TestClass("test name", "testId"));
        }

        public TestClass(String name, String id) {
            this.name = name;
            this.id = id;
        }


        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getIgnoredField() {
            return ignoredField;
        }

        public void setIgnoredField(String ignoredField) {
            this.ignoredField = ignoredField;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Map<String, TestClass> getFields() {
            return fields;
        }

        public void setFields(Map<String, TestClass> fields) {
            this.fields = fields;
        }
    }
}
