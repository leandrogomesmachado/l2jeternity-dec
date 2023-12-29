package com.mchange.v2.beans.swing;

import com.mchange.v2.beans.BeansUtils;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.beans.IntrospectionException;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyEditor;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JTextField;

public class PropertyBoundTextField extends JTextField {
   PropertyComponentBindingUtility pcbu;
   HostBindingInterface myHbi = new PropertyBoundTextField.MyHbi();

   public PropertyBoundTextField(Object var1, String var2, int var3) throws IntrospectionException {
      super(var3);
      this.pcbu = new PropertyComponentBindingUtility(this.myHbi, var1, var2, true);
      this.pcbu.resync();
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
         PropertyBoundTextField var3 = new PropertyBoundTextField(var1, "theString", 20);
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
      @Override
      public void syncToValue(PropertyEditor var1, Object var2) {
         if (var2 == null) {
            PropertyBoundTextField.this.setText("");
         } else {
            var1.setValue(var2);
            String var3 = var1.getAsText();
            PropertyBoundTextField.this.setText(var3);
         }
      }

      @Override
      public void addUserModificationListeners() {
         PropertyBoundTextField.WeChangedListener var1 = PropertyBoundTextField.this.new WeChangedListener();
         PropertyBoundTextField.this.addActionListener(var1);
         PropertyBoundTextField.this.addFocusListener(var1);
      }

      @Override
      public Object fetchUserModification(PropertyEditor var1, Object var2) {
         String var3 = PropertyBoundTextField.this.getText().trim();
         if ("".equals(var3)) {
            return null;
         } else {
            var1.setAsText(var3);
            return var1.getValue();
         }
      }

      @Override
      public void alertErroneousInput() {
         PropertyBoundTextField.this.getToolkit().beep();
      }
   }

   class WeChangedListener implements ActionListener, FocusListener {
      @Override
      public void actionPerformed(ActionEvent var1) {
         PropertyBoundTextField.this.pcbu.userModification();
      }

      @Override
      public void focusGained(FocusEvent var1) {
      }

      @Override
      public void focusLost(FocusEvent var1) {
         PropertyBoundTextField.this.pcbu.userModification();
      }
   }
}
