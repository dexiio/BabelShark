package com.vonhof.babelshark.node;

/**
 *
 * @author Henrik Hofmeister <@vonhofdk>
 */

abstract public class SharkNode {
    private final NodeType type;
    private boolean attribute;

    public SharkNode(NodeType type) {
        this.type = type;
    }

    public NodeType getType() {
        return type;
    }
    
    public boolean is(NodeType type) {
        return this.type.equals(type);
    }

    public boolean isAttribute() {
        return attribute;
    }

    public void setAttribute(boolean attribute) {
        this.attribute = attribute;
    }
    
    
    
    @Override
    public String toString() {
        return String.format("SharkNode<%s>",type);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SharkNode other = (SharkNode) obj;
        if (this.type != other.type) {
            return false;
        }
        if (this.attribute != other.attribute) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 43 * hash + (this.type != null ? this.type.hashCode() : 0);
        hash = 43 * hash + (this.attribute ? 1 : 0);
        return hash;
    }
    
    public static enum NodeType {MAP,LIST,VALUE}
}
