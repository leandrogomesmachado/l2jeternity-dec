package com.mysql.cj.protocol.a;

import com.mysql.cj.Messages;
import com.mysql.cj.conf.RuntimeProperty;
import com.mysql.cj.exceptions.CJPacketTooBigException;
import com.mysql.cj.protocol.MessageReader;
import com.mysql.cj.protocol.SocketConnection;
import java.io.IOException;
import java.util.Optional;

public class SimplePacketReader implements MessageReader<NativePacketHeader, NativePacketPayload> {
   protected SocketConnection socketConnection;
   protected RuntimeProperty<Integer> maxAllowedPacket;
   private byte readPacketSequence = -1;

   public SimplePacketReader(SocketConnection socketConnection, RuntimeProperty<Integer> maxAllowedPacket) {
      this.socketConnection = socketConnection;
      this.maxAllowedPacket = maxAllowedPacket;
   }

   public NativePacketHeader readHeader() throws IOException {
      NativePacketHeader hdr = new NativePacketHeader();

      try {
         this.socketConnection.getMysqlInput().readFully(hdr.getBuffer().array(), 0, 4);
         int packetLength = hdr.getMessageSize();
         if (packetLength > this.maxAllowedPacket.getValue()) {
            throw new CJPacketTooBigException((long)packetLength, (long)this.maxAllowedPacket.getValue().intValue());
         }
      } catch (CJPacketTooBigException | IOException var5) {
         try {
            this.socketConnection.forceClose();
         } catch (Exception var4) {
         }

         throw var5;
      }

      this.readPacketSequence = hdr.getMessageSequence();
      return hdr;
   }

   public NativePacketPayload readMessage(Optional<NativePacketPayload> reuse, NativePacketHeader header) throws IOException {
      try {
         int packetLength = header.getMessageSize();
         NativePacketPayload buf;
         if (reuse.isPresent()) {
            buf = reuse.get();
            buf.setPosition(0);
            if (buf.getByteBuffer().length < packetLength) {
               buf.setByteBuffer(new byte[packetLength]);
            }

            buf.setPayloadLength(packetLength);
         } else {
            buf = new NativePacketPayload(new byte[packetLength]);
         }

         int numBytesRead = this.socketConnection.getMysqlInput().readFully(buf.getByteBuffer(), 0, packetLength);
         if (numBytesRead != packetLength) {
            throw new IOException(Messages.getString("PacketReader.1", new Object[]{packetLength, numBytesRead}));
         } else {
            return buf;
         }
      } catch (IOException var7) {
         try {
            this.socketConnection.forceClose();
         } catch (Exception var6) {
         }

         throw var7;
      }
   }

   @Override
   public byte getMessageSequence() {
      return this.readPacketSequence;
   }

   @Override
   public void resetMessageSequence() {
      this.readPacketSequence = 0;
   }
}
