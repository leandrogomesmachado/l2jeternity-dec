package l2e.gameserver.model.actor.events;

import java.util.logging.Level;
import l2e.commons.util.Util;
import l2e.gameserver.instancemanager.DoubleSessionManager;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.events.annotations.Message;
import l2e.gameserver.model.actor.events.annotations.UseAntiFeed;
import l2e.gameserver.model.actor.events.listeners.IDlgAnswerEventListener;
import l2e.gameserver.model.actor.events.listeners.IFamePointsChangeEventListener;
import l2e.gameserver.model.actor.events.listeners.IKarmaChangeEventListener;
import l2e.gameserver.model.actor.events.listeners.IPKPointsChangeEventListener;
import l2e.gameserver.model.actor.events.listeners.IPlayerLoginEventListener;
import l2e.gameserver.model.actor.events.listeners.IPlayerLogoutEventListener;
import l2e.gameserver.model.actor.events.listeners.IPvPKillEventListener;
import l2e.gameserver.model.actor.events.listeners.IPvPPointsEventChange;

public class PlayerEvents extends PlayableEvents {
   public PlayerEvents(Player activeChar) {
      super(activeChar);
   }

   public Player getActingPlayer() {
      return (Player)super.getActingPlayer();
   }

   public boolean onKarmaChange(int oldKarma, int newKarma) {
      if (this.hasListeners()) {
         for(IKarmaChangeEventListener listener : this.getEventListeners(IKarmaChangeEventListener.class)) {
            try {
               if (!listener.onKarmaChange(this.getActingPlayer(), oldKarma, newKarma)) {
                  return false;
               }
            } catch (Exception var6) {
               _log.log(Level.WARNING, this.getClass().getSimpleName() + ": Exception caught: ", (Throwable)var6);
            }
         }
      }

      return true;
   }

   public boolean onPKChange(int oldPKPoints, int newPKPoints) {
      if (this.hasListeners()) {
         for(IPKPointsChangeEventListener listener : this.getEventListeners(IPKPointsChangeEventListener.class)) {
            try {
               if (!listener.onPKPointsChange(this.getActingPlayer(), oldPKPoints, newPKPoints)) {
                  return false;
               }
            } catch (Exception var6) {
               _log.log(Level.WARNING, this.getClass().getSimpleName() + ": Exception caught: ", (Throwable)var6);
            }
         }
      }

      return true;
   }

   public boolean onPvPChange(int oldPvPPoints, int newPvPPoints) {
      if (this.hasListeners()) {
         for(IPvPPointsEventChange listener : this.getEventListeners(IPvPPointsEventChange.class)) {
            try {
               if (!listener.onPvPPointsChange(this.getActingPlayer(), oldPvPPoints, newPvPPoints)) {
                  return false;
               }
            } catch (Exception var6) {
               _log.log(Level.WARNING, this.getClass().getSimpleName() + ": Exception caught: ", (Throwable)var6);
            }
         }
      }

      return true;
   }

   public boolean onFameChange(int oldFamePoints, int newFamePoints) {
      if (this.hasListeners()) {
         for(IFamePointsChangeEventListener listener : this.getEventListeners(IFamePointsChangeEventListener.class)) {
            try {
               if (!listener.onFamePointsChange(this.getActingPlayer(), oldFamePoints, newFamePoints)) {
                  return false;
               }
            } catch (Exception var6) {
               _log.log(Level.WARNING, this.getClass().getSimpleName() + ": Exception caught: ", (Throwable)var6);
            }
         }
      }

      return true;
   }

   public boolean onDlgAnswer(int messageId, int answer, int requesterId) {
      if (this.hasListeners()) {
         for(IDlgAnswerEventListener listener : this.getEventListeners(IDlgAnswerEventListener.class)) {
            try {
               Message messageA = listener.getClass().getAnnotation(Message.class);
               if ((messageA == null || Util.contains(messageA.value(), messageId))
                  && !listener.onDlgAnswer(this.getActingPlayer(), messageId, answer, requesterId)) {
                  return false;
               }
            } catch (Exception var7) {
               _log.log(Level.WARNING, this.getClass().getSimpleName() + ": Exception caught: ", (Throwable)var7);
            }
         }
      }

      return true;
   }

   public void onPlayerLogin() {
      if (this.hasListeners()) {
         for(IPlayerLoginEventListener listener : this.getEventListeners(IPlayerLoginEventListener.class)) {
            try {
               listener.onPlayerLogin(this.getActingPlayer());
            } catch (Exception var4) {
               _log.log(Level.WARNING, this.getClass().getSimpleName() + ": Exception caught: ", (Throwable)var4);
            }
         }
      }
   }

   public void onPlayerLogout() {
      if (this.hasListeners()) {
         for(IPlayerLogoutEventListener listener : this.getEventListeners(IPlayerLogoutEventListener.class)) {
            try {
               listener.onPlayerLogout(this.getActingPlayer());
            } catch (Exception var4) {
               _log.log(Level.WARNING, this.getClass().getSimpleName() + ": Exception caught: ", (Throwable)var4);
            }
         }
      }
   }

   public void onPvPKill(Player target) {
      if (this.hasListeners()) {
         for(IPvPKillEventListener listener : this.getEventListeners(IPvPKillEventListener.class)) {
            try {
               UseAntiFeed useAntiFeed = listener.getClass().getAnnotation(UseAntiFeed.class);
               if (useAntiFeed == null || DoubleSessionManager.getInstance().check(this.getActingPlayer(), target)) {
                  listener.onPvPKill(this.getActingPlayer(), target);
               }
            } catch (Exception var5) {
               _log.log(Level.WARNING, this.getClass().getSimpleName() + ": Exception caught: ", (Throwable)var5);
            }
         }
      }
   }
}
