package l2e.gameserver.listener;

public abstract class ScriptManagerLoader<S extends ManagedLoader> {
   public abstract Iterable<S> getAllManagedScripts();

   public abstract String getScriptManagerName();

   public boolean reload(S ms) {
      return ms.reload();
   }

   public boolean unload(S ms) {
      return ms.unload();
   }

   public void setActive(S ms, boolean status) {
      ms.setActive(status);
   }
}
