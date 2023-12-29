package org.napile.primitive.pair;

import org.napile.primitive.pair.absint.key.IntKeyPair;
import org.napile.primitive.pair.absint.value.IntValuePair;

public interface IntIntPair extends IntKeyPair, IntValuePair {
   @Override
   int getKey();

   @Override
   int getValue();

   @Override
   int setValue(int var1);
}
