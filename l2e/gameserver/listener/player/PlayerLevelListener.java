package l2e.gameserver.listener.player;

import l2e.gameserver.listener.AbstractListener;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.events.AbstractCharEvents;
import l2e.gameserver.model.actor.events.listeners.ILevelChangeEventListener;

public abstract class PlayerLevelListener extends AbstractListener implements ILevelChangeEventListener {
   public PlayerLevelListener(Player activeChar) {
      super(activeChar);
      this.register();
   }

   @Override
   public void register() {
      if (this.getPlayer() == null) {
         AbstractCharEvents.registerStaticListener(this);
      } else {
         this.getPlayer().getEvents().registerListener(this);
      }
   }

   @Override
   public void unregister() {
      if (this.getPlayer() == null) {
         AbstractCharEvents.unregisterStaticListener(this);
      } else {
         this.getPlayer().getEvents().unregisterListener(this);
      }
   }
}
