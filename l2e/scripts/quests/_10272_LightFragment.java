package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _10272_LightFragment extends Quest {
   public _10272_LightFragment(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(32560);
      this.addTalkId(32560);
      this.addTalkId(32559);
      this.addTalkId(32566);
      this.addTalkId(32567);
      this.addTalkId(32557);
      this.addKillId(new int[]{22536, 22537, 22538, 22539, 22540, 22541, 22542, 22543, 22544, 22547, 22550, 22551, 22552, 22596});
      this.questItemIds = new int[]{13853, 13854};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("32560-06.htm")) {
            st.startQuest();
         } else if (event.equalsIgnoreCase("32559-03.htm")) {
            st.setCond(2, true);
         } else if (event.equalsIgnoreCase("32559-07.htm")) {
            st.setCond(3, true);
         } else if (event.equalsIgnoreCase("pay")) {
            if (st.getQuestItemsCount(57) >= 10000L) {
               st.takeItems(57, 10000L);
               htmltext = "32566-05.htm";
            } else if (st.getQuestItemsCount(57) < 10000L) {
               htmltext = "32566-04a.htm";
            }
         } else if (event.equalsIgnoreCase("32567-04.htm")) {
            st.setCond(4, true);
         } else if (event.equalsIgnoreCase("32559-12.htm")) {
            st.setCond(5, true);
         } else if (event.equalsIgnoreCase("32557-03.htm")) {
            if (st.getQuestItemsCount(13854) >= 100L) {
               st.takeItems(13854, 100L);
               st.set("wait", "1");
            } else {
               htmltext = "32557-04.htm";
            }
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
         if (npc.getId() == 32560) {
            switch(st.getState()) {
               case 0:
                  QuestState _prev = player.getQuestState("_10271_TheEnvelopingDarkness");
                  if (_prev != null && _prev.getState() == 2 && player.getLevel() >= 75) {
                     htmltext = "32560-01.htm";
                  } else {
                     htmltext = "32560-02.htm";
                  }

                  if (player.getLevel() < 75) {
                     htmltext = "32560-03.htm";
                  }
                  break;
               case 1:
                  htmltext = "32560-06.htm";
                  break;
               case 2:
                  htmltext = "32560-04.htm";
            }

            if (st.isCond(2)) {
               htmltext = "32560-06.htm";
            }
         } else if (npc.getId() == 32559) {
            switch(st.getState()) {
               case 2:
                  htmltext = "32559-19.htm";
               default:
                  if (st.isCond(1)) {
                     htmltext = "32559-01.htm";
                  } else if (st.isCond(2)) {
                     htmltext = "32559-04.htm";
                  } else if (st.isCond(3)) {
                     htmltext = "32559-08.htm";
                  } else if (st.isCond(4)) {
                     htmltext = "32559-10.htm";
                  } else if (st.isCond(5)) {
                     if (st.getQuestItemsCount(13853) >= 100L) {
                        htmltext = "32559-15.htm";
                        st.setCond(6, true);
                     } else if (st.getQuestItemsCount(13853) >= 1L) {
                        htmltext = "32559-14.htm";
                     } else if (st.getQuestItemsCount(13853) < 1L) {
                        htmltext = "32559-13.htm";
                     }
                  } else if (st.isCond(6)) {
                     if (st.getQuestItemsCount(13854) < 100L) {
                        htmltext = "32559-16.htm";
                     } else {
                        htmltext = "32559-17.htm";
                        st.setCond(7, true);
                     }
                  } else if (st.isCond(8)) {
                     htmltext = "32559-18.htm";
                     st.calcExpAndSp(this.getId());
                     st.calcReward(this.getId());
                     st.exitQuest(false, true);
                  }
            }
         } else if (npc.getId() == 32566) {
            switch(st.getState()) {
               case 2:
                  htmltext = "32559-19.htm";
               default:
                  if (st.isCond(1)) {
                     htmltext = "32566-02.htm";
                  } else if (st.isCond(2)) {
                     htmltext = "32566-02.htm";
                  } else if (st.isCond(3)) {
                     htmltext = "32566-01.htm";
                  } else if (st.isCond(4)) {
                     htmltext = "32566-09.htm";
                  } else if (st.isCond(5)) {
                     htmltext = "32566-10.htm";
                  } else if (st.isCond(6)) {
                     htmltext = "32566-10.htm";
                  }
            }
         } else if (npc.getId() == 32567) {
            if (st.isCond(3)) {
               htmltext = "32567-01.htm";
            } else if (st.isCond(4)) {
               htmltext = "32567-05.htm";
            }
         } else if (npc.getId() == 32557) {
            if (st.isCond(7)) {
               if (st.getInt("wait") == 1) {
                  htmltext = "32557-05.htm";
                  st.unset("wait");
                  st.setCond(8, true);
                  st.giveItems(13855, 1L);
               } else {
                  htmltext = "32557-01.htm";
               }
            } else if (st.isCond(8)) {
               htmltext = "32557-06.htm";
            }
         }

         return htmltext;
      }
   }

   @Override
   public final String onKill(Npc npc, Player player, boolean isSummon) {
      Player partyMember = this.getRandomPartyMember(player, 5);
      if (partyMember == null) {
         return super.onKill(npc, player, isSummon);
      } else {
         QuestState st = player.getQuestState(this.getName());
         if (st.calcDropItems(this.getId(), 13853, npc.getId(), 100)) {
         }

         return null;
      }
   }

   public static void main(String[] args) {
      new _10272_LightFragment(10272, _10272_LightFragment.class.getSimpleName(), "");
   }
}
