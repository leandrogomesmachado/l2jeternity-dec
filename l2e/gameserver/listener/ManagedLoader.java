package l2e.gameserver.listener;

import java.io.File;
import javax.script.ScriptException;

public abstract class ManagedLoader {
   private final File _scriptFile = ScriptListenerLoader.getInstance().getCurrentLoadingScript();
   private long _lastLoadTime;
   private boolean _isActive;

   public ManagedLoader() {
      this.setLastLoadTime(System.currentTimeMillis());
   }

   public boolean reload() {
      try {
         ScriptListenerLoader.getInstance().executeScript(this.getScriptFile());
         return true;
      } catch (ScriptException var2) {
         return false;
      }
   }

   public abstract boolean unload();

   public void setActive(boolean status) {
      this._isActive = status;
   }

   public boolean isActive() {
      return this._isActive;
   }

   public File getScriptFile() {
      return this._scriptFile;
   }

   protected void setLastLoadTime(long lastLoadTime) {
      this._lastLoadTime = lastLoadTime;
   }

   protected long getLastLoadTime() {
      return this._lastLoadTime;
   }

   public abstract String getScriptName();

   public abstract ScriptManagerLoader<?> getScriptManager();
}
