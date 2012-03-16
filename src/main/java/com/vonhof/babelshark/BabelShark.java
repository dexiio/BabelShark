package com.vonhof.babelshark;

import com.vonhof.babelshark.exception.MappingException;
import com.vonhof.babelshark.impl.DefaultNodeMapper;
import com.vonhof.babelshark.node.ArrayNode;
import com.vonhof.babelshark.node.SharkNode;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Singleton instance of the babelshark engine
 * @author Henrik Hofmeister <hh@cphse.com>
 */
public class BabelShark {
    public static final String[] CONTENT_JSON = {"application/json","text/json"};
    public static final String[] CONTENT_XML = {"text/xml"};
    public static final String[] CONTENT_CSV = {"text/csv"};
    public static final String[] CONTENT_HTML = {"text/html"};
    public static final String[] CONTENT_JAVAS = {"text/java-serialized"};
    public static final String[] CONTENT_PHPS = {"text/php-serialized"};
    
    private static final BabelShark instance = new BabelShark();
    private BabelShark() {
        
    }
    public static BabelShark getInstance() {
        return instance;
    }
    
    private NodeMapper mapper = new DefaultNodeMapper();
    private Map<String,SharkLanguage> languages = new HashMap<String, SharkLanguage>();
    private Map<String,ObjectReader> readers = new HashMap<String, ObjectReader>();
    private Map<String,ObjectWriter> writers = new HashMap<String, ObjectWriter>();
    
    public void register(SharkLanguage language) {
        final ObjectReader reader = language.getObjectReader();
        final ObjectWriter writer = language.getObjectWriter();
        
        for(String contentType:reader.getContentTypes()) {
            contentType = normalizeContentType(contentType);
            readers.put(contentType,reader);
        }
        writers.put(writer.getContentType(), writer);
        
        //Add language ids as content type - for convenience
        readers.put(language.getId(),reader);
        writers.put(language.getId(), writer);
        
        languages.put(language.getId(), language);
    }
    
    private ObjectReader getReader(String contentType) {
        contentType = normalizeContentType(contentType);
        return readers.get(contentType);
    }
    
    private ObjectWriter getWriter(String contentType) {
        contentType = normalizeContentType(contentType);
        return writers.get(contentType);
    }
    
    public <T> T read(Input input,Class<T> clz) throws MappingException, IOException {
        ObjectReader reader = getReader(input.getContentType());
        if (reader == null)
            throw new MappingException(String.format("Unknown content type:",input.getContentType()));
        SharkNode map = reader.read(input);
        return convert(map, clz);
    }
    
    public <T> T convert(SharkNode node,Class<T> clz) throws MappingException {
        return mapper.readAs(node,clz);
    }
    
    public void write(Output output,Object value) throws MappingException, IOException {
        ObjectWriter writer = getWriter(output.getContentType());
        if (writer == null)
            throw new MappingException(String.format("Unknown content type:",output.getContentType()));
        SharkNode map = mapper.toNode(value);
        writer.write(output,map);
    }
    
    private static String normalizeContentType(String contentType) {
        return contentType.toLowerCase().trim();
    }

    public String writeToString(SharkNode node, String contentType) throws MappingException, IOException {
        return new String(writeToByteArray(node, contentType));
    }
    
    public byte[] writeToByteArray(SharkNode node, String contentType) throws MappingException, IOException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        Output output = new Output(stream, contentType);
        write(output,node);
        return stream.toByteArray();
    }
}
