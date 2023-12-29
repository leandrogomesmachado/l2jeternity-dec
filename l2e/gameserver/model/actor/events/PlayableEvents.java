package l2e.gameserver.model.actor.events;

import java.util.logging.Level;
import l2e.gameserver.model.actor.Playable;
import l2e.gameserver.model.actor.events.annotations.PlayerOnly;
import l2e.gameserver.model.actor.events.listeners.IExperienceReceivedEventListener;
import l2e.gameserver.model.actor.events.listeners.ILevelChangeEventListener;

public class PlayableEvents extends CharEvents {
   public PlayableEvents(Playable activeChar) {
      super(activeChar);
   }

   public boolean onExperienceReceived(long exp) {
      if (this.hasListeners()) {
         for(IExperienceReceivedEventListener listener : this.getEventListeners(IExperienceReceivedEventListener.class)) {
            try {
               if ((!listener.getClass().isAnnotationPresent(PlayerOnly.class) || this.getActingPlayer().isPlayer())
                  && !listener.onExperienceReceived(this.getActingPlayer(), exp)) {
                  return false;
               }
            } catch (Exception var6) {
               _log.log(Level.WARNING, this.getClass().getSimpleName() + ": Exception caught: ", (Throwable)var6);
            }
         }
      }

      return true;
   }

   public boolean onLevelChange(byte levels) {
      if (this.hasListeners()) {
         for(ILevelChangeEventListener listener : this.getEventListeners(ILevelChangeEventListener.class)) {
            try {
               if ((!listener.getClass().isAnnotationPresent(PlayerOnly.class) || this.getActingPlayer().isPlayer())
                  && !listener.onLevelChange(this.getActingPlayer(), levels)) {
                  return false;
               }
            } catch (Exception var5) {
               _log.log(Level.WARNING, this.getClass().getSimpleName() + ": Exception caught: ", (Throwable)var5);
            }
         }
      }

      return true;
   }

   public Playable getActingPlayer() {
      return (Playable)super.getActingPlayer();
   }
}
