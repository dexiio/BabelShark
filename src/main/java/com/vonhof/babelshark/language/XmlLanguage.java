package com.vonhof.babelshark.language;

import com.vonhof.babelshark.*;
import com.vonhof.babelshark.node.ArrayNode;
import com.vonhof.babelshark.node.ObjectNode;
import com.vonhof.babelshark.node.SharkNode;
import com.vonhof.babelshark.node.ValueNode;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.w3c.dom.*;
import sun.misc.IOUtils;

/**
 *
 * @author Henrik Hofmeister <@vonhofdk>
 */
public class XmlLanguage extends SharkLanguageBase {
    private final static Logger log = Logger.getLogger(XmlLanguage.class);

    private final static String LIST_ENTRY_NAME = "entry";
    private final static DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    
    private final Reader reader = new Reader();
    private final Writer writer = new Writer();

    public XmlLanguage() {
        super("xml", new String[]{"application/xml","text/xml"});
    }
    
    public ObjectReader getObjectReader() {
        return reader;
    }

    public ObjectWriter getObjectWriter() {
        return writer;
    }
    
    public class Reader implements ObjectReader {

        public String[] getContentTypes() {
            return XmlLanguage.this.getContentTypes();
        }

        public SharkNode read(Input input) throws IOException {
            try {
                DocumentBuilder docBuilder = factory.newDocumentBuilder();
                Document doc = docBuilder.parse(input.getStream());
                
                return xmlToShark(doc);
            } catch (Throwable ex) {
                byte[] body = IOUtils.readFully(input.getStream(),0, true);
                throw new IOException(new String(body),ex);
            }
        }
        private SharkNode xmlToShark(Document node) {
            if (node == null)
                return new ValueNode<Object>(null);
            //Documents should only have 1 child
            return xmlToShark(node.getFirstChild());
        }
        
        private List<Node> getChildren(Node node,short type) {
            List<Node> out = new ArrayList<Node>();
            if (node.hasChildNodes()) {
                NodeList children = node.getChildNodes();
                for(int i = 0; i < children.getLength();i++) {
                    Node child = children.item(i);
                    if (child.getNodeType() == type)
                        out.add(child);
                }
            }
            return out;
        }
        private boolean isArray(Node node) {
            if (!node.hasChildNodes())
                return false;
            List<Node> children = getChildren(node,Node.ELEMENT_NODE);
            if (children.isEmpty())
                return false;
            String lastName = null;
            for(Node child:children) {
                String nodeName = child.getNodeName();
                if (lastName == null)
                    lastName = nodeName;
                if (!nodeName.equalsIgnoreCase(lastName))
                    return false;
            }
            return true;
        }
        
        private boolean isObject(Node node) {
            if (!node.hasChildNodes())
                return false;
            List<Node> children = getChildren(node,Node.ELEMENT_NODE);
            if (children.isEmpty())
                return false;
            String lastName = null;
            for(Node child:children) {
                String nodeName = child.getNodeName();
                if (lastName == null)
                    lastName = nodeName;
                else if (nodeName.equalsIgnoreCase(lastName))
                    return false;
            }
            return true;
        }
        private boolean isValue(Node node) {
            if (isArray(node) || isObject(node)) 
                return false;
            if (node.hasChildNodes()) {
                NodeList children = node.getChildNodes();
                for(int i = 0; i < children.getLength();i++) {
                    Node child = children.item(i);
                    switch(child.getNodeType()) {
                        case Node.CDATA_SECTION_NODE:
                        case Node.TEXT_NODE:
                            return true;
                    }
                }
            }
            return node.getNodeValue() != null && !node.getNodeValue().isEmpty();
        }
        private String getValue(Node node) {
            if (node.hasChildNodes()) {
                NodeList children = node.getChildNodes();
                for(int i = 0; i < children.getLength();i++) {
                    Node child = children.item(i);
                    switch(child.getNodeType()) {
                        case Node.CDATA_SECTION_NODE:
                        case Node.TEXT_NODE:
                            return child.getNodeValue();
                    }
                }
            }
            return node.getNodeValue();
        }
        private SharkNode xmlToShark(Node node) {
            if (node == null)
                return new ValueNode<Object>(null);
            
            
            if (isArray(node)) {
                ArrayNode out = new ArrayNode();
                List<Node> children = getChildren(node, Node.ELEMENT_NODE);
                for(Node child:children) {
                    out.add(xmlToShark(child));
                    
                }
                return out;
            }
            
            if (isObject(node)) {
                ObjectNode out = new ObjectNode();
                List<Node> children = getChildren(node, Node.ELEMENT_NODE);
                for(Node child:children) {
                    out.put(child.getNodeName(),xmlToShark(child));
                }
                return out;
            }
            if (isValue(node)) {
                String value = getValue(node);
                if (value != null)
                    return new ValueNode(value);
                
            }
            return new ValueNode(null);
        }
        
    }
    
    public class Writer implements ObjectWriter {

        public String getContentType() {
            return XmlLanguage.this.getContentTypes()[0];
        }

        public void write(Output output, SharkNode node) throws IOException {
            try {
                DocumentBuilder docBuilder = factory.newDocumentBuilder();
                Document doc = docBuilder.newDocument();
                
                Element rootNode = doc.createElement("out");
                doc.appendChild(rootNode);
                Node out = writeNode(rootNode,doc,node);
                rootNode.appendChild(out);
                
                TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		DOMSource source = new DOMSource(doc);
                
                transformer.transform(source, new StreamResult(output.getStream()));
                
            } catch (TransformerException ex) {
                log.error("Failed to parse XML", ex);
            } catch (ParserConfigurationException ex) {
                log.error("Failed to parse XML", ex);
            }
        }
        private Node writeNode(Node parent,Document doc,SharkNode node) throws IOException {
            if (node instanceof ValueNode) {
                return doc.createTextNode(String.valueOf(((ValueNode)node).getValue()));
            }
            if (node instanceof ArrayNode) {
                ArrayNode array = (ArrayNode) node;
                DocumentFragment listNode = doc.createDocumentFragment();
                
                for(SharkNode child:array) {
                    Element elm = doc.createElement(LIST_ENTRY_NAME);
                    Node entryNode = writeNode(elm,doc,child);
                    elm.appendChild(entryNode);
                    listNode.appendChild(elm);
                }
                return listNode;
            }
            if (node instanceof ObjectNode) {
                
                DocumentFragment structNode = doc.createDocumentFragment();
                ObjectNode object = (ObjectNode) node;
                
                for(String field:object.getFields()) {
                    SharkNode childNode = object.get(field);
                    if (childNode.isAttribute() && parent != null && parent instanceof Element) {
                        ValueNode value = (ValueNode) childNode;
                        ((Element)parent).setAttribute(field,String.valueOf(value.getValue()));
                        continue;
                    }
                    Element elm = doc.createElement(field);
                    Node entryNode = writeNode(elm,doc, childNode);
                    elm.appendChild(entryNode);
                    structNode.appendChild(elm);
                }
                
                return structNode;
            }
            
            throw new UnknownError(String.format("Unkown node type: %s",node));
        }
    }

}
