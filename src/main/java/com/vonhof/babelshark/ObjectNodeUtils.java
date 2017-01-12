package com.vonhof.babelshark;

import com.vonhof.babelshark.node.ObjectNode;
import com.vonhof.babelshark.node.SharkNode;
import com.vonhof.babelshark.node.SharkType;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ObjectNodeUtils {

    private static BabelSharkInstance bs = BabelShark.getDefaultInstance();

    /**
     * Get a value from a path:
     * <ul>
     *     <li>Simple path, e.g. "path"</li>
     *     <li>Object (complex) path, e.g. "path/to/value"</li>
     * </ul>
     *
     * The value itself can be simple an object.
     *
     * NB! The method is recursive.
     *
     * @param values
     * @param path
     * @return the value identified by the path
     */
    public static SharkNode getValueFromPath(Map<String, SharkNode> values, String path) {
        if (values == null || MapUtils.isEmpty(values) || path == null || StringUtils.isEmpty(path)) {
            return null;
        }

        if (!path.contains("/")) {
            return values.get(path);
        }

        String[] parts = path.split("/");

        List<String> remainingParts = new ArrayList<>();
        String firstPart = null;
        for(int i = 0; i < parts.length; i++) {

            // Find first non-empty part of path
            if (StringUtils.isEmpty(firstPart) &&
                    !StringUtils.isEmpty(parts[i])) {
                firstPart = parts[i];
                continue;
            }

            // Find remaining parts of path
            if (!StringUtils.isEmpty(firstPart) &&
                    !StringUtils.isEmpty(parts[i])) {
                remainingParts.add(parts[i]);
            }
        }

        // path = "/"
        if (StringUtils.isEmpty(firstPart)) {
            return null;
        }

        SharkNode subNode = values.get(firstPart);

        // Not possible to recurse deeper
        if (subNode == null ||
                subNode.is(SharkNode.NodeType.VALUE) ||
                remainingParts.isEmpty()) {

            // We reached the bottom
            if (remainingParts.isEmpty()) {
                return subNode;
            }

            return null;
        } else {
            // At this point, there is one or more path elements remaining
            String remainingPath = StringUtils.join(remainingParts, "/");

            SharkType<Map, SharkNode> mapType = SharkType.forMap(Map.class, SharkNode.class);
            Map subNodeValues = bs.convert(subNode, mapType);

            // Recursive call
            return getValueFromPath(subNodeValues, remainingPath);
        }
    }

    /**
     * Recursive method to set a value on a "complex"/object path in target row values.
     *
     * @param targetRowValues
     * @param path
     * @param value
     */
    public static void setValueFromPath(ObjectNode targetRowValues, String path, SharkNode value) {
        if (!path.contains("/")) {
            targetRowValues.put(path, value);
            return;
        }
        String[] rawParts = path.split("/");

        List<String> parts = new ArrayList<>();

        for(int i = 0; i < rawParts.length; i++) {

            //Skip empty parts
            if (StringUtils.isEmpty(rawParts[i])) {
                continue;
            }

            parts.add(rawParts[i]);
        }

        for(int i = 0; i < parts.size(); i++) {
            boolean isLast = (parts.size() - 1) == i;
            String part = parts.get(i);

            if (isLast) {
                targetRowValues.put(part, value);
            } else {
                if (targetRowValues.get(part) == null) {
                    targetRowValues.put(part, new ObjectNode());
                }

                SharkNode testNode = targetRowValues.get(part);

                if (!testNode.is(SharkNode.NodeType.MAP)) {
                    return; //Unable to set child value of a non-map
                }

                targetRowValues = (ObjectNode) testNode;
            }
        }
    }

    /**
     * Map values of columns in {@param sourceRowValues} using {@param columnMapping}.
     *
     * @param sourceRowValues
     * @param columnMapping
     * @return the mapped values
     */
    // TODO: use for "Data set A/B column mapping for RL?
    public static Map<String, SharkNode> mapColumns(Map<String, SharkNode> sourceRowValues, Map<String, String>
            columnMapping) {

        //No mapping available
        if (columnMapping == null) {
            return sourceRowValues;
        }

        ObjectNode result = new ObjectNode();
        for (String sourceColumnName : columnMapping.keySet()) {
            String targetColumnName = columnMapping.get(sourceColumnName);
            SharkNode rowValue = getValueFromPath(sourceRowValues, sourceColumnName);
            if (targetColumnName != null && rowValue != null) {
                setValueFromPath(result, targetColumnName, rowValue);
            }
        }

        return bs.convert(result, SharkType.forMap(Map.class, SharkNode.class));
    }

}
