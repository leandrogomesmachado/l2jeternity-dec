package org.nio.impl;

import java.nio.ByteBuffer;

public interface IPacketHandler<T extends MMOClient> {
   ReceivablePacket<T> handlePacket(ByteBuffer var1, T var2);
}
