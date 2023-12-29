package com.mchange.v1.xml;

import com.mchange.v1.util.DebugUtils;
import java.util.ArrayList;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

public final class DomParseUtils {
   static final boolean DEBUG = true;

   public static String allTextFromUniqueChild(Element var0, String var1) throws DOMException {
      return allTextFromUniqueChild(var0, var1, false);
   }

   public static String allTextFromUniqueChild(Element var0, String var1, boolean var2) throws DOMException {
      Element var3 = uniqueChildByTagName(var0, var1);
      return var3 == null ? null : allTextFromElement(var3, var2);
   }

   public static Element uniqueChild(Element var0, String var1) throws DOMException {
      return uniqueChildByTagName(var0, var1);
   }

   /** @deprecated */
   public static Element uniqueChildByTagName(Element var0, String var1) throws DOMException {
      NodeList var2 = var0.getElementsByTagName(var1);
      int var3 = var2.getLength();
      DebugUtils.myAssert(var3 <= 1, "There is more than one (" + var3 + ") child with tag name: " + var1 + "!!!");
      return var3 == 1 ? (Element)var2.item(0) : null;
   }

   public static String allText(Element var0) throws DOMException {
      return allTextFromElement(var0);
   }

   public static String allText(Element var0, boolean var1) throws DOMException {
      return allTextFromElement(var0, var1);
   }

   /** @deprecated */
   public static String allTextFromElement(Element var0) throws DOMException {
      return allTextFromElement(var0, false);
   }

   /** @deprecated */
   public static String allTextFromElement(Element var0, boolean var1) throws DOMException {
      StringBuffer var2 = new StringBuffer();
      NodeList var3 = var0.getChildNodes();
      int var4 = 0;

      for(int var5 = var3.getLength(); var4 < var5; ++var4) {
         Node var6 = var3.item(var4);
         if (var6 instanceof Text) {
            var2.append(var6.getNodeValue());
         }
      }

      String var7 = var2.toString();
      return var1 ? var7.trim() : var7;
   }

   public static String[] allTextFromImmediateChildElements(Element var0, String var1) throws DOMException {
      return allTextFromImmediateChildElements(var0, var1, false);
   }

   public static String[] allTextFromImmediateChildElements(Element var0, String var1, boolean var2) throws DOMException {
      NodeList var3 = immediateChildElementsByTagName(var0, var1);
      int var4 = var3.getLength();
      String[] var5 = new String[var4];

      for(int var6 = 0; var6 < var4; ++var6) {
         var5[var6] = allText((Element)var3.item(var6), var2);
      }

      return var5;
   }

   public static NodeList immediateChildElementsByTagName(Element var0, String var1) throws DOMException {
      return getImmediateChildElementsByTagName(var0, var1);
   }

   /** @deprecated */
   public static NodeList getImmediateChildElementsByTagName(Element var0, String var1) throws DOMException {
      final ArrayList var2 = new ArrayList();

      for(Node var3 = var0.getFirstChild(); var3 != null; var3 = var3.getNextSibling()) {
         if (var3 instanceof Element && ((Element)var3).getTagName().equals(var1)) {
            var2.add(var3);
         }
      }

      return new NodeList() {
         @Override
         public int getLength() {
            return var2.size();
         }

         @Override
         public Node item(int var1) {
            return (Node)var2.get(var1);
         }
      };
   }

   public static String allTextFromUniqueImmediateChild(Element var0, String var1) throws DOMException {
      Element var2 = uniqueImmediateChildByTagName(var0, var1);
      return var2 == null ? null : allTextFromElement(var2);
   }

   public static Element uniqueImmediateChild(Element var0, String var1) throws DOMException {
      return uniqueImmediateChildByTagName(var0, var1);
   }

   /** @deprecated */
   public static Element uniqueImmediateChildByTagName(Element var0, String var1) throws DOMException {
      NodeList var2 = getImmediateChildElementsByTagName(var0, var1);
      int var3 = var2.getLength();
      DebugUtils.myAssert(var3 <= 1, "There is more than one (" + var3 + ") child with tag name: " + var1 + "!!!");
      return var3 == 1 ? (Element)var2.item(0) : null;
   }

   /** @deprecated */
   public static String attrValFromElement(Element var0, String var1) throws DOMException {
      Attr var2 = var0.getAttributeNode(var1);
      return var2 == null ? null : var2.getValue();
   }

   private DomParseUtils() {
   }
}
