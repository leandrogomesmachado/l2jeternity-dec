package l2e.scripts.quests;

import l2e.gameserver.ai.model.CtrlIntention;
import l2e.gameserver.instancemanager.ReflectionManager;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.instance.MonsterInstance;
import l2e.gameserver.model.entity.Reflection;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _129_PailakaDevilsLegacy extends Quest {
   private static final int[][] DROPLIST = new int[][]{{32495, 13033, 20}, {32495, 13049, 40}, {32495, 13059, 60}, {32495, 13150, 80}, {32495, 13048, 100}};

   public _129_PailakaDevilsLegacy(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(32498);
      this.addFirstTalkId(new int[]{32498, 32501, 32508, 32511});
      this.addTalkId(new int[]{32498, 32501, 32508, 32511});
      this.addAttackId(32495);
      this.addKillId(new int[]{18629, 18630, 18631, 18632, 32495, 18622, 18623, 18624, 18625, 18626, 18627});
      this.questItemIds = new int[]{13042, 13043, 13044, 13046, 13047, 13033, 13048, 13049, 13059, 13150};
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

   protected static final void teleportPlayer(Player player, int[] coords, int instanceId) {
      player.getAI().setIntention(CtrlIntention.IDLE);
      player.setReflectionId(instanceId);
      player.teleToLocation(coords[0], coords[1], coords[2], true);
   }

   @Override
   public final String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return getNoQuestMsg(player);
      } else {
         int cond = st.getCond();
         if (event.equalsIgnoreCase("32498-05.htm")) {
            if (cond == 0) {
               st.startQuest();
            }
         } else if (event.equalsIgnoreCase("32501-03.htm")) {
            if (cond == 2) {
               st.giveItems(13042, 1L);
               st.setCond(3, true);
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
         }

         return event;
      }
   }

   @Override
   public final String onFirstTalk(Npc npc, Player player) {
      QuestState st = player.getQuestState(this.getName());
      return st != null && npc.getId() == 32511 && st.getState() == 2 ? "32511-03.htm" : npc.getId() + ".htm";
   }

   @Override
   public final String onTalk(Npc npc, Player player) {
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return getNoQuestMsg(player);
      } else {
         int cond = st.getCond();
         switch(npc.getId()) {
            case 32498:
               switch(st.getState()) {
                  case 0:
                     if (player.getLevel() < 61) {
                        return "32498-11.htm";
                     } else {
                        if (player.getLevel() > 67) {
                           return "32498-12.htm";
                        }

                        return "32498-01.htm";
                     }
                  case 1:
                     if (cond > 1) {
                        return "32498-08.htm";
                     }

                     return "32498-06.htm";
                  case 2:
                     return "32498-10.htm";
                  default:
                     return "32498-01.htm";
               }
            case 32501:
               if (st.getInt("cond") > 2) {
                  return "32501-04.htm";
               }

               return "32501-01.htm";
            case 32508:
               if (!player.hasSummon()) {
                  if (st.getQuestItemsCount(13042) > 0L) {
                     if (st.getQuestItemsCount(13046) > 0L) {
                        st.takeItems(13042, -1L);
                        st.takeItems(13046, -1L);
                        st.giveItems(13043, 1L);
                        return "32508-03.htm";
                     }

                     return "32508-02.htm";
                  }

                  if (st.getQuestItemsCount(13043) > 0L) {
                     if (st.getQuestItemsCount(13047) > 0L) {
                        st.takeItems(13043, -1L);
                        st.takeItems(13047, -1L);
                        st.giveItems(13044, 1L);
                        return "32508-05.htm";
                     }

                     return "32508-04.htm";
                  }

                  if (st.getQuestItemsCount(13044) > 0L) {
                     return "32508-06.htm";
                  }

                  return "32508-00.htm";
               }

               return "32508-07.htm";
            case 32511:
               if (!player.hasSummon()) {
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

                  return "32511-01.htm";
               }

               return "32511-02.htm";
            default:
               return getNoQuestMsg(player);
         }
      }
   }

   @Override
   public final String onAttack(Npc npc, Player attacker, int damage, boolean isSummon) {
      if (npc.getId() == 32495) {
         dropItem(npc, attacker);
         npc.doDie(attacker);
      }

      return super.onAttack(npc, attacker, damage, isSummon);
   }

   @Override
   public final String onKill(Npc npc, Player player, boolean isSummon) {
      QuestState st = player.getQuestState(this.getName());
      if (st != null && st.getState() == 1) {
         switch(npc.getId()) {
            case 18629:
               if (st.getQuestItemsCount(13042) > 0L) {
                  st.playSound("ItemSound.quest_itemget");
                  st.giveItems(13046, 1L);
               }
               break;
            case 18631:
               if (st.getQuestItemsCount(13043) > 0L) {
                  st.playSound("ItemSound.quest_itemget");
                  st.giveItems(13047, 1L);
               }
         }

         return super.onKill(npc, player, isSummon);
      } else {
         return null;
      }
   }

   public static void main(String[] args) {
      new _129_PailakaDevilsLegacy(129, _129_PailakaDevilsLegacy.class.getSimpleName(), "");
   }
}
