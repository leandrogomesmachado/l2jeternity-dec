package com.mchange.v2.beans.swing;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.IntrospectionException;
import java.beans.PropertyEditor;
import java.util.HashMap;
import java.util.Map;
import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.JButton;

class PropertyBoundButtonGroup extends ButtonGroup {
   PropertyComponentBindingUtility pcbu;
   HostBindingInterface myHbi;
   PropertyBoundButtonGroup.WeChangedListener wcl = new PropertyBoundButtonGroup.WeChangedListener();
   Map buttonsModelsToValues = new HashMap();
   Map valuesToButtonModels = new HashMap();
   JButton fakeButton = new JButton();

   public PropertyBoundButtonGroup(Object var1, String var2) throws IntrospectionException {
      this.myHbi = new PropertyBoundButtonGroup.MyHbi();
      this.pcbu = new PropertyComponentBindingUtility(this.myHbi, var1, var2, false);
      this.add(this.fakeButton, null);
      this.pcbu.resync();
   }

   public void add(AbstractButton var1, Object var2) {
      super.add(var1);
      this.buttonsModelsToValues.put(var1.getModel(), var2);
      this.valuesToButtonModels.put(var2, var1.getModel());
      var1.addActionListener(this.wcl);
      this.pcbu.resync();
   }

   @Override
   public void add(AbstractButton var1) {
      System.err.println(this + "Warning: The button '" + var1 + "' has been implicitly associated with a null value!");
      System.err.println("To avoid this warning, please use public void add(AbstractButton button, Object associatedValue)");
      System.err.println("instead of the single-argument add method.");
      super.add(var1);
      var1.addActionListener(this.wcl);
      this.pcbu.resync();
   }

   @Override
   public void remove(AbstractButton var1) {
      var1.removeActionListener(this.wcl);
      super.remove(var1);
   }

   class MyHbi implements HostBindingInterface {
      @Override
      public void syncToValue(PropertyEditor var1, Object var2) {
         ButtonModel var3 = (ButtonModel)PropertyBoundButtonGroup.this.valuesToButtonModels.get(var2);
         if (var3 != null) {
            PropertyBoundButtonGroup.this.setSelected(var3, true);
         } else {
            PropertyBoundButtonGroup.this.setSelected(PropertyBoundButtonGroup.this.fakeButton.getModel(), true);
         }
      }

      @Override
      public void addUserModificationListeners() {
      }

      @Override
      public Object fetchUserModification(PropertyEditor var1, Object var2) {
         ButtonModel var3 = PropertyBoundButtonGroup.this.getSelection();
         return PropertyBoundButtonGroup.this.buttonsModelsToValues.get(var3);
      }

      @Override
      public void alertErroneousInput() {
         Toolkit.getDefaultToolkit().beep();
      }
   }

   class WeChangedListener implements ActionListener {
      @Override
      public void actionPerformed(ActionEvent var1) {
         PropertyBoundButtonGroup.this.pcbu.userModification();
      }
   }
}
