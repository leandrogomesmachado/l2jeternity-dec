package com.mchange.v2.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Properties;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class XmlProperties extends Properties {
   static final String DTD_SYSTEM_ID = "http://www.mchange.com/dtd/xml-properties.dtd";
   static final String DTD_RSRC_PATH = "dtd/xml-properties.dtd";
   DocumentBuilder docBuilder;
   Transformer identityTransformer;

   public XmlProperties() throws ParserConfigurationException, TransformerConfigurationException {
      EntityResolver var1 = new EntityResolver() {
         @Override
         public InputSource resolveEntity(String var1, String var2) {
            if ("http://www.mchange.com/dtd/xml-properties.dtd".equals(var2)) {
               InputStream var3 = XmlProperties.class.getResourceAsStream("dtd/xml-properties.dtd");
               return new InputSource(var3);
            } else {
               return null;
            }
         }
      };
      ErrorHandler var2 = new ErrorHandler() {
         @Override
         public void warning(SAXParseException var1) throws SAXException {
            System.err.println("[Warning] " + var1.toString());
         }

         @Override
         public void error(SAXParseException var1) throws SAXException {
            System.err.println("[Error] " + var1.toString());
         }

         @Override
         public void fatalError(SAXParseException var1) throws SAXException {
            System.err.println("[Fatal Error] " + var1.toString());
         }
      };
      DocumentBuilderFactory var3 = DocumentBuilderFactory.newInstance();
      var3.setValidating(true);
      var3.setCoalescing(true);
      var3.setIgnoringComments(true);
      this.docBuilder = var3.newDocumentBuilder();
      this.docBuilder.setEntityResolver(var1);
      this.docBuilder.setErrorHandler(var2);
      TransformerFactory var4 = TransformerFactory.newInstance();
      this.identityTransformer = var4.newTransformer();
      this.identityTransformer.setOutputProperty("indent", "yes");
      this.identityTransformer.setOutputProperty("doctype-system", "http://www.mchange.com/dtd/xml-properties.dtd");
   }

   public synchronized void loadXml(InputStream var1) throws IOException, SAXException {
      Document var2 = this.docBuilder.parse(var1);
      NodeList var3 = var2.getElementsByTagName("property");
      int var4 = 0;

      for(int var5 = var3.getLength(); var4 < var5; ++var4) {
         this.extractProperty(var3.item(var4));
      }
   }

   private void extractProperty(Node var1) {
      Element var2 = (Element)var1;
      String var3 = var2.getAttribute("name");
      boolean var4 = Boolean.valueOf(var2.getAttribute("trim"));
      NodeList var5 = var2.getChildNodes();
      int var6 = var5.getLength();

      assert var6 >= 0 && var6 <= 1 : "Bad number of children of property element: " + var6;

      String var7 = var6 == 0 ? "" : ((Text)var5.item(0)).getNodeValue();
      if (var4) {
         var7 = var7.trim();
      }

      this.put(var3, var7);
   }

   public synchronized void saveXml(OutputStream var1) throws IOException, TransformerException {
      this.storeXml(var1, null);
   }

   public synchronized void storeXml(OutputStream var1, String var2) throws IOException, TransformerException {
      Document var3 = this.docBuilder.newDocument();
      if (var2 != null) {
         Comment var4 = var3.createComment(var2);
         var3.appendChild(var4);
      }

      Element var10 = var3.createElement("xml-properties");
      Iterator var5 = this.keySet().iterator();

      while(var5.hasNext()) {
         Element var6 = var3.createElement("property");
         String var7 = (String)var5.next();
         String var8 = (String)this.get(var7);
         var6.setAttribute("name", var7);
         Text var9 = var3.createTextNode(var8);
         var6.appendChild(var9);
         var10.appendChild(var6);
      }

      var3.appendChild(var10);
      this.identityTransformer.transform(new DOMSource(var3), new StreamResult(var1));
   }

   public static void main(String[] var0) {
      BufferedInputStream var1 = null;
      BufferedOutputStream var2 = null;

      try {
         var1 = new BufferedInputStream(new FileInputStream(var0[0]));
         var2 = new BufferedOutputStream(new FileOutputStream(var0[1]));
         XmlProperties var3 = new XmlProperties();
         var3.loadXml(var1);
         var3.list(System.out);
         var3.storeXml(var2, "This is the resaved test document.");
         var2.flush();
      } catch (Exception var16) {
         var16.printStackTrace();
      } finally {
         try {
            if (var1 != null) {
               var1.close();
            }
         } catch (Exception var15) {
            var15.printStackTrace();
         }

         try {
            if (var2 != null) {
               var2.close();
            }
         } catch (Exception var14) {
            var14.printStackTrace();
         }
      }
   }
}
