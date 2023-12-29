package l2e.gameserver.listener.events;

import l2e.gameserver.listener.AbstractListener;
import l2e.gameserver.model.entity.FortSiege;

public abstract class FortSiegeListener extends AbstractListener {
   public FortSiegeListener() {
      this.register();
   }

   public abstract boolean onStart(FortSiegeEvent var1);

   public abstract void onEnd(FortSiegeEvent var1);

   @Override
   public void register() {
      FortSiege.addFortSiegeListener(this);
   }

   @Override
   public void unregister() {
      FortSiege.removeFortSiegeListener(this);
   }
}
