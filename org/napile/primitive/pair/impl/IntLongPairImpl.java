package org.napile.primitive.pair.impl;

import org.napile.primitive.pair.abstracts.AbstractIntLongPair;

public class IntLongPairImpl extends AbstractIntLongPair {
   public IntLongPairImpl(int key, long value) {
      super(key, value);
   }

   @Override
   public long setValue(long value) {
      long old = this._value;
      this._value = value;
      return old;
   }
}
