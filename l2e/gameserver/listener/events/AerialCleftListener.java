package l2e.gameserver.listener.events;

import l2e.gameserver.listener.AbstractListener;
import l2e.gameserver.model.entity.events.cleft.AerialCleftEvent;

public abstract class AerialCleftListener extends AbstractListener {
   public AerialCleftListener() {
      this.register();
   }

   public abstract void onBegin();

   public abstract void onKill(AerialCleftKillEvent var1);

   public abstract void onEnd();

   public abstract void onRegistrationStart();

   @Override
   public void register() {
      AerialCleftEvent.getInstance().addAerialCleftListener(this);
   }

   @Override
   public void unregister() {
      AerialCleftEvent.getInstance().removeAerialCleftListener(this);
   }
}
