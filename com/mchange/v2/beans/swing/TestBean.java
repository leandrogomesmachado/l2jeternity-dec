package com.mchange.v2.beans.swing;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class TestBean {
   String s;
   int i;
   float f;
   PropertyChangeSupport pcs = new PropertyChangeSupport(this);

   public String getTheString() {
      return this.s;
   }

   public int getTheInt() {
      return this.i;
   }

   public float getTheFloat() {
      return this.f;
   }

   public void setTheString(String var1) {
      if (!this.eqOrBothNull(var1, this.s)) {
         String var2 = this.s;
         this.s = var1;
         this.pcs.firePropertyChange("theString", var2, this.s);
      }
   }

   public void setTheInt(int var1) {
      if (var1 != this.i) {
         int var2 = this.i;
         this.i = var1;
         this.pcs.firePropertyChange("theInt", var2, this.i);
      }
   }

   public void setTheFloat(float var1) {
      if (var1 != this.f) {
         float var2 = this.f;
         this.f = var1;
         this.pcs.firePropertyChange("theFloat", new Float(var2), new Float(this.f));
      }
   }

   public void addPropertyChangeListener(PropertyChangeListener var1) {
      this.pcs.addPropertyChangeListener(var1);
   }

   public void removePropertyChangeListener(PropertyChangeListener var1) {
      this.pcs.removePropertyChangeListener(var1);
   }

   private boolean eqOrBothNull(Object var1, Object var2) {
      return var1 == var2 || var1 != null && var1.equals(var2);
   }
}
