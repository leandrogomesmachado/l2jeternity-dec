package org.napile.primitive.pair.impl;

import org.napile.primitive.pair.abstracts.AbstractIntIntPair;

public class ImmutableIntIntPairImpl extends AbstractIntIntPair {
   public ImmutableIntIntPairImpl(int key, int value) {
      super(key, value);
   }

   @Override
   public int setValue(int value) {
      throw new UnsupportedOperationException();
   }
}
