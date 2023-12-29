package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _10293_SevenSignsForbiddenBook extends Quest {
   public _10293_SevenSignsForbiddenBook(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(32784);
      this.addStartNpc(32863);
      this.addTalkId(32784);
      this.addTalkId(32596);
      this.addTalkId(32785);
      this.addTalkId(32809);
      this.addTalkId(32810);
      this.addTalkId(32811);
      this.addTalkId(32812);
      this.addTalkId(32813);
      this.addTalkId(32861);
      this.addTalkId(32863);
      this.addFirstTalkId(32863);
      this.questItemIds = new int[]{17213};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return event;
      } else {
         if (npc.getId() == 32784) {
            if (event.equalsIgnoreCase("32784-04.htm")) {
               st.startQuest();
            } else if (event.equalsIgnoreCase("32784-09.htm")) {
               if (player.isSubClassActive()) {
                  htmltext = "32784-10.htm";
               } else {
                  st.calcExpAndSp(this.getId());
                  st.exitQuest(false, true);
                  htmltext = "32784-09.htm";
               }
            }
         } else if (npc.getId() == 32861) {
            if (event.equalsIgnoreCase("32861-04.htm")) {
               st.setCond(2, true);
            }

            if (event.equalsIgnoreCase("32861-08.htm")) {
               st.setCond(4, true);
            }

            if (event.equalsIgnoreCase("32861-11.htm")) {
               st.setCond(6, true);
            }
         } else if (npc.getId() == 32785) {
            if (event.equalsIgnoreCase("32785-07.htm")) {
               st.setCond(5, true);
            }
         } else if (npc.getId() == 32809 && event.equalsIgnoreCase("32809-02.htm")) {
            st.giveItems(17213, 1L);
            st.setCond(7, true);
         }

         return htmltext;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return htmltext;
      } else {
         if (npc.getId() == 32784) {
            if (st.getState() == 2) {
               htmltext = "32784-02.htm";
            } else if (player.getLevel() < 81) {
               htmltext = "32784-11.htm";
            } else if (player.getQuestState("_10292_SevenSignsGirlofDoubt") == null || player.getQuestState("_10292_SevenSignsGirlofDoubt").getState() != 2) {
               htmltext = "32784-11.htm";
            } else if (st.getState() == 0) {
               htmltext = "32784-01.htm";
            } else if (st.isCond(1)) {
               htmltext = "32784-06.htm";
            } else if (st.getCond() >= 8) {
               htmltext = "32784-07.htm";
            }
         } else if (npc.getId() == 32785) {
            switch(st.getCond()) {
               case 1:
                  htmltext = "32785-01.htm";
                  break;
               case 2:
                  htmltext = "32785-04.htm";
                  st.setCond(3, true);
                  break;
               case 3:
                  htmltext = "32785-05.htm";
                  break;
               case 4:
                  htmltext = "32785-06.htm";
                  break;
               case 5:
                  htmltext = "32785-08.htm";
                  break;
               case 6:
                  htmltext = "32785-09.htm";
                  break;
               case 7:
                  htmltext = "32785-11.htm";
                  st.setCond(8, true);
                  break;
               case 8:
                  htmltext = "32785-12.htm";
            }
         } else if (npc.getId() == 32596) {
            switch(st.getCond()) {
               case 1:
               case 2:
               case 3:
               case 4:
               case 5:
               case 6:
               case 7:
                  htmltext = "32596-01.htm";
                  break;
               case 8:
                  htmltext = "32596-05.htm";
            }
         } else if (npc.getId() == 32861) {
            switch(st.getCond()) {
               case 1:
                  htmltext = "32861-01.htm";
                  break;
               case 2:
                  htmltext = "32861-05.htm";
                  break;
               case 3:
                  htmltext = "32861-06.htm";
                  break;
               case 4:
                  htmltext = "32861-09.htm";
                  break;
               case 5:
                  htmltext = "32861-10.htm";
                  break;
               case 6:
               case 7:
                  htmltext = "32861-12.htm";
                  break;
               case 8:
                  htmltext = "32861-14.htm";
            }
         } else if (npc.getId() == 32809) {
            if (st.isCond(6)) {
               htmltext = "32809-01.htm";
            }
         } else if (npc.getId() == 32810) {
            if (st.isCond(6)) {
               htmltext = "32810-01.htm";
            }
         } else if (npc.getId() == 32811) {
            if (st.isCond(6)) {
               htmltext = "32811-01.htm";
            }
         } else if (npc.getId() == 32812) {
            if (st.isCond(6)) {
               htmltext = "32812-01.htm";
            }
         } else if (npc.getId() == 32813 && st.isCond(6)) {
            htmltext = "32813-01.htm";
         }

         return htmltext;
      }
   }

   @Override
   public final String onFirstTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState(this.getName());
      if (npc.getId() == 32863) {
         switch(st.getCond()) {
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
               htmltext = "32863-01.htm";
               break;
            case 8:
               htmltext = "32863-04.htm";
         }
      }

      return htmltext;
   }

   public static void main(String[] args) {
      new _10293_SevenSignsForbiddenBook(10293, _10293_SevenSignsForbiddenBook.class.getSimpleName(), "");
   }
}
