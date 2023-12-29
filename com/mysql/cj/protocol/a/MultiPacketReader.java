package com.mysql.cj.protocol.a;

import com.mysql.cj.Messages;
import com.mysql.cj.protocol.MessageReader;
import java.io.IOException;
import java.util.Optional;

public class MultiPacketReader implements MessageReader<NativePacketHeader, NativePacketPayload> {
   private MessageReader<NativePacketHeader, NativePacketPayload> packetReader;

   public MultiPacketReader(MessageReader<NativePacketHeader, NativePacketPayload> packetReader) {
      this.packetReader = packetReader;
   }

   public NativePacketHeader readHeader() throws IOException {
      return this.packetReader.readHeader();
   }

   public NativePacketPayload readMessage(Optional<NativePacketPayload> reuse, NativePacketHeader header) throws IOException {
      int packetLength = header.getMessageSize();
      NativePacketPayload buf = this.packetReader.readMessage(reuse, header);
      if (packetLength == 16777215) {
         buf.setPosition(16777215);
         NativePacketPayload multiPacket = null;
         int multiPacketLength = -1;
         byte multiPacketSeq = this.getMessageSequence();

         do {
            NativePacketHeader hdr = this.readHeader();
            multiPacketLength = hdr.getMessageSize();
            if (multiPacket == null) {
               multiPacket = new NativePacketPayload(multiPacketLength);
            }

            if (++multiPacketSeq != hdr.getMessageSequence()) {
               throw new IOException(Messages.getString("PacketReader.10"));
            }

            this.packetReader.readMessage(Optional.of(multiPacket), hdr);
            buf.writeBytes(NativeConstants.StringLengthDataType.STRING_FIXED, multiPacket.getByteBuffer(), 0, multiPacketLength);
         } while(multiPacketLength == 16777215);

         buf.setPosition(0);
      }

      return buf;
   }

   @Override
   public byte getMessageSequence() {
      return this.packetReader.getMessageSequence();
   }

   @Override
   public void resetMessageSequence() {
      this.packetReader.resetMessageSequence();
   }

   @Override
   public MessageReader<NativePacketHeader, NativePacketPayload> undecorateAll() {
      return this.packetReader.undecorateAll();
   }

   @Override
   public MessageReader<NativePacketHeader, NativePacketPayload> undecorate() {
      return this.packetReader;
   }
}
