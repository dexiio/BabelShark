/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.vonhof.babelshark;

import com.vonhof.babelshark.node.ArrayNode;
import com.vonhof.babelshark.node.ObjectNode;
import com.vonhof.babelshark.node.SharkNode;
import com.vonhof.babelshark.node.ValueNode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import junit.framework.TestCase;

/**
 *
 * @author Henrik Hofmeister <@vonhofdk>
 */
public class BabelSharkInstanceTest extends TestCase {
    
    BabelSharkInstance bs = new BabelSharkInstance();
    
    public BabelSharkInstanceTest(String testName) {
        super(testName);
    }
    
    private ObjectNode makeObject() {
        ObjectNode node = new ObjectNode();
        node.put("test", true);
        node.put("list",makeList());
        return node;
    }
    
    private ObjectNode makeMap() {
        ObjectNode node = new ObjectNode();
        node.put("test", true);
        node.put("list",makeList());
        return node;
    }
    private Map makeTestMap() {
        Map<String,Object> map = new HashMap<String, Object>();
        
        map.put("test",true);
        map.put("list",makeTestList());
        
        return map;
    }
    
    private TestClass makeTestInstance() {
        return new TestClass(true,0,1,2,3,4);
    }
    
    private List<Double> makeTestList() {
        List<Double> out =  new ArrayList<Double>();
        for(int i = 0;i < 5;i++)
            out.add((double)i);
        return out;
    }
    
    private double[] makeTestArray() {
        return new double[]{0,1,2,3,4};
    }
    
    private ArrayNode makeList() {
        ArrayNode node = new ArrayNode();
        node.add(0D,1D,2D,3D,4D);
        return node;
    }
    

    public void test_can_read_simple_value() throws Exception {
        Boolean out = bs.read(new ValueNode(true), Boolean.class);
        assertTrue(out);
    }
    
    public void test_can_read_bean_value() throws Exception {
        
        TestClass out = bs.read(makeObject(), TestClass.class);
        assertEquals(makeTestInstance(),out);
    }
    
    public void test_can_read_list_value() throws Exception {
        ArrayNode node = makeList();
        List<Integer> result = bs.read(node, List.class);
        assertEquals(makeTestList(), result);
    }
    
    public void test_can_read_array_value() throws Exception {
        ArrayNode node = makeList();
        double[] testArray = makeTestArray();
        double[] result = bs.read(node, testArray.getClass());
        assertEquals(testArray.length, result.length);
        assertEquals(testArray[2], result[2]);
        assertEquals(testArray[4], result[4]);
    }
    
    public void test_can_read_map_value() throws Exception {
        ObjectNode node = makeMap();
        Map result = bs.read(node, Map.class);
        assertEquals(makeTestMap(), result);
    }
    
    
    public void test_can_write_simple_value() throws Exception {
        SharkNode out = bs.write(true);
        assertEquals(new ValueNode(true),out);
    }
    
    public void test_can_write_bean_value() throws Exception {
        
        SharkNode out = bs.write(makeTestInstance());
        assertEquals(makeObject(),out);
    }
    
    public void test_can_write_list_value() throws Exception {
        SharkNode result = bs.write(makeTestList());
        assertEquals(makeList(), result);
    }
    
    public void test_can_write_array_value() throws Exception {
        SharkNode result = bs.write(makeTestArray());
        assertEquals(makeList(), result);
    }
    
    public void test_can_write_map_value() throws Exception {
        SharkNode result = bs.write(makeTestMap());
        assertEquals(makeMap(), result);
    }
    
    public static class TestClass {
        public boolean test;
        public List<Double> list;

        public TestClass(boolean test, double ... entries) {
            this.test = test;
            this.list = new ArrayList<Double>();
            for(double entry:entries)
                list.add(entry);
        }

        public TestClass() {
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final TestClass other = (TestClass) obj;
            if (this.test != other.test) {
                return false;
            }
            if (this.list != other.list && (this.list == null || !this.list.equals(other.list))) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 83 * hash + (this.test ? 1 : 0);
            hash = 83 * hash + (this.list != null ? this.list.hashCode() : 0);
            return hash;
        }
        
    }

}
