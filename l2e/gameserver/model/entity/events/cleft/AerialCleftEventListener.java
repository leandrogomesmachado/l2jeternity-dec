package l2e.gameserver.model.entity.events.cleft;

import l2e.gameserver.listener.player.EventListener;
import l2e.gameserver.model.actor.Player;

public final class AerialCleftEventListener extends EventListener {
   protected AerialCleftEventListener(Player player) {
      super(player);
   }

   @Override
   public void unregister() {
      super.unregister();
      this.getPlayer().setCanRevive(true);
   }

   @Override
   public boolean isOnEvent() {
      return AerialCleftEvent.getInstance().isStarted() && AerialCleftEvent.getInstance().isPlayerParticipant(this.getPlayer().getObjectId());
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
