package l2e.scripts.quests;

import l2e.commons.util.Rnd;
import l2e.gameserver.Config;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _381_LetsBecomeARoyalMember extends Quest {
   private static final String qn = "_381_LetsBecomeARoyalMember";
   private static int SORINT = 30232;
   private static int SANDRA = 30090;
   private static int KAILS_COIN = 5899;
   private static int COIN_ALBUM = 5900;
   private static int MEMBERSHIP_1 = 3813;
   private static int CLOVER_COIN = 7569;
   private static int MEMBERSHIP = 5898;
   private static int GARGOYLE = 21018;
   private static int VEGUS = 27316;

   public _381_LetsBecomeARoyalMember(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(SORINT);
      this.addTalkId(SORINT);
      this.addTalkId(SANDRA);
      this.addKillId(GARGOYLE);
      this.addKillId(VEGUS);
      this.questItemIds = new int[]{KAILS_COIN, COIN_ALBUM, CLOVER_COIN};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      QuestState st = player.getQuestState("_381_LetsBecomeARoyalMember");
      if (st == null) {
         return null;
      } else {
         if (event.equalsIgnoreCase("30232-02.htm")) {
            if (player.getLevel() >= 55 && st.getQuestItemsCount(MEMBERSHIP_1) > 0L) {
               st.set("cond", "1");
               st.setState((byte)1);
               st.playSound("ItemSound.quest_accept");
               htmltext = "30232-03.htm";
            } else {
               st.exitQuest(true);
            }
         } else if (event.equalsIgnoreCase("30090-02.htm") && st.getInt("cond") == 1) {
            st.set("id", "1");
            st.playSound("ItemSound.quest_accept");
         }

         return htmltext;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = Quest.getNoQuestMsg(player);
      QuestState st = player.getQuestState("_381_LetsBecomeARoyalMember");
      if (st == null) {
         return htmltext;
      } else {
         int cond = st.getInt("cond");
         int npcId = npc.getId();
         long album = st.getQuestItemsCount(COIN_ALBUM);
         switch(st.getState()) {
            case 0:
               if (npcId == SORINT) {
                  htmltext = "30232-01.htm";
               }
               break;
            case 1:
               if (npcId == SORINT) {
                  if (cond == 1) {
                     long coin = st.getQuestItemsCount(KAILS_COIN);
                     if (coin > 0L && album > 0L) {
                        st.takeItems(KAILS_COIN, -1L);
                        st.takeItems(COIN_ALBUM, -1L);
                        st.giveItems(MEMBERSHIP, 1L);
                        st.playSound("ItemSound.quest_finish");
                        st.exitQuest(true);
                        htmltext = "30232-06.htm";
                     } else if (album == 0L) {
                        htmltext = "30232-05.htm";
                     } else if (coin == 0L) {
                        htmltext = "30232-04.htm";
                     }
                  }
               } else if (npcId == SANDRA) {
                  long clover = st.getQuestItemsCount(CLOVER_COIN);
                  if (album > 0L) {
                     htmltext = "30090-05.htm";
                  } else if (clover > 0L) {
                     st.takeItems(CLOVER_COIN, -1L);
                     st.giveItems(COIN_ALBUM, 1L);
                     st.playSound("ItemSound.quest_itemget");
                     htmltext = "30090-04.htm";
                  } else if (st.getInt("id") == 0) {
                     htmltext = "30090-01.htm";
                  } else {
                     htmltext = "30090-03.htm";
                  }
               }
         }

         return htmltext;
      }
   }

   @Override
   public final String onKill(Npc npc, Player player, boolean isSummon) {
      QuestState st = player.getQuestState("_381_LetsBecomeARoyalMember");
      if (st != null && st.isStarted()) {
         int npcId = npc.getId();
         long album = st.getQuestItemsCount(COIN_ALBUM);
         long coin = st.getQuestItemsCount(KAILS_COIN);
         long clover = st.getQuestItemsCount(CLOVER_COIN);
         if (npcId == GARGOYLE && coin == 0L) {
            if (Rnd.chance((double)(5.0F * Config.RATE_QUEST_DROP))) {
               st.giveItems(KAILS_COIN, 1L);
               if (album <= 0L && clover <= 0L) {
                  st.playSound("ItemSound.quest_itemget");
               } else {
                  st.playSound("ItemSound.quest_middle");
               }
            }
         } else if (npcId == VEGUS && clover + album == 0L && st.getInt("id") != 0 && Rnd.chance((double)(100.0F * Config.RATE_QUEST_DROP))) {
            st.giveItems(CLOVER_COIN, 1L);
            if (coin > 0L) {
               st.playSound("ItemSound.quest_middle");
            } else {
               st.playSound("ItemSound.quest_itemget");
            }
         }

         return null;
      } else {
         return null;
      }
   }

   public static void main(String[] args) {
      new _381_LetsBecomeARoyalMember(381, "_381_LetsBecomeARoyalMember", "");
   }
}
