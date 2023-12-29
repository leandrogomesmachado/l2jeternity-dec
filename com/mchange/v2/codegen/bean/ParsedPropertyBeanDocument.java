package com.mchange.v2.codegen.bean;

import com.mchange.v1.xml.DomParseUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class ParsedPropertyBeanDocument {
   static final String[] EMPTY_SA = new String[0];
   String packageName;
   int class_modifiers;
   String className;
   String superclassName;
   String[] interfaceNames = EMPTY_SA;
   String[] generalImports = EMPTY_SA;
   String[] specificImports = EMPTY_SA;
   Property[] properties;

   public ParsedPropertyBeanDocument(Document var1) {
      Element var2 = var1.getDocumentElement();
      this.packageName = DomParseUtils.allTextFromUniqueChild(var2, "package");
      Element var3 = DomParseUtils.uniqueImmediateChild(var2, "modifiers");
      if (var3 != null) {
         this.class_modifiers = parseModifiers(var3);
      } else {
         this.class_modifiers = 1;
      }

      Element var4 = DomParseUtils.uniqueChild(var2, "imports");
      if (var4 != null) {
         this.generalImports = DomParseUtils.allTextFromImmediateChildElements(var4, "general");
         this.specificImports = DomParseUtils.allTextFromImmediateChildElements(var4, "specific");
      }

      this.className = DomParseUtils.allTextFromUniqueChild(var2, "output-class");
      this.superclassName = DomParseUtils.allTextFromUniqueChild(var2, "extends");
      Element var5 = DomParseUtils.uniqueChild(var2, "implements");
      if (var5 != null) {
         this.interfaceNames = DomParseUtils.allTextFromImmediateChildElements(var5, "interface");
      }

      Element var6 = DomParseUtils.uniqueChild(var2, "properties");
      this.properties = this.findProperties(var6);
   }

   public ClassInfo getClassInfo() {
      return new ClassInfo() {
         @Override
         public String getPackageName() {
            return ParsedPropertyBeanDocument.this.packageName;
         }

         @Override
         public int getModifiers() {
            return ParsedPropertyBeanDocument.this.class_modifiers;
         }

         @Override
         public String getClassName() {
            return ParsedPropertyBeanDocument.this.className;
         }

         @Override
         public String getSuperclassName() {
            return ParsedPropertyBeanDocument.this.superclassName;
         }

         @Override
         public String[] getInterfaceNames() {
            return ParsedPropertyBeanDocument.this.interfaceNames;
         }

         @Override
         public String[] getGeneralImports() {
            return ParsedPropertyBeanDocument.this.generalImports;
         }

         @Override
         public String[] getSpecificImports() {
            return ParsedPropertyBeanDocument.this.specificImports;
         }
      };
   }

   public Property[] getProperties() {
      return (Property[])this.properties.clone();
   }

   private Property[] findProperties(Element var1) {
      NodeList var2 = DomParseUtils.immediateChildElementsByTagName(var1, "property");
      int var3 = var2.getLength();
      Property[] var4 = new Property[var3];

      for(int var5 = 0; var5 < var3; ++var5) {
         Element var6 = (Element)var2.item(var5);
         int var7 = modifiersThroughParentElem(var6, "variable", 2);
         String var8 = DomParseUtils.allTextFromUniqueChild(var6, "name", true);
         String var9 = DomParseUtils.allTextFromUniqueChild(var6, "type", true);
         String var10 = DomParseUtils.allTextFromUniqueChild(var6, "defensive-copy", true);
         String var11 = DomParseUtils.allTextFromUniqueChild(var6, "default-value", true);
         int var12 = modifiersThroughParentElem(var6, "getter", 1);
         int var13 = modifiersThroughParentElem(var6, "setter", 1);
         Element var17 = DomParseUtils.uniqueChild(var6, "read-only");
         boolean var14 = var17 != null;
         Element var18 = DomParseUtils.uniqueChild(var6, "bound");
         boolean var15 = var18 != null;
         Element var19 = DomParseUtils.uniqueChild(var6, "constrained");
         boolean var16 = var19 != null;
         var4[var5] = new SimpleProperty(var7, var8, var9, var10, var11, var12, var13, var14, var15, var16);
      }

      return var4;
   }

   private static int modifiersThroughParentElem(Element var0, String var1, int var2) {
      Element var3 = DomParseUtils.uniqueChild(var0, var1);
      if (var3 != null) {
         Element var4 = DomParseUtils.uniqueChild(var3, "modifiers");
         return var4 != null ? parseModifiers(var4) : var2;
      } else {
         return var2;
      }
   }

   private static int parseModifiers(Element var0) {
      int var1 = 0;
      String[] var2 = DomParseUtils.allTextFromImmediateChildElements(var0, "modifier", true);
      int var3 = 0;

      for(int var4 = var2.length; var3 < var4; ++var3) {
         String var5 = var2[var3];
         if ("public".equals(var5)) {
            var1 |= 1;
         } else if ("protected".equals(var5)) {
            var1 |= 4;
         } else if ("private".equals(var5)) {
            var1 |= 2;
         } else if ("final".equals(var5)) {
            var1 |= 16;
         } else if ("abstract".equals(var5)) {
            var1 |= 1024;
         } else if ("static".equals(var5)) {
            var1 |= 8;
         } else if ("synchronized".equals(var5)) {
            var1 |= 32;
         } else if ("volatile".equals(var5)) {
            var1 |= 64;
         } else if ("transient".equals(var5)) {
            var1 |= 128;
         } else if ("strictfp".equals(var5)) {
            var1 |= 2048;
         } else if ("native".equals(var5)) {
            var1 |= 256;
         } else {
            if (!"interface".equals(var5)) {
               throw new IllegalArgumentException("Bad modifier: " + var5);
            }

            var1 |= 512;
         }
      }

      return var1;
   }
}
