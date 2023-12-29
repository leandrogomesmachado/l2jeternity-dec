package l2e.scripts.quests;

import l2e.commons.util.Rnd;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public final class _456_DontKnowDontCare extends Quest {
   public _456_DontKnowDontCare(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(new int[]{32864, 32865, 32866, 32867, 32868, 32869, 32870, 32891});
      this.addTalkId(new int[]{32864, 32865, 32866, 32867, 32868, 32869, 32870, 32891, 32884, 32885, 32886});
      this.addFirstTalkId(new int[]{32884, 32885, 32886});
      this.questItemIds = new int[]{17251, 17252, 17253};
   }

   @Override
   public String onFirstTalk(Npc npc, Player player) {
      QuestState st = player.getQuestState(this.getName());
      if (st != null && st.isCond(1) && st.get("RaidKilled") != null) {
         String htmltext = null;
         if (st.isCond(1)) {
            switch(npc.getId()) {
               case 32884:
                  if (st.getQuestItemsCount(17251) < 1L) {
                     st.giveItems(17251, 1L);
                     st.playSound("ItemSound.quest_itemget");
                     st.unset("RaidKilled");
                     htmltext = npc.getId() + "-01.htm";
                  } else {
                     htmltext = npc.getId() + "-03.htm";
                  }
                  break;
               case 32885:
                  if (st.getQuestItemsCount(17252) < 1L) {
                     st.giveItems(17252, 1L);
                     st.playSound("ItemSound.quest_itemget");
                     st.unset("RaidKilled");
                     htmltext = npc.getId() + "-01.htm";
                  } else {
                     htmltext = npc.getId() + "-03.htm";
                  }
                  break;
               case 32886:
                  if (st.getQuestItemsCount(17253) < 1L) {
                     st.giveItems(17253, 1L);
                     st.playSound("ItemSound.quest_itemget");
                     st.unset("RaidKilled");
                     htmltext = npc.getId() + "-01.htm";
                  } else {
                     htmltext = npc.getId() + "-03.htm";
                  }
            }

            if (st.getQuestItemsCount(17251) > 0L && st.getQuestItemsCount(17252) > 0L && st.getQuestItemsCount(17253) > 0L) {
               st.setCond(2, true);
            }
         }

         return htmltext;
      } else {
         return npc.getId() + "-02.htm";
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      QuestState st = player.getQuestState(this.getName());
      String htmltext = getNoQuestMsg(player);
      if (st == null) {
         return htmltext;
      } else {
         if (npc.getId() == 32864
            || npc.getId() == 32865
            || npc.getId() == 32866
            || npc.getId() == 32867
            || npc.getId() == 32868
            || npc.getId() == 32869
            || npc.getId() == 32870
            || npc.getId() == 32891) {
            switch(st.getState()) {
               case 1:
                  switch(st.getCond()) {
                     case 1:
                        return this.hasAtLeastOneQuestItem(player, this.getRegisteredItemIds()) ? "32864-09.htm" : "32864-08.htm";
                     case 2:
                        if (hasQuestItems(player, this.getRegisteredItemIds())) {
                           if (Rnd.chance(1)) {
                              st.calcReward(this.getId(), 1, true);
                           } else if (Rnd.chance(5)) {
                              st.calcReward(this.getId(), 2, true);
                           } else if (Rnd.chance(10)) {
                              st.calcReward(this.getId(), 3, true);
                           } else if (Rnd.chance(15)) {
                              st.calcReward(this.getId(), 4, true);
                           } else {
                              st.calcReward(this.getId(), 5, true);
                           }

                           st.calcReward(this.getId(), 6);
                           st.exitQuest(QuestState.QuestType.DAILY, true);
                           htmltext = "32864-10.htm";
                        }

                        return htmltext;
                     default:
                        return htmltext;
                  }
               case 2:
                  if (!st.isNowAvailable()) {
                     htmltext = "32864-02.htm";
                     break;
                  } else {
                     st.setState((byte)0);
                  }
               case 0:
                  htmltext = player.getLevel() >= 80 ? "32864-01.htm" : "32864-03.htm";
            }
         }

         return htmltext;
      }
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = null;
      switch(event) {
         case "32864-04.htm":
         case "32864-05.htm":
         case "32864-06.htm":
            QuestState qs = player.getQuestState(this.getName());
            if (qs != null && qs.isCreated()) {
               htmltext = event;
            }
            break;
         case "32864-07.htm":
            QuestState qs = player.getQuestState(this.getName());
            if (qs != null && qs.isCreated()) {
               qs.startQuest();
               htmltext = event;
            }
      }

      return htmltext;
   }

   public static void main(String[] args) {
      new _456_DontKnowDontCare(456, _456_DontKnowDontCare.class.getSimpleName(), "");
   }
}
