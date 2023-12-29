package com.mysql.cj.protocol.a;

import com.mysql.cj.protocol.ColumnDefinition;
import com.mysql.cj.protocol.ProtocolEntityFactory;
import com.mysql.cj.protocol.ProtocolEntityReader;
import com.mysql.cj.protocol.Resultset;
import com.mysql.cj.protocol.ResultsetRow;
import com.mysql.cj.protocol.ResultsetRows;
import com.mysql.cj.protocol.a.result.OkPacket;
import com.mysql.cj.protocol.a.result.ResultsetRowsStatic;
import com.mysql.cj.protocol.a.result.ResultsetRowsStreaming;
import java.io.IOException;
import java.util.ArrayList;

public class TextResultsetReader implements ProtocolEntityReader<Resultset, NativePacketPayload> {
   protected NativeProtocol protocol;

   public TextResultsetReader(NativeProtocol prot) {
      this.protocol = prot;
   }

   public Resultset read(
      int maxRows,
      boolean streamResults,
      NativePacketPayload resultPacket,
      ColumnDefinition metadata,
      ProtocolEntityFactory<Resultset, NativePacketPayload> resultSetFactory
   ) throws IOException {
      Resultset rs = null;
      long columnCount = resultPacket.readInteger(NativeConstants.IntegerDataType.INT_LENENC);
      if (columnCount > 0L) {
         ColumnDefinition cdef = this.protocol.read(ColumnDefinition.class, new ColumnDefinitionFactory(columnCount, metadata));
         if (!this.protocol.getServerSession().isEOFDeprecated()) {
            this.protocol.skipPacket();
         }

         ResultsetRows rows = null;
         if (streamResults) {
            rows = new ResultsetRowsStreaming<>(this.protocol, cdef, false, resultSetFactory);
            this.protocol.setStreamingData(rows);
         } else {
            TextRowFactory trf = new TextRowFactory(this.protocol, cdef, resultSetFactory.getResultSetConcurrency(), false);
            ArrayList<ResultsetRow> rowList = new ArrayList<>();

            for(ResultsetRow row = this.protocol.read(ResultsetRow.class, trf); row != null; row = this.protocol.read(ResultsetRow.class, trf)) {
               if (maxRows == -1 || rowList.size() < maxRows) {
                  rowList.add(row);
               }
            }

            rows = new ResultsetRowsStatic(rowList, cdef);
         }

         rs = resultSetFactory.createFromProtocolEntity(rows);
      } else {
         if (columnCount == -1L) {
            String charEncoding = this.protocol.getPropertySet().getStringProperty("characterEncoding").getValue();
            String fileName = resultPacket.readString(
               NativeConstants.StringSelfDataType.STRING_TERM, this.protocol.doesPlatformDbCharsetMatches() ? charEncoding : null
            );
            resultPacket = this.protocol.sendFileToServer(fileName);
         }

         OkPacket ok = this.protocol.readServerStatusForResultSets(resultPacket, false);
         rs = resultSetFactory.createFromProtocolEntity(ok);
      }

      return rs;
   }
}
