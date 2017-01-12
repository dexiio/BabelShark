package com.vonhof.babelshark;

import com.vonhof.babelshark.node.ObjectNode;
import com.vonhof.babelshark.node.SharkNode;
import com.vonhof.babelshark.node.ValueNode;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static com.vonhof.babelshark.ObjectNodeUtils.getValueFromPath;
import static org.junit.Assert.*;

public class ObjectNodeUtilsTest {

    private Map<String, SharkNode> values;
    private String simpleValue;

    @Before
    public void init() {
        values = new HashMap<>();
        simpleValue = "simple value";
    }

    @Test
    public void can_get_value_from_path_with_no_values_and_simple_paths() {
        SharkNode actualValue = getValueFromPath(values, "");
        assertNull(actualValue);

        actualValue = getValueFromPath(values, "path");
        assertNull(actualValue);

        actualValue = getValueFromPath(values, "path/");
        assertNull(actualValue);

        actualValue = getValueFromPath(values, "/");
        assertNull(actualValue);
    }

    @Test
    public void can_get_value_from_path_with_values_and_simple_paths() {
        String path = "path";
        values.put(path, new ValueNode<>(simpleValue));

        ValueNode actualValue = (ValueNode) getValueFromPath(values, "");
        assertNull(actualValue);

        actualValue = (ValueNode) getValueFromPath(values, "/");
        assertNull(actualValue);

        actualValue = (ValueNode) getValueFromPath(values, path);
        assertEquals(simpleValue, actualValue.getValue());

        actualValue = (ValueNode) getValueFromPath(values, path + "/");
        assertEquals(simpleValue, actualValue.getValue());
    }

    @Test
    public void can_get_value_from_path_with_one_object_and_object_path() {
        ObjectNode objectValue = new ObjectNode();
        objectValue.put("to", simpleValue);
        values.put("path", objectValue);

        ValueNode actualValue = (ValueNode) getValueFromPath(values, "path/to");
        assertEquals(simpleValue, actualValue.getValue());
    }

    @Test
    public void can_get_value_from_path_with_one_object_and_simple_path() {
        ObjectNode objectValue = new ObjectNode();
        objectValue.put("to", simpleValue);
        values.put("path", objectValue);

        ObjectNode actualValue = (ObjectNode) getValueFromPath(values, "path");
        ValueNode toValue = (ValueNode) actualValue.get("to");
        assertEquals(simpleValue, toValue.getValue());
    }

    @Test
    public void can_get_value_from_path_with_multiple_objects_and_object_path() {
        ObjectNode valueValue = new ObjectNode();
        valueValue.put("value", simpleValue);

        ObjectNode toValue = new ObjectNode();
        toValue.put("to", valueValue);

        values.put("path", toValue);

        ValueNode actualValue = (ValueNode) getValueFromPath(values, "path/to/value");
        assertEquals(simpleValue, actualValue.getValue());

        actualValue = (ValueNode) getValueFromPath(values, "path/to/value/");
        assertEquals(simpleValue, actualValue.getValue());
    }

    @Test
    public void can_set_value_on_no_path() {
        ObjectNode values = new ObjectNode();
        ObjectNodeUtils.setValueFromPath(values, "", new ValueNode<>(simpleValue));
        assertTrue(values.getFields().isEmpty());

        values = new ObjectNode();
        ObjectNodeUtils.setValueFromPath(values, "/", new ValueNode<>(simpleValue));
        assertTrue(values.getFields().isEmpty());
    }

    @Test
    public void can_set_simple_value_on_simple_path() {
        ObjectNode values = new ObjectNode();
        ValueNode<String> valueNode = new ValueNode<>(simpleValue);
        ObjectNodeUtils.setValueFromPath(values, "path", valueNode);
        ValueNode actualValue = (ValueNode) values.get("path");
        assertEquals(simpleValue, actualValue.getValue());

        values = new ObjectNode();
        valueNode = new ValueNode<>(simpleValue);
        ObjectNodeUtils.setValueFromPath(values, "path/", valueNode);
        actualValue = (ValueNode) values.get("path");
        assertEquals(simpleValue, actualValue.getValue());
    }

    @Test
    public void can_set_simple_value_on_object_path() {
        ObjectNode values = new ObjectNode();
        ValueNode<String> valueNode = new ValueNode<>(simpleValue);

        ObjectNodeUtils.setValueFromPath(values, "path/to/simpleValue", valueNode);

        ObjectNode pathNode = values.getObject("path");
        ObjectNode toNode = pathNode.getObject("to");
        String actualSimpleValue = toNode.getValue("simpleValue", String.class);
        assertEquals(simpleValue, actualSimpleValue);
    }

    @Test
    public void can_set_object_value_on_object_path() {
        ObjectNode values = new ObjectNode();

        ObjectNode objectNode = new ObjectNode();
        ObjectNode objectValueNode = new ObjectNode();
        int field1Value = 1;
        objectValueNode.put("field1", field1Value);
        double field2Value = 2092.35478;
        objectValueNode.put("field2", field2Value);
        objectNode.put("objectValue", objectValueNode);

        ObjectNodeUtils.setValueFromPath(values, "path/to", objectNode);

        ObjectNode pathNode = values.getObject("path");
        ObjectNode toNode = pathNode.getObject("to");
        ObjectNode actualObjectValueNode = toNode.getObject("objectValue");

        int actualField1Value = actualObjectValueNode.getValue("field1", Integer.class);
        assertEquals(field1Value, actualField1Value);

        double actualField2Value = actualObjectValueNode.getValue("field2", Double.class);
        assertEquals(field2Value, actualField2Value, 0.0);
    }

    @Test
    public void can_overwrite_value_on_object_path() {
        ObjectNode values = new ObjectNode();

        ValueNode<String> existingFieldNode = new ValueNode<>(simpleValue);
        ObjectNodeUtils.setValueFromPath(values, "path/to/existingField", existingFieldNode);

        ObjectNode nodeToOverwriteExistingField = new ObjectNode();
        nodeToOverwriteExistingField.put("newValue", "Some value");
        ObjectNodeUtils.setValueFromPath(values, "path/to/existingField", nodeToOverwriteExistingField);

        ObjectNode pathNode = values.getObject("path");
        ObjectNode toNode = pathNode.getObject("to");
        ObjectNode actualExistingFieldNode = (ObjectNode) toNode.get("existingField");
        assertEquals(nodeToOverwriteExistingField, actualExistingFieldNode);
    }

    @Test
    public void can_map_columns_no_mapping() {
        ObjectNode values = new ObjectNode();
        String fieldName = "simpleField";
        ObjectNodeUtils.setValueFromPath(values, fieldName, new ValueNode<>(simpleValue));

        Map<String, SharkNode> mappedValues = ObjectNodeUtils.mapColumns(values.toMap(), null);
        assertNotNull(mappedValues);
        ValueNode actualValue = (ValueNode) getValueFromPath(mappedValues, fieldName);
        assertEquals(simpleValue, actualValue.getValue());
    }

    @Test
    public void can_map_columns_simple_to_simple() {
        ObjectNode values = new ObjectNode();
        String sourceFieldName = "simpleField";
        ObjectNodeUtils.setValueFromPath(values, sourceFieldName, new ValueNode<>(simpleValue));

        Map<String, String> columnMapping = new HashMap<>();
        String targetFieldName = "anotherSimpleField";
        columnMapping.put(sourceFieldName, targetFieldName);

        Map<String, SharkNode> mappedValues = ObjectNodeUtils.mapColumns(values.toMap(), columnMapping);
        assertNotNull(mappedValues);

        ValueNode actualValue = (ValueNode) getValueFromPath(mappedValues, targetFieldName);
        assertEquals(simpleValue, actualValue.getValue());
    }

    @Test
    public void can_map_columns_simple_to_object() {
        ObjectNode values = new ObjectNode();
        String sourceFieldName = "simpleField";
        ObjectNodeUtils.setValueFromPath(values, sourceFieldName, new ValueNode<>(simpleValue));

        Map<String, String> columnMapping = new HashMap<>();
        String targetFieldName = "path/to/objectField";
        columnMapping.put(sourceFieldName, targetFieldName);

        Map<String, SharkNode> mappedValues = ObjectNodeUtils.mapColumns(values.toMap(), columnMapping);
        assertNotNull(mappedValues);
        ValueNode actualValue = (ValueNode) getValueFromPath(mappedValues, targetFieldName);
        assertEquals(simpleValue, actualValue.getValue());
    }

    @Test
    public void can_map_columns_object_to_simple() {
        ObjectNode values = new ObjectNode();
        String sourceFieldName = "path/to/objectField";
        ObjectNodeUtils.setValueFromPath(values, sourceFieldName, new ValueNode<>(simpleValue));

        Map<String, String> columnMapping = new HashMap<>();
        String targetFieldName = "simpleField";
        columnMapping.put(sourceFieldName, targetFieldName);

        Map<String, SharkNode> mappedValues = ObjectNodeUtils.mapColumns(values.toMap(), columnMapping);
        assertNotNull(mappedValues);
        ValueNode actualValue = (ValueNode) getValueFromPath(mappedValues, targetFieldName);
        assertEquals(simpleValue, actualValue.getValue());
    }

    @Test
    public void can_map_columns_object_to_object() {
        ObjectNode values = new ObjectNode();
        String sourceFieldName = "path/to/objectField";
        ObjectNodeUtils.setValueFromPath(values, sourceFieldName, new ValueNode<>(simpleValue));

        Map<String, String> columnMapping = new HashMap<>();
        String targetFieldName = "path/to/anotherObjectField";
        columnMapping.put(sourceFieldName, targetFieldName);

        Map<String, SharkNode> mappedValues = ObjectNodeUtils.mapColumns(values.toMap(), columnMapping);
        assertNotNull(mappedValues);
        ValueNode actualValue = (ValueNode) getValueFromPath(mappedValues, targetFieldName);
        assertEquals(simpleValue, actualValue.getValue());
    }

}