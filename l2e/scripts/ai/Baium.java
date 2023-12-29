package l2e.scripts.ai;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import l2e.commons.util.Rnd;
import l2e.gameserver.ai.DefaultAI;
import l2e.gameserver.data.parser.SkillsParser;
import l2e.gameserver.geodata.GeoEngine;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.skills.Skill;

public class Baium extends DefaultAI {
   private long _newTarget = 0L;
   private final Skill _normal_attack = SkillsParser.getInstance().getInfo(4127, 1);
   private final Skill _energy_wave = SkillsParser.getInstance().getInfo(4128, 1);
   private final Skill _earth_quake = SkillsParser.getInstance().getInfo(4129, 1);
   private final Skill _thunderbolt = SkillsParser.getInstance().getInfo(4130, 1);
   private final Skill _group_hold = SkillsParser.getInstance().getInfo(4131, 1);

   public Baium(Attackable actor) {
      super(actor);
      actor.setIsGlobalAI(true);
   }

   @Override
   protected void onEvtSpawn() {
      this._newTarget = System.currentTimeMillis();
      super.onEvtSpawn();
   }

   @Override
   protected void thinkAttack() {
      Attackable actor = this.getActiveChar();
      if (actor != null) {
         if (this._newTarget + 20000L < System.currentTimeMillis()) {
            List<Creature> alive = new ArrayList<>();

            for(Creature target : World.getInstance().getAroundCharacters(actor, 4000, 200)) {
               if (target != null
                  && !target.isDead()
                  && !target.isInvisible()
                  && GeoEngine.canSeeTarget(actor, target, false)
                  && (target.getId() != 29021 || Rnd.get(100) > 50)) {
                  alive.add(target);
               }
            }

            if (alive == null || alive.isEmpty()) {
               super.thinkAttack();
               return;
            }

            Creature rndTarget = alive.get(Rnd.get(alive.size()));
            if (rndTarget != null && (rndTarget.getId() == 29021 || rndTarget.isPlayer())) {
               Creature mostHate = actor.getMostHated();
               if (mostHate != null) {
                  actor.addDamageHate(rndTarget, 0, actor.getHating(mostHate) + 500);
               } else {
                  actor.addDamageHate(rndTarget, 0, 2000);
               }

               actor.setTarget(rndTarget);
               this.setAttackTarget(rndTarget);
            }

            this._newTarget = System.currentTimeMillis();
         }

         super.thinkAttack();
      }
   }

   @Override
   protected boolean createNewTask() {
      Attackable actor = this.getActiveChar();
      if (actor == null) {
         return true;
      } else {
         GameObject target = actor.getTarget();
         if (target == null) {
            return false;
         } else if (actor.isMoving()) {
            return true;
         } else {
            int energy_wave = 20;
            int earth_quake = 20;
            int group_hold = actor.getCurrentHpPercents() > 50.0 ? 0 : 20;
            int thunderbolt = actor.getCurrentHpPercents() > 25.0 ? 0 : 20;
            Skill select = null;
            if (actor.isMovementDisabled()) {
               select = this._thunderbolt;
               return this.cast(select);
            } else {
               if (!Rnd.chance(100 - thunderbolt - group_hold - 20 - 20)) {
                  Map<Skill, Integer> skillList = new HashMap<>();
                  double distance = actor.getDistance(target);
                  this.addDesiredSkill(skillList, (Creature)target, distance, this._energy_wave);
                  this.addDesiredSkill(skillList, (Creature)target, distance, this._earth_quake);
                  if (group_hold > 0) {
                     this.addDesiredSkill(skillList, (Creature)target, distance, this._group_hold);
                  }

                  if (thunderbolt > 0) {
                     this.addDesiredSkill(skillList, (Creature)target, distance, this._thunderbolt);
                  }

                  select = this.selectTopSkill(skillList);
               }

               if (Rnd.chance(20)) {
                  if (select == null) {
                     select = this._normal_attack;
                  }

                  return this.cast(select);
               } else {
                  return false;
               }
            }
         }
      }
   }
}
