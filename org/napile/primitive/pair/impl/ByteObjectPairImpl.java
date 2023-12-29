package org.napile.primitive.pair.impl;

import org.napile.primitive.pair.abstracts.AbstractByteObjectPair;

public class ByteObjectPairImpl<G> extends AbstractByteObjectPair<G> {
   public ByteObjectPairImpl(byte key, G value) {
      super(key, value);
   }

   @Override
   public G setValue(G value) {
      G old = this._value;
      this._value = value;
      return old;
   }
}
