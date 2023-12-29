package org.napile.primitive.pair.abstracts;

import org.napile.HashUtils;
import org.napile.primitive.pair.IntObjectPair;

public abstract class AbstractIntObjectPair<G> implements IntObjectPair<G> {
   protected int _key;
   protected G _value;

   public AbstractIntObjectPair(int key, G value) {
      this._key = key;
      this._value = value;
   }

   @Override
   public int getKey() {
      return this._key;
   }

   @Override
   public G getValue() {
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
      if (!(o instanceof IntObjectPair)) {
         return false;
      } else {
         IntObjectPair<?> p = (IntObjectPair)o;
         return p.getKey() == this._key && p.getValue() == this._value;
      }
   }
}
