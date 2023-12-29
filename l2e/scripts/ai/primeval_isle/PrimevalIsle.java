package l2e.scripts.ai.primeval_isle;

import l2e.commons.util.Util;
import l2e.gameserver.ai.model.CtrlIntention;
import l2e.gameserver.data.parser.SpawnParser;
import l2e.gameserver.geodata.GeoEngine;
import l2e.gameserver.handler.itemhandlers.IItemHandler;
import l2e.gameserver.handler.itemhandlers.ItemHandler;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Playable;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.holders.SkillHolder;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.spawn.Spawner;
import l2e.gameserver.network.NpcStringId;
import l2e.scripts.ai.AbstractNpcAI;

public final class PrimevalIsle extends AbstractNpcAI {
   private static final int EGG = 18344;
   private static final int SAILREN = 29065;
   private static final int ORNIT = 22742;
   private static final int DEINO = 22743;
   private static final int[] SPRIGNANT = new int[]{18345, 18346};
   private static final int[] MONSTERS = new int[]{
      22196, 22198, 22200, 22202, 22203, 22205, 22208, 22210, 22211, 22213, 22223, 22224, 22225, 22226, 22227, 22742, 22743
   };
   private static final int[] TREX = new int[]{22215, 22216, 22217};
   private static final int[] VEGETABLE = new int[]{22200, 22201, 22202, 22203, 22204, 22205, 22224, 22225};
   private static final int DEINONYCHUS = 14828;
   private static final SkillHolder SELFBUFF1 = new SkillHolder(5087, 1);
   private static final SkillHolder SELFBUFF2 = new SkillHolder(5087, 2);
   private static final SkillHolder LONGRANGEDMAGIC1 = new SkillHolder(5120, 1);
   private static final SkillHolder PHYSICALSPECIAL1 = new SkillHolder(5083, 4);
   private static final SkillHolder PHYSICALSPECIAL2 = new SkillHolder(5081, 4);
   private static final SkillHolder PHYSICALSPECIAL3 = new SkillHolder(5082, 4);
   private static final SkillHolder CREW_SKILL = new SkillHolder(6172, 1);
   private static final SkillHolder INVIN_BUFF_ON = new SkillHolder(5225, 1);

   private PrimevalIsle() {
      super(PrimevalIsle.class.getSimpleName(), "ai");
      this.addAggroRangeEnterId(TREX);
      this.addSpellFinishedId(TREX);
      this.addAttackId(18344);
      this.addAttackId(TREX);
      this.addAttackId(MONSTERS);
      this.addKillId(new int[]{18344, 29065, 22743, 22742});
      this.addSeeCreatureId(TREX);
      this.addSeeCreatureId(MONSTERS);

      for(int npcId : SPRIGNANT) {
         for(Spawner spawn : SpawnParser.getInstance().getSpawnData()) {
            if (spawn != null && spawn.getId() == npcId) {
               this.onSpawn(spawn.getLastSpawn());
            }
         }
      }

      for(int npcId : TREX) {
         for(Spawner spawn : SpawnParser.getInstance().getSpawnData()) {
            if (spawn != null && spawn.getId() == npcId) {
               this.onSpawn(spawn.getLastSpawn());
            }
         }
      }
   }

   @Override
   public String onSpellFinished(Npc npc, Player player, Skill skill) {
      if (skill.getId() == CREW_SKILL.getId()) {
         this.startQuestTimer("START_INVUL", 4000L, npc, null);
      }

      if (npc.isInCombat()) {
         Attackable mob = (Attackable)npc;
         Creature target = mob.getMostHated();
         if (npc.getCurrentHp() / npc.getMaxHp() * 100.0 < 60.0) {
            if (skill.getId() == SELFBUFF1.getId()) {
               npc.setScriptValue(3);
               if (target != null) {
                  npc.setTarget(target);
                  mob.setIsRunning(true);
                  mob.addDamageHate(target, 0, 555);
                  mob.getAI().setIntention(CtrlIntention.ATTACK, target);
               }
            }
         } else if (npc.getCurrentHp() / npc.getMaxHp() * 100.0 < 30.0) {
            if (skill.getId() == SELFBUFF1.getId()) {
               npc.setScriptValue(1);
               if (target != null) {
                  npc.setTarget(target);
                  mob.setIsRunning(true);
                  mob.addDamageHate(target, 0, 555);
                  mob.getAI().setIntention(CtrlIntention.ATTACK, target);
               }
            } else if (skill.getId() == SELFBUFF2.getId()) {
               npc.setScriptValue(5);
               if (target != null) {
                  npc.setTarget(target);
                  mob.setIsRunning(true);
                  mob.addDamageHate(target, 0, 555);
                  mob.getAI().setIntention(CtrlIntention.ATTACK, target);
               }
            }
         }
      }

      return super.onSpellFinished(npc, player, skill);
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      switch(event) {
         case "TREX_ATTACK":
            if (npc != null && player != null) {
               npc.setScriptValue(0);
               if (player.isInsideRadius(npc, 800, true, false)) {
                  npc.setTarget(player);
                  npc.doCast(LONGRANGEDMAGIC1.getSkill());
                  this.attackPlayer((Attackable)npc, player);
               }
            }
            break;
         case "START_INVUL":
            if (npc != null && !npc.isDead()) {
               npc.doCast(INVIN_BUFF_ON.getSkill());
            }
      }

      return super.onAdvEvent(event, npc, player);
   }

   @Override
   public String onSeeCreature(Npc npc, Creature creature, boolean isSummon) {
      if (Util.contains(MONSTERS, npc.getId())) {
         if (creature.isPlayer()) {
            Attackable mob = (Attackable)npc;
            if (getRandom(100) < 30 && npc.getId() == 22743 || npc.getId() == 22742 && npc.isScriptValue(0)) {
               mob.clearAggroList();
               npc.setScriptValue(1);
               npc.setRunning();
               int distance = 3000;
               int heading = Util.calculateHeadingFrom(creature, npc);
               double angle = Util.convertHeadingToDegree(heading);
               double radian = Math.toRadians(angle);
               double sin = Math.sin(radian);
               double cos = Math.cos(radian);
               int newX = (int)((double)npc.getX() + cos * 3000.0);
               int newY = (int)((double)npc.getY() + sin * 3000.0);
               Location loc = GeoEngine.moveCheck(npc.getX(), npc.getY(), npc.getZ(), newX, newY, npc.getGeoIndex());
               npc.getAI().setIntention(CtrlIntention.MOVING, loc, Integer.valueOf(0));
            }
         }
      } else if (Util.contains(VEGETABLE, creature.getId())) {
         npc.setTarget(creature);
         npc.doCast(CREW_SKILL.getSkill());
         npc.setIsRunning(true);
         npc.getAI().setIntention(CtrlIntention.ATTACK, creature);
      }

      return super.onSeeCreature(npc, creature, isSummon);
   }

   @Override
   public String onAggroRangeEnter(Npc npc, Player player, boolean isSummon) {
      if (npc.isScriptValue(0)) {
         npc.setScriptValue(1);
         ((Attackable)npc).clearAggroList();
         this.startQuestTimer("TREX_ATTACK", 6000L, npc, player);
      }

      return super.onAggroRangeEnter(npc, player, isSummon);
   }

   @Override
   public String onAttack(Npc npc, Player attacker, int damage, boolean isSummon) {
      if (npc.getId() == 18344) {
         if (getRandom(100) <= 80 && npc.isScriptValue(0)) {
            npc.setScriptValue(1);
            Playable playable = (Playable)(isSummon ? attacker.getSummon() : attacker);

            for(Npc npcs : World.getInstance().getAroundNpc(npc, 500, 200)) {
               if (npcs.isAttackable() && getRandomBoolean()) {
                  Attackable monster = (Attackable)npcs;
                  this.attackPlayer(monster, playable);
               }
            }
         }
      } else if (Util.contains(TREX, npc.getId())) {
         Attackable mob = (Attackable)npc;
         Creature target = mob.getMostHated();
         if (npc.getCurrentHp() / npc.getMaxHp() * 100.0 <= 30.0) {
            if (npc.isScriptValue(3)) {
               if (!npc.isSkillDisabled(SELFBUFF1.getSkill())) {
                  npc.doCast(SELFBUFF1.getSkill());
               }
            } else if (npc.isScriptValue(1) && !npc.isSkillDisabled(SELFBUFF2.getSkill())) {
               npc.doCast(SELFBUFF2.getSkill());
            }
         } else if (npc.getCurrentHp() / npc.getMaxHp() * 100.0 <= 60.0 && npc.isScriptValue(3) && !npc.isSkillDisabled(SELFBUFF1.getSkill())) {
            npc.doCast(SELFBUFF1.getSkill());
         }

         if (Util.calculateDistance(npc, attacker, true) > 100.0) {
            if (!npc.isSkillDisabled(LONGRANGEDMAGIC1.getSkill()) && getRandom(100) <= 10 * npc.getScriptValue()) {
               npc.setTarget(attacker);
               npc.doCast(LONGRANGEDMAGIC1.getSkill());
            }
         } else if (target != null) {
            if (!npc.isSkillDisabled(LONGRANGEDMAGIC1.getSkill()) && getRandom(100) <= 10 * npc.getScriptValue()) {
               npc.setTarget(target);
               npc.doCast(LONGRANGEDMAGIC1.getSkill());
            }

            if (!npc.isSkillDisabled(PHYSICALSPECIAL1.getSkill()) && getRandom(100) <= 5 * npc.getScriptValue()) {
               npc.setTarget(target);
               npc.doCast(PHYSICALSPECIAL1.getSkill());
            }

            if (!npc.isSkillDisabled(PHYSICALSPECIAL2.getSkill()) && getRandom(100) <= 3 * npc.getScriptValue()) {
               npc.setTarget(target);
               npc.doCast(PHYSICALSPECIAL2.getSkill());
            }

            if (!npc.isSkillDisabled(PHYSICALSPECIAL3.getSkill()) && getRandom(100) <= 5 * npc.getScriptValue()) {
               npc.setTarget(target);
               npc.doCast(PHYSICALSPECIAL3.getSkill());
            }
         }
      }

      return super.onAttack(npc, attacker, damage, isSummon);
   }

   @Override
   public String onKill(Npc npc, Player killer, boolean isSummon) {
      if (npc.getId() != 22743 && (npc.getId() != 22742 || npc.isScriptValue(1))) {
         if (npc.getId() == 29065 || getRandom(100) < 3) {
            Player player = npc.getId() == 29065 ? this.getRandomPartyMember(killer) : killer;
            if ((double)player.getInventory().getSize(false) <= (double)player.getInventoryLimit() * 0.8) {
               giveItems(player, 14828, 1L);
               ItemInstance summonItem = player.getInventory().getItemByItemId(14828);
               IItemHandler handler = ItemHandler.getInstance().getHandler(summonItem.getEtcItem());
               if (handler != null && !player.hasPet()) {
                  handler.useItem(player, summonItem, true);
               }

               showOnScreenMsg(player, NpcStringId.LIFE_STONE_FROM_THE_BEGINNING_ACQUIRED, 2, 6000, new String[0]);
            } else {
               showOnScreenMsg(
                  player,
                  NpcStringId.WHEN_INVENTORY_WEIGHT_NUMBER_ARE_MORE_THAN_80_THE_LIFE_STONE_FROM_THE_BEGINNING_CANNOT_BE_ACQUIRED,
                  2,
                  6000,
                  new String[0]
               );
            }
         }

         return super.onKill(npc, killer, isSummon);
      } else {
         return super.onKill(npc, killer, isSummon);
      }
   }

   public static void main(String[] args) {
      new PrimevalIsle();
   }
}
