package l2e.gameserver.geodata.editor;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GeoEditorListener extends Thread {
   private static GeoEditorListener _instance;
   private static final int PORT = 9011;
   private static Logger _log = Logger.getLogger(GeoEditorListener.class.getName());
   private final ServerSocket _serverSocket = new ServerSocket(9011);
   private GeoEditorThread _geoEditor;

   public static GeoEditorListener getInstance() {
      synchronized(GeoEditorListener.class) {
         if (_instance == null) {
            try {
               _instance = new GeoEditorListener();
               _instance.start();
               _log.info("GeoEditorListener Initialized.");
            } catch (IOException var3) {
               _log.log(Level.SEVERE, "Error creating geoeditor listener! " + var3.getMessage(), (Throwable)var3);
               System.exit(1);
            }
         }
      }

      return _instance;
   }

   private GeoEditorListener() throws IOException {
   }

   public GeoEditorThread getThread() {
      return this._geoEditor;
   }

   public String getStatus() {
      return this._geoEditor != null && this._geoEditor.isWorking() ? "Geoeditor connected." : "Geoeditor not connected.";
   }

   @Override
   public void run() {
      try (Socket connection = this._serverSocket.accept()) {
         while(!this.isInterrupted()) {
            if (this._geoEditor != null && this._geoEditor.isWorking()) {
               _log.warning("Geoeditor already connected!");
            } else {
               _log.info("Received geoeditor connection from: " + connection.getInetAddress().getHostAddress());
               this._geoEditor = new GeoEditorThread(connection);
               this._geoEditor.start();
            }
         }
      } catch (Exception var28) {
         _log.log(Level.WARNING, "GeoEditorListener: " + var28.getMessage(), (Throwable)var28);
      } finally {
         try {
            this._serverSocket.close();
         } catch (Exception var24) {
            _log.log(Level.WARNING, "GeoEditorListener: " + var24.getMessage(), (Throwable)var24);
         }

         _log.warning("GeoEditorListener Closed!");
      }
   }
}
