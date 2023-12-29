package org.nio.impl;

import java.nio.channels.SocketChannel;

public interface IAcceptFilter {
   boolean accept(SocketChannel var1);
}
