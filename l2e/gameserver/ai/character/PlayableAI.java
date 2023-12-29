package l2e.gameserver.ai.character;

import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Playable;
import l2e.gameserver.model.entity.events.AbstractFightEvent;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.zone.ZoneId;
import l2e.gameserver.network.SystemMessageId;

public abstract class PlayableAI extends CharacterAI {
   public PlayableAI(Playable playable) {
      super(playable);
   }

   @Override
   protected void onIntentionAttack(Creature target) {
      if (target instanceof Playable) {
         if (this._actor.isInFightEvent() && target.isInFightEvent()) {
            for(AbstractFightEvent e : this._actor.getFightEvents()) {
               if (e != null && !e.canAttack(target, this._actor)) {
                  this.clientActionFailed();
                  return;
               }
            }
         }

         if (target.getActingPlayer().isProtectionBlessingAffected()
            && this._actor.getActingPlayer().getLevel() - target.getActingPlayer().getLevel() >= 10
            && this._actor.getActingPlayer().getKarma() > 0
            && !target.isInsideZone(ZoneId.PVP)) {
            this._actor.getActingPlayer().sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
            this.clientActionFailed();
            return;
         }

         if (this._actor.getActingPlayer().isProtectionBlessingAffected()
            && target.getActingPlayer().getLevel() - this._actor.getActingPlayer().getLevel() >= 10
            && target.getActingPlayer().getKarma() > 0
            && !target.isInsideZone(ZoneId.PVP)) {
            this._actor.getActingPlayer().sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
            this.clientActionFailed();
            return;
         }

         if (target.getActingPlayer().isCursedWeaponEquipped() && this._actor.getActingPlayer().getLevel() <= 20) {
            this._actor.getActingPlayer().sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
            this.clientActionFailed();
            return;
         }

         if (this._actor.getActingPlayer().isCursedWeaponEquipped() && target.getActingPlayer().getLevel() <= 20) {
            this._actor.getActingPlayer().sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
            this.clientActionFailed();
            return;
         }
      }

      super.onIntentionAttack(target);
   }

   @Override
   protected void onIntentionCast(Skill skill, GameObject target) {
      if (target instanceof Playable && skill.isOffensive()) {
         if (this._actor.isInFightEvent() && target.getActingPlayer() != null && target.getActingPlayer().isInFightEvent()) {
            for(AbstractFightEvent e : this._actor.getFightEvents()) {
               if (e != null && !e.canUseMagic(target.getActingPlayer(), this._actor, skill)) {
                  this.clientActionFailed();
                  this._actor.setIsCastingNow(false);
                  return;
               }
            }
         }

         if (target.getActingPlayer().isProtectionBlessingAffected()
            && this._actor.getActingPlayer().getLevel() - target.getActingPlayer().getLevel() >= 10
            && this._actor.getActingPlayer().getKarma() > 0
            && !target.isInsideZone(ZoneId.PVP)) {
            this._actor.getActingPlayer().sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
            this.clientActionFailed();
            this._actor.setIsCastingNow(false);
            return;
         }

         if (this._actor.getActingPlayer().isProtectionBlessingAffected()
            && target.getActingPlayer().getLevel() - this._actor.getActingPlayer().getLevel() >= 10
            && target.getActingPlayer().getKarma() > 0
            && !target.isInsideZone(ZoneId.PVP)) {
            this._actor.getActingPlayer().sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
            this.clientActionFailed();
            this._actor.setIsCastingNow(false);
            return;
         }

         if (target.getActingPlayer().isCursedWeaponEquipped() && this._actor.getActingPlayer().getLevel() <= 20) {
            this._actor.getActingPlayer().sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
            this.clientActionFailed();
            this._actor.setIsCastingNow(false);
            return;
         }

         if (this._actor.getActingPlayer().isCursedWeaponEquipped() && target.getActingPlayer().getLevel() <= 20) {
            this._actor.getActingPlayer().sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
            this.clientActionFailed();
            this._actor.setIsCastingNow(false);
            return;
         }
      }

      super.onIntentionCast(skill, target);
   }
}
