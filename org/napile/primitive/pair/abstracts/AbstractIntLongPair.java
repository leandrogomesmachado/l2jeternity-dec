package org.napile.primitive.pair.abstracts;

import org.napile.HashUtils;
import org.napile.primitive.pair.IntLongPair;

public abstract class AbstractIntLongPair implements IntLongPair {
   protected int _key;
   protected long _value;

   public AbstractIntLongPair(int key, long value) {
      this._key = key;
      this._value = value;
   }

   @Override
   public int getKey() {
      return this._key;
   }

   @Override
   public long getValue() {
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
      if (!(o instanceof IntLongPair)) {
         return false;
      } else {
         IntLongPair p = (IntLongPair)o;
         return p.getKey() == this._key && p.getValue() == this._value;
      }
   }
}
