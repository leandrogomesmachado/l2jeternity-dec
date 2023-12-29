package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.network.NpcStringId;

public class _296_SilkOfTarantula extends Quest {
   private static final String qn = "_296_SilkOfTarantula";
   private static final int TARANTULA_SPIDER_SILK = 1493;
   private static final int TARANTULA_SPINNERETTE = 1494;
   private static final int RING_OF_RACCOON = 1508;
   private static final int RING_OF_FIREFLY = 1509;

   public _296_SilkOfTarantula(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30519);
      this.addTalkId(30519);
      this.addTalkId(30548);
      this.addKillId(20394);
      this.addKillId(20403);
      this.addKillId(20508);
      this.questItemIds = new int[]{1493, 1494};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      QuestState st = player.getQuestState("_296_SilkOfTarantula");
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("30519-03.htm")) {
            st.set("cond", "1");
            st.setState((byte)1);
            st.playSound("ItemSound.quest_accept");
         } else if (event.equalsIgnoreCase("30519-06.htm")) {
            st.takeItems(1494, -1L);
            st.exitQuest(true);
            st.playSound("ItemSound.quest_finish");
         } else if (event.equalsIgnoreCase("30548-02.htm")) {
            if (st.getQuestItemsCount(1494) >= 1L) {
               htmltext = "30548-03.htm";
               st.giveItems(1493, 17L);
               st.takeItems(1494, -1L);
            }
         } else if (event.equalsIgnoreCase("30519-09.htm")) {
            st.exitQuest(true);
         }

         return htmltext;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      QuestState st = player.getQuestState("_296_SilkOfTarantula");
      String htmltext = getNoQuestMsg(player);
      if (st == null) {
         return htmltext;
      } else {
         int npcId = npc.getId();
         int cond = st.getInt("cond");
         int id = st.getState();
         int onlyone = st.getInt("onlyone");
         if (npcId != 30519 && id != 1) {
            return htmltext;
         } else {
            if (id == 0) {
               st.set("cond", "0");
            }

            if (npcId == 30519) {
               if (cond == 0) {
                  if (player.getLevel() >= 15) {
                     if (st.getQuestItemsCount(1508) <= 0L && st.getQuestItemsCount(1509) <= 0L) {
                        return "30519-08.htm";
                     }

                     htmltext = "30519-02.htm";
                  } else {
                     htmltext = "30519-01.htm";
                     st.exitQuest(true);
                  }
               } else if (st.getQuestItemsCount(1493) < 1L) {
                  htmltext = "30519-04.htm";
               } else if (st.getQuestItemsCount(1493) >= 1L) {
                  htmltext = "30519-05.htm";
                  st.giveItems(57, st.getQuestItemsCount(1493) * 23L);
                  st.takeItems(1493, -1L);
                  if (onlyone == 0) {
                     st.set("onlyone", "1");
                     st.playTutorialVoice("tutorial_voice_026");
                     showOnScreenMsg(player, NpcStringId.LAST_DUTY_COMPLETE_N_GO_FIND_THE_NEWBIE_GUIDE, 2, 5000, new String[0]);
                  }
               }
            } else if (npcId == 30548 && cond == 1) {
               htmltext = "30548-01.htm";
            }

            return htmltext;
         }
      }
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      QuestState st = player.getQuestState("_296_SilkOfTarantula");
      if (st == null) {
         return null;
      } else {
         int n = getRandom(100);
         int cond = st.getInt("cond");
         if (cond == 1) {
            if (n > 95) {
               st.giveItems(1494, 1L);
               st.playSound("ItemSound.quest_itemget");
            } else if (n > 45) {
               st.giveItems(1493, 1L);
               st.playSound("ItemSound.quest_itemget");
            }
         }

         return null;
      }
   }

   public static void main(String[] args) {
      new _296_SilkOfTarantula(296, "_296_SilkOfTarantula", "");
   }
}
