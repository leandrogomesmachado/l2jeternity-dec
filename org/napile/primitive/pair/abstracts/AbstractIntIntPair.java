package org.napile.primitive.pair.abstracts;

import org.napile.HashUtils;
import org.napile.primitive.pair.IntIntPair;

public abstract class AbstractIntIntPair implements IntIntPair {
   protected int _key;
   protected int _value;

   public AbstractIntIntPair(int key, int value) {
      this._key = key;
      this._value = value;
   }

   @Override
   public int getKey() {
      return this._key;
   }

   @Override
   public int getValue() {
      return this._value;
   }

   @Override
   public String toString() {
      return this._key + "=" + this._value;
   }

   @Override
   public int hashCode() {
      return HashUtils.hashCode(this._key) ^ HashUtils.hashCode(this._value);
   }

   @Override
   public boolean equals(Object o) {
      if (!(o instanceof IntIntPair)) {
         return false;
      } else {
         IntIntPair p = (IntIntPair)o;
         return p.getKey() == this._key && p.getValue() == this._value;
      }
   }
}
