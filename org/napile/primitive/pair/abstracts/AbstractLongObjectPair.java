package org.napile.primitive.pair.abstracts;

import org.napile.HashUtils;
import org.napile.primitive.pair.LongObjectPair;

public abstract class AbstractLongObjectPair<G> implements LongObjectPair<G> {
   protected long _key;
   protected G _value;

   public AbstractLongObjectPair(long key, G value) {
      this._key = key;
      this._value = value;
   }

   @Override
   public long getKey() {
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
      if (!(o instanceof LongObjectPair)) {
         return false;
      } else {
         LongObjectPair<?> p = (LongObjectPair)o;
         return p.getKey() == this._key && p.getValue() == this._value;
      }
   }
}
