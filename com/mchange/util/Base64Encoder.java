package com.mchange.util;

public interface Base64Encoder {
   String encode(byte[] var1);

   byte[] decode(String var1) throws Base64FormatException;
}
