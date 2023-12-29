package com.mysql.cj.protocol.a;

import com.mysql.cj.protocol.ColumnDefinition;
import com.mysql.cj.protocol.ProtocolEntityFactory;
import com.mysql.cj.protocol.ProtocolEntityReader;
import com.mysql.cj.protocol.Resultset;
import com.mysql.cj.protocol.ResultsetRow;
import com.mysql.cj.protocol.ResultsetRows;
import com.mysql.cj.protocol.a.result.OkPacket;
import com.mysql.cj.protocol.a.result.ResultsetRowsCursor;
import com.mysql.cj.protocol.a.result.ResultsetRowsStatic;
import com.mysql.cj.protocol.a.result.ResultsetRowsStreaming;
import java.io.IOException;
import java.util.ArrayList;

public class BinaryResultsetReader implements ProtocolEntityReader<Resultset, NativePacketPayload> {
   protected NativeProtocol protocol;

   public BinaryResultsetReader(NativeProtocol prot) {
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
         ColumnDefinition cdef = this.protocol.read(ColumnDefinition.class, new MergingColumnDefinitionFactory(columnCount, metadata));
         boolean isCursorPosible = this.protocol.getPropertySet().getBooleanProperty("useCursorFetch").getValue()
            && resultSetFactory.getResultSetType() == Resultset.Type.FORWARD_ONLY
            && resultSetFactory.getFetchSize() > 0;
         if (isCursorPosible || !this.protocol.getServerSession().isEOFDeprecated()) {
            this.protocol.readServerStatusForResultSets(this.protocol.readMessage(this.protocol.getReusablePacket()), true);
         }

         ResultsetRows rows = null;
         if (isCursorPosible && this.protocol.getServerSession().cursorExists()) {
            rows = new ResultsetRowsCursor(this.protocol, cdef);
         } else if (streamResults) {
            rows = new ResultsetRowsStreaming<>(this.protocol, cdef, true, resultSetFactory);
            this.protocol.setStreamingData(rows);
         } else {
            BinaryRowFactory brf = new BinaryRowFactory(this.protocol, cdef, resultSetFactory.getResultSetConcurrency(), false);
            ArrayList<ResultsetRow> rowList = new ArrayList<>();

            for(ResultsetRow row = this.protocol.read(ResultsetRow.class, brf); row != null; row = this.protocol.read(ResultsetRow.class, brf)) {
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
