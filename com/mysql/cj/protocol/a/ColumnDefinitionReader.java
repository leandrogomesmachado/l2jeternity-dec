package com.mysql.cj.protocol.a;

import com.mysql.cj.MysqlType;
import com.mysql.cj.protocol.ColumnDefinition;
import com.mysql.cj.protocol.ProtocolEntityFactory;
import com.mysql.cj.protocol.ProtocolEntityReader;
import com.mysql.cj.result.Field;
import com.mysql.cj.util.LazyString;

public class ColumnDefinitionReader implements ProtocolEntityReader<ColumnDefinition, NativePacketPayload> {
   private NativeProtocol protocol;

   public ColumnDefinitionReader(NativeProtocol prot) {
      this.protocol = prot;
   }

   public ColumnDefinition read(ProtocolEntityFactory<ColumnDefinition, NativePacketPayload> sf) {
      ColumnDefinitionFactory cdf = (ColumnDefinitionFactory)sf;
      long columnCount = cdf.getColumnCount();
      ColumnDefinition cdef = cdf.getColumnDefinitionFromCache();
      if (cdef != null && !cdf.mergeColumnDefinitions()) {
         for(int i = 0; (long)i < columnCount; ++i) {
            this.protocol.skipPacket();
         }

         return cdef;
      } else {
         Field[] fields = null;
         boolean checkEOF = !this.protocol.getServerSession().isEOFDeprecated();
         fields = new Field[(int)columnCount];

         for(int i = 0; (long)i < columnCount; ++i) {
            NativePacketPayload fieldPacket = this.protocol.readMessage(null);
            if (checkEOF && fieldPacket.isEOFPacket()) {
               break;
            }

            fields[i] = this.unpackField(fieldPacket, this.protocol.getServerSession().getCharacterSetMetadata());
         }

         return cdf.createFromFields(fields);
      }
   }

   protected Field unpackField(NativePacketPayload packet, String characterSetMetadata) {
      int length = (int)packet.readInteger(NativeConstants.IntegerDataType.INT_LENENC);
      packet.setPosition(packet.getPosition() + length);
      length = (int)packet.readInteger(NativeConstants.IntegerDataType.INT_LENENC);
      int offset = packet.getPosition();
      LazyString databaseName = new LazyString(packet.getByteBuffer(), offset, length, characterSetMetadata);
      packet.setPosition(packet.getPosition() + length);
      length = (int)packet.readInteger(NativeConstants.IntegerDataType.INT_LENENC);
      offset = packet.getPosition();
      LazyString tableName = new LazyString(packet.getByteBuffer(), offset, length, characterSetMetadata);
      packet.setPosition(packet.getPosition() + length);
      length = (int)packet.readInteger(NativeConstants.IntegerDataType.INT_LENENC);
      offset = packet.getPosition();
      LazyString originalTableName = new LazyString(packet.getByteBuffer(), offset, length, characterSetMetadata);
      packet.setPosition(packet.getPosition() + length);
      length = (int)packet.readInteger(NativeConstants.IntegerDataType.INT_LENENC);
      offset = packet.getPosition();
      LazyString columnName = new LazyString(packet.getByteBuffer(), offset, length, characterSetMetadata);
      packet.setPosition(packet.getPosition() + length);
      length = (int)packet.readInteger(NativeConstants.IntegerDataType.INT_LENENC);
      offset = packet.getPosition();
      LazyString originalColumnName = new LazyString(packet.getByteBuffer(), offset, length, characterSetMetadata);
      packet.setPosition(packet.getPosition() + length);
      packet.readInteger(NativeConstants.IntegerDataType.INT1);
      short collationIndex = (short)((int)packet.readInteger(NativeConstants.IntegerDataType.INT2));
      long colLength = packet.readInteger(NativeConstants.IntegerDataType.INT4);
      int colType = (int)packet.readInteger(NativeConstants.IntegerDataType.INT1);
      short colFlag = (short)(
         (int)packet.readInteger(
            this.protocol.getServerSession().hasLongColumnInfo() ? NativeConstants.IntegerDataType.INT2 : NativeConstants.IntegerDataType.INT1
         )
      );
      int colDecimals = (int)packet.readInteger(NativeConstants.IntegerDataType.INT1);
      String encoding = this.protocol.getServerSession().getEncodingForIndex(collationIndex);
      MysqlType mysqlType = NativeProtocol.findMysqlType(
         this.protocol.getPropertySet(), colType, colFlag, colLength, tableName, originalTableName, collationIndex, encoding
      );
      return new Field(
         databaseName,
         tableName,
         originalTableName,
         columnName,
         originalColumnName,
         colLength,
         colType,
         colFlag,
         colDecimals,
         collationIndex,
         encoding,
         mysqlType
      );
   }
}
