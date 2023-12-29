package com.mchange.v2.beans.swing;

import com.mchange.v2.beans.BeansUtils;
import java.beans.BeanInfo;
import java.beans.EventSetDescriptor;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyDescriptor;
import java.beans.PropertyEditor;
import java.beans.PropertyVetoException;
import java.lang.reflect.Method;
import javax.swing.SwingUtilities;

class PropertyComponentBindingUtility {
   static final Object[] EMPTY_ARGS = new Object[0];
   HostBindingInterface hbi;
   Object bean;
   PropertyDescriptor pd = null;
   EventSetDescriptor propChangeEsd = null;
   Method addMethod = null;
   Method removeMethod = null;
   Method propGetter = null;
   Method propSetter = null;
   PropertyEditor propEditor = null;
   Object nullReplacement = null;

   PropertyComponentBindingUtility(final HostBindingInterface var1, Object var2, final String var3, boolean var4) throws IntrospectionException {
      this.hbi = var1;
      this.bean = var2;
      BeanInfo var5 = Introspector.getBeanInfo(var2.getClass());
      PropertyDescriptor[] var6 = var5.getPropertyDescriptors();
      int var7 = 0;

      for(int var8 = var6.length; var7 < var8; ++var7) {
         PropertyDescriptor var9 = var6[var7];
         if (var3.equals(var9.getName())) {
            this.pd = var9;
            break;
         }
      }

      if (this.pd == null) {
         throw new IntrospectionException("Cannot find property on bean Object with name '" + var3 + "'.");
      } else {
         EventSetDescriptor[] var12 = var5.getEventSetDescriptors();
         int var13 = 0;

         for(int var15 = var12.length; var13 < var15; ++var13) {
            EventSetDescriptor var10 = var12[var13];
            if ("propertyChange".equals(var10.getName())) {
               this.propChangeEsd = var10;
               break;
            }
         }

         if (this.propChangeEsd == null) {
            throw new IntrospectionException("Cannot find PropertyChangeEvent on bean Object with name '" + var3 + "'.");
         } else {
            this.propEditor = BeansUtils.findPropertyEditor(this.pd);
            if (var4 && this.propEditor == null) {
               throw new IntrospectionException("Could not find an appropriate PropertyEditor for property: " + var3);
            } else {
               this.propGetter = this.pd.getReadMethod();
               this.propSetter = this.pd.getWriteMethod();
               if (this.propGetter != null && this.propSetter != null) {
                  Class var14 = this.pd.getPropertyType();
                  if (var14.isPrimitive()) {
                     if (var14 == Boolean.TYPE) {
                        this.nullReplacement = Boolean.FALSE;
                     }

                     if (var14 == Byte.TYPE) {
                        this.nullReplacement = new Byte((byte)0);
                     } else if (var14 == Character.TYPE) {
                        this.nullReplacement = new Character('\u0000');
                     } else if (var14 == Short.TYPE) {
                        this.nullReplacement = new Short((short)0);
                     } else if (var14 == Integer.TYPE) {
                        this.nullReplacement = new Integer(0);
                     } else if (var14 == Long.TYPE) {
                        this.nullReplacement = new Long(0L);
                     } else if (var14 == Float.TYPE) {
                        this.nullReplacement = new Float(0.0F);
                     } else {
                        if (var14 != Double.TYPE) {
                           throw new InternalError("What kind of primitive is " + var14.getName() + "???");
                        }

                        this.nullReplacement = new Double(0.0);
                     }
                  }

                  this.addMethod = this.propChangeEsd.getAddListenerMethod();
                  this.removeMethod = this.propChangeEsd.getAddListenerMethod();
                  PropertyChangeListener var16 = new PropertyChangeListener() {
                     @Override
                     public void propertyChange(PropertyChangeEvent var1x) {
                        String var2 = var1x.getPropertyName();
                        if (var2.equals(var3)) {
                           var1.syncToValue(PropertyComponentBindingUtility.this.propEditor, var1x.getNewValue());
                        }
                     }
                  };

                  try {
                     this.addMethod.invoke(var2, var16);
                  } catch (Exception var11) {
                     var11.printStackTrace();
                     throw new IntrospectionException("The introspected PropertyChangeEvent adding method failed with an Exception.");
                  }

                  var1.addUserModificationListeners();
               } else {
                  throw new IntrospectionException("The specified property '" + var3 + "' must be both readdable and writable, but it is not!");
               }
            }
         }
      }
   }

   public void userModification() {
      Object var1 = null;

      try {
         var1 = this.propGetter.invoke(this.bean, EMPTY_ARGS);
      } catch (Exception var3) {
         var3.printStackTrace();
      }

      try {
         Object var2 = this.hbi.fetchUserModification(this.propEditor, var1);
         if (var2 == null) {
            var2 = this.nullReplacement;
         }

         this.propSetter.invoke(this.bean, var2);
      } catch (Exception var4) {
         if (!(var4 instanceof PropertyVetoException)) {
            var4.printStackTrace();
         }

         this.syncComponentToValue(true);
      }
   }

   public void resync() {
      this.syncComponentToValue(false);
   }

   private void syncComponentToValue(final boolean var1) {
      try {
         final Object var2 = this.propGetter.invoke(this.bean, EMPTY_ARGS);
         Runnable var3 = new Runnable() {
            @Override
            public void run() {
               if (var1) {
                  PropertyComponentBindingUtility.this.hbi.alertErroneousInput();
               }

               PropertyComponentBindingUtility.this.hbi.syncToValue(PropertyComponentBindingUtility.this.propEditor, var2);
            }
         };
         SwingUtilities.invokeLater(var3);
      } catch (Exception var4) {
         var4.printStackTrace();
      }
   }
}
