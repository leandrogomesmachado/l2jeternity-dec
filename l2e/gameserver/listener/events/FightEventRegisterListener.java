package l2e.gameserver.listener.events;

import l2e.gameserver.listener.AbstractListener;
import l2e.gameserver.model.entity.events.AbstractFightEvent;

public abstract class FightEventRegisterListener extends AbstractListener {
   public FightEventRegisterListener() {
      this.register();
   }

   public abstract void onBegin();

   public abstract void onEnd();

   public abstract void onRegistrationStart();

   @Override
   public void register() {
      AbstractFightEvent.addListener(this);
   }

   @Override
   public void unregister() {
      AbstractFightEvent.removeListener(this);
   }
}
