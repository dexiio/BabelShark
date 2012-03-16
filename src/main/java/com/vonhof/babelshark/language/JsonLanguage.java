package com.vonhof.babelshark.language;

import com.vonhof.babelshark.*;
import com.vonhof.babelshark.node.ArrayNode;
import com.vonhof.babelshark.node.ObjectNode;
import com.vonhof.babelshark.node.SharkNode;
import com.vonhof.babelshark.node.ValueNode;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map.Entry;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.ObjectMapper;

/**
 *
 * @author Henrik Hofmeister <@vonhofdk>
 */
public class JsonLanguage extends SharkLanguageBase {
    private final JsonFactory jsonFactory = new JsonFactory(); 
    private final ObjectMapper om = new ObjectMapper(jsonFactory);
    private final Reader reader = new Reader();
    private final Writer writer = new Writer();

    public JsonLanguage() {
        super("json", new String[]{"application/json","text/json"});
    }
    
    public ObjectReader getObjectReader() {
        return reader;
    }

    public ObjectWriter getObjectWriter() {
        return writer;
    }
    
    public class Reader implements ObjectReader {

        public String[] getContentTypes() {
            return JsonLanguage.this.getContentTypes();
        }

        public SharkNode read(Input input) throws IOException {
            JsonParser parser = jsonFactory.createJsonParser(input.getStream());
            parser.setCodec(om);
            JsonNode node = parser.readValueAsTree();
            return jsonToShark(node);
        }
        private SharkNode jsonToShark(JsonNode node) {
            if (node == null || node.isNull())
                return new ValueNode<Object>(null);
            
            if (node.isBigDecimal())
                return new ValueNode(node.getDecimalValue());
            if (node.isBigInteger())
                return new ValueNode(node.getBigIntegerValue());
            if (node.isBoolean())
                return new ValueNode(node.getBooleanValue());
            if (node.isDouble())
                return new ValueNode(node.getDoubleValue());
            if (node.isInt())
                return new ValueNode(node.getIntValue());
            if (node.isLong())
                return new ValueNode(node.getLongValue());
            if (node.isTextual())
                return new ValueNode(node.getTextValue());
            
            if (node.isArray()) {
                ArrayNode out = new ArrayNode();
                for(JsonNode child:node) {
                    out.add(jsonToShark(child));
                }
                return out;
            }
            
            if (node.isObject()) {
                ObjectNode out = new ObjectNode();
                Iterator<Entry<String, JsonNode>> fields = node.getFields();
                while(fields.hasNext()) {
                    Entry<String, JsonNode> entry = fields.next();
                    out.put(entry.getKey(),jsonToShark(entry.getValue()));
                }
                return out;
            }
            throw new UnknownError(String.format("Unkown node type: %s",node));
        }
        
    }
    
    public class Writer implements ObjectWriter {

        public String getContentType() {
            return JsonLanguage.this.getContentTypes()[0];
        }

        public void write(Output output, SharkNode node) throws IOException {
            JsonGenerator g = jsonFactory.createJsonGenerator(output.getStream());
            writeNode(g, node);
            g.close();
        }
        private void writeNode(JsonGenerator g,SharkNode node) throws IOException {
            if (node instanceof ValueNode) {
                g.writeObject(((ValueNode)node).getValue());
                return;
            }
            if (node instanceof ArrayNode) {
                ArrayNode array = (ArrayNode) node;
                g.writeStartArray();
                for(SharkNode child:array) {
                    writeNode(g, child);
                }
                g.writeEndArray();
                return;
            }
            if (node instanceof ObjectNode) {
                ObjectNode object = (ObjectNode) node;
                g.writeStartObject();
                for(String field:object.getFields()) {
                    g.writeFieldName(field);
                    writeNode(g, object.get(field));
                }
                g.writeEndObject();
                return;
            }
            
            throw new UnknownError(String.format("Unkown node type: %s",node));
        }
    }

}
