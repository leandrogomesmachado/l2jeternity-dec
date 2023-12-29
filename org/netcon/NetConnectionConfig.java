package org.netcon;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

public final class NetConnectionConfig {
   public final String INITIAL_CRYPT;
   public final String TCP_EXTERNAL_HOST_ADDRESS;
   public final int TCP_EXTERNAL_PORT;
   public final int TCP_CONNECTION_QUEUE;
   public final boolean TCP_FLOOD_PROTECTION_ENABLED;
   public final int TCP_FAST_CONNECTION_LIMIT;
   public final int TCP_FAST_CONNECTION_TIME;
   public final int TCP_NORMAL_CONNECTION_TIME;
   public final int TCP_MAX_CONNECTION_PER_IP;
   public final boolean TCP_IP_BANN_ENABLED;
   public final String[] TCP_IP_BANN_LIST;
   public final int TCP_SEND_BUFFER_SIZE;
   public final int TCP_RECEIVE_BUFFER_SIZE;

   public NetConnectionConfig(String configFilePath) throws Exception {
      Properties settings = new Properties();
      InputStream is = new FileInputStream(new File(configFilePath));
      settings.load(is);
      is.close();
      this.INITIAL_CRYPT = settings.getProperty("InitialCrypt", "_;v.]05-31!|+-%xT!^[$\u0000");
      this.TCP_EXTERNAL_HOST_ADDRESS = settings.getProperty("ExternalHostAddress", "*");
      this.TCP_EXTERNAL_PORT = Integer.parseInt(settings.getProperty("ExternalPort", "0"));
      this.TCP_CONNECTION_QUEUE = Integer.parseInt(settings.getProperty("ConnectionQueue", "50"));
      this.TCP_FLOOD_PROTECTION_ENABLED = Boolean.parseBoolean(settings.getProperty("FloodProtectionEnabled", "False"));
      this.TCP_FAST_CONNECTION_LIMIT = Integer.parseInt(settings.getProperty("FastConnectionLimit", "15"));
      this.TCP_FAST_CONNECTION_TIME = Integer.parseInt(settings.getProperty("FastConnectionTime", "350"));
      this.TCP_NORMAL_CONNECTION_TIME = Integer.parseInt(settings.getProperty("NormalConnectionTime", "700"));
      this.TCP_MAX_CONNECTION_PER_IP = Integer.parseInt(settings.getProperty("MaxConnectionperIP", "50"));
      this.TCP_IP_BANN_ENABLED = Boolean.parseBoolean(settings.getProperty("IPBannEnabled", "False"));
      this.TCP_IP_BANN_LIST = settings.getProperty("IPBannList", "").split(":");
      this.TCP_SEND_BUFFER_SIZE = Integer.parseInt(settings.getProperty("SendBufferSize", "8192"));
      this.TCP_RECEIVE_BUFFER_SIZE = Integer.parseInt(settings.getProperty("ReceiveBufferSize", "8192"));
      if (this.TCP_SEND_BUFFER_SIZE < 1024) {
         throw new IllegalArgumentException("Init: TCP_SEND_BUFFER_SIZE < 1024");
      } else if (this.TCP_RECEIVE_BUFFER_SIZE < 1024) {
         throw new IllegalArgumentException("Init: TCP_RECEIVE_BUFFER_SIZE < 2048");
      }
   }
}
