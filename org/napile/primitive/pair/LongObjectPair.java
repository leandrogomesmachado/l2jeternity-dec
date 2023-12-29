package org.napile.primitive.pair;

import org.napile.primitive.pair.absint.key.LongKeyPair;
import org.napile.primitive.pair.absint.value.ObjectValuePair;

public interface LongObjectPair<G> extends LongKeyPair, ObjectValuePair<G> {
   @Override
   long getKey();

   @Override
   G getValue();

   @Override
   G setValue(G var1);
}
