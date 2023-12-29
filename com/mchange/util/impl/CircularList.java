package com.mchange.util.impl;

import java.util.Enumeration;

public class CircularList implements Cloneable {
   CircularListRecord firstRecord = null;
   int size = 0;

   private void addElement(Object var1, boolean var2) {
      if (this.firstRecord == null) {
         this.firstRecord = new CircularListRecord(var1);
      } else {
         CircularListRecord var3 = new CircularListRecord(var1, this.firstRecord.prev, this.firstRecord);
         this.firstRecord.prev.next = var3;
         this.firstRecord.prev = var3;
         if (var2) {
            this.firstRecord = var3;
         }
      }

      ++this.size;
   }

   private void removeElement(boolean var1) {
      if (this.size == 1) {
         this.firstRecord = null;
      } else {
         if (var1) {
            this.firstRecord = this.firstRecord.next;
         }

         this.zap(this.firstRecord.prev);
      }

      --this.size;
   }

   private void zap(CircularListRecord var1) {
      var1.next.prev = var1.prev;
      var1.prev.next = var1.next;
   }

   public void appendElement(Object var1) {
      this.addElement(var1, false);
   }

   public void addElementToFront(Object var1) {
      this.addElement(var1, true);
   }

   public void removeFirstElement() {
      this.removeElement(true);
   }

   public void removeLastElement() {
      this.removeElement(false);
   }

   public void removeFromFront(int var1) {
      if (var1 > this.size) {
         throw new IndexOutOfBoundsException(var1 + ">" + this.size);
      } else {
         for(int var2 = 0; var2 < var1; ++var2) {
            this.removeElement(true);
         }
      }
   }

   public void removeFromBack(int var1) {
      if (var1 > this.size) {
         throw new IndexOutOfBoundsException(var1 + ">" + this.size);
      } else {
         for(int var2 = 0; var2 < var1; ++var2) {
            this.removeElement(false);
         }
      }
   }

   public void removeAllElements() {
      this.size = 0;
      this.firstRecord = null;
   }

   public Object getElementFromFront(int var1) {
      if (var1 >= this.size) {
         throw new IndexOutOfBoundsException(var1 + ">=" + this.size);
      } else {
         CircularListRecord var2 = this.firstRecord;

         for(int var3 = 0; var3 < var1; ++var3) {
            var2 = var2.next;
         }

         return var2.object;
      }
   }

   public Object getElementFromBack(int var1) {
      if (var1 >= this.size) {
         throw new IndexOutOfBoundsException(var1 + ">=" + this.size);
      } else {
         CircularListRecord var2 = this.firstRecord.prev;

         for(int var3 = 0; var3 < var1; ++var3) {
            var2 = var2.prev;
         }

         return var2.object;
      }
   }

   public Object getFirstElement() {
      try {
         return this.firstRecord.object;
      } catch (NullPointerException var2) {
         throw new IndexOutOfBoundsException("CircularList is empty.");
      }
   }

   public Object getLastElement() {
      try {
         return this.firstRecord.prev.object;
      } catch (NullPointerException var2) {
         throw new IndexOutOfBoundsException("CircularList is empty.");
      }
   }

   public Enumeration elements(boolean var1, boolean var2) {
      return new CircularListEnumeration(this, var1, var2);
   }

   public Enumeration elements(boolean var1) {
      return this.elements(var1, true);
   }

   public Enumeration elements() {
      return this.elements(true, true);
   }

   public int size() {
      return this.size;
   }

   @Override
   public Object clone() {
      CircularList var1 = new CircularList();
      int var2 = this.size();

      for(int var3 = 0; var3 < var2; ++var3) {
         var1.appendElement(this.getElementFromFront(var3));
      }

      return var1;
   }

   public static void main(String[] var0) {
      CircularList var1 = new CircularList();
      var1.appendElement("Hello");
      var1.appendElement("There");
      var1.appendElement("Joe.");
      Enumeration var2 = var1.elements();

      while(var2.hasMoreElements()) {
         System.out.println("x " + var2.nextElement());
      }
   }
}
