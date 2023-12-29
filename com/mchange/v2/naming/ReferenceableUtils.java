package com.mchange.v2.naming;

import com.mchange.v2.log.MLevel;
import com.mchange.v2.log.MLog;
import com.mchange.v2.log.MLogger;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Hashtable;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.StringRefAddr;
import javax.naming.spi.ObjectFactory;

public final class ReferenceableUtils {
   static final MLogger logger = MLog.getLogger(ReferenceableUtils.class);
   static final String REFADDR_VERSION = "version";
   static final String REFADDR_CLASSNAME = "classname";
   static final String REFADDR_FACTORY = "factory";
   static final String REFADDR_FACTORY_CLASS_LOCATION = "factoryClassLocation";
   static final String REFADDR_SIZE = "size";
   static final int CURRENT_REF_VERSION = 1;

   public static String literalNullToNull(String var0) {
      return var0 != null && !"null".equals(var0) ? var0 : null;
   }

   public static Object referenceToObject(Reference var0, Name var1, Context var2, Hashtable var3) throws NamingException {
      try {
         String var4 = var0.getFactoryClassName();
         String var11 = var0.getFactoryClassLocation();
         ClassLoader var6 = Thread.currentThread().getContextClassLoader();
         if (var6 == null) {
            var6 = ReferenceableUtils.class.getClassLoader();
         }

         Object var7;
         if (var11 == null) {
            var7 = var6;
         } else {
            URL var8 = new URL(var11);
            var7 = new URLClassLoader(new URL[]{var8}, var6);
         }

         Class var12 = Class.forName(var4, true, (ClassLoader)var7);
         ObjectFactory var9 = (ObjectFactory)var12.newInstance();
         return var9.getObjectInstance(var0, var1, var2, var3);
      } catch (Exception var10) {
         if (logger.isLoggable(MLevel.FINE)) {
            logger.log(MLevel.FINE, "Could not resolve Reference to Object!", (Throwable)var10);
         }

         NamingException var5 = new NamingException("Could not resolve Reference to Object!");
         var5.setRootCause(var10);
         throw var5;
      }
   }

   /** @deprecated */
   public static void appendToReference(Reference var0, Reference var1) throws NamingException {
      int var2 = var1.size();
      var0.add(new StringRefAddr("version", String.valueOf(1)));
      var0.add(new StringRefAddr("classname", var1.getClassName()));
      var0.add(new StringRefAddr("factory", var1.getFactoryClassName()));
      var0.add(new StringRefAddr("factoryClassLocation", var1.getFactoryClassLocation()));
      var0.add(new StringRefAddr("size", String.valueOf(var2)));

      for(int var3 = 0; var3 < var2; ++var3) {
         var0.add(var1.get(var3));
      }
   }

   /** @deprecated */
   public static ReferenceableUtils.ExtractRec extractNestedReference(Reference var0, int var1) throws NamingException {
      try {
         int var2 = Integer.parseInt((String)var0.get(var1++).getContent());
         if (var2 != 1) {
            throw new NamingException("Bad version of nested reference!!!");
         } else {
            String var3 = (String)var0.get(var1++).getContent();
            String var4 = (String)var0.get(var1++).getContent();
            String var5 = (String)var0.get(var1++).getContent();
            Reference var6 = new Reference(var3, var4, var5);
            int var7 = Integer.parseInt((String)var0.get(var1++).getContent());

            for(int var8 = 0; var8 < var7; ++var8) {
               var6.add(var0.get(var1++));
            }

            return new ReferenceableUtils.ExtractRec(var6, var1);
         }
      } catch (NumberFormatException var9) {
         if (logger.isLoggable(MLevel.FINE)) {
            logger.log(MLevel.FINE, "Version or size nested reference was not a number!!!", (Throwable)var9);
         }

         throw new NamingException("Version or size nested reference was not a number!!!");
      }
   }

   private ReferenceableUtils() {
   }

   /** @deprecated */
   public static class ExtractRec {
      public Reference ref;
      public int index;

      private ExtractRec(Reference var1, int var2) {
         this.ref = var1;
         this.index = var2;
      }
   }
}
