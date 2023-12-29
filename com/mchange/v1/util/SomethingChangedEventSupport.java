package com.mchange.v1.util;

import java.util.Enumeration;
import java.util.Vector;

public class SomethingChangedEventSupport {
   Object source;
   Vector listeners = new Vector();

   public SomethingChangedEventSupport(Object var1) {
      this.source = var1;
   }

   public synchronized void addSomethingChangedListener(SomethingChangedListener var1) {
      if (!this.listeners.contains(var1)) {
         this.listeners.addElement(var1);
      }
   }

   public synchronized void removeSomethingChangedListener(SomethingChangedListener var1) {
      this.listeners.removeElement(var1);
   }

   public synchronized void fireSomethingChanged() {
      SomethingChangedEvent var1 = new SomethingChangedEvent(this.source);
      Enumeration var2 = this.listeners.elements();

      while(var2.hasMoreElements()) {
         SomethingChangedListener var3 = (SomethingChangedListener)var2.nextElement();
         var3.somethingChanged(var1);
      }
   }
}
