package com.mchange.v2.beans.swing;

import com.mchange.v2.beans.BeansUtils;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.IntrospectionException;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyEditor;
import java.util.Collection;
import java.util.Iterator;
import javax.swing.BoxLayout;
import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JFrame;

public class PropertyBoundComboBox extends JComboBox {
   PropertyComponentBindingUtility pcbu;
   PropertyBoundComboBox.MyHbi myHbi;
   Object itemsSrc = null;
   Object nullObject = null;

   public PropertyBoundComboBox(Object var1, String var2, Object var3, Object var4) throws IntrospectionException {
      this.myHbi = new PropertyBoundComboBox.MyHbi();
      this.pcbu = new PropertyComponentBindingUtility(this.myHbi, var1, var2, false);
      this.nullObject = var4;
      this.setItemsSrc(var3);
   }

   public Object getItemsSrc() {
      return this.itemsSrc;
   }

   public void setItemsSrc(Object var1) {
      this.myHbi.suspendNotifications();
      this.removeAllItems();
      if (var1 instanceof Object[]) {
         Object[] var2 = (Object[])var1;
         int var3 = 0;

         for(int var4 = var2.length; var3 < var4; ++var3) {
            this.addItem(var2[var3]);
         }
      } else if (var1 instanceof Collection) {
         Collection var5 = (Collection)var1;
         Iterator var6 = var5.iterator();

         while(var6.hasNext()) {
            this.addItem(var6.next());
         }
      } else {
         if (!(var1 instanceof ComboBoxModel)) {
            throw new IllegalArgumentException("itemsSrc must be an Object[], a Collection, or a ComboBoxModel");
         }

         this.setModel((ComboBoxModel)var1);
      }

      this.itemsSrc = var1;
      this.pcbu.resync();
      this.myHbi.resumeNotifications();
   }

   public void setNullObject(Object var1) {
      this.nullObject = null;
      this.pcbu.resync();
   }

   public Object getNullObject() {
      return this.nullObject;
   }

   public static void main(String[] var0) {
      try {
         TestBean var1 = new TestBean();
         PropertyChangeListener var2 = new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent var1) {
               BeansUtils.debugShowPropertyChange(var1);
            }
         };
         var1.addPropertyChangeListener(var2);
         PropertyBoundComboBox var3 = new PropertyBoundComboBox(var1, "theString", new String[]{"SELECT", "Frog", "Fish", "Puppy"}, "SELECT");
         PropertyBoundTextField var4 = new PropertyBoundTextField(var1, "theInt", 5);
         PropertyBoundTextField var5 = new PropertyBoundTextField(var1, "theFloat", 5);
         JFrame var6 = new JFrame();
         BoxLayout var7 = new BoxLayout(var6.getContentPane(), 1);
         var6.getContentPane().setLayout(var7);
         var6.getContentPane().add(var3);
         var6.getContentPane().add(var4);
         var6.getContentPane().add(var5);
         var6.pack();
         var6.show();
      } catch (Exception var8) {
         var8.printStackTrace();
      }
   }

   class MyHbi implements HostBindingInterface {
      boolean suspend_notice = false;

      public void suspendNotifications() {
         this.suspend_notice = true;
      }

      public void resumeNotifications() {
         this.suspend_notice = false;
      }

      @Override
      public void syncToValue(PropertyEditor var1, Object var2) {
         if (var2 == null) {
            PropertyBoundComboBox.this.setSelectedItem(PropertyBoundComboBox.this.nullObject);
         } else {
            PropertyBoundComboBox.this.setSelectedItem(var2);
         }
      }

      @Override
      public void addUserModificationListeners() {
         ItemListener var1 = new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent var1) {
               if (!MyHbi.this.suspend_notice) {
                  PropertyBoundComboBox.this.pcbu.userModification();
               }
            }
         };
         PropertyBoundComboBox.this.addItemListener(var1);
      }

      @Override
      public Object fetchUserModification(PropertyEditor var1, Object var2) {
         Object var3 = PropertyBoundComboBox.this.getSelectedItem();
         if (PropertyBoundComboBox.this.nullObject != null && PropertyBoundComboBox.this.nullObject.equals(var3)) {
            var3 = null;
         }

         return var3;
      }

      @Override
      public void alertErroneousInput() {
         PropertyBoundComboBox.this.getToolkit().beep();
      }
   }
}
