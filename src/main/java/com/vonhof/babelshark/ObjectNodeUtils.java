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
     * Get a {@code value} in {@code values} from a {@code path}:
     * <ul>
     *     <li>Simple path, e.g. "path"</li>
     *     <li>Object (complex) path, e.g. "path/to/value"</li>
     * </ul>
     *
     * The value itself can be a simple value or an object.
     *
     * NB! The method is recursive.
     *
     * @param values
     * @param path
     * @return the value identified by the path
     */
    public static SharkNode getValueFromPath(Map<String, SharkNode> values, String path) {
        if (values == null || MapUtils.isEmpty(values) || StringUtils.isEmpty(path)) {
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

        // Handle path = "/"
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
     * Set a {@code value} in {@code values} on a {@code path}:
     * <ul>
     *     <li>Simple path, e.g. "path"</li>
     *     <li>Object (complex) path, e.g. "path/to/value"</li>
     * </ul>
     *
     * The value itself can be a simple value or an object.
     *
     * @param values
     * @param path
     * @param value
     */
    public static void setValueFromPath(ObjectNode values, String path, SharkNode value) {
        if (values == null || StringUtils.isEmpty(path)) {
            return;
        }

        if (!path.contains("/")) {
            values.put(path, value);
            return;
        }

        // Skip empty parts
        String[] rawParts = path.split("/");
        List<String> parts = new ArrayList<>();
        for(int i = 0; i < rawParts.length; i++) {
            if (StringUtils.isEmpty(rawParts[i])) {
                continue;
            }

            parts.add(rawParts[i]);
        }

        for(int i = 0; i < parts.size(); i++) {
            boolean isLast = (parts.size() - 1) == i;
            String part = parts.get(i);

            if (isLast) {
                values.put(part, value);
            } else {
                // Encountering the first field of the object will create the "container" object
                if (!(values.get(part) instanceof ObjectNode)) {
                    values.put(part, new ObjectNode());
                }

                SharkNode testNode = values.get(part);

                values = (ObjectNode) testNode;
            }
        }
    }

    /**
     * Map values of columns in {@param values} using {@param columnMapping}.
     *
     * @param values
     * @param columnMapping
     * @return the mapped values
     */
    public static Map<String, SharkNode> mapColumns(Map<String, SharkNode> values, Map<String, String>
            columnMapping) {
        if (columnMapping == null) {
            return values;
        }

        ObjectNode result = new ObjectNode();
        for (String sourceColumnName : columnMapping.keySet()) {
            String targetColumnName = columnMapping.get(sourceColumnName);
            SharkNode rowValue = getValueFromPath(values, sourceColumnName);
            if (targetColumnName != null && rowValue != null) {
                setValueFromPath(result, targetColumnName, rowValue);
            }
        }

        return bs.convert(result, SharkType.forMap(Map.class, SharkNode.class));
    }

}
