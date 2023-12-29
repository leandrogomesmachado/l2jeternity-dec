package l2e.scripts.quests;

import l2e.gameserver.model.ClanMember;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _123_TheLeaderAndTheFollower extends Quest {
   private static final String qn = "_123_TheLeaderAndTheFollower";
   private static final int NEWYEAR = 31961;
   private static final int BLOOD = 8549;
   private static final int LEG = 8550;
   private static final int BRUIN_LIZARDMAN = 27321;
   private static final int PICOT_ARANEID = 27322;

   public _123_TheLeaderAndTheFollower(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(31961);
      this.addTalkId(31961);
      this.addKillId(new int[]{27321, 27322});
      this.questItemIds = new int[]{8549, 8550};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      QuestState st = player.getQuestState("_123_TheLeaderAndTheFollower");
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("31961-02.htm")) {
            st.set("cond", "1");
            st.setState((byte)1);
            st.playSound("ItemSound.quest_accept");
         } else if (event.equalsIgnoreCase("31961-05a.htm")) {
            if (st.getQuestItemsCount(8549) >= 10L) {
               st.takeItems(8549, -1L);
               st.set("cond", "3");
               st.set("settype", "1");
               st.playSound("ItemSound.quest_middle");
            } else {
               htmltext = "no_items.htm";
            }
         } else if (event.equalsIgnoreCase("31961-05b.htm")) {
            if (st.getQuestItemsCount(8549) >= 10L) {
               st.takeItems(8549, -1L);
               st.set("cond", "4");
               st.set("settype", "2");
               st.playSound("ItemSound.quest_middle");
            } else {
               htmltext = "no_items.htm";
            }
         } else if (event.equalsIgnoreCase("31961-05c.htm")) {
            if (st.getQuestItemsCount(8549) >= 10L) {
               st.takeItems(8549, -1L);
               st.set("cond", "5");
               st.set("settype", "3");
               st.playSound("ItemSound.quest_middle");
            } else {
               htmltext = "no_items.htm";
            }
         } else if (event.equalsIgnoreCase("31961-09.htm")) {
            ClanMember cm_apprentice = player.getClan().getClanMember(player.getApprentice());
            if (cm_apprentice.isOnline()) {
               Player apprentice = cm_apprentice.getPlayerInstance();
               if (apprentice != null) {
                  QuestState apQuest = apprentice.getQuestState("_123_TheLeaderAndTheFollower");
                  if (apQuest != null) {
                     int crystals = apQuest.getInt("cond") == 3 ? 922 : 771;
                     if (st.getQuestItemsCount(1458) >= (long)crystals) {
                        htmltext = "31961-10.htm";
                        st.takeItems(1458, (long)crystals);
                        st.playSound("ItemSound.quest_finish");
                        apQuest.set("cond", "6");
                        apQuest.playSound("ItemSound.quest_middle");
                     }
                  }
               }
            }

            st.exitQuest(true);
         }

         return htmltext;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState("_123_TheLeaderAndTheFollower");
      if (st == null) {
         return htmltext;
      } else {
         switch(st.getState()) {
            case 0:
               if (player.getSponsor() > 0) {
                  if (player.getLevel() > 19 && player.getPledgeType() == -1) {
                     return "31961-01.htm";
                  }
               } else if (player.getApprentice() > 0) {
                  ClanMember cm = player.getClan().getClanMember(player.getApprentice());
                  if (cm != null && cm.isOnline()) {
                     Player apprentice = cm.getPlayerInstance();
                     if (apprentice != null) {
                        QuestState apQuest = apprentice.getQuestState("_123_TheLeaderAndTheFollower");
                        if (apQuest != null) {
                           int apCond = apQuest.getInt("cond");
                           if (apCond == 3) {
                              return "31961-09a.htm";
                           }

                           if (apCond == 4) {
                              return "31961-09b.htm";
                           }

                           if (apCond == 5) {
                              return "31961-09c.htm";
                           }
                        }
                     }
                  }
               }

               htmltext = "31961-00.htm";
               st.exitQuest(true);
               break;
            case 1:
               int cond = st.getInt("cond");
               if (player.getSponsor() > 0) {
                  if (cond == 1) {
                     htmltext = "31961-03.htm";
                  } else if (cond == 2) {
                     htmltext = "31961-04.htm";
                  } else if (cond == 3) {
                     htmltext = "31961-05d.htm";
                  } else if (cond == 4) {
                     htmltext = "31961-05e.htm";
                  } else if (cond == 5) {
                     htmltext = "31961-05f.htm";
                  } else if (cond == 6) {
                     htmltext = "31961-06.htm";
                     st.set("cond", "7");
                     st.playSound("ItemSound.quest_middle");
                  } else if (cond == 7) {
                     htmltext = "31961-07.htm";
                  } else if (cond == 8 && st.getQuestItemsCount(8550) == 8L) {
                     htmltext = "31961-08.htm";
                     st.takeItems(8550, -1L);
                     st.giveItems(7850, 1L);
                     switch(st.getInt("settype")) {
                        case 1:
                           st.giveItems(7851, 1L);
                           st.giveItems(7852, 1L);
                           st.giveItems(7853, 1L);
                           break;
                        case 2:
                           st.giveItems(7854, 1L);
                           st.giveItems(7855, 1L);
                           st.giveItems(7856, 1L);
                           break;
                        case 3:
                           st.giveItems(7857, 1L);
                           st.giveItems(7858, 1L);
                           st.giveItems(7859, 1L);
                     }

                     st.playSound("ItemSound.quest_finish");
                     st.exitQuest(false);
                  }
               }
               break;
            case 2:
               htmltext = getAlreadyCompletedMsg(player);
         }

         return htmltext;
      }
   }

   @Override
   public final String onKill(Npc npc, Player player, boolean isSummon) {
      QuestState st = player.getQuestState("_123_TheLeaderAndTheFollower");
      if (st == null) {
         return null;
      } else {
         int sponsor = player.getSponsor();
         if (sponsor == 0) {
            st.exitQuest(true);
            return null;
         } else {
            switch(npc.getId()) {
               case 27321:
                  if (st.getInt("cond") == 1 && st.dropQuestItems(8549, 1, 10L, 600000, true)) {
                     st.set("cond", "2");
                  }
                  break;
               case 27322:
                  ClanMember cmSponsor = player.getClan().getClanMember(sponsor);
                  if (cmSponsor != null && cmSponsor.isOnline()) {
                     Player sponsorHelper = cmSponsor.getPlayerInstance();
                     if (sponsorHelper != null
                        && player.isInsideRadius(sponsorHelper, 1100, true, false)
                        && st.getInt("cond") == 7
                        && st.dropQuestItems(8550, 1, 8L, 700000, true)) {
                        st.set("cond", "8");
                     }
                  }
            }

            return null;
         }
      }
   }

   public static void main(String[] args) {
      new _123_TheLeaderAndTheFollower(123, "_123_TheLeaderAndTheFollower", "");
   }
}
