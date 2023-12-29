package org.napile.primitive.pair;

import org.napile.primitive.pair.absint.key.IntKeyPair;
import org.napile.primitive.pair.absint.value.LongValuePair;

public interface IntLongPair extends IntKeyPair, LongValuePair {
   @Override
   int getKey();

   @Override
   long getValue();

   @Override
   long setValue(long var1);
}
