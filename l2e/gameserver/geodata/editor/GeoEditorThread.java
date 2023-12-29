package l2e.gameserver.geodata.editor;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import l2e.gameserver.model.actor.Player;

public class GeoEditorThread extends Thread {
   private static Logger _log = Logger.getLogger(GeoEditorThread.class.getName());
   private boolean _working = false;
   private int _mode = 0;
   private int _sendDelay = 1000;
   private final Socket _geSocket;
   private OutputStream _out;
   private final List<Player> _gms = new ArrayList<>();

   public GeoEditorThread(Socket ge) {
      this._geSocket = ge;
      this._working = true;
   }

   @Override
   public void interrupt() {
      try {
         this._geSocket.close();
      } catch (Exception var2) {
      }

      super.interrupt();
   }

   @Override
   public void run() {
      try {
         this._out = this._geSocket.getOutputStream();
         int timer = 0;

         while(this._working) {
            if (!this.isConnected()) {
               this._working = false;
            }

            if (this._mode == 2 && timer > this._sendDelay) {
               for(Player gm : this._gms) {
                  if (!gm.getClient().getConnection().isClosed()) {
                     this.sendGmPosition(gm);
                  } else {
                     this._gms.remove(gm);
                  }
               }

               timer = 0;
            }

            try {
               sleep(100L);
               if (this._mode == 2) {
                  timer += 100;
               }
            } catch (Exception var13) {
            }
         }
      } catch (Exception var14) {
         _log.log(Level.WARNING, "GeoEditor disconnected. " + var14.getMessage(), (Throwable)var14);
      } finally {
         try {
            this._geSocket.close();
         } catch (Exception var12) {
         }

         this._working = false;
      }
   }

   public void sendGmPosition(int gx, int gy, short z) {
      if (this.isConnected()) {
         try {
            synchronized(this._out) {
               this.writeC(11);
               this.writeC(1);
               this.writeD(gx);
               this.writeD(gy);
               this.writeH(z);
               this._out.flush();
            }
         } catch (Exception var16) {
            _log.log(Level.WARNING, "GeoEditor disconnected. " + var16.getMessage(), (Throwable)var16);
            this._working = false;
         } finally {
            try {
               this._geSocket.close();
            } catch (Exception var14) {
            }

            this._working = false;
         }
      }
   }

   public void sendGmPosition(Player _gm) {
      this.sendGmPosition(_gm.getX(), _gm.getY(), (short)_gm.getZ());
   }

   public void sendPing() {
      if (this.isConnected()) {
         try {
            synchronized(this._out) {
               this.writeC(1);
               this.writeC(2);
               this._out.flush();
            }
         } catch (Exception var13) {
            _log.log(Level.WARNING, "GeoEditor disconnected. " + var13.getMessage(), (Throwable)var13);
            this._working = false;
         } finally {
            try {
               this._geSocket.close();
            } catch (Exception var11) {
            }

            this._working = false;
         }
      }
   }

   private void writeD(int value) throws IOException {
      this._out.write(value & 0xFF);
      this._out.write(value >> 8 & 0xFF);
      this._out.write(value >> 16 & 0xFF);
      this._out.write(value >> 24 & 0xFF);
   }

   private void writeH(int value) throws IOException {
      this._out.write(value & 0xFF);
      this._out.write(value >> 8 & 0xFF);
   }

   private void writeC(int value) throws IOException {
      this._out.write(value & 0xFF);
   }

   public void setMode(int value) {
      this._mode = value;
   }

   public void setTimer(int value) {
      if (value < 500) {
         this._sendDelay = 500;
      } else if (value > 60000) {
         this._sendDelay = 60000;
      } else {
         this._sendDelay = value;
      }
   }

   public void addGM(Player gm) {
      if (!this._gms.contains(gm)) {
         this._gms.add(gm);
      }
   }

   public void removeGM(Player gm) {
      if (this._gms.contains(gm)) {
         this._gms.remove(gm);
      }
   }

   public boolean isSend(Player gm) {
      return this._mode == 1 && this._gms.contains(gm);
   }

   private boolean isConnected() {
      return this._geSocket.isConnected() && !this._geSocket.isClosed();
   }

   public boolean isWorking() {
      this.sendPing();
      return this._working;
   }

   public int getMode() {
      return this._mode;
   }
}
