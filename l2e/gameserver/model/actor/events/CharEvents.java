package l2e.gameserver.model.actor.events;

import java.util.logging.Level;
import java.util.logging.Logger;
import l2e.commons.util.Util;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.events.annotations.PlayerOnly;
import l2e.gameserver.model.actor.events.annotations.SkillId;
import l2e.gameserver.model.actor.events.annotations.SkillLevel;
import l2e.gameserver.model.actor.events.listeners.IAttackEventListener;
import l2e.gameserver.model.actor.events.listeners.IDamageDealtEventListener;
import l2e.gameserver.model.actor.events.listeners.IDamageReceivedEventListener;
import l2e.gameserver.model.actor.events.listeners.IDeathEventListener;
import l2e.gameserver.model.actor.events.listeners.ISkillUseEventListener;
import l2e.gameserver.model.actor.events.listeners.ITeleportedEventListener;
import l2e.gameserver.model.skills.Skill;

public class CharEvents extends AbstractCharEvents {
   protected static final Logger _log = Logger.getLogger(CharEvents.class.getName());
   private final Creature _activeChar;

   public CharEvents(Creature activeChar) {
      this._activeChar = activeChar;
   }

   public boolean onAttack(Creature target) {
      if (this.hasListeners()) {
         for(IAttackEventListener listener : this.getEventListeners(IAttackEventListener.class)) {
            try {
               if ((!listener.getClass().isAnnotationPresent(PlayerOnly.class) || target.isPlayer()) && !listener.onAttack(this.getActingPlayer(), target)) {
                  return false;
               }
            } catch (Exception var5) {
               _log.log(Level.WARNING, this.getClass().getSimpleName() + ": Exception caught: ", (Throwable)var5);
            }
         }
      }

      return true;
   }

   public boolean onMagic(Skill skill, boolean simultaneously, Creature target, GameObject[] targets) {
      if (this.hasListeners()) {
         for(ISkillUseEventListener listener : this.getEventListeners(ISkillUseEventListener.class)) {
            try {
               if (!listener.getClass().isAnnotationPresent(PlayerOnly.class) || target.isPlayer()) {
                  SkillId skillIdA = listener.getClass().getAnnotation(SkillId.class);
                  if (skillIdA == null || Util.contains(skillIdA.value(), skill.getId())) {
                     SkillLevel skillLevelA = listener.getClass().getAnnotation(SkillLevel.class);
                     if ((skillLevelA == null || Util.contains(skillLevelA.value(), skill.getLevel()))
                        && !listener.onSkillUse(this.getActingPlayer(), skill, simultaneously, target, targets)) {
                        return false;
                     }
                  }
               }
            } catch (Exception var9) {
               _log.log(Level.WARNING, this.getClass().getSimpleName() + ": Exception caught: ", (Throwable)var9);
            }
         }
      }

      return true;
   }

   public boolean onDeath(Creature killer) {
      if (this.hasListeners()) {
         for(IDeathEventListener listener : this.getEventListeners(IDeathEventListener.class)) {
            try {
               if ((!listener.getClass().isAnnotationPresent(PlayerOnly.class) || killer.isPlayer()) && !listener.onDeath(killer, this.getActingPlayer())) {
                  return false;
               }
            } catch (Exception var5) {
               _log.log(Level.WARNING, this.getClass().getSimpleName() + ": Exception caught: ", (Throwable)var5);
            }
         }
      }

      return true;
   }

   public void onDamageDealt(double damage, Creature target, Skill skill, boolean crit, boolean damageOverTime) {
      if (this.hasListeners()) {
         for(IDamageDealtEventListener listener : this.getEventListeners(IDamageDealtEventListener.class)) {
            try {
               if (!listener.getClass().isAnnotationPresent(PlayerOnly.class) || target.isPlayer()) {
                  listener.onDamageDealtEvent(this.getActingPlayer(), target, damage, skill, crit, damageOverTime);
               }
            } catch (Exception var10) {
               _log.log(Level.WARNING, this.getClass().getSimpleName() + ": Exception caught: ", (Throwable)var10);
            }
         }
      }
   }

   public void onDamageReceived(double damage, Creature attacker, Skill skill, boolean crit, boolean damageOverTime) {
      if (this.hasListeners()) {
         for(IDamageReceivedEventListener listener : this.getEventListeners(IDamageReceivedEventListener.class)) {
            try {
               if (!listener.getClass().isAnnotationPresent(PlayerOnly.class) || attacker.isPlayer()) {
                  listener.onDamageReceivedEvent(attacker, this.getActingPlayer(), damage, skill, crit, damageOverTime);
               }
            } catch (Exception var10) {
               _log.log(Level.WARNING, this.getClass().getSimpleName() + ": Exception caught: ", (Throwable)var10);
            }
         }
      }
   }

   public void onTeleported() {
      if (this.hasListeners()) {
         for(ITeleportedEventListener listener : this.getEventListeners(ITeleportedEventListener.class)) {
            try {
               listener.onTeleported(this.getActingPlayer());
            } catch (Exception var4) {
               _log.log(Level.WARNING, this.getClass().getSimpleName() + ": Exception caught: ", (Throwable)var4);
            }
         }
      }
   }

   public Creature getActingPlayer() {
      return this._activeChar;
   }
}
