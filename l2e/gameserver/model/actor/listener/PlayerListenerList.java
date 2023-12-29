package l2e.gameserver.model.actor.listener;

import l2e.commons.listener.Listener;
import l2e.gameserver.listener.player.QuestionMarkListener;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Player;

public class PlayerListenerList extends CharListenerList {
   public PlayerListenerList(Player actor) {
      super(actor);
   }

   public Player getActor() {
      return (Player)this._actor;
   }

   public void onQuestionMarkClicked(int questionMarkId) {
      if (!_global.getListeners().isEmpty()) {
         for(Listener<Creature> listener : _global.getListeners()) {
            if (QuestionMarkListener.class.isInstance(listener)) {
               ((QuestionMarkListener)listener).onQuestionMarkClicked(this.getActor(), questionMarkId);
            }
         }
      }

      if (!this.getListeners().isEmpty()) {
         for(Listener<Creature> listener : this.getListeners()) {
            if (QuestionMarkListener.class.isInstance(listener)) {
               ((QuestionMarkListener)listener).onQuestionMarkClicked(this.getActor(), questionMarkId);
            }
         }
      }
   }
}
