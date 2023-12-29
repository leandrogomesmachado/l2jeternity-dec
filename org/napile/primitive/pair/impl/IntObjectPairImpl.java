package org.napile.primitive.pair.impl;

import org.napile.primitive.pair.abstracts.AbstractIntObjectPair;

public class IntObjectPairImpl<G> extends AbstractIntObjectPair<G> {
   public IntObjectPairImpl(int key, G value) {
      super(key, value);
   }

   @Override
   public G setValue(G value) {
      G old = this._value;
      this._value = value;
      return old;
   }
}
