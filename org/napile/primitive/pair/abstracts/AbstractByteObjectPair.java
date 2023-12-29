package org.napile.primitive.pair.abstracts;

import org.napile.HashUtils;
import org.napile.primitive.pair.ByteObjectPair;

public abstract class AbstractByteObjectPair<G> implements ByteObjectPair<G> {
   protected byte _key;
   protected G _value;

   public AbstractByteObjectPair(byte key, G value) {
      this._key = key;
      this._value = value;
   }

   @Override
   public byte getKey() {
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
      if (!(o instanceof ByteObjectPair)) {
         return false;
      } else {
         ByteObjectPair<?> p = (ByteObjectPair)o;
         return p.getKey() == this._key && p.getValue() == this._value;
      }
   }
}
