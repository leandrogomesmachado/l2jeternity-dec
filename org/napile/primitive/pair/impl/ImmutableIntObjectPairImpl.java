package org.napile.primitive.pair.impl;

import org.napile.primitive.pair.abstracts.AbstractIntObjectPair;

public class ImmutableIntObjectPairImpl<G> extends AbstractIntObjectPair<G> {
   public ImmutableIntObjectPairImpl(int key, G value) {
      super(key, value);
   }

   @Override
   public G setValue(G value) {
      throw new UnsupportedOperationException();
   }
}
