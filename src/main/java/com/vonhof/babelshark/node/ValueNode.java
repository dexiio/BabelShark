package com.vonhof.babelshark.node;

/**
 *
 * @author Henrik Hofmeister <hh@cphse.com>
 */
public final class ValueNode<T> extends SharkNode {
    private T value;

    public ValueNode() {
        super(NodeType.VALUE);
    }

    public ValueNode(T value) {
        this();
        this.value = value;
    }
    

    public T getValue() {
        return value;
    }
    
    public void setValue(T value) {
        this.value = value;
    }

    @Override
    public String toString() {
        if (value instanceof String || value instanceof Enum)
            return String.format("\"%s\"",value);
        return String.format("%s",value);
    }

    @Override
    public boolean equals(Object obj) {
        if (!super.equals(obj))
            return false;
        final ValueNode<T> other = (ValueNode<T>) obj;
        if (this.value != other.value && (this.value == null || !this.value.equals(other.value))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = super.hashCode();
        hash = 47 * hash + (this.value != null ? this.value.hashCode() : 0);
        return hash;
    }
    
    
    
    
}
