package com.mysql.cj.protocol;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;
import java.net.Proxy.Type;
import java.util.Properties;

public class SocksProxySocketFactory extends StandardSocketFactory {
   public static int SOCKS_DEFAULT_PORT = 1080;

   @Override
   protected Socket createSocket(Properties props) {
      String socksProxyHost = props.getProperty("socksProxyHost");
      String socksProxyPortString = props.getProperty("socksProxyPort", String.valueOf(SOCKS_DEFAULT_PORT));
      int socksProxyPort = SOCKS_DEFAULT_PORT;

      try {
         socksProxyPort = Integer.valueOf(socksProxyPortString);
      } catch (NumberFormatException var6) {
      }

      return new Socket(new Proxy(Type.SOCKS, new InetSocketAddress(socksProxyHost, socksProxyPort)));
   }
}
