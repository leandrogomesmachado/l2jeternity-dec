package l2e.gameserver.listener.player;

import l2e.gameserver.listener.AbstractListener;
import l2e.gameserver.model.actor.events.AbstractCharEvents;
import l2e.gameserver.model.actor.events.listeners.IPlayerLogoutEventListener;

public abstract class PlayerDespawnListener extends AbstractListener implements IPlayerLogoutEventListener {
   public PlayerDespawnListener() {
      this.register();
   }

   @Override
   public void register() {
      AbstractCharEvents.registerStaticListener(this);
   }

   @Override
   public void unregister() {
      AbstractCharEvents.unregisterStaticListener(this);
   }
}
