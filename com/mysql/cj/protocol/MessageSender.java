package com.mysql.cj.protocol;

import com.mysql.cj.exceptions.CJOperationNotSupportedException;
import com.mysql.cj.exceptions.ExceptionFactory;
import java.io.IOException;
import java.nio.channels.CompletionHandler;

public interface MessageSender<M extends Message> {
   default void send(byte[] message, int messageLen, byte messageSequence) throws IOException {
      throw (CJOperationNotSupportedException)ExceptionFactory.createException(CJOperationNotSupportedException.class, "Not supported");
   }

   default void send(M message) {
      throw (CJOperationNotSupportedException)ExceptionFactory.createException(CJOperationNotSupportedException.class, "Not supported");
   }

   default void send(M message, CompletionHandler<Long, Void> callback) {
      throw (CJOperationNotSupportedException)ExceptionFactory.createException(CJOperationNotSupportedException.class, "Not supported");
   }

   default void setMaxAllowedPacket(int maxAllowedPacket) {
      throw (CJOperationNotSupportedException)ExceptionFactory.createException(CJOperationNotSupportedException.class, "Not supported");
   }

   default MessageSender<M> undecorateAll() {
      return this;
   }

   default MessageSender<M> undecorate() {
      return this;
   }
}
