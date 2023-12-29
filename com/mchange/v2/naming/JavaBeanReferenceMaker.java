package com.mchange.v2.naming;

import com.mchange.v2.beans.BeansUtils;
import com.mchange.v2.lang.Coerce;
import com.mchange.v2.log.MLevel;
import com.mchange.v2.log.MLog;
import com.mchange.v2.log.MLogger;
import com.mchange.v2.ser.IndirectPolicy;
import com.mchange.v2.ser.SerializableUtils;
import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.beans.PropertyEditor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import javax.naming.BinaryRefAddr;
import javax.naming.NamingException;
import javax.naming.RefAddr;
import javax.naming.Reference;
import javax.naming.StringRefAddr;

public class JavaBeanReferenceMaker implements ReferenceMaker {
   private static final MLogger logger = MLog.getLogger(JavaBeanReferenceMaker.class);
   static final String REF_PROPS_KEY = "com.mchange.v2.naming.JavaBeanReferenceMaker.REF_PROPS_KEY";
   static final Object[] EMPTY_ARGS = new Object[0];
   static final byte[] NULL_TOKEN_BYTES = new byte[0];
   String factoryClassName = "com.mchange.v2.naming.JavaBeanObjectFactory";
   String defaultFactoryClassLocation = null;
   Set referenceProperties = new HashSet();
   ReferenceIndirector indirector = new ReferenceIndirector();

   public Hashtable getEnvironmentProperties() {
      return this.indirector.getEnvironmentProperties();
   }

   public void setEnvironmentProperties(Hashtable var1) {
      this.indirector.setEnvironmentProperties(var1);
   }

   public void setFactoryClassName(String var1) {
      this.factoryClassName = var1;
   }

   public String getFactoryClassName() {
      return this.factoryClassName;
   }

   public String getDefaultFactoryClassLocation() {
      return this.defaultFactoryClassLocation;
   }

   public void setDefaultFactoryClassLocation(String var1) {
      this.defaultFactoryClassLocation = var1;
   }

   public void addReferenceProperty(String var1) {
      this.referenceProperties.add(var1);
   }

   public void removeReferenceProperty(String var1) {
      this.referenceProperties.remove(var1);
   }

   @Override
   public Reference createReference(Object var1) throws NamingException {
      try {
         BeanInfo var2 = Introspector.getBeanInfo(var1.getClass());
         PropertyDescriptor[] var3 = var2.getPropertyDescriptors();
         ArrayList var4 = new ArrayList();
         String var5 = this.defaultFactoryClassLocation;
         boolean var6 = this.referenceProperties.size() > 0;
         if (var6) {
            var4.add(new BinaryRefAddr("com.mchange.v2.naming.JavaBeanReferenceMaker.REF_PROPS_KEY", SerializableUtils.toByteArray(this.referenceProperties)));
         }

         int var7 = 0;

         for(int var8 = var3.length; var7 < var8; ++var7) {
            PropertyDescriptor var9 = var3[var7];
            String var10 = var9.getName();
            if (!var6 || this.referenceProperties.contains(var10)) {
               Class var11 = var9.getPropertyType();
               Method var12 = var9.getReadMethod();
               Method var13 = var9.getWriteMethod();
               if (var12 != null && var13 != null) {
                  Object var14 = var12.invoke(var1, EMPTY_ARGS);
                  if (var10.equals("factoryClassLocation")) {
                     if (String.class != var11) {
                        throw new NamingException(
                           this.getClass().getName() + " requires a factoryClassLocation property to be a string, " + var11.getName() + " is not valid."
                        );
                     }

                     var5 = (String)var14;
                  }

                  if (var14 == null) {
                     BinaryRefAddr var15 = new BinaryRefAddr(var10, NULL_TOKEN_BYTES);
                     var4.add(var15);
                  } else if (Coerce.canCoerce(var11)) {
                     StringRefAddr var21 = new StringRefAddr(var10, String.valueOf(var14));
                     var4.add(var21);
                  } else {
                     Object var22 = null;
                     PropertyEditor var16 = BeansUtils.findPropertyEditor(var9);
                     if (var16 != null) {
                        var16.setValue(var14);
                        String var17 = var16.getAsText();
                        if (var17 != null) {
                           var22 = new StringRefAddr(var10, var17);
                        }
                     }

                     if (var22 == null) {
                        var22 = new BinaryRefAddr(var10, SerializableUtils.toByteArray(var14, this.indirector, IndirectPolicy.INDIRECT_ON_EXCEPTION));
                     }

                     var4.add(var22);
                  }
               } else if (logger.isLoggable(MLevel.WARNING)) {
                  logger.warning(this.getClass().getName() + ": Skipping " + var10 + " because it is " + (var13 == null ? "read-only." : "write-only."));
               }
            }
         }

         Reference var19 = new Reference(var1.getClass().getName(), this.factoryClassName, var5);
         Iterator var20 = var4.iterator();

         while(var20.hasNext()) {
            var19.add((RefAddr)var20.next());
         }

         return var19;
      } catch (Exception var18) {
         if (logger.isLoggable(MLevel.FINE)) {
            logger.log(MLevel.FINE, "Exception trying to create Reference.", (Throwable)var18);
         }

         throw new NamingException("Could not create reference from bean: " + var18.toString());
      }
   }
}
