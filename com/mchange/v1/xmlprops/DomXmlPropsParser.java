package com.mchange.v1.xmlprops;

import com.mchange.v1.xml.ResourceEntityResolver;
import com.mchange.v1.xml.StdErrErrorHandler;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;

public class DomXmlPropsParser {
   static final String XMLPROPS_NAMESPACE_URI = "http://www.mchange.com/namespaces/xmlprops";
   static DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

   public Properties parseXmlProps(InputStream var1) throws XmlPropsException {
      return this.parseXmlProps(new InputSource(var1), new ResourceEntityResolver(this.getClass()), new StdErrErrorHandler());
   }

   private Properties parseXmlProps(InputSource var1, EntityResolver var2, ErrorHandler var3) throws XmlPropsException {
      try {
         Properties var4 = new Properties();
         DocumentBuilder var5 = factory.newDocumentBuilder();
         var5.setEntityResolver(var2);
         var5.setErrorHandler(var3);
         Document var6 = var5.parse(var1);
         Element var7 = var6.getDocumentElement();
         NodeList var8 = var7.getElementsByTagName("property");
         int var9 = 0;

         for(int var10 = var8.getLength(); var9 < var10; ++var9) {
            Element var11 = (Element)var8.item(var9);
            String var12 = var11.getAttribute("name");
            StringBuffer var13 = new StringBuffer();
            NodeList var14 = var11.getChildNodes();
            int var15 = 0;

            for(int var16 = var14.getLength(); var15 < var16; ++var15) {
               Node var17 = var14.item(var15);
               if (var17.getNodeType() == 3) {
                  var13.append(var17.getNodeValue());
               }
            }

            var4.put(var12, var13.toString());
         }

         return var4;
      } catch (Exception var18) {
         var18.printStackTrace();
         throw new XmlPropsException(var18);
      }
   }

   public static void main(String[] var0) {
      try {
         BufferedInputStream var1 = new BufferedInputStream(new FileInputStream(var0[0]));
         DomXmlPropsParser var2 = new DomXmlPropsParser();
         Properties var3 = var2.parseXmlProps(var1);

         for(String var5 : var3.keySet()) {
            String var6 = var3.getProperty(var5);
            System.err.println(var5 + '=' + var6);
         }
      } catch (Exception var7) {
         var7.printStackTrace();
      }
   }

   static {
      factory.setNamespaceAware(true);
      factory.setValidating(true);
   }
}
