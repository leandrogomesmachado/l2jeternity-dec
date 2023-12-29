package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.network.NpcStringId;

public class _106_ForgottenTruth extends Quest {
   private static final String qn = "_106_ForgottenTruth";
   private static final int ONYX_TALISMAN1 = 984;
   private static final int ONYX_TALISMAN2 = 985;
   private static final int ANCIENT_SCROLL = 986;
   private static final int ANCIENT_CLAY_TABLET = 987;
   private static final int KARTAS_TRANSLATION = 988;
   private static final int ELDRITCH_DAGGER = 989;
   private static final int SPIRITSHOT_FOR_BEGINNERS = 5790;
   private static final int SOULSHOT_FOR_BEGINNERS = 5789;

   public _106_ForgottenTruth(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30358);
      this.addTalkId(30358);
      this.addTalkId(30133);
      this.addKillId(27070);
      this.questItemIds = new int[]{984, 985, 986, 987, 988};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState("_106_ForgottenTruth");
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("30358-05.htm")) {
            st.giveItems(984, 1L);
            st.set("cond", "1");
            st.setState((byte)1);
            st.playSound("ItemSound.quest_accept");
         }

         return event;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState("_106_ForgottenTruth");
      if (st == null) {
         return htmltext;
      } else {
         if (st.isCompleted()) {
            htmltext = getAlreadyCompletedMsg(player);
         }

         int npcId = npc.getId();
         int cond = st.getInt("cond");
         int id = st.getState();
         if (id == 0) {
            if (player.getRace().ordinal() == 2) {
               if (player.getLevel() >= 10) {
                  htmltext = "30358-03.htm";
               } else {
                  htmltext = "30358-02.htm";
                  st.exitQuest(true);
               }
            } else {
               htmltext = "30358-00.htm";
               st.exitQuest(true);
            }
         } else if (id == 1) {
            if (cond == 1) {
               if (npcId == 30358) {
                  htmltext = "30358-06.htm";
               } else if (npcId == 30133 && st.getQuestItemsCount(984) > 0L) {
                  htmltext = "30133-01.htm";
                  st.takeItems(984, 1L);
                  st.giveItems(985, 1L);
                  st.set("cond", "2");
                  st.playSound("ItemSound.quest_middle");
               }
            } else if (cond == 2) {
               if (npcId == 30358) {
                  htmltext = "30358-06.htm";
               } else if (npcId == 30133) {
                  htmltext = "30133-02.htm";
               }
            } else if (cond == 3) {
               if (npcId == 30358) {
                  htmltext = "30358-06.htm";
               } else if (npcId == 30133 && st.getQuestItemsCount(986) > 0L && st.getQuestItemsCount(987) > 0L) {
                  htmltext = "30133-03.htm";
                  st.takeItems(985, 1L);
                  st.takeItems(986, 1L);
                  st.takeItems(987, 1L);
                  st.giveItems(988, 1L);
                  st.set("cond", "4");
                  st.playSound("ItemSound.quest_middle");
               }
            } else if (cond == 4) {
               if (npcId == 30358 && st.getQuestItemsCount(988) > 0L) {
                  htmltext = "30358-07.htm";
                  st.takeItems(988, 1L);
                  st.giveItems(989, 1L);
                  st.giveItems(1060, 100L);
                  st.addExpAndSp(24195, 2074);

                  for(int ECHO_CHRYTSAL = 4412; ECHO_CHRYTSAL <= 4417; ++ECHO_CHRYTSAL) {
                     st.giveItems(ECHO_CHRYTSAL, 10L);
                  }

                  if (player.getClassId().isMage()) {
                     st.playTutorialVoice("tutorial_voice_027");
                     st.giveItems(5790, 3000L);
                     st.giveItems(2509, 500L);
                  } else {
                     st.playTutorialVoice("tutorial_voice_026");
                     st.giveItems(1835, 1000L);
                     st.giveItems(5789, 6000L);
                  }

                  showOnScreenMsg(player, NpcStringId.ACQUISITION_OF_RACE_SPECIFIC_WEAPON_COMPLETE_N_GO_FIND_THE_NEWBIE_GUIDE, 2, 5000, new String[0]);
                  st.unset("cond");
                  st.exitQuest(false);
                  st.playSound("ItemSound.quest_finish");
               } else if (npcId == 30133) {
                  htmltext = "30133-04.htm";
               }
            }
         }

         return htmltext;
      }
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      QuestState st = player.getQuestState("_106_ForgottenTruth");
      if (st == null) {
         return null;
      } else {
         int cond = st.getInt("cond");
         if (cond == 2 && getRandom(100) < 20) {
            if (st.getQuestItemsCount(986) == 0L) {
               st.giveItems(986, 1L);
               st.playSound("Itemsound.quest_itemget");
            } else if (st.getQuestItemsCount(987) == 0L) {
               st.giveItems(987, 1L);
               st.playSound("ItemSound.quest_middle");
               st.set("cond", "3");
            }
         }

         return null;
      }
   }

   public static void main(String[] args) {
      new _106_ForgottenTruth(106, "_106_ForgottenTruth", "");
   }
}
