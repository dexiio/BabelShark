package com.vonhof.babelshark.node;

import java.util.*;
import java.util.Map.Entry;

/**
 *
 * @author Henrik Hofmeister <hh@cphse.com>
 */
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
    
}
