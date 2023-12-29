package com.mchange.v2.beans.swing;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import java.beans.IntrospectionException;
import java.beans.PropertyEditor;
import java.util.HashSet;
import java.util.Set;
import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.event.ChangeListener;

class SetPropertyElementBoundButtonModel implements ButtonModel {
   Object putativeElement;
   ButtonModel inner;
   PropertyComponentBindingUtility pcbu;

   public static void bind(AbstractButton[] var0, Object[] var1, Object var2, String var3) throws IntrospectionException {
      int var4 = 0;

      for(int var5 = var0.length; var4 < var5; ++var4) {
         AbstractButton var6 = var0[var4];
         var6.setModel(new SetPropertyElementBoundButtonModel(var6.getModel(), var2, var3, var1[var4]));
      }
   }

   public SetPropertyElementBoundButtonModel(ButtonModel var1, Object var2, String var3, Object var4) throws IntrospectionException {
      this.inner = var1;
      this.putativeElement = var4;
      this.pcbu = new PropertyComponentBindingUtility(new SetPropertyElementBoundButtonModel.MyHbi(), var2, var3, false);
      this.pcbu.resync();
   }

   @Override
   public boolean isArmed() {
      return this.inner.isArmed();
   }

   @Override
   public boolean isSelected() {
      return this.inner.isSelected();
   }

   @Override
   public boolean isEnabled() {
      return this.inner.isEnabled();
   }

   @Override
   public boolean isPressed() {
      return this.inner.isPressed();
   }

   @Override
   public boolean isRollover() {
      return this.inner.isRollover();
   }

   @Override
   public void setArmed(boolean var1) {
      this.inner.setArmed(var1);
   }

   @Override
   public void setSelected(boolean var1) {
      this.inner.setSelected(var1);
   }

   @Override
   public void setEnabled(boolean var1) {
      this.inner.setEnabled(var1);
   }

   @Override
   public void setPressed(boolean var1) {
      this.inner.setPressed(var1);
   }

   @Override
   public void setRollover(boolean var1) {
      this.inner.setRollover(var1);
   }

   @Override
   public void setMnemonic(int var1) {
      this.inner.setMnemonic(var1);
   }

   @Override
   public int getMnemonic() {
      return this.inner.getMnemonic();
   }

   @Override
   public void setActionCommand(String var1) {
      this.inner.setActionCommand(var1);
   }

   @Override
   public String getActionCommand() {
      return this.inner.getActionCommand();
   }

   @Override
   public void setGroup(ButtonGroup var1) {
      this.inner.setGroup(var1);
   }

   @Override
   public Object[] getSelectedObjects() {
      return this.inner.getSelectedObjects();
   }

   @Override
   public void addActionListener(ActionListener var1) {
      this.inner.addActionListener(var1);
   }

   @Override
   public void removeActionListener(ActionListener var1) {
      this.inner.removeActionListener(var1);
   }

   @Override
   public void addItemListener(ItemListener var1) {
      this.inner.addItemListener(var1);
   }

   @Override
   public void removeItemListener(ItemListener var1) {
      this.inner.removeItemListener(var1);
   }

   @Override
   public void addChangeListener(ChangeListener var1) {
      this.inner.addChangeListener(var1);
   }

   @Override
   public void removeChangeListener(ChangeListener var1) {
      this.inner.removeChangeListener(var1);
   }

   class MyHbi implements HostBindingInterface {
      @Override
      public void syncToValue(PropertyEditor var1, Object var2) {
         if (var2 == null) {
            SetPropertyElementBoundButtonModel.this.setSelected(false);
         } else {
            SetPropertyElementBoundButtonModel.this.setSelected(((Set)var2).contains(SetPropertyElementBoundButtonModel.this.putativeElement));
         }
      }

      @Override
      public void addUserModificationListeners() {
         ActionListener var1 = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent var1) {
               SetPropertyElementBoundButtonModel.this.pcbu.userModification();
            }
         };
         SetPropertyElementBoundButtonModel.this.addActionListener(var1);
      }

      @Override
      public Object fetchUserModification(PropertyEditor var1, Object var2) {
         HashSet var3;
         if (var2 == null) {
            if (!SetPropertyElementBoundButtonModel.this.isSelected()) {
               return null;
            }

            var3 = new HashSet();
         } else {
            var3 = new HashSet((Set)var2);
         }

         if (SetPropertyElementBoundButtonModel.this.isSelected()) {
            var3.add(SetPropertyElementBoundButtonModel.this.putativeElement);
         } else {
            var3.remove(SetPropertyElementBoundButtonModel.this.putativeElement);
         }

         return var3;
      }

      @Override
      public void alertErroneousInput() {
         Toolkit.getDefaultToolkit().beep();
      }
   }
}
