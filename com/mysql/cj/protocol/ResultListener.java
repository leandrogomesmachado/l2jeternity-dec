package com.mysql.cj.protocol;

import com.mysql.cj.result.Row;

public interface ResultListener<OK extends ProtocolEntity> {
   void onMetadata(ColumnDefinition var1);

   void onRow(Row var1);

   void onComplete(OK var1);

   void onException(Throwable var1);
}
