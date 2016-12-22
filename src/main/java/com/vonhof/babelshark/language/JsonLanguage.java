package com.vonhof.babelshark.language;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vonhof.babelshark.Input;
import com.vonhof.babelshark.ObjectReader;
import com.vonhof.babelshark.ObjectWriter;
import com.vonhof.babelshark.Output;
import com.vonhof.babelshark.SharkLanguageBase;
import com.vonhof.babelshark.node.ArrayNode;
import com.vonhof.babelshark.node.ObjectNode;
import com.vonhof.babelshark.node.SharkNode;
import com.vonhof.babelshark.node.ValueNode;
import java.io.IOException;
import java.util.Iterator;

import org.apache.commons.io.IOUtils;

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
        super("json", "application/json","text/json");
    }
    
    @Override
    public ObjectReader getObjectReader() {
        return reader;
    }

    @Override
    public ObjectWriter getObjectWriter() {
        return writer;
    }
    
    public class Reader implements ObjectReader {

        @Override
        public String[] getContentTypes() {
            return JsonLanguage.this.getContentTypes();
        }

        @Override
        public SharkNode read(Input input) throws IOException {
            String json = IOUtils.toString(input.getStream(), "UTF-8");
            try {
                JsonNode node = om.readTree(json);
                return jsonToShark(node);
            } catch (IOException ex) {
                throw ex;
            } catch (Throwable ex) {
                throw new IOException(IOUtils.toString(input.getStream(), "UTF-8"),ex);
            }
        }
        private SharkNode jsonToShark(JsonNode node) {
            if (node == null || node.isNull())
                return new ValueNode<Object>(null);
            
            if (node.isBigDecimal())
                return new ValueNode(node.decimalValue());
            if (node.isBigInteger())
                return new ValueNode(node.bigIntegerValue());
            if (node.isBoolean())
                return new ValueNode(node.booleanValue());
            if (node.isDouble())
                return new ValueNode(node.doubleValue());
            if (node.isInt())
                return new ValueNode(node.intValue());
            if (node.isLong())
                return new ValueNode(node.longValue());
            
            if (node.isTextual())
                return new ValueNode(node.textValue());
            
            if (node.isArray()) {
                ArrayNode out = new ArrayNode();
                for(JsonNode child:node) {
                    out.add(jsonToShark(child));
                }
                return out;
            }
            
            if (node.isObject()) {
                ObjectNode out = new ObjectNode();
                Iterator<String> fields = node.fieldNames();
                while(fields.hasNext()) {
                    String name = fields.next();
                    JsonNode entry = node.get(name);
                    out.put(name,jsonToShark(entry));
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
            JsonGenerator g = jsonFactory.createJsonGenerator(output.getStream(), JsonEncoding.UTF8);
            writeNode(g, node);
            g.close();
        }
        private void writeNode(JsonGenerator g,SharkNode node) throws IOException {
            if (node == null) {
                g.writeNull();
                return;
            }
        
            if (node instanceof ValueNode) {
                Object value = ((ValueNode)node).getValue();
                if (value instanceof Enum)
                    value = ((Enum)value).name();
                g.writeObject(value);
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
