package l2e.scripts.ai;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import l2e.commons.util.NpcUtils;
import l2e.commons.util.Rnd;
import l2e.gameserver.ai.model.CtrlEvent;
import l2e.gameserver.ai.npc.Fighter;
import l2e.gameserver.data.parser.SkillsParser;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Playable;
import l2e.gameserver.model.actor.instance.MonsterInstance;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.zone.ZoneId;
import l2e.gameserver.network.NpcStringId;
import l2e.scripts.ai.grandboss.ValakasManager;

public class Valakas extends Fighter {
   final Skill _lavaSkill = this.getSkill(4680, 1);
   final Skill _fearSkill = this.getSkill(4689, 1);
   final Skill _defSkill = this.getSkill(5864, 1);
   final Skill _berserkSkill = this.getSkill(5865, 1);
   final Skill _trempleSkill = this.getSkill(4681, 1);
   final Skill _tremplerSkill = this.getSkill(4682, 1);
   final Skill _tailSkill = this.getSkill(4685, 1);
   final Skill _taillSkill = this.getSkill(4688, 1);
   final Skill _meteorSkill = this.getSkill(4690, 1);
   final Skill _breathlSkill = this.getSkill(4683, 1);
   final Skill _breathhSkill = this.getSkill(4684, 1);
   final Skill _destroySkill = this.getSkill(5860, 1);
   final Skill _destroysSkill = this.getSkill(5861, 1);
   private long _defenceDownTimer = Long.MAX_VALUE;
   private final long _defenceDownReuse = 120000L;
   private double _rangedAttacksIndex;
   private double _counterAttackIndex;
   private double _attacksIndex;
   private int _hpStage = 0;
   private final List<MonsterInstance> _minions = new ArrayList<>();

   public Valakas(Attackable actor) {
      super(actor);
   }

   @Override
   protected void onEvtAttacked(Creature attacker, int damage) {
      Attackable actor = this.getActiveChar();
      if (actor != null) {
         for(Playable p : ValakasManager.getZone().getPlayersInside()) {
            this.notifyEvent(CtrlEvent.EVT_AGGRESSION, p, Integer.valueOf(1));
         }

         if (damage > 100) {
            if (attacker.getDistance(actor) > 400.0) {
               this._rangedAttacksIndex += (double)damage / 1000.0;
            } else {
               this._counterAttackIndex += (double)damage / 1000.0;
            }
         }

         this._attacksIndex += (double)damage / 1000.0;
         super.onEvtAttacked(attacker, damage);
      }
   }

   @Override
   protected boolean createNewTask() {
      Attackable actor = this.getActiveChar();
      if (actor != null && !actor.isDead()) {
         GameObject target = actor.getTarget();
         if (target == null) {
            return false;
         } else if (actor.isMoving()) {
            return true;
         } else {
            double distance = actor.getDistance(target);
            double chp = actor.getCurrentHpPercents();
            if (this._hpStage == 0) {
               actor.makeTriggerCast(this.getSkill(4691, 1), actor);
               this._hpStage = 1;
            } else if (chp < 80.0 && this._hpStage == 1) {
               actor.makeTriggerCast(this.getSkill(4691, 2), actor);
               this._defenceDownTimer = System.currentTimeMillis();
               this._hpStage = 2;
            } else if (chp < 50.0 && this._hpStage == 2) {
               actor.makeTriggerCast(this.getSkill(4691, 3), actor);
               this._hpStage = 3;
            } else if (chp < 30.0 && this._hpStage == 3) {
               actor.makeTriggerCast(this.getSkill(4691, 4), actor);
               this._hpStage = 4;
            } else if (chp < 10.0 && this._hpStage == 4) {
               actor.makeTriggerCast(this.getSkill(4691, 5), actor);
               this._hpStage = 5;
            }

            if (this.getAliveMinionsCount() < 12 && Rnd.chance(2)) {
               MonsterInstance minion = NpcUtils.spawnSingle(29029, Location.findPointToStay(actor.getLocation(), 400, 700, actor.getGeoIndex(), false));
               this._minions.add(minion);
               ValakasManager.addValakasMinion(minion);
            }

            if (this._counterAttackIndex > 2000.0) {
               ValakasManager.broadcastScreenMessage(
                  NpcStringId.BECAUSE_THE_COWARDLY_COUNTERATTACKS_CONTINUED_VALAKASS_FURY_HAS_REACHED_ITS_MAXIMUMNVALAKASS_P_ATK_IS_GREATLY_INCREASED
               );
               this._counterAttackIndex = 0.0;
               return this.cast(this._berserkSkill);
            } else {
               if (this._rangedAttacksIndex > 2000.0) {
                  if (!Rnd.chance(60)) {
                     ValakasManager.broadcastScreenMessage(
                        NpcStringId.LONG_RANGE_CONCENTRATION_ATTACKS_HAVE_ENRAGED_VALAKASNIF_YOU_CONTINUE_IT_MAY_BECOME_A_DANGEROUS_SITUATION
                     );
                     this._rangedAttacksIndex = 0.0;
                     return this.cast(this._berserkSkill);
                  }

                  this.aggroReconsider();
                  ValakasManager.broadcastScreenMessage(
                     NpcStringId.VALAKAS_HAS_BEEN_ENRAGED_BY_THE_LONG_RANGE_CONCENTRATION_ATTACKS_AND_IS_COMING_TOWARD_ITS_TARGET_WITH_EVEN_GREATER_ZEAL
                  );
                  this._rangedAttacksIndex = 0.0;
               } else {
                  if (this._attacksIndex > 3000.0) {
                     ValakasManager.broadcastScreenMessage(
                        NpcStringId.SOME_WARRIORS_BLOW_HAS_LEFT_A_HUGE_GASH_BETWEEN_THE_GREAT_SCALES_OF_VALAKASNVALAKASS_P_DEF_IS_GREATLY_DECREASED
                     );
                     this._attacksIndex = 0.0;
                     return this.cast(this._defSkill);
                  }

                  if (this._defenceDownTimer < System.currentTimeMillis()) {
                     ValakasManager.broadcastScreenMessage(
                        NpcStringId.ANNOYING_CONCENTRATION_ATTACKS_ARE_DISRUPTING_VALAKASS_CONCENTRATIONNIF_IT_CONTINUES_YOU_MAY_GET_A_GREAT_OPPORTUNITY
                     );
                     this._defenceDownTimer = System.currentTimeMillis() + 120000L + (long)Rnd.get(60) * 1000L;
                     return this.cast(this._fearSkill);
                  }
               }

               if (Rnd.chance(30)) {
                  return this.cast(Rnd.chance(50) ? this._trempleSkill : this._tremplerSkill);
               } else {
                  Map<Skill, Integer> d_skill = new HashMap<>();
                  switch(this._hpStage) {
                     case 1:
                        this.addDesiredSkill(d_skill, (Creature)target, distance, this._breathlSkill);
                        this.addDesiredSkill(d_skill, (Creature)target, distance, this._tailSkill);
                        this.addDesiredSkill(d_skill, (Creature)target, distance, this._meteorSkill);
                        this.addDesiredSkill(d_skill, (Creature)target, distance, this._defSkill);
                        break;
                     case 2:
                     case 3:
                        this.addDesiredSkill(d_skill, (Creature)target, distance, this._breathlSkill);
                        this.addDesiredSkill(d_skill, (Creature)target, distance, this._tailSkill);
                        this.addDesiredSkill(d_skill, (Creature)target, distance, this._breathhSkill);
                        this.addDesiredSkill(d_skill, (Creature)target, distance, this._taillSkill);
                        this.addDesiredSkill(d_skill, (Creature)target, distance, this._destroySkill);
                        this.addDesiredSkill(d_skill, (Creature)target, distance, this._destroysSkill);
                        this.addDesiredSkill(d_skill, (Creature)target, distance, this._meteorSkill);
                        this.addDesiredSkill(d_skill, (Creature)target, distance, this._fearSkill);
                        break;
                     case 4:
                     case 5:
                        this.addDesiredSkill(d_skill, (Creature)target, distance, this._breathlSkill);
                        this.addDesiredSkill(d_skill, (Creature)target, distance, this._tailSkill);
                        this.addDesiredSkill(d_skill, (Creature)target, distance, this._breathhSkill);
                        this.addDesiredSkill(d_skill, (Creature)target, distance, this._taillSkill);
                        this.addDesiredSkill(d_skill, (Creature)target, distance, this._destroySkill);
                        this.addDesiredSkill(d_skill, (Creature)target, distance, this._destroysSkill);
                        this.addDesiredSkill(d_skill, (Creature)target, distance, this._meteorSkill);
                        this.addDesiredSkill(d_skill, (Creature)target, distance, this._fearSkill);
                  }

                  if (Rnd.chance(20)) {
                     Skill r_skill = this.selectTopSkill(d_skill);
                     if (r_skill != null) {
                        if (!r_skill.isOffensive()) {
                           ;
                        }

                        return this.cast(r_skill);
                     }
                  }

                  return false;
               }
            }
         }
      } else {
         return false;
      }
   }

   @Override
   protected void thinkAttack() {
      Attackable actor = this.getActiveChar();
      if (actor != null) {
         if (actor.isInsideZone(ZoneId.DANGER_AREA) && actor.getFirstEffect(this._lavaSkill) == null) {
            actor.makeTriggerCast(this._lavaSkill, actor);
         }

         super.thinkAttack();
      }
   }

   private Skill getSkill(int id, int level) {
      return SkillsParser.getInstance().getInfo(id, level);
   }

   private int getAliveMinionsCount() {
      int i = 0;

      for(MonsterInstance n : this._minions) {
         if (n != null && !n.isDead() && n.getDistance(this._actor) < 8000.0) {
            ++i;
         }
      }

      return i;
   }
}
