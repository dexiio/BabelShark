package com.vonhof.babelshark.node;

import com.vonhof.babelshark.annotation.Name;
import java.util.*;
import java.util.Map.Entry;

/**
 *
 * @author Henrik Hofmeister <@vonhofdk>
 */
@Name("Object")
public final class ObjectNode extends SharkNode {
    private final Map<String,SharkNode> fields = new LinkedHashMap<String, SharkNode>();

    public ObjectNode() {
        super(NodeType.MAP);
    }
    
    public Collection<String> getFields() {
        return Collections.unmodifiableCollection(fields.keySet());
    }
    
    public SharkNode get(String fieldName) {
        return fields.get(fieldName);
    }
    
    public <T extends SharkNode> T put(String fieldName,T node) {
        fields.put(fieldName,node);
        return node;
    }
    
    public ObjectNode putObject(String fieldName) {
        return put(fieldName,new ObjectNode());
    }
    
    public ArrayNode putArray(String fieldName) {
        return put(fieldName,new ArrayNode());
    }

    public ValueNode put(String fieldName, String value) {
        return put(fieldName,new ValueNode(value));
    }
    
    public ValueNode put(String fieldName, int value) {
        return put(fieldName,new ValueNode(value));
    }
    
    public ValueNode put(String fieldName, float value) {
        return put(fieldName,new ValueNode(value));
    }
    
    public ValueNode put(String fieldName, double value) {
        return put(fieldName,new ValueNode(value));
    }
    public ValueNode put(String fieldName, long value) {
        return put(fieldName,new ValueNode(value));
    }
    
    public ValueNode put(String fieldName, Date value) {
        return put(fieldName,new ValueNode(value));
    }
    
    public ValueNode put(String fieldName, boolean value) {
        return put(fieldName,new ValueNode(value));
    }
    
    public ValueNode put(String fieldName, Enum value) {
        return put(fieldName,new ValueNode(value));
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        boolean first = true;
        for(Entry<String,SharkNode> entry:fields.entrySet()) {
            if (first)
                first = false;
            else
                sb.append(",");
            sb.append(entry.getKey());
            sb.append("=");
            sb.append(entry.getValue());
        }
        sb.append("}");
        return sb.toString();
        
    }

    @Override
    public boolean equals(Object obj) {
        if (!super.equals(obj))
            return false;
        
        final ObjectNode other = (ObjectNode) obj;
        if (this.fields != other.fields && (this.fields == null || !this.fields.equals(other.fields))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = super.hashCode();
        hash = 59 * hash + (this.fields != null ? this.fields.hashCode() : 0);
        return hash;
    }
    
    public ArrayNode getArray(String field) {
        return (ArrayNode) get(field);
    }
    
    public ObjectNode getObject(String field) {
        return (ObjectNode) get(field);
    }
    
    
    public <T> T getValue(String field,Class<T> valueType) {
        return getValue(field, valueType,null);
    }
    
    public <T> T getValue(String field,Class<T> valueType,T defaultValue) {
        ValueNode node = (ValueNode) get(field);
        if (node != null)
            return (T) node.getValue();
        return defaultValue;
    }

    public String getString(String field) {
        return getValue(field, String.class);
    }
    
    public int getInt(String field) {
        return (int) getValue(field, Integer.class,0);
    }
    
    public float getFloat(String field) {
        return (float) getValue(field, Float.class,0F);
    }
    
    public double getDouble(String field) {
        return (double) getValue(field, Double.class,0D);
    }
    
    public boolean getBoolean(String field) {
        return (boolean) getValue(field, Boolean.class,false);
    }
}
