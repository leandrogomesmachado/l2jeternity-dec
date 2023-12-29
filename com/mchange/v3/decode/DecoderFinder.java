package com.mchange.v3.decode;

public interface DecoderFinder {
   String decoderClassName(Object var1) throws CannotDecodeException;
}
