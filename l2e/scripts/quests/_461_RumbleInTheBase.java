package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _461_RumbleInTheBase extends Quest {
   private static final String qn = "_461_RumbleInTheBase";
   private static final int STAN = 30200;
   private static final int COOK = 18908;
   private static final int[] MOB = new int[]{22780, 22782, 22784};
   private static final int fish = 15503;
   private static final int shoes = 16382;

   public _461_RumbleInTheBase(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30200);
      this.addTalkId(30200);

      for(int npcId : MOB) {
         this.addKillId(npcId);
      }

      this.addKillId(18908);
      this.questItemIds = new int[]{15503, 16382};
   }

   @Override
   public final String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState("_461_RumbleInTheBase");
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("30200-05.htm")) {
            st.setState((byte)1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
         }

         return event;
      }
   }

   @Override
   public final String onTalk(Npc npc, Player player) {
      QuestState st = player.getQuestState("_461_RumbleInTheBase");
      int id = st.getState();
      int npcId = npc.getId();
      int cond = st.getInt("cond");
      String htmltext = getNoQuestMsg(player);
      QuestState _prev = player.getQuestState("_252_ItSmellsDelicious");
      if (npcId == 30200) {
         if (id == 0) {
            if (_prev != null && _prev.getState() == 2 && player.getLevel() >= 82) {
               htmltext = "30200-01.htm";
            } else {
               htmltext = "30200-02.htm";
            }
         } else if (id == 1) {
            if (cond == 1) {
               htmltext = "30200-06.htm";
            } else {
               st.addExpAndSp(224784, 342528);
               st.playSound("ItemSound.quest_finish");
               st.takeItems(15503, -1L);
               st.takeItems(16382, -1L);
               st.exitQuest(QuestState.QuestType.DAILY);
               htmltext = "30200-07.htm";
            }
         } else if (id == 2) {
            if (!st.isNowAvailable()) {
               htmltext = "30200-03.htm";
            } else {
               st.setState((byte)0);
               if (_prev != null && _prev.getState() == 2 && player.getLevel() >= 82) {
                  htmltext = "30200-01.htm";
               } else {
                  htmltext = "30200-02.htm";
               }
            }
         }
      }

      return htmltext;
   }

   @Override
   public final String onKill(Npc npc, Player player, boolean isSummon) {
      QuestState st = player.getQuestState("_461_RumbleInTheBase");
      if (st == null) {
         return null;
      } else {
         int npcId = npc.getId();
         if (st.getState() == 1 && Integer.parseInt(st.get("cond")) == 1) {
            long random = (long)getRandom(10);
            if (npcId == 18908 && st.getQuestItemsCount(15503) < 5L && random < 3L) {
               st.giveItems(15503, 1L);
               st.playSound("ItemSound.quest_itemget");
            }

            if ((npcId == MOB[0] || npcId == MOB[1] || npcId == MOB[2]) && st.getQuestItemsCount(16382) < 10L && random >= 4L) {
               st.giveItems(16382, 1L);
               st.playSound("ItemSound.quest_itemget");
            }

            if (st.getQuestItemsCount(15503) == 5L && st.getQuestItemsCount(16382) == 10L) {
               st.set("cond", "2");
               st.playSound("ItemSound.quest_middle");
            }
         }

         return null;
      }
   }

   public static void main(String[] args) {
      new _461_RumbleInTheBase(461, "_461_RumbleInTheBase", "");
   }
}
