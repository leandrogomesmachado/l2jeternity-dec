package l2e.gameserver.listener.player;

import l2e.gameserver.listener.AbstractListener;
import l2e.gameserver.model.actor.events.AbstractCharEvents;
import l2e.gameserver.model.actor.events.listeners.IPlayerLoginEventListener;

public abstract class PlayerSpawnListener extends AbstractListener implements IPlayerLoginEventListener {
   public PlayerSpawnListener() {
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
