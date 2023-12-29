package com.mchange.v2.naming;

import com.mchange.v2.beans.BeansUtils;
import com.mchange.v2.lang.Coerce;
import com.mchange.v2.log.MLevel;
import com.mchange.v2.log.MLog;
import com.mchange.v2.log.MLogger;
import com.mchange.v2.ser.SerializableUtils;
import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.beans.PropertyEditor;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import javax.naming.BinaryRefAddr;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.RefAddr;
import javax.naming.Reference;
import javax.naming.StringRefAddr;
import javax.naming.spi.ObjectFactory;

public class JavaBeanObjectFactory implements ObjectFactory {
   private static final MLogger logger = MLog.getLogger(JavaBeanObjectFactory.class);
   static final Object NULL_TOKEN = new Object();

   @Override
   public Object getObjectInstance(Object var1, Name var2, Context var3, Hashtable var4) throws Exception {
      if (!(var1 instanceof Reference)) {
         return null;
      } else {
         Reference var5 = (Reference)var1;
         HashMap var6 = new HashMap();
         Enumeration var7 = var5.getAll();

         while(var7.hasMoreElements()) {
            RefAddr var8 = (RefAddr)var7.nextElement();
            var6.put(var8.getType(), var8);
         }

         Class var11 = Class.forName(var5.getClassName());
         Set var12 = null;
         BinaryRefAddr var9 = (BinaryRefAddr)var6.remove("com.mchange.v2.naming.JavaBeanReferenceMaker.REF_PROPS_KEY");
         if (var9 != null) {
            var12 = (Set)SerializableUtils.fromByteArray((byte[])var9.getContent());
         }

         Map var10 = this.createPropertyMap(var11, var6);
         return this.findBean(var11, var10, var12);
      }
   }

   private Map createPropertyMap(Class var1, Map var2) throws Exception {
      BeanInfo var3 = Introspector.getBeanInfo(var1);
      PropertyDescriptor[] var4 = var3.getPropertyDescriptors();
      HashMap var5 = new HashMap();
      int var6 = 0;

      for(int var7 = var4.length; var6 < var7; ++var6) {
         PropertyDescriptor var8 = var4[var6];
         String var9 = var8.getName();
         Class var10 = var8.getPropertyType();
         Object var11 = var2.remove(var9);
         if (var11 != null) {
            if (var11 instanceof StringRefAddr) {
               String var12 = (String)((StringRefAddr)var11).getContent();
               if (Coerce.canCoerce(var10)) {
                  var5.put(var9, Coerce.toObject(var12, var10));
               } else {
                  PropertyEditor var13 = BeansUtils.findPropertyEditor(var8);
                  var13.setAsText(var12);
                  var5.put(var9, var13.getValue());
               }
            } else if (var11 instanceof BinaryRefAddr) {
               byte[] var16 = (byte[])((BinaryRefAddr)var11).getContent();
               if (var16.length == 0) {
                  var5.put(var9, NULL_TOKEN);
               } else {
                  var5.put(var9, SerializableUtils.fromByteArray(var16));
               }
            } else if (logger.isLoggable(MLevel.WARNING)) {
               logger.warning(this.getClass().getName() + " -- unknown RefAddr subclass: " + var11.getClass().getName());
            }
         }
      }

      for(String var15 : var2.keySet()) {
         if (logger.isLoggable(MLevel.WARNING)) {
            logger.warning(this.getClass().getName() + " -- RefAddr for unknown property: " + var15);
         }
      }

      return var5;
   }

   protected Object createBlankInstance(Class var1) throws Exception {
      return var1.newInstance();
   }

   protected Object findBean(Class var1, Map var2, Set var3) throws Exception {
      Object var4 = this.createBlankInstance(var1);
      BeanInfo var5 = Introspector.getBeanInfo(var4.getClass());
      PropertyDescriptor[] var6 = var5.getPropertyDescriptors();
      int var7 = 0;

      for(int var8 = var6.length; var7 < var8; ++var7) {
         PropertyDescriptor var9 = var6[var7];
         String var10 = var9.getName();
         Object var11 = var2.get(var10);
         Method var12 = var9.getWriteMethod();
         if (var11 != null) {
            if (var12 != null) {
               var12.invoke(var4, var11 == NULL_TOKEN ? null : var11);
            } else if (logger.isLoggable(MLevel.WARNING)) {
               logger.warning(this.getClass().getName() + ": Could not restore read-only property '" + var10 + "'.");
            }
         } else if (var12 != null && (var3 == null || var3.contains(var10)) && logger.isLoggable(MLevel.WARNING)) {
            logger.warning(this.getClass().getName() + " -- Expected writable property ''" + var10 + "'' left at default value");
         }
      }

      return var4;
   }
}
