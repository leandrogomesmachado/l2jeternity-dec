package org.napile.primitive.pair;

import org.napile.primitive.pair.absint.key.ByteKeyPair;
import org.napile.primitive.pair.absint.value.ObjectValuePair;

public interface ByteObjectPair<G> extends ByteKeyPair, ObjectValuePair<G> {
   @Override
   byte getKey();

   @Override
   G getValue();

   @Override
   G setValue(G var1);
}
