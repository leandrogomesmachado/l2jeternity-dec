package l2e.gameserver.model.entity.events.model.listener;

import l2e.gameserver.listener.player.EventListener;
import l2e.gameserver.model.actor.Player;

public final class FightEventListener extends EventListener {
   public FightEventListener(Player player) {
      super(player);
   }

   @Override
   public void unregister() {
      super.unregister();
      this.getPlayer().setCanRevive(true);
   }

   @Override
   public boolean isOnEvent() {
      return this.getPlayer().isRegisteredInFightEvent() && this.getPlayer().isInFightEvent();
   }

   @Override
   public boolean isBlockingExit() {
      return true;
   }

   @Override
   public boolean isBlockingDeathPenalty() {
      return true;
   }
}
