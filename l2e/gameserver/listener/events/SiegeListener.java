package l2e.gameserver.listener.events;

import l2e.gameserver.listener.AbstractListener;
import l2e.gameserver.model.entity.Siege;

public abstract class SiegeListener extends AbstractListener {
   public SiegeListener() {
      this.register();
   }

   public abstract boolean onStart(SiegeEvent var1);

   public abstract void onEnd(SiegeEvent var1);

   public abstract void onControlChange(SiegeEvent var1);

   @Override
   public void register() {
      Siege.addSiegeListener(this);
   }

   @Override
   public void unregister() {
      Siege.removeSiegeListener(this);
   }
}
