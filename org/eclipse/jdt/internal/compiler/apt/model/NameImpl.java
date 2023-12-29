package org.eclipse.jdt.internal.compiler.apt.model;

import javax.lang.model.element.Name;

public class NameImpl implements Name {
   private final String _name;

   private NameImpl() {
      this._name = null;
   }

   public NameImpl(CharSequence cs) {
      this._name = cs.toString();
   }

   public NameImpl(char[] chars) {
      this._name = String.valueOf(chars);
   }

   @Override
   public boolean contentEquals(CharSequence cs) {
      return this._name.equals(cs.toString());
   }

   @Override
   public char charAt(int index) {
      return this._name.charAt(index);
   }

   @Override
   public int length() {
      return this._name.length();
   }

   @Override
   public CharSequence subSequence(int start, int end) {
      return this._name.subSequence(start, end);
   }

   @Override
   public String toString() {
      return this._name;
   }

   @Override
   public int hashCode() {
      return this._name.hashCode();
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else if (obj == null) {
         return false;
      } else if (this.getClass() != obj.getClass()) {
         return false;
      } else {
         NameImpl other = (NameImpl)obj;
         return this._name.equals(other._name);
      }
   }
}
