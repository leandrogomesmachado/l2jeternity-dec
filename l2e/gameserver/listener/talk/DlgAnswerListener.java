package l2e.gameserver.listener.talk;

import l2e.gameserver.listener.AbstractListener;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.events.AbstractCharEvents;
import l2e.gameserver.model.actor.events.listeners.IDlgAnswerEventListener;

public abstract class DlgAnswerListener extends AbstractListener implements IDlgAnswerEventListener {
   private final Player _player;

   public DlgAnswerListener(Player player) {
      this._player = player;
      this.register();
   }

   @Override
   public void register() {
      if (this._player == null) {
         AbstractCharEvents.registerStaticListener(this);
      } else {
         this._player.getEvents().registerListener(this);
      }
   }

   @Override
   public void unregister() {
      if (this._player == null) {
         AbstractCharEvents.unregisterStaticListener(this);
      } else {
         this._player.getEvents().unregisterListener(this);
      }
   }

   @Override
   public Player getPlayer() {
      return this._player;
   }
}
