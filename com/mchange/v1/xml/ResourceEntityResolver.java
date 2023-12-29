package com.mchange.v1.xml;

import java.io.IOException;
import java.io.InputStream;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class ResourceEntityResolver implements EntityResolver {
   ClassLoader cl;
   String prefix;

   public ResourceEntityResolver(ClassLoader var1, String var2) {
      this.cl = var1;
      this.prefix = var2;
   }

   public ResourceEntityResolver(Class var1) {
      this(var1.getClassLoader(), classToPrefix(var1));
   }

   @Override
   public InputSource resolveEntity(String var1, String var2) throws SAXException, IOException {
      if (var2 == null) {
         return null;
      } else {
         int var3 = var2.lastIndexOf(47);
         String var4 = var3 >= 0 ? var2.substring(var3 + 1) : var2;
         InputStream var5 = this.cl.getResourceAsStream(this.prefix + var4);
         return var5 == null ? null : new InputSource(var5);
      }
   }

   private static String classToPrefix(Class var0) {
      String var1 = var0.getName();
      int var2 = var1.lastIndexOf(46);
      String var3 = var2 > 0 ? var1.substring(0, var2) : null;
      StringBuffer var4 = new StringBuffer(256);
      if (var3 != null) {
         var4.append(var3);
         int var5 = 0;

         for(int var6 = var4.length(); var5 < var6; ++var5) {
            if (var4.charAt(var5) == '.') {
               var4.setCharAt(var5, '/');
            }
         }

         var4.append('/');
      }

      return var4.toString();
   }
}
