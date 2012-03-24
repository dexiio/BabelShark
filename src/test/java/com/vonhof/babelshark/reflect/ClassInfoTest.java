/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.vonhof.babelshark.reflect;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import junit.framework.TestCase;

/**
 *
 * @author Henrik Hofmeister <hh@cphse.com>
 */
public class ClassInfoTest extends TestCase {
    
    public ClassInfoTest(String testName) {
        super(testName);
    }
    
    public void test_can_read_class() {
        ClassInfo classInfo = ClassInfo.from(CrazyBean.class);
        
        assertEquals(true,classInfo.isBean());
        
        assertEquals(true,classInfo.isBean());
        
    }

    public static class CrazyBean {
        private List<String> someList = new ArrayList<String>();
        private Map<String,Integer> someMap = new HashMap<String, Integer>();
        
        private int[] intArray = {1,2,3,4,5};
        private String[] stringArray = {"sdf","sdgfhtd"};
        
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

        public List<String> getSomeList() {
            return someList;
        }

        public void setSomeList(List<String> someList) {
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
}
