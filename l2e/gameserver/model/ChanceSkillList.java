package l2e.gameserver.model;

import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import l2e.gameserver.data.parser.SkillsParser;
import l2e.gameserver.handler.skillhandlers.ISkillHandler;
import l2e.gameserver.handler.skillhandlers.SkillHandler;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Playable;
import l2e.gameserver.model.interfaces.IChanceSkillTrigger;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.skills.SkillType;
import l2e.gameserver.model.skills.effects.Effect;
import l2e.gameserver.model.skills.targets.TargetType;
import l2e.gameserver.network.serverpackets.MagicSkillLaunched;
import l2e.gameserver.network.serverpackets.MagicSkillUse;

public class ChanceSkillList extends ConcurrentHashMap<IChanceSkillTrigger, ChanceCondition> {
   protected static final Logger _log = Logger.getLogger(ChanceSkillList.class.getName());
   private static final long serialVersionUID = 1L;
   private final Creature _owner;

   public ChanceSkillList(Creature owner) {
      this._owner = owner;
   }

   public Creature getOwner() {
      return this._owner;
   }

   public void onHit(Creature target, int damage, boolean ownerWasHit, boolean wasCrit) {
      int event;
      if (ownerWasHit) {
         event = 384;
         if (wasCrit) {
            event |= 512;
         }
      } else {
         event = 1;
         if (wasCrit) {
            event |= 2;
         }
      }

      this.onEvent(event, damage, target, null, (byte)-1);
   }

   public void onEvadedHit(Creature attacker) {
      this.onEvent(8192, 0, attacker, null, (byte)-1);
   }

   public void onSkillHit(Creature target, double damage, Skill skill, boolean ownerWasHit) {
      int event;
      if (ownerWasHit) {
         event = 1024;
         if (skill.isOffensive()) {
            event |= 2048;
            event |= 128;
            event |= 256;
         } else {
            event |= 4096;
         }
      } else {
         event = 4;
         event |= skill.isMagic() ? 16 : 8;
         event |= skill.isOffensive() ? 64 : 32;
      }

      this.onEvent(event, (int)damage, target, skill, skill.getElement());
   }

   public void onStart(byte element) {
      this.onEvent(16384, 0, this._owner, null, element);
   }

   public void onActionTime(byte element) {
      this.onEvent(32768, 0, this._owner, null, element);
   }

   public void onExit(byte element) {
      this.onEvent(65536, 0, this._owner, null, element);
   }

   public void onEvent(int event, int damage, Creature target, Skill skill, byte element) {
      if (!this._owner.isDead()) {
         boolean playable = target instanceof Playable;

         for(Entry<IChanceSkillTrigger, ChanceCondition> entry : this.entrySet()) {
            IChanceSkillTrigger trigger = entry.getKey();
            ChanceCondition cond = entry.getValue();
            if (cond != null && cond.trigger(event, damage, element, playable, skill)) {
               if (trigger instanceof Skill) {
                  this._owner.makeTriggerCast((Skill)trigger, target);
               } else {
                  this.makeCast((Effect)trigger, target);
               }
            }
         }
      }
   }

   private void makeCast(Effect effect, Creature target) {
      try {
         if (effect == null || !effect.triggersChanceSkill()) {
            return;
         }

         Skill triggered = SkillsParser.getInstance().getInfo(effect.getTriggeredChanceId(), effect.getTriggeredChanceLevel());
         if (triggered == null) {
            return;
         }

         Creature caster = triggered.getTargetType() == TargetType.SELF ? this._owner : effect.getEffector();
         if (caster == null || triggered.getSkillType() == SkillType.NOTDONE || caster.isSkillDisabled(triggered) || caster.isSkillBlocked(triggered)) {
            return;
         }

         if (triggered.getReuseDelay() > 0) {
            caster.disableSkill(triggered, (long)triggered.getReuseDelay());
         }

         GameObject[] targets = triggered.getTargetList(caster, false, target);
         if (targets.length == 0) {
            return;
         }

         Creature firstTarget = (Creature)targets[0];
         ISkillHandler handler = SkillHandler.getInstance().getHandler(triggered.getSkillType());
         this._owner.broadcastPacket(new MagicSkillLaunched(this._owner, triggered.getDisplayId(), triggered.getDisplayLevel(), targets));
         this._owner.broadcastPacket(new MagicSkillUse(this._owner, firstTarget, triggered.getDisplayId(), triggered.getDisplayLevel(), 0, 0));
         if (handler != null) {
            handler.useSkill(caster, triggered, targets);
         } else {
            triggered.useSkill(caster, targets);
         }
      } catch (Exception var8) {
         _log.log(Level.WARNING, "", (Throwable)var8);
      }
   }
}
