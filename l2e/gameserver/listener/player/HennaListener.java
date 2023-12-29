package l2e.gameserver.listener.player;

import l2e.gameserver.listener.AbstractListener;
import l2e.gameserver.listener.events.HennaEvent;
import l2e.gameserver.model.actor.Player;

public abstract class HennaListener extends AbstractListener {
   public HennaListener() {
      this.register();
   }

   public abstract boolean onAddHenna(HennaEvent var1);

   public abstract boolean onRemoveHenna(HennaEvent var1);

   @Override
   public void register() {
      Player.addHennaListener(this);
   }

   @Override
   public void unregister() {
      Player.removeHennaListener(this);
   }
}
