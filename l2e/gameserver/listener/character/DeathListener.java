package l2e.gameserver.listener.character;

import l2e.gameserver.listener.AbstractListener;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.events.AbstractCharEvents;
import l2e.gameserver.model.actor.events.listeners.IDeathEventListener;

public abstract class DeathListener extends AbstractListener implements IDeathEventListener {
   private Creature _character = null;

   public DeathListener(Creature character) {
      this._character = character;
      this.register();
   }

   public Creature getCharacter() {
      return this._character;
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
}
