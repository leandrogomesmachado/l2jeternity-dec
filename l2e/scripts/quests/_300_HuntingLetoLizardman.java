package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _300_HuntingLetoLizardman extends Quest {
   private static final String qn = "_300_HuntingLetoLizardman";
   private static final int RATH = 30126;
   private static final int BRACELET = 7139;

   public _300_HuntingLetoLizardman(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30126);
      this.addTalkId(30126);
      this.addKillId(new int[]{20577, 20578, 20579, 20580, 20582});
      this.questItemIds = new int[]{7139};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      QuestState st = player.getQuestState("_300_HuntingLetoLizardman");
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("30126-03.htm")) {
            st.set("cond", "1");
            st.setState((byte)1);
            st.playSound("ItemSound.quest_accept");
         } else if (event.equalsIgnoreCase("30126-05.htm") && st.getQuestItemsCount(7139) >= 60L) {
            htmltext = "30126-06.htm";
            int luck = st.getRandom(3);
            st.takeItems(7139, -1L);
            if (luck == 0) {
               st.rewardItems(57, 30000L);
            } else if (luck == 1) {
               st.rewardItems(1867, 50L);
            } else if (luck == 2) {
               st.rewardItems(1872, 50L);
            }

            st.playSound("ItemSound.quest_finish");
            st.exitQuest(true);
         }

         return htmltext;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      QuestState st = player.getQuestState("_300_HuntingLetoLizardman");
      String htmltext = getNoQuestMsg(player);
      if (st == null) {
         return htmltext;
      } else {
         switch(st.getState()) {
            case 0:
               if (player.getLevel() >= 34 && player.getLevel() <= 39) {
                  htmltext = "30126-02.htm";
               } else {
                  htmltext = "30126-01.htm";
                  st.exitQuest(true);
               }
               break;
            case 1:
               if (st.getQuestItemsCount(7139) >= 60L) {
                  htmltext = "30126-04.htm";
               } else {
                  htmltext = "30126-04a.htm";
               }
         }

         return htmltext;
      }
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      Player partyMember = this.getRandomPartyMember(player, 1);
      if (partyMember == null) {
         return null;
      } else {
         QuestState st = partyMember.getQuestState("_300_HuntingLetoLizardman");
         if (st.getRandom(100) < 33) {
            st.giveItems(7139, 1L);
            if (st.getQuestItemsCount(7139) == 60L) {
               st.set("cond", "2");
               st.playSound("ItemSound.quest_middle");
            } else {
               st.playSound("ItemSound.quest_itemget");
            }
         }

         return null;
      }
   }

   public static void main(String[] args) {
      new _300_HuntingLetoLizardman(300, "_300_HuntingLetoLizardman", "");
   }
}
