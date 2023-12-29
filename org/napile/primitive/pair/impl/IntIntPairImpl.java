package org.napile.primitive.pair.impl;

import org.napile.primitive.pair.abstracts.AbstractIntIntPair;

public class IntIntPairImpl extends AbstractIntIntPair {
   public IntIntPairImpl(int key, int value) {
      super(key, value);
   }

   @Override
   public int setValue(int value) {
      int old = this._value;
      this._value = value;
      return old;
   }
}
