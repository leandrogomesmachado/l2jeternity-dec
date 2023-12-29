package com.mysql.cj.protocol;

import com.mysql.cj.CharsetMapping;
import com.mysql.cj.Messages;
import com.mysql.cj.ServerVersion;
import com.mysql.cj.conf.PropertySet;
import com.mysql.cj.exceptions.ExceptionFactory;
import com.mysql.cj.exceptions.ExceptionInterceptor;

public interface AuthenticationProvider<M extends Message> {
   void init(Protocol<M> var1, PropertySet var2, ExceptionInterceptor var3);

   void connect(ServerSession var1, String var2, String var3, String var4);

   void changeUser(ServerSession var1, String var2, String var3, String var4);

   String getEncodingForHandshake();

   static byte getCharsetForHandshake(String enc, ServerVersion sv) {
      int charsetIndex = 0;
      if (enc != null) {
         charsetIndex = CharsetMapping.getCollationIndexForJavaEncoding(enc, sv);
      }

      if (charsetIndex == 0) {
         charsetIndex = 33;
      }

      if (charsetIndex > 255) {
         throw ExceptionFactory.createException(Messages.getString("MysqlIO.113", new Object[]{enc}));
      } else {
         return (byte)charsetIndex;
      }
   }
}
