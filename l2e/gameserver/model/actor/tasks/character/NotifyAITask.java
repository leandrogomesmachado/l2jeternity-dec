package l2e.gameserver.model.actor.tasks.character;

import l2e.gameserver.ai.model.CtrlEvent;
import l2e.gameserver.model.actor.Creature;

public final class NotifyAITask implements Runnable {
   private final Creature _character;
   private final CtrlEvent _event;
   private final Object _agr0;

   public NotifyAITask(Creature character, CtrlEvent event, Object agr0) {
      this._character = character;
      this._event = event;
      this._agr0 = agr0;
   }

   public NotifyAITask(Creature character, CtrlEvent event) {
      this._character = character;
      this._event = event;
      this._agr0 = null;
   }

   @Override
   public void run() {
      if (this._character != null) {
         this._character.getAI().notifyEvent(this._event, this._agr0);
      }
   }
}
