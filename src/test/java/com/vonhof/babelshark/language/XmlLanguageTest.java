package com.vonhof.babelshark.language;

import com.vonhof.babelshark.BabelShark;
import com.vonhof.babelshark.Input;
import com.vonhof.babelshark.exception.MappingException;
import com.vonhof.babelshark.node.ArrayNode;
import com.vonhof.babelshark.node.ObjectNode;
import com.vonhof.babelshark.node.ValueNode;
import java.io.IOException;
import junit.framework.TestCase;

/**
 *
 * @author Henrik Hofmeister <@vonhofdk>
 */
public class XmlLanguageTest extends TestCase {
    
    private static final String XML_VERSION = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>";
    private static final String XML_SIMPLE = XML_VERSION+"<out><entry>true</entry><entry>false</entry></out>";
    private static final String XML_OBJECT = XML_VERSION+"<out><id>Some id</id><name>some name</name><active>true</active><visits>123</visits></out>";

    
    public XmlLanguageTest(String testName) {
        super(testName);
    }
    
    @Override
    protected void setUp() throws Exception {
        BabelShark.register(new XmlLanguage());
    }
    

    public void testCanReadSimpleXml() throws MappingException, IOException {
        Input input = new Input(XML_SIMPLE, "xml");
        ArrayNode result = BabelShark.read(input,ArrayNode.class);
        assertEquals(new ValueNode("true"),result.get(0));
        assertEquals(new ValueNode("false"),result.get(1));
    }
    
    public void testCanReadXmlObject() throws MappingException, IOException {
        Input input = new Input(XML_OBJECT, "xml");
        ObjectNode result = BabelShark.read(input,ObjectNode.class);
        
        assertEquals(new ValueNode("Some id"),result.get("id"));
        assertEquals(new ValueNode("some name"),result.get("name"));
        assertEquals(new ValueNode("true"),result.get("active"));
        assertEquals(new ValueNode("123"),result.get("visits"));
    }
    
    
    public void testCanWriteSimpleXml() throws MappingException, IOException {
        ArrayNode array = new ArrayNode();
        array.add(true,false);
        
        String xml = BabelShark.writeToString(array,"xml");
        assertEquals(XML_SIMPLE,xml);
    }
    
    public void testCanWriteXmlObject() throws MappingException, IOException {
        ObjectNode obj = new ObjectNode();
        obj.put("id","Some id");
        obj.put("name","some name");
        obj.put("active",true);
        obj.put("visits",123);
        
        String xml = BabelShark.writeToString(obj,"xml");
        assertEquals(XML_OBJECT,xml);
    }
}
