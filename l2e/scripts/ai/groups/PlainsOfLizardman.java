package l2e.scripts.ai.groups;

import l2e.gameserver.ai.model.CtrlIntention;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.holders.SkillHolder;
import l2e.scripts.ai.AbstractNpcAI;

public final class PlainsOfLizardman extends AbstractNpcAI {
   private static final int INVISIBLE_NPC = 18919;
   private static final int TANTA_GUARD = 18862;
   private static final int FANTASY_MUSHROOM = 18864;
   private static final int STICKY_MUSHROOM = 18865;
   private static final int RAINBOW_FROG = 18866;
   private static final int ENERGY_PLANT = 18868;
   private static final int TANTA_SCOUT = 22768;
   private static final int TANTA_MAGICIAN = 22773;
   private static final int TANTA_SUMMONER = 22774;
   private static final int[] MOBS = new int[]{22768, 22769, 22770, 22771, 22772, 22773, 22774};
   private static final SkillHolder STUN_EFFECT = new SkillHolder(6622, 1);
   private static final SkillHolder DEMOTIVATION_HEX = new SkillHolder(6425, 1);
   private static final SkillHolder FANTASY_MUSHROOM_SKILL = new SkillHolder(6427, 1);
   private static final SkillHolder RAINBOW_FROG_SKILL = new SkillHolder(6429, 1);
   private static final SkillHolder STICKY_MUSHROOM_SKILL = new SkillHolder(6428, 1);
   private static final SkillHolder ENERGY_PLANT_SKILL = new SkillHolder(6430, 1);
   private static final SkillHolder[] BUFFS = new SkillHolder[]{
      new SkillHolder(6625, 1),
      new SkillHolder(6626, 2),
      new SkillHolder(6627, 3),
      new SkillHolder(6628, 1),
      new SkillHolder(6629, 2),
      new SkillHolder(6630, 3),
      new SkillHolder(6631, 1),
      new SkillHolder(6633, 1),
      new SkillHolder(6635, 1),
      new SkillHolder(6636, 1),
      new SkillHolder(6638, 1),
      new SkillHolder(6639, 1),
      new SkillHolder(6640, 1),
      new SkillHolder(6674, 1)
   };
   private static final int[] BUFF_LIST = new int[]{6, 7, 8, 11, 13};

   private PlainsOfLizardman(String name, String descr) {
      super(name, descr);
      this.addAttackId(new int[]{18864, 18866, 18865, 18868, 22774});
      this.addKillId(MOBS);
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      if (event.equals("fantasy_mushroom") && npc != null && player != null) {
         npc.doCast(FANTASY_MUSHROOM_SKILL.getSkill());

         for(Npc target : World.getInstance().getAroundNpc(npc, 200, 200)) {
            if (target != null && target.isAttackable()) {
               Attackable monster = (Attackable)target;
               npc.setTarget(monster);
               npc.doCast(STUN_EFFECT.getSkill());
               this.attackPlayer(monster, player);
            }
         }

         npc.doDie(player);
      }

      return null;
   }

   @Override
   public String onAttack(Npc npc, Player attacker, int damage, boolean isSummon) {
      switch(npc.getId()) {
         case 18864:
            if (npc.isScriptValue(0)) {
               npc.setScriptValue(1);
               npc.setIsInvul(true);

               for(Npc target : World.getInstance().getAroundNpc(npc, 1000, 200)) {
                  if (target != null && target.isAttackable()) {
                     Attackable monster = (Attackable)target;
                     if (monster.getId() == 22773 || monster.getId() == 22768) {
                        target.setIsRunning(true);
                        target.getAI().setIntention(CtrlIntention.MOVING, new Location(npc.getX(), npc.getY(), npc.getZ(), 0));
                     }
                  }
               }

               this.startQuestTimer("fantasy_mushroom", 4000L, npc, attacker);
            }
            break;
         case 18865:
            this.castSkill(npc, attacker, STICKY_MUSHROOM_SKILL);
            break;
         case 18866:
            this.castSkill(npc, attacker, RAINBOW_FROG_SKILL);
            break;
         case 18868:
            this.castSkill(npc, attacker, ENERGY_PLANT_SKILL);
            break;
         case 22774:
            if (npc.getFirstEffect(DEMOTIVATION_HEX.getId()) == null) {
               npc.doCast(DEMOTIVATION_HEX.getSkill());
            }
      }

      return super.onAttack(npc, attacker, damage, isSummon);
   }

   @Override
   public String onKill(Npc npc, Player killer, boolean isSummon) {
      if (getRandom(1000) == 0) {
         Npc guard = addSpawn(18862, npc);
         this.attackPlayer((Attackable)guard, killer);
      }

      int random = getRandom(100);
      Npc buffer = addSpawn(18919, npc.getLocation(), false, 6000L);
      buffer.setTarget(killer);
      if (random <= 42) {
         this.castRandomBuff(buffer, 7, 45, BUFFS[0], BUFFS[1], BUFFS[2]);
      }

      if (random <= 11) {
         this.castRandomBuff(buffer, 8, 60, BUFFS[3], BUFFS[4], BUFFS[5]);
         this.castRandomBuff(buffer, 3, 6, BUFFS[9], BUFFS[10], BUFFS[12]);
      }

      if (random <= 25) {
         buffer.doCast(BUFFS[BUFF_LIST[getRandom(BUFF_LIST.length)]].getSkill());
      }

      if (random <= 10) {
         buffer.doCast(BUFFS[13].getSkill());
      }

      if (random <= 1) {
         int i = getRandom(100);
         if (i <= 34) {
            buffer.doCast(BUFFS[6].getSkill());
            buffer.doCast(BUFFS[7].getSkill());
            buffer.doCast(BUFFS[8].getSkill());
         } else if (i < 67) {
            buffer.doCast(BUFFS[13].getSkill());
         } else {
            buffer.doCast(BUFFS[2].getSkill());
            buffer.doCast(BUFFS[5].getSkill());
         }
      }

      return super.onKill(npc, killer, isSummon);
   }

   private void castRandomBuff(Npc npc, int chance1, int chance2, SkillHolder... buffs) {
      int rand = getRandom(100);
      if (rand <= chance1) {
         npc.doCast(buffs[2].getSkill());
      } else if (rand <= chance2) {
         npc.doCast(buffs[1].getSkill());
      } else {
         npc.doCast(buffs[0].getSkill());
      }
   }

   private void castSkill(Npc npc, Creature target, SkillHolder skill) {
      npc.doDie(target);
      Npc buffer = addSpawn(18919, npc.getLocation(), false, 6000L);
      buffer.setTarget(target);
      buffer.doCast(skill.getSkill());
   }

   public static void main(String[] args) {
      new PlainsOfLizardman(PlainsOfLizardman.class.getSimpleName(), "ai");
   }
}
