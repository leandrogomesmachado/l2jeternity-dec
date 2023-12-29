package com.mysql.cj;

import java.io.InputStream;

public interface BindValue {
   BindValue clone();

   void reset();

   boolean isNull();

   void setNull(boolean var1);

   boolean isStream();

   void setIsStream(boolean var1);

   MysqlType getMysqlType();

   void setMysqlType(MysqlType var1);

   byte[] getByteValue();

   void setByteValue(byte[] var1);

   InputStream getStreamValue();

   void setStreamValue(InputStream var1, int var2);

   int getStreamLength();

   boolean isSet();
}
