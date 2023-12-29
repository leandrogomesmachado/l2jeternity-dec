package l2e.scripts.quests;

import l2e.commons.util.Broadcast;
import l2e.gameserver.instancemanager.ReflectionManager;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.instance.MonsterInstance;
import l2e.gameserver.model.entity.Reflection;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.serverpackets.NpcSay;

public class _128_PailakaSongOfIceAndFire extends Quest {
   private static final int[][] DROPLIST = new int[][]{
      {18616, 13032, 30},
      {18616, 13033, 80},
      {32492, 13032, 10},
      {32492, 13041, 40},
      {32492, 13033, 80},
      {32493, 13032, 10},
      {32493, 13040, 40},
      {32493, 13033, 80}
   };

   public _128_PailakaSongOfIceAndFire(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(32497);
      this.addFirstTalkId(new int[]{32497, 32510, 32500, 32507});
      this.addTalkId(new int[]{32497, 32510, 32500, 32507});
      this.addAttackId(new int[]{32492, 32493});
      this.addSeeCreatureId(new int[]{18607});
      this.addKillId(new int[]{18610, 18609, 18608, 18607, 18620, 18616, 32492, 32493, 18611, 18612, 18613, 18614, 18615});
      this.questItemIds = new int[]{13034, 13035, 13036, 13130, 13131, 13132, 13133, 13134, 13135, 13136, 13038, 13039, 13032, 13033, 13040, 13041};
   }

   private static final void dropItem(Npc mob, Player player) {
      int npcId = mob.getId();
      int chance = getRandom(100);

      for(int[] drop : DROPLIST) {
         if (npcId == drop[0] && chance < drop[2]) {
            ((MonsterInstance)mob).dropItem(player, drop[1], (long)getRandom(1, 6));
            return;
         }

         if (npcId < drop[0]) {
            return;
         }
      }
   }

   @Override
   public final String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return getNoQuestMsg(player);
      } else {
         int cond = st.getCond();
         if (event.equalsIgnoreCase("32497-03.htm")) {
            if (cond == 0) {
               st.startQuest();
            }
         } else if (event.equalsIgnoreCase("32500-06.htm")) {
            if (cond == 1) {
               st.setCond(2, true);
               st.giveItems(13034, 1L);
               st.giveItems(13130, 1L);
            }
         } else if (event.equalsIgnoreCase("32507-04.htm")) {
            if (cond == 3) {
               st.setCond(4, true);
               st.takeItems(13034, -1L);
               st.takeItems(13038, -1L);
               st.takeItems(13131, -1L);
               st.giveItems(13132, 1L);
               st.giveItems(13035, 1L);
               addSpawn(18609, -53903, 181484, -4555, 30456, false, 0L, false, npc.getReflectionId());
            }
         } else if (event.equalsIgnoreCase("32507-08.htm")) {
            if (cond == 6) {
               st.setCond(7, true);
               st.takeItems(13035, -1L);
               st.takeItems(13134, -1L);
               st.takeItems(13039, -1L);
               st.giveItems(13036, 1L);
               st.giveItems(13135, 1L);
               addSpawn(18607, -61354, 183624, -4821, 63613, false, 0L, false, npc.getReflectionId());
            }
         } else if (event.equalsIgnoreCase("32510-02.htm")) {
            st.exitQuest(false, true);
            Reflection inst = ReflectionManager.getInstance().getReflection(npc.getReflectionId());
            if (inst != null) {
               inst.setDuration(300000);
               inst.setEmptyDestroyTime(0L);
               if (inst.containsPlayer(player.getObjectId())) {
                  player.setVitalityPoints(20000, true);
                  st.calcExpAndSp(this.getId());
                  st.calcReward(this.getId());
               }
            }
         } else if (event.equalsIgnoreCase("GARGOS_LAUGH")) {
            Broadcast.toKnownPlayers(npc, new NpcSay(npc.getObjectId(), 23, npc.getTemplate().getIdTemplate(), NpcStringId.OHHOHOH));
         }

         return event;
      }
   }

   @Override
   public final String onFirstTalk(Npc npc, Player player) {
      return npc.getId() + ".htm";
   }

   @Override
   public final String onTalk(Npc npc, Player player) {
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return getNoQuestMsg(player);
      } else {
         int cond = st.getCond();
         switch(npc.getId()) {
            case 32497:
               switch(st.getState()) {
                  case 0:
                     if (player.getLevel() < 36) {
                        return "32497-05.htm";
                     } else {
                        if (player.getLevel() > 42) {
                           return "32497-06.htm";
                        }

                        return "32497-01.htm";
                     }
                  case 1:
                     if (cond > 1) {
                        return "32497-00.htm";
                     }

                     return "32497-03.htm";
                  case 2:
                     return "32497-07.htm";
                  default:
                     return "32497-01.htm";
               }
            case 32500:
               if (cond > 1) {
                  return "32500-00.htm";
               }

               return "32500-01.htm";
            case 32507:
               switch(st.getCond()) {
                  case 1:
                     return "32507-01.htm";
                  case 2:
                     return "32507-02.htm";
                  case 3:
                     return "32507-03.htm";
                  case 4:
                  case 5:
                     return "32507-05.htm";
                  case 6:
                     return "32507-06.htm";
                  default:
                     return "32507-09.htm";
               }
            case 32510:
               if (st.getState() == 2) {
                  return "32510-00.htm";
               } else if (cond == 9) {
                  return "32510-01.htm";
               }
            default:
               return getNoQuestMsg(player);
         }
      }
   }

   @Override
   public final String onAttack(Npc npc, Player attacker, int damage, boolean isSummon) {
      if (!npc.isDead()) {
         npc.doDie(attacker);
      }

      return super.onAttack(npc, attacker, damage, isSummon);
   }

   @Override
   public final String onKill(Npc npc, Player player, boolean isSummon) {
      QuestState st = player.getQuestState(this.getName());
      if (st != null && st.getState() == 1) {
         int cond = st.getCond();
         switch(npc.getId()) {
            case 18607:
               if (cond == 7) {
                  st.set("cond", "8");
                  st.playSound("ItemSound.quest_itemget");
                  st.takeItems(13135, -1L);
                  st.giveItems(13136, 1L);
               }

               addSpawn(18620, -53297, 185027, -4617, 1512, false, 0L, false, npc.getReflectionId());
               break;
            case 18608:
               if (cond == 5) {
                  st.set("cond", "6");
                  st.playSound("ItemSound.quest_itemget");
                  st.takeItems(13133, -1L);
                  st.giveItems(13134, 1L);
                  st.giveItems(13039, 1L);
               }
               break;
            case 18609:
               if (cond == 4) {
                  st.takeItems(13132, -1L);
                  st.giveItems(13133, 1L);
                  st.set("cond", "5");
                  st.playSound("ItemSound.quest_itemget");
               }

               addSpawn(18608, -61415, 181418, -4818, 63852, false, 0L, false, npc.getReflectionId());
               break;
            case 18610:
               if (cond == 2) {
                  st.set("cond", "3");
                  st.playSound("ItemSound.quest_itemget");
                  st.takeItems(13130, -1L);
                  st.giveItems(13131, 1L);
                  st.giveItems(13038, 1L);
               }
               break;
            case 18616:
            case 32492:
            case 32493:
               dropItem(npc, player);
               break;
            case 18620:
               if (cond == 8) {
                  st.set("cond", "9");
                  st.playSound("ItemSound.quest_middle");
                  st.takeItems(13136, -1L);
                  addSpawn(32510, -53297, 185027, -4617, 33486, false, 0L, false, npc.getReflectionId());
               }
         }

         return super.onKill(npc, player, isSummon);
      } else {
         return null;
      }
   }

   @Override
   public String onSeeCreature(Npc npc, Creature creature, boolean isSummon) {
      if (npc.isScriptValue(0) && creature.isPlayer()) {
         npc.setScriptValue(1);
         this.startQuestTimer("GARGOS_LAUGH", 1000L, npc, creature.getActingPlayer());
      }

      return super.onSeeCreature(npc, creature, isSummon);
   }

   public static void main(String[] args) {
      new _128_PailakaSongOfIceAndFire(128, _128_PailakaSongOfIceAndFire.class.getSimpleName(), "");
   }
}
