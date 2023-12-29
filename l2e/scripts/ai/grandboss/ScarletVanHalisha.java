package l2e.scripts.ai.grandboss;

import java.util.ArrayList;
import l2e.commons.util.Rnd;
import l2e.commons.util.Util;
import l2e.gameserver.ai.model.CtrlIntention;
import l2e.gameserver.data.parser.SkillsParser;
import l2e.gameserver.geodata.GeoEngine;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.instance.DecoyInstance;
import l2e.gameserver.model.skills.Skill;
import l2e.scripts.ai.AbstractNpcAI;

public final class ScarletVanHalisha extends AbstractNpcAI {
   private Creature _target;
   private Skill _skill;
   private long _lastRangedSkillTime;
   private final int _rangedSkillMinCoolTime = 60000;
   private static final int HALISHA2 = 29046;
   private static final int HALISHA3 = 29047;

   public ScarletVanHalisha(String name, String descr) {
      super(name, descr);
      this.addAttackId(new int[]{29046, 29047});
      this.addKillId(new int[]{29046, 29047});
      this.addSpellFinishedId(new int[]{29046, 29047});
      this.registerMobs(new int[]{29046, 29047});
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      switch(event) {
         case "attack":
            if (npc != null) {
               this.getSkillAI(npc);
            }
            break;
         case "random_target":
            this._target = this.getRandomTarget(npc, null);
      }

      return super.onAdvEvent(event, npc, player);
   }

   @Override
   public String onSpellFinished(Npc npc, Player player, Skill skill) {
      this.getSkillAI(npc);
      return super.onSpellFinished(npc, player, skill);
   }

   @Override
   public String onAttack(Npc npc, Player attacker, int damage, boolean isSummon) {
      this.startQuestTimer("random_Target", 5000L, npc, null, true);
      this.startQuestTimer("attack", 500L, npc, null, true);
      return super.onAttack(npc, attacker, damage, isSummon);
   }

   @Override
   public String onKill(Npc npc, Player killer, boolean isSummon) {
      this.cancelQuestTimers("attack");
      this.cancelQuestTimers("random_Target");
      return super.onKill(npc, killer, isSummon);
   }

   private Skill getRndSkills(Npc npc) {
      switch(npc.getId()) {
         case 29046:
            if (Rnd.get(100) < 10) {
               return SkillsParser.getInstance().getInfo(5015, 2);
            } else if (Rnd.get(100) < 10) {
               return SkillsParser.getInstance().getInfo(5015, 5);
            } else {
               if (Rnd.get(100) < 2) {
                  return SkillsParser.getInstance().getInfo(5016, 1);
               }

               return SkillsParser.getInstance().getInfo(5014, 2);
            }
         case 29047:
            if (Rnd.get(100) < 10) {
               return SkillsParser.getInstance().getInfo(5015, 3);
            } else if (Rnd.get(100) < 10) {
               return SkillsParser.getInstance().getInfo(5015, 6);
            } else if (Rnd.get(100) < 10) {
               return SkillsParser.getInstance().getInfo(5015, 2);
            } else if (this._lastRangedSkillTime + 60000L < System.currentTimeMillis() && Rnd.get(100) < 10) {
               return SkillsParser.getInstance().getInfo(5019, 1);
            } else if (this._lastRangedSkillTime + 60000L < System.currentTimeMillis() && Rnd.get(100) < 10) {
               return SkillsParser.getInstance().getInfo(5018, 1);
            } else {
               if (Rnd.get(100) < 2) {
                  return SkillsParser.getInstance().getInfo(5016, 1);
               }

               return SkillsParser.getInstance().getInfo(5014, 3);
            }
         default:
            return SkillsParser.getInstance().getInfo(5014, 1);
      }
   }

   private synchronized void getSkillAI(Npc npc) {
      if (!npc.isInvul() && !npc.isCastingNow()) {
         if (Rnd.get(100) < 30 || this._target == null || this._target.isDead()) {
            this._skill = this.getRndSkills(npc);
            this._target = this.getRandomTarget(npc, this._skill);
         }

         Creature target = this._target;
         Skill skill = this._skill;
         if (skill == null) {
            skill = this.getRndSkills(npc);
         }

         if (!npc.isPhysicalMuted()) {
            if (target != null && !target.isDead()) {
               if (Util.checkIfInRange(skill.getCastRange(), npc, target, true)) {
                  npc.getAI().setIntention(CtrlIntention.IDLE);
                  npc.setTarget(target);
                  npc.setIsCastingNow(true);
                  this._target = null;
                  npc.doCast(skill);
               } else {
                  npc.getAI().setIntention(CtrlIntention.FOLLOW, target, null);
                  npc.getAI().setIntention(CtrlIntention.ATTACK, target, null);
                  npc.setIsCastingNow(false);
               }
            } else {
               npc.setIsCastingNow(false);
            }
         }
      }
   }

   private Creature getRandomTarget(Npc npc, Skill skill) {
      ArrayList<Creature> result = new ArrayList<>();

      for(Creature obj : World.getInstance().getAroundCharacters(npc, 600, 200)) {
         if ((
               !obj.isPlayable() && !(obj instanceof DecoyInstance)
                  || (!obj.isPlayer() || !obj.getActingPlayer().isInvisible())
                     && (obj.getZ() >= npc.getZ() - 100 || obj.getZ() <= npc.getZ() + 100)
                     && GeoEngine.canSeeTarget(obj, npc, false)
            )
            && (obj.isPlayable() || obj instanceof DecoyInstance)) {
            int skillRange = 150;
            if (skill != null) {
               switch(skill.getId()) {
                  case 5014:
                     skillRange = 150;
                     break;
                  case 5015:
                     skillRange = 400;
                     break;
                  case 5016:
                     skillRange = 200;
                  case 5017:
                  default:
                     break;
                  case 5018:
                  case 5019:
                     this._lastRangedSkillTime = System.currentTimeMillis();
                     skillRange = 550;
               }
            }

            if (Util.checkIfInRange(skillRange, npc, obj, true) && !obj.isDead()) {
               result.add(obj);
            }
         }
      }

      if (!result.isEmpty() && result.size() != 0) {
         Object[] characters = result.toArray();
         return (Creature)characters[Rnd.get(characters.length)];
      } else {
         return null;
      }
   }

   public static void main(String[] args) {
      new ScarletVanHalisha(ScarletVanHalisha.class.getSimpleName(), "ai");
   }
}
