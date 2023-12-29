package com.mysql.cj.xdevapi;

import com.mysql.cj.protocol.ColumnDefinition;
import com.mysql.cj.protocol.ProtocolEntity;
import com.mysql.cj.protocol.ProtocolEntityFactory;
import com.mysql.cj.protocol.x.XMessage;
import java.util.TimeZone;

public class RowFactory implements ProtocolEntityFactory<Row, XMessage> {
   private ColumnDefinition metadata;
   private TimeZone defaultTimeZone;

   public RowFactory(ColumnDefinition metadata, TimeZone defaultTimeZone) {
      this.metadata = metadata;
      this.defaultTimeZone = defaultTimeZone;
   }

   public Row createFromProtocolEntity(ProtocolEntity internalRow) {
      return new RowImpl((com.mysql.cj.result.Row)internalRow, this.metadata, this.defaultTimeZone);
   }
}
