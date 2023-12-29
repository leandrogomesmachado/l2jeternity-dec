package l2e.scripts.quests;

import l2e.commons.util.Rnd;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.network.NpcStringId;

public class _293_HiddenVein extends Quest {
   private static final String qn = "_293_HiddenVein";
   private static int Filaur = 30535;
   private static int Chichirin = 30539;
   private static int Utuku_Orc = 20446;
   private static int Utuku_Orc_Archer = 20447;
   private static int Utuku_Orc_Grunt = 20448;
   private static int Chrysolite_Ore = 1488;
   private static int Torn_Map_Fragment = 1489;
   private static int Hidden_Ore_Map = 1490;
   private static final int NEWBIE_REWARD = 4;
   private static final int SPIRITSHOT_FOR_BEGINNERS = 5790;
   private static final int SOULSHOT_FOR_BEGINNERS = 5789;

   public _293_HiddenVein(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(Filaur);
      this.addTalkId(Filaur);
      this.addTalkId(Chichirin);
      this.addKillId(Utuku_Orc);
      this.addKillId(Utuku_Orc_Archer);
      this.addKillId(Utuku_Orc_Grunt);
      this.questItemIds = new int[]{Chrysolite_Ore, Torn_Map_Fragment, Hidden_Ore_Map};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      QuestState st = player.getQuestState("_293_HiddenVein");
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("30535-03.htm")) {
            st.set("cond", "1");
            st.setState((byte)1);
            st.playSound("ItemSound.quest_accept");
         } else if (event.equalsIgnoreCase("30535-06.htm")) {
            st.takeItems(Torn_Map_Fragment, -1L);
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(false);
         } else if (event.equalsIgnoreCase("30539-02.htm") && st.getQuestItemsCount(Torn_Map_Fragment) >= 4L) {
            htmltext = "30539-03.htm";
            st.takeItems(Torn_Map_Fragment, 4L);
            st.giveItems(Hidden_Ore_Map, 1L);
         }

         return htmltext;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      QuestState st = player.getQuestState("_293_HiddenVein");
      String htmltext = getNoQuestMsg(player);
      if (st == null) {
         return htmltext;
      } else {
         int id = st.getState();
         int npcId = npc.getId();
         if (npcId != Filaur && id != 1) {
            return htmltext;
         } else {
            if (id == 0) {
               st.set("cond", "0");
            }

            if (npcId == Filaur) {
               if (st.getInt("cond") != 0) {
                  long Chrysolite_Ore_count = st.getQuestItemsCount(Chrysolite_Ore);
                  long Hidden_Ore_Map_count = st.getQuestItemsCount(Hidden_Ore_Map);
                  long reward = st.getQuestItemsCount(Chrysolite_Ore) * 10L + st.getQuestItemsCount(Hidden_Ore_Map) * 1000L;
                  if (reward == 0L) {
                     htmltext = "30535-04.htm";
                  }

                  if (Chrysolite_Ore_count > 0L) {
                     st.takeItems(Chrysolite_Ore, -1L);
                  }

                  if (Hidden_Ore_Map_count > 0L) {
                     st.takeItems(Hidden_Ore_Map, -1L);
                  }

                  st.giveItems(57, reward);
                  int newbie = player.getNewbie();
                  if ((newbie | 4) != newbie) {
                     player.setNewbie(newbie | 4);
                     if (player.getClassId().isMage()) {
                        st.playTutorialVoice("tutorial_voice_027");
                        st.giveItems(5790, 3000L);
                     } else {
                        st.playTutorialVoice("tutorial_voice_026");
                        st.giveItems(5789, 6000L);
                     }

                     showOnScreenMsg(player, NpcStringId.ACQUISITION_OF_SOULSHOT_FOR_BEGINNERS_COMPLETE_N_GO_FIND_THE_NEWBIE_GUIDE, 2, 5000, new String[0]);
                  }

                  return Chrysolite_Ore_count > 0L && Hidden_Ore_Map_count > 0L
                     ? "30535-09.htm"
                     : (Hidden_Ore_Map_count > 0L ? "30535-08.htm" : "30535-05.htm");
               }

               if (player.getRace().ordinal() != 4) {
                  st.exitQuest(true);
                  htmltext = "30535-00.htm";
               } else if (player.getLevel() >= 6) {
                  htmltext = "30535-02.htm";
               } else {
                  st.exitQuest(true);
                  htmltext = "30535-01.htm";
               }
            }

            return npcId == Chichirin ? "30539-01.htm" : htmltext;
         }
      }
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      QuestState st = player.getQuestState("_293_HiddenVein");
      if (st == null) {
         return null;
      } else {
         if (Rnd.getChance(5)) {
            st.giveItems(Torn_Map_Fragment, 1L);
            st.playSound("ItemSound.quest_itemget");
         } else if (Rnd.getChance(45)) {
            st.giveItems(Chrysolite_Ore, 1L);
            st.playSound("ItemSound.quest_itemget");
         }

         return null;
      }
   }

   public static void main(String[] args) {
      new _293_HiddenVein(293, "_293_HiddenVein", "");
   }
}
