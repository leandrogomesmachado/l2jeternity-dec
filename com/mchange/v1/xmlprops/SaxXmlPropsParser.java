package com.mchange.v1.xmlprops;

import com.mchange.v1.xml.StdErrErrorHandler;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

public class SaxXmlPropsParser {
   static final String DEFAULT_XML_READER = "org.apache.xerces.parsers.SAXParser";
   static final String XMLPROPS_NAMESPACE_URI = "http://www.mchange.com/namespaces/xmlprops";

   public static Properties parseXmlProps(InputStream var0) throws XmlPropsException {
      try {
         String var1 = "org.apache.xerces.parsers.SAXParser";
         XMLReader var2 = (XMLReader)Class.forName(var1).newInstance();
         InputSource var3 = new InputSource(var0);
         return parseXmlProps(var3, var2, null, null);
      } catch (XmlPropsException var4) {
         throw var4;
      } catch (Exception var5) {
         var5.printStackTrace();
         throw new XmlPropsException("Exception while instantiating XMLReader.", var5);
      }
   }

   private static Properties parseXmlProps(InputSource var0, XMLReader var1, EntityResolver var2, ErrorHandler var3) throws XmlPropsException {
      try {
         if (var2 != null) {
            var1.setEntityResolver(var2);
         }

         if (var3 == null) {
            var3 = new StdErrErrorHandler();
         }

         var1.setErrorHandler((ErrorHandler)var3);
         SaxXmlPropsParser.XmlPropsContentHandler var4 = new SaxXmlPropsParser.XmlPropsContentHandler();
         var1.setContentHandler(var4);
         var1.parse(var0);
         return var4.getLastProperties();
      } catch (Exception var5) {
         if (var5 instanceof SAXException) {
            ((SAXException)var5).getException().printStackTrace();
         }

         var5.printStackTrace();
         throw new XmlPropsException(var5);
      }
   }

   public static void main(String[] var0) {
      try {
         BufferedInputStream var1 = new BufferedInputStream(new FileInputStream(var0[0]));
         SaxXmlPropsParser var2 = new SaxXmlPropsParser();
         Properties var3 = parseXmlProps(var1);

         for(String var5 : var3.keySet()) {
            String var6 = var3.getProperty(var5);
            System.err.println(var5 + '=' + var6);
         }
      } catch (Exception var7) {
         var7.printStackTrace();
      }
   }

   static class XmlPropsContentHandler implements ContentHandler {
      Locator locator;
      Properties props;
      String name;
      StringBuffer valueBuf;

      @Override
      public void setDocumentLocator(Locator var1) {
         this.locator = var1;
      }

      @Override
      public void startDocument() throws SAXException {
         this.props = new Properties();
      }

      @Override
      public void startElement(String var1, String var2, String var3, Attributes var4) {
         System.err.println("--> startElement( " + var1 + ", " + var2 + ", " + var4 + ")");
         if (var1.equals("") || var1.equals("http://www.mchange.com/namespaces/xmlprops")) {
            if (var2.equals("property")) {
               this.name = var4.getValue(var1, "name");
               this.valueBuf = new StringBuffer();
            }
         }
      }

      @Override
      public void characters(char[] var1, int var2, int var3) throws SAXException {
         if (this.valueBuf != null) {
            this.valueBuf.append(var1, var2, var3);
         }
      }

      @Override
      public void ignorableWhitespace(char[] var1, int var2, int var3) throws SAXException {
         if (this.valueBuf != null) {
            this.valueBuf.append(var1, var2, var3);
         }
      }

      @Override
      public void endElement(String var1, String var2, String var3) throws SAXException {
         if (var1.equals("") || var1.equals("http://www.mchange.com/namespaces/xmlprops")) {
            if (var2.equals("property")) {
               System.err.println("NAME: " + this.name);
               this.props.put(this.name, this.valueBuf.toString());
               this.valueBuf = null;
            }
         }
      }

      @Override
      public void endDocument() throws SAXException {
      }

      @Override
      public void startPrefixMapping(String var1, String var2) throws SAXException {
      }

      @Override
      public void endPrefixMapping(String var1) throws SAXException {
      }

      @Override
      public void processingInstruction(String var1, String var2) throws SAXException {
      }

      @Override
      public void skippedEntity(String var1) throws SAXException {
      }

      public Properties getLastProperties() {
         return this.props;
      }
   }
}
