package org.napile.primitive.pair.impl;

import org.napile.primitive.pair.abstracts.AbstractLongObjectPair;

public class LongObjectPairImpl<G> extends AbstractLongObjectPair<G> {
   public LongObjectPairImpl(long key, G value) {
      super(key, value);
   }

   @Override
   public G setValue(G value) {
      G old = this._value;
      this._value = value;
      return old;
   }
}
