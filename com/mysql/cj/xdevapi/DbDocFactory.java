package com.mysql.cj.xdevapi;

import com.mysql.cj.protocol.ProtocolEntity;
import com.mysql.cj.protocol.ProtocolEntityFactory;
import com.mysql.cj.protocol.x.XMessage;

public class DbDocFactory implements ProtocolEntityFactory<DbDoc, XMessage> {
   public DbDoc createFromProtocolEntity(ProtocolEntity internalRow) {
      return ((com.mysql.cj.result.Row)internalRow).getValue(0, new DbDocValueFactory());
   }
}
