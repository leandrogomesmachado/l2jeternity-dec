package l2e.gameserver.listener.character;

import l2e.gameserver.listener.AbstractListener;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.events.AbstractCharEvents;
import l2e.gameserver.model.actor.events.listeners.ISkillUseEventListener;

public abstract class SkillUseListener extends AbstractListener implements ISkillUseEventListener {
   private Creature _character = null;

   public SkillUseListener(Creature character) {
      this._character = character;
      this.register();
   }

   @Override
   public void register() {
      if (this._character == null) {
         AbstractCharEvents.registerStaticListener(this);
      } else {
         this._character.getEvents().registerListener(this);
      }
   }

   @Override
   public void unregister() {
      if (this._character == null) {
         AbstractCharEvents.unregisterStaticListener(this);
      } else {
         this._character.getEvents().unregisterListener(this);
      }
   }

   public Creature getCharacter() {
      return this._character;
   }
}
