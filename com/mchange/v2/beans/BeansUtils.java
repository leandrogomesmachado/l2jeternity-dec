package com.mchange.v2.beans;

import com.mchange.v2.lang.Coerce;
import com.mchange.v2.log.MLevel;
import com.mchange.v2.log.MLog;
import com.mchange.v2.log.MLogger;
import java.beans.BeanInfo;
import java.beans.IndexedPropertyDescriptor;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyDescriptor;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public final class BeansUtils {
   static final MLogger logger = MLog.getLogger(BeansUtils.class);
   static final Object[] EMPTY_ARGS = new Object[0];

   public static PropertyEditor findPropertyEditor(PropertyDescriptor var0) {
      PropertyEditor var1 = null;
      Class var2 = null;

      try {
         var2 = var0.getPropertyEditorClass();
         if (var2 != null) {
            var1 = (PropertyEditor)var2.newInstance();
         }
      } catch (Exception var4) {
         if (logger.isLoggable(MLevel.WARNING)) {
            logger.log(MLevel.WARNING, "Bad property editor class " + var2.getName() + " registered for property " + var0.getName(), (Throwable)var4);
         }
      }

      if (var1 == null) {
         var1 = PropertyEditorManager.findEditor(var0.getPropertyType());
      }

      return var1;
   }

   public static boolean equalsByAccessibleProperties(Object var0, Object var1) throws IntrospectionException {
      return equalsByAccessibleProperties(var0, var1, Collections.EMPTY_SET);
   }

   public static boolean equalsByAccessibleProperties(Object var0, Object var1, Collection var2) throws IntrospectionException {
      HashMap var3 = new HashMap();
      HashMap var4 = new HashMap();
      extractAccessiblePropertiesToMap(var3, var0, var2);
      extractAccessiblePropertiesToMap(var4, var1, var2);
      return var3.equals(var4);
   }

   public static boolean equalsByAccessiblePropertiesVerbose(Object var0, Object var1, Collection var2) throws IntrospectionException {
      HashMap var3 = new HashMap();
      HashMap var4 = new HashMap();
      extractAccessiblePropertiesToMap(var3, var0, var2);
      extractAccessiblePropertiesToMap(var4, var1, var2);
      boolean var5 = true;
      if (var3.size() != var4.size()) {
         System.err.println("Unequal sizes --> Map0: " + var3.size() + "; m1: " + var4.size());
         Set var6 = var3.keySet();
         var6.removeAll(var4.keySet());
         Set var7 = var4.keySet();
         var7.removeAll(var3.keySet());
         if (var6.size() > 0) {
            System.err.println("Map0 extras:");
            Iterator var8 = var6.iterator();

            while(var8.hasNext()) {
               System.err.println('\t' + var8.next().toString());
            }
         }

         if (var7.size() > 0) {
            System.err.println("Map1 extras:");
            Iterator var12 = var7.iterator();

            while(var12.hasNext()) {
               System.err.println('\t' + var12.next().toString());
            }
         }

         var5 = false;
      }

      for(String var11 : var3.keySet()) {
         Object var13 = var3.get(var11);
         Object var9 = var4.get(var11);
         if (var13 == null && var9 != null || var13 != null && !var13.equals(var9)) {
            System.err.println('\t' + var11 + ": " + var13 + " != " + var9);
            var5 = false;
         }
      }

      return var5;
   }

   public static void overwriteAccessibleProperties(Object var0, Object var1) throws IntrospectionException {
      overwriteAccessibleProperties(var0, var1, Collections.EMPTY_SET);
   }

   public static void overwriteAccessibleProperties(Object var0, Object var1, Collection var2) throws IntrospectionException {
      try {
         BeanInfo var3 = Introspector.getBeanInfo(var0.getClass(), Object.class);
         PropertyDescriptor[] var4 = var3.getPropertyDescriptors();
         int var5 = 0;

         for(int var6 = var4.length; var5 < var6; ++var5) {
            PropertyDescriptor var7 = var4[var5];
            if (!var2.contains(var7.getName())) {
               Method var8 = var7.getReadMethod();
               Method var9 = var7.getWriteMethod();
               if (var8 != null && var9 != null) {
                  Object var10 = var8.invoke(var0, EMPTY_ARGS);
                  var9.invoke(var1, var10);
               } else {
                  if (var7 instanceof IndexedPropertyDescriptor && logger.isLoggable(MLevel.WARNING)) {
                     logger.warning(
                        "BeansUtils.overwriteAccessibleProperties() does not support indexed properties that do not provide single-valued array getters and setters! [The indexed methods provide no means of modifying the size of the array in the destination bean if it does not match the source.]"
                     );
                  }

                  if (logger.isLoggable(MLevel.INFO)) {
                     logger.info("Property inaccessible for overwriting: " + var7.getName());
                  }
               }
            }
         }
      } catch (IntrospectionException var11) {
         throw var11;
      } catch (Exception var12) {
         if (logger.isLoggable(MLevel.FINE)) {
            logger.log(MLevel.FINE, "Converting exception to throwable IntrospectionException");
         }

         throw new IntrospectionException(var12.getMessage());
      }
   }

   public static void overwriteAccessiblePropertiesFromMap(Map var0, Object var1, boolean var2) throws IntrospectionException {
      overwriteAccessiblePropertiesFromMap(var0, var1, var2, Collections.EMPTY_SET);
   }

   public static void overwriteAccessiblePropertiesFromMap(Map var0, Object var1, boolean var2, Collection var3) throws IntrospectionException {
      overwriteAccessiblePropertiesFromMap(var0, var1, var2, var3, false, MLevel.WARNING, MLevel.WARNING, true);
   }

   public static void overwriteAccessiblePropertiesFromMap(
      Map var0, Object var1, boolean var2, Collection var3, boolean var4, MLevel var5, MLevel var6, boolean var7
   ) throws IntrospectionException {
      if (var5 == null) {
         var5 = MLevel.WARNING;
      }

      if (var6 == null) {
         var6 = MLevel.WARNING;
      }

      Set var8 = var0.keySet();
      Object var9 = null;
      BeanInfo var10 = Introspector.getBeanInfo(var1.getClass(), Object.class);
      PropertyDescriptor[] var11 = var10.getPropertyDescriptors();
      int var12 = 0;

      for(int var13 = var11.length; var12 < var13; ++var12) {
         PropertyDescriptor var14 = var11[var12];
         String var25 = var14.getName();
         if (var8.contains(var25) && (var3 == null || !var3.contains(var25))) {
            Object var15 = var0.get(var25);
            if (var15 != null || !var2) {
               Method var16 = var14.getWriteMethod();
               boolean var17 = false;
               Class var18 = var14.getPropertyType();
               if (var16 == null) {
                  if (var14 instanceof IndexedPropertyDescriptor && logger.isLoggable(MLevel.FINER)) {
                     logger.finer(
                        "BeansUtils.overwriteAccessiblePropertiesFromMap() does not support indexed properties that do not provide single-valued array getters and setters! [The indexed methods provide no means of modifying the size of the array in the destination bean if it does not match the source.]"
                     );
                  }

                  if (logger.isLoggable(var5)) {
                     String var30 = "Property inaccessible for overwriting: " + var25;
                     logger.log(var5, var30);
                     if (var7) {
                        var17 = true;
                        throw new IntrospectionException(var30);
                     }
                  }
               } else if (var4
                  && var15 != null
                  && var15.getClass() == String.class
                  && (var18 = var14.getPropertyType()) != String.class
                  && Coerce.canCoerce(var18)) {
                  try {
                     Object var19 = Coerce.toObject((String)var15, var18);
                     var16.invoke(var1, var19);
                  } catch (IllegalArgumentException var22) {
                     String var31 = "Failed to coerce property: " + var25 + " [propVal: " + var15 + "; propType: " + var18 + "]";
                     if (logger.isLoggable(var6)) {
                        logger.log(var6, var31, (Throwable)var22);
                     }

                     if (var7) {
                        var17 = true;
                        throw new IntrospectionException(var31);
                     }
                  } catch (Exception var23) {
                     String var21 = "Failed to set property: " + var25 + " [propVal: " + var15 + "; propType: " + var18 + "]";
                     if (logger.isLoggable(var5)) {
                        logger.log(var5, var21, (Throwable)var23);
                     }

                     if (var7) {
                        var17 = true;
                        throw new IntrospectionException(var21);
                     }
                  }
               } else {
                  try {
                     var16.invoke(var1, var15);
                  } catch (Exception var24) {
                     String var20 = "Failed to set property: " + var25 + " [propVal: " + var15 + "; propType: " + var18 + "]";
                     if (logger.isLoggable(var5)) {
                        logger.log(var5, var20, (Throwable)var24);
                     }

                     if (var7) {
                        var17 = true;
                        throw new IntrospectionException(var20);
                     }
                  }
               }
            }
         }
      }
   }

   public static void appendPropNamesAndValues(StringBuffer var0, Object var1, Collection var2) throws IntrospectionException {
      TreeMap var3 = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
      extractAccessiblePropertiesToMap(var3, var1, var2);
      boolean var4 = true;

      for(String var6 : var3.keySet()) {
         Object var7 = var3.get(var6);
         if (var4) {
            var4 = false;
         } else {
            var0.append(", ");
         }

         var0.append(var6);
         var0.append(" -> ");
         var0.append(var7);
      }
   }

   public static void extractAccessiblePropertiesToMap(Map var0, Object var1) throws IntrospectionException {
      extractAccessiblePropertiesToMap(var0, var1, Collections.EMPTY_SET);
   }

   public static void extractAccessiblePropertiesToMap(Map var0, Object var1, Collection var2) throws IntrospectionException {
      String var3 = null;

      try {
         BeanInfo var4 = Introspector.getBeanInfo(var1.getClass(), Object.class);
         PropertyDescriptor[] var5 = var4.getPropertyDescriptors();
         int var6 = 0;

         for(int var7 = var5.length; var6 < var7; ++var6) {
            PropertyDescriptor var8 = var5[var6];
            var3 = var8.getName();
            if (!var2.contains(var3)) {
               Method var9 = var8.getReadMethod();
               Object var10 = var9.invoke(var1, EMPTY_ARGS);
               var0.put(var3, var10);
            }
         }
      } catch (IntrospectionException var11) {
         if (logger.isLoggable(MLevel.WARNING)) {
            logger.warning("Problem occurred while overwriting property: " + var3);
         }

         if (logger.isLoggable(MLevel.FINE)) {
            logger.logp(
               MLevel.FINE,
               BeansUtils.class.getName(),
               "extractAccessiblePropertiesToMap( Map fillMe, Object bean, Collection ignoreProps )",
               (var3 != null ? "Problem occurred while overwriting property: " + var3 : "") + " throwing...",
               (Throwable)var11
            );
         }

         throw var11;
      } catch (Exception var12) {
         if (logger.isLoggable(MLevel.FINE)) {
            logger.logp(
               MLevel.FINE,
               BeansUtils.class.getName(),
               "extractAccessiblePropertiesToMap( Map fillMe, Object bean, Collection ignoreProps )",
               "Caught unexpected Exception; Converting to IntrospectionException.",
               (Throwable)var12
            );
         }

         throw new IntrospectionException(var12.toString() + (var3 == null ? "" : " [" + var3 + ']'));
      }
   }

   private static void overwriteProperty(String var0, Object var1, Method var2, Object var3) throws Exception {
      if (var2.getDeclaringClass().isAssignableFrom(var3.getClass())) {
         var2.invoke(var3, var1);
      } else {
         BeanInfo var4 = Introspector.getBeanInfo(var3.getClass(), Object.class);
         PropertyDescriptor var5 = null;
         PropertyDescriptor[] var6 = var4.getPropertyDescriptors();
         int var7 = 0;

         for(int var8 = var6.length; var7 < var8; ++var7) {
            if (var0.equals(var6[var7].getName())) {
               var5 = var6[var7];
               break;
            }
         }

         Method var9 = var5.getWriteMethod();
         var9.invoke(var3, var1);
      }
   }

   public static void overwriteSpecificAccessibleProperties(Object var0, Object var1, Collection var2) throws IntrospectionException {
      try {
         HashSet var3 = new HashSet(var2);
         BeanInfo var4 = Introspector.getBeanInfo(var0.getClass(), Object.class);
         PropertyDescriptor[] var5 = var4.getPropertyDescriptors();
         int var6 = 0;

         for(int var7 = var5.length; var6 < var7; ++var6) {
            PropertyDescriptor var8 = var5[var6];
            String var9 = var8.getName();
            if (var3.remove(var9)) {
               Method var10 = var8.getReadMethod();
               Method var11 = var8.getWriteMethod();
               if (var10 != null && var11 != null) {
                  Object var12 = var10.invoke(var0, EMPTY_ARGS);
                  overwriteProperty(var9, var12, var11, var1);
               } else {
                  if (var8 instanceof IndexedPropertyDescriptor && logger.isLoggable(MLevel.WARNING)) {
                     logger.warning(
                        "BeansUtils.overwriteAccessibleProperties() does not support indexed properties that do not provide single-valued array getters and setters! [The indexed methods provide no means of modifying the size of the array in the destination bean if it does not match the source.]"
                     );
                  }

                  if (logger.isLoggable(MLevel.INFO)) {
                     logger.info("Property inaccessible for overwriting: " + var8.getName());
                  }
               }
            }
         }

         if (logger.isLoggable(MLevel.WARNING)) {
            Iterator var15 = var3.iterator();

            while(var15.hasNext()) {
               logger.warning("failed to find expected property: " + var15.next());
            }
         }
      } catch (IntrospectionException var13) {
         throw var13;
      } catch (Exception var14) {
         if (logger.isLoggable(MLevel.FINE)) {
            logger.logp(
               MLevel.FINE,
               BeansUtils.class.getName(),
               "overwriteSpecificAccessibleProperties( Object sourceBean, Object destBean, Collection props )",
               "Caught unexpected Exception; Converting to IntrospectionException.",
               (Throwable)var14
            );
         }

         throw new IntrospectionException(var14.getMessage());
      }
   }

   public static void debugShowPropertyChange(PropertyChangeEvent var0) {
      System.err
         .println(
            "PropertyChangeEvent: [ propertyName -> "
               + var0.getPropertyName()
               + ", oldValue -> "
               + var0.getOldValue()
               + ", newValue -> "
               + var0.getNewValue()
               + " ]"
         );
   }

   private BeansUtils() {
   }
}
