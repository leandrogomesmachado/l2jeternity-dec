package org.netcon;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class NetConnectionListener extends Thread {
   private static final Logger _log = Logger.getLogger(NetConnectionListener.class.getName());
   private final NetConnectionConfig _config;
   private final ServerSocket _serverSocket;
   private final Map<String, NetConnectionListener.ForeignConnection> _floodProtection;
   private final List<String> _ipBanns;

   protected NetConnectionListener(NetConnectionConfig config) throws IOException {
      this._config = config;
      if (this._config.TCP_IP_BANN_ENABLED) {
         this._ipBanns = new ArrayList<>();

         for(String ip : this._config.TCP_IP_BANN_LIST) {
            this._ipBanns.add(ip);
         }
      } else {
         this._ipBanns = null;
      }

      if (this._config.TCP_FLOOD_PROTECTION_ENABLED) {
         this._floodProtection = new HashMap<>();
      } else {
         this._floodProtection = null;
      }

      if (this._config.TCP_EXTERNAL_HOST_ADDRESS.equals("*")) {
         this._serverSocket = new ServerSocket(this._config.TCP_EXTERNAL_PORT, this._config.TCP_CONNECTION_QUEUE);
      } else {
         this._serverSocket = new ServerSocket(
            this._config.TCP_EXTERNAL_PORT, this._config.TCP_CONNECTION_QUEUE, InetAddress.getByName(this._config.TCP_EXTERNAL_HOST_ADDRESS)
         );
      }
   }

   @Override
   public final void run() {
      Socket connection = null;

      while(true) {
         try {
            connection = this._serverSocket.accept();
            String connectionAddress = connection.getInetAddress().getHostAddress();
            _log.log(Level.INFO, "Received connection: " + connectionAddress);
            if (this._config.TCP_IP_BANN_ENABLED && this._ipBanns.contains(connectionAddress)) {
               throw new IOException("IP: " + connectionAddress + " is on TCP_IP_BANN_LIST. Closing connection...");
            }

            if (this._config.TCP_FLOOD_PROTECTION_ENABLED) {
               NetConnectionListener.ForeignConnection fConnection = this._floodProtection.get(connectionAddress);
               if (fConnection == null) {
                  fConnection = new NetConnectionListener.ForeignConnection(System.currentTimeMillis());
                  this._floodProtection.put(connectionAddress, fConnection);
               } else {
                  ++fConnection.connectionNumber;
                  if (fConnection.connectionNumber > this._config.TCP_FAST_CONNECTION_LIMIT
                        && System.currentTimeMillis() - fConnection.lastConnection < (long)this._config.TCP_NORMAL_CONNECTION_TIME
                     || System.currentTimeMillis() - fConnection.lastConnection < (long)this._config.TCP_FAST_CONNECTION_TIME
                     || fConnection.connectionNumber > this._config.TCP_MAX_CONNECTION_PER_IP) {
                     fConnection.lastConnection = System.currentTimeMillis();
                     --fConnection.connectionNumber;
                     fConnection.isFlooding = true;
                     throw new IOException("IP: " + connectionAddress + " is marked as Flooding. Closing connection...");
                  }

                  fConnection.lastConnection = System.currentTimeMillis();
                  if (fConnection.isFlooding) {
                     fConnection.isFlooding = false;
                  }

                  _log.log(Level.FINE, "IP: " + connectionAddress + " is no longer marked as Flooding.");
               }
            }

            this.buildTCPNetConnection(this._config, connection);
         } catch (IOException var6) {
            _log.log(Level.WARNING, "", (Throwable)var6);
            if (connection != null) {
               try {
                  connection.close();
               } catch (IOException var5) {
                  _log.log(Level.WARNING, "Failed closing connection.", (Throwable)var5);
               }
            }

            try {
               if (this.isInterrupted()) {
                  this.close();
               }
            } catch (IOException var4) {
               _log.log(Level.WARNING, "Failed closing listener.", (Throwable)var4);
            }
         }
      }
   }

   public final void close() throws IOException {
      this._serverSocket.close();
   }

   public final void removeTCPNetConnection(NetConnection connection) throws IOException {
      if (this._config.TCP_FLOOD_PROTECTION_ENABLED) {
         String connectionAddress = connection.getConnectionAddress();
         NetConnectionListener.ForeignConnection fConnection = this._floodProtection.get(connectionAddress);
         if (fConnection != null) {
            --fConnection.connectionNumber;
            if (fConnection.connectionNumber == 0) {
               this._floodProtection.remove(connectionAddress);
            }
         }
      }
   }

   protected abstract void buildTCPNetConnection(NetConnectionConfig var1, Socket var2) throws IOException;

   private final class ForeignConnection {
      protected int connectionNumber;
      protected long lastConnection;
      protected boolean isFlooding = false;

      protected ForeignConnection(long time) {
         this.lastConnection = time;
         this.connectionNumber = 1;
      }
   }
}
