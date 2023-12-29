package l2e.gameserver.model.strings.server;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServerMessage {
   private static final Logger _log = Logger.getLogger(ServerMessage.class.getName());
   private int _index = 0;
   private String _message;
   private String _messageName;
   private boolean _isStoreType;
   private ArrayList<String> _args;

   public ServerMessage(String unicName, String lang) {
      this._message = ServerStorage.getInstance().getString(lang, unicName);
      if (this._message == null) {
         _log.log(Level.SEVERE, "ServerMessage[getString()]: message named \"" + unicName + "\" not found!");
         this._message = "";
      }
   }

   public ServerMessage(String unicName, boolean isStoreType) {
      this._messageName = unicName;
      this._isStoreType = isStoreType;
   }

   public void add(Object l) {
      if (this._isStoreType) {
         this.getStoredArgs().add(String.valueOf(l));
      } else {
         this._message = this._message.replace(String.format("{%d}", this._index), String.valueOf(l));
         ++this._index;
      }
   }

   @Override
   public String toString() {
      return this._isStoreType ? this.toString("en") : this._message;
   }

   public String toString(String lang) {
      if (!this._isStoreType) {
         return "";
      } else {
         this._message = ServerStorage.getInstance().getString(lang, this._messageName);
         if (this._message == null) {
            _log.log(Level.SEVERE, "ServerMessage[getString()]: message named \"" + this._messageName + "\" not found!");
            return "";
         } else {
            for(String arg : this.getStoredArgs()) {
               this._message = this._message.replace(String.format("{%d}", this._index), arg);
               ++this._index;
            }

            this._index = 0;
            return this._message;
         }
      }
   }

   private ArrayList<String> getStoredArgs() {
      if (this._args == null) {
         this._args = new ArrayList<>();
      }

      return this._args;
   }
}
