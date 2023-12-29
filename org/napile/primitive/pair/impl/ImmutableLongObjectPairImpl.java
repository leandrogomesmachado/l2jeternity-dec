package org.napile.primitive.pair.impl;

import org.napile.primitive.pair.abstracts.AbstractLongObjectPair;

public class ImmutableLongObjectPairImpl<G> extends AbstractLongObjectPair<G> {
   public ImmutableLongObjectPairImpl(long key, G value) {
      super(key, value);
   }

   @Override
   public G setValue(G value) {
      throw new UnsupportedOperationException();
   }
}
