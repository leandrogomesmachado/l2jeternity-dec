package org.nio.impl;

import java.nio.ByteBuffer;

public abstract class ReceivablePacket<T extends MMOClient> extends org.nio.ReceivablePacket<T> {
   protected T _client;
   protected ByteBuffer _buf;

   protected void setByteBuffer(ByteBuffer buf) {
      this._buf = buf;
   }

   @Override
   protected ByteBuffer getByteBuffer() {
      return this._buf;
   }

   protected void setClient(T client) {
      this._client = client;
   }

   public T getClient() {
      return this._client;
   }

   @Override
   protected abstract boolean read();
}
