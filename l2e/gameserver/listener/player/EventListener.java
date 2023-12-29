package l2e.gameserver.listener.player;

import l2e.gameserver.listener.AbstractListener;
import l2e.gameserver.model.actor.Player;

public abstract class EventListener extends AbstractListener {
   public EventListener(Player activeChar) {
      super(activeChar);
      this.register();
   }

   public abstract boolean isOnEvent();

   public abstract boolean isBlockingExit();

   public abstract boolean isBlockingDeathPenalty();

   @Override
   public void register() {
      if (this.getPlayer() != null) {
         this.getPlayer().addEventListener(this);
      }
   }

   @Override
   public void unregister() {
      if (this.getPlayer() != null) {
         this.getPlayer().removeEventListener(this);
      }
   }
}
