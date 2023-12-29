package org.napile.primitive.pair.impl;

import org.napile.primitive.pair.abstracts.AbstractIntLongPair;

public class ImmutableIntLongPairImpl extends AbstractIntLongPair {
   public ImmutableIntLongPairImpl(int key, long value) {
      super(key, value);
   }

   @Override
   public long setValue(long value) {
      throw new UnsupportedOperationException();
   }
}
