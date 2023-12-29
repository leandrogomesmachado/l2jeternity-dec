package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _003_WillTheSealBeBroken extends Quest {
   public _003_WillTheSealBeBroken(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30141);
      this.addTalkId(30141);
      this.addKillId(20031);
      this.addKillId(20041);
      this.addKillId(20046);
      this.addKillId(20048);
      this.addKillId(20052);
      this.addKillId(20057);
      this.questItemIds = new int[]{1081, 1082, 1083};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("30141-03.htm")) {
            st.startQuest();
         }

         return event;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         st = this.newQuestState(player);
      }

      String htmltext = getNoQuestMsg(player);
      int npcId = npc.getId();
      int cond = st.getCond();
      if (npcId == 30141) {
         switch(st.getState()) {
            case 0:
               if (player.getRace().ordinal() != 2) {
                  htmltext = "30141-00.htm";
                  st.exitQuest(true);
               } else if (player.getLevel() >= 16) {
                  htmltext = "30141-02.htm";
               } else {
                  htmltext = "30141-01.htm";
                  st.exitQuest(true);
               }
               break;
            case 1:
               switch(cond) {
                  case 2:
                     if (st.getQuestItemsCount(1081) > 0L && st.getQuestItemsCount(1082) > 0L && st.getQuestItemsCount(1083) > 0L) {
                        htmltext = "30141-06.htm";
                        st.takeItems(1081, 1L);
                        st.takeItems(1082, 1L);
                        st.takeItems(1083, 1L);
                        st.calcReward(this.getId());
                        st.exitQuest(false, true);
                     } else {
                        htmltext = "30141-04.htm";
                     }

                     return htmltext;
                  default:
                     return htmltext;
               }
            case 2:
               htmltext = getAlreadyCompletedMsg(player);
         }
      }

      return htmltext;
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return null;
      } else {
         int npcId = npc.getId();
         if (st.getState() == 1 && st.isCond(1)) {
            if (npcId == 20031) {
               if (st.getQuestItemsCount(1081) == 0L) {
                  st.giveItems(1081, 1L);
                  st.playSound("Itemsound.quest_itemget");
               }
            } else if (npcId != 20041 && npcId != 20046) {
               if ((npcId == 20048 || npcId == 20052 || npcId == 20057) && st.getQuestItemsCount(1083) == 0L) {
                  st.giveItems(1083, 1L);
                  st.playSound("Itemsound.quest_itemget");
               }
            } else if (st.getQuestItemsCount(1082) == 0L) {
               st.giveItems(1082, 1L);
               st.playSound("Itemsound.quest_itemget");
            }

            if (st.getQuestItemsCount(1081) == 1L && st.getQuestItemsCount(1082) == 1L && st.getQuestItemsCount(1083) == 1L) {
               st.setCond(2, true);
            }
         }

         return null;
      }
   }

   public static void main(String[] args) {
      new _003_WillTheSealBeBroken(3, _003_WillTheSealBeBroken.class.getSimpleName(), "");
   }
}
