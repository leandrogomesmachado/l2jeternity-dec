package org.netcon;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import org.netcon.crypt.NewCrypt;

public abstract class NetConnection extends Thread {
   private static final NewCrypt INITIAL_CRYPT = new NewCrypt("_;v.]05-31!|+-%xT!^[$\u0000");
   private final NetConnectionConfig _config;
   private Socket _tcpCon;
   private BufferedInputStream _tcpIn;
   private BufferedOutputStream _tcpOut;
   private NewCrypt _crypt;

   protected NetConnection(NetConnectionConfig config) {
      this._config = config;
   }

   public final void connect(String address, int port) throws UnknownHostException, IOException {
      this.connect(new Socket(address, port));
   }

   public final void connect(Socket remoteConnection) throws IOException {
      if (this.isConnected()) {
         throw new IOException("TCP Connect: Allready connected.");
      } else {
         this._crypt = INITIAL_CRYPT;
         this._tcpCon = remoteConnection;
         this._tcpOut = new BufferedOutputStream(this._tcpCon.getOutputStream(), this._config.TCP_SEND_BUFFER_SIZE);
         this._tcpIn = new BufferedInputStream(this._tcpCon.getInputStream(), this._config.TCP_RECEIVE_BUFFER_SIZE);
      }
   }

   public final boolean isConnected() {
      return this._tcpCon != null && this._tcpCon.isConnected();
   }

   public final int getConnectionPort() throws IOException {
      if (!this.isConnected()) {
         throw new IOException("TCP: Not connected.");
      } else {
         return this._tcpCon.getPort();
      }
   }

   public final String getConnectionAddress() throws IOException {
      if (!this.isConnected()) {
         throw new IOException("TCP: Not connected.");
      } else {
         return this._tcpCon.getInetAddress().getHostAddress();
      }
   }

   protected final byte[] read() throws IOException {
      if (this._tcpCon == null) {
         throw new IOException("TCP Read: Not initialized.");
      } else if (this._tcpCon.isClosed()) {
         throw new IOException("TCP Read: Connection closed.");
      } else {
         int lengthLo = this._tcpIn.read();
         int lengthHi = this._tcpIn.read();
         int length = lengthHi * 256 + lengthLo;
         if (lengthHi < 0) {
            throw new IOException("TCP Read: Failed reading.");
         } else {
            byte[] data = new byte[length - 2];
            int receivedBytes = 0;

            for(int newBytes = 0; newBytes != -1 && receivedBytes < length - 2; receivedBytes += newBytes) {
               newBytes = this._tcpIn.read(data, 0, length - 2);
            }

            if (receivedBytes != length - 2) {
               throw new IOException("TCP Read: Incomplete Packet recived.");
            } else {
               return this.decrypt(data);
            }
         }
      }
   }

   protected final void write(BaseWritePacket packet) throws IOException {
      if (this._tcpCon == null) {
         throw new IOException("TCP Write: Not initialized.");
      } else if (this._tcpCon.isClosed()) {
         throw new IOException("TCP Write: Connection closed.");
      } else {
         byte[] data = this.crypt(packet.getContent());
         int len = data.length + 2;
         synchronized(this._tcpOut) {
            this._tcpOut.write(len & 0xFF);
            this._tcpOut.write(len >> 8 & 0xFF);
            this._tcpOut.write(data);
            this._tcpOut.flush();
         }
      }
   }

   protected final void close(BaseWritePacket packet) throws IOException {
      try {
         if (packet != null) {
            this.write(packet);
         }
      } finally {
         if (this._tcpIn != null) {
            this._tcpIn.close();
            this._tcpIn = null;
         }

         if (this._tcpOut != null) {
            this._tcpOut.close();
            this._tcpOut = null;
         }

         if (this._tcpCon != null) {
            this._tcpCon.close();
            this._tcpCon = null;
         }
      }
   }

   public final void setCrypt(NewCrypt crypt) {
      this._crypt = crypt;
   }

   private final byte[] decrypt(byte[] data) throws IOException {
      data = this._crypt.decrypt(data);
      if (!NewCrypt.verifyChecksum(data)) {
         throw new IOException("CRYPT: Incorrect packet checksum.");
      } else {
         return data;
      }
   }

   private final byte[] crypt(byte[] data) throws IOException {
      NewCrypt.appendChecksum(data);
      return this._crypt.crypt(data);
   }

   @Override
   public abstract void run();
}
