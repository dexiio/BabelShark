package com.vonhof.babelshark;

import com.vonhof.babelshark.node.ObjectNode;
import com.vonhof.babelshark.node.SharkNode;
import com.vonhof.babelshark.node.ValueNode;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class ObjectNodeUtilsTest {

    private Map<String, SharkNode> values;
    private String simpleValue;


    @Before
    public void init() {
        values = new HashMap<>();
        simpleValue = "simple value";
    }

    @Test
    public void testGetValueFromPathNoValuesNoPath() {
        SharkNode actualValue = ObjectNodeUtils.getValueFromPath(values, "");
        assertNull(actualValue);
    }

    @Test
    public void testGetValueFromPathNoValuesSimplePath() {
        SharkNode actualValue = ObjectNodeUtils.getValueFromPath(values, "path");
        assertNull(actualValue);
    }

    @Test
    public void testGetValueFromPathNoValuesSimplePathWithSeparator() {
        SharkNode actualValue = ObjectNodeUtils.getValueFromPath(values, "path/");
        assertNull(actualValue);
    }

    @Test
    public void testGetValueFromPathNoValuesSeparatorOnlyPath() {
        SharkNode actualValue = ObjectNodeUtils.getValueFromPath(values, "/");
        assertNull(actualValue);
    }

    @Test
    public void testGetValueFromPathHasValuesNoPath() {
        values.put("path", new ValueNode<>(simpleValue));

        SharkNode actualValue = ObjectNodeUtils.getValueFromPath(values, "");
        assertNull(actualValue);
    }

    @Test
    public void testGetValueFromPathHasValuesSimplePath() {
        String path = "path";
        values.put(path, new ValueNode<>(simpleValue));

        ValueNode actualValue = (ValueNode) ObjectNodeUtils.getValueFromPath(values, path);
        assertEquals(simpleValue, actualValue.getValue());
    }

    @Test
    public void testGetValueFromPathHasValuesSimplePathWithSeparator() {
        String path = "path";
        values.put(path, new ValueNode<>(simpleValue));

        ValueNode actualValue = (ValueNode) ObjectNodeUtils.getValueFromPath(values, path + "/");
        assertEquals(simpleValue, actualValue.getValue());
    }

    @Test
    public void testGetValueFromPathHasValuesSeparatorOnlyPath() {
        values.put("path", new ValueNode<>(simpleValue));

        ValueNode actualValue = (ValueNode) ObjectNodeUtils.getValueFromPath(values, "/");
        assertNull(actualValue);
    }

    @Test
    public void testGetValueFromPathHasOneObjectValueObjectPath() {
        ObjectNode objectValue = new ObjectNode();
        objectValue.put("to", simpleValue);
        values.put("path", objectValue);

        ValueNode actualValue = (ValueNode) ObjectNodeUtils.getValueFromPath(values, "path/to");
        assertEquals(simpleValue, actualValue.getValue());
    }

    @Test
    public void testGetValueFromPathHasMultipleObjectValuesObjectPath() {
        ObjectNode valueValue = new ObjectNode();
        valueValue.put("value", simpleValue);

        ObjectNode toValue = new ObjectNode();
        toValue.put("to", valueValue);

        values.put("path", toValue);

        ValueNode actualValue = (ValueNode) ObjectNodeUtils.getValueFromPath(values, "path/to/value");
        assertEquals(simpleValue, actualValue.getValue());

        actualValue = (ValueNode) ObjectNodeUtils.getValueFromPath(values, "path/to/value/");
        assertEquals(simpleValue, actualValue.getValue());
    }

}
