package org.napile.primitive.pair;

import org.napile.primitive.pair.absint.key.IntKeyPair;
import org.napile.primitive.pair.absint.value.ObjectValuePair;

public interface IntObjectPair<G> extends IntKeyPair, ObjectValuePair<G> {
   @Override
   int getKey();

   @Override
   G getValue();

   @Override
   G setValue(G var1);
}
