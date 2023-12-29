package l2e.gameserver.listener;

import java.util.logging.Logger;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.events.listeners.IEventListener;

public abstract class AbstractListener implements IEventListener {
   protected static Logger log = Logger.getLogger(AbstractListener.class.getName());
   private Player _player = null;

   public AbstractListener() {
   }

   public AbstractListener(Player player) {
      this._player = player;
   }

   public abstract void register();

   public abstract void unregister();

   public Player getPlayer() {
      return this._player;
   }
}
