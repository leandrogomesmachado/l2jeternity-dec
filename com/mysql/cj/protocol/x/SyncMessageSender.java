package com.mysql.cj.protocol.x;

import com.google.protobuf.MessageLite;
import com.mysql.cj.Messages;
import com.mysql.cj.exceptions.CJCommunicationsException;
import com.mysql.cj.exceptions.CJPacketTooBigException;
import com.mysql.cj.protocol.MessageSender;
import com.mysql.cj.protocol.PacketSentTimeHolder;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.CompletionHandler;

public class SyncMessageSender implements MessageSender<XMessage>, PacketSentTimeHolder {
   static final int HEADER_LEN = 5;
   private BufferedOutputStream outputStream;
   private long lastPacketSentTime = 0L;
   private long previousPacketSentTime = 0L;
   private int maxAllowedPacket = -1;
   Object waitingAsyncOperationMonitor = new Object();

   public SyncMessageSender(BufferedOutputStream os) {
      this.outputStream = os;
   }

   public void send(XMessage message) {
      synchronized(this.waitingAsyncOperationMonitor) {
         MessageLite msg = message.getMessage();

         try {
            int type = MessageConstants.getTypeForMessageClass(msg.getClass());
            int size = 1 + msg.getSerializedSize();
            if (this.maxAllowedPacket > 0 && size > this.maxAllowedPacket) {
               throw new CJPacketTooBigException(Messages.getString("PacketTooBigException.1", new Object[]{size, this.maxAllowedPacket}));
            }

            byte[] sizeHeader = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(size).array();
            this.outputStream.write(sizeHeader);
            this.outputStream.write(type);
            msg.writeTo(this.outputStream);
            this.outputStream.flush();
            this.previousPacketSentTime = this.lastPacketSentTime;
            this.lastPacketSentTime = System.currentTimeMillis();
         } catch (IOException var8) {
            throw new CJCommunicationsException("Unable to write message", var8);
         }
      }
   }

   public void send(XMessage message, CompletionHandler<Long, Void> callback) {
      synchronized(this.waitingAsyncOperationMonitor) {
         MessageLite msg = message.getMessage();

         try {
            this.send(message);
            long result = (long)(5 + msg.getSerializedSize());
            callback.completed(result, null);
         } catch (Throwable var8) {
            callback.failed(var8, null);
         }
      }
   }

   @Override
   public long getLastPacketSentTime() {
      return this.lastPacketSentTime;
   }

   @Override
   public long getPreviousPacketSentTime() {
      return this.previousPacketSentTime;
   }

   @Override
   public void setMaxAllowedPacket(int maxAllowedPacket) {
      this.maxAllowedPacket = maxAllowedPacket;
   }
}
