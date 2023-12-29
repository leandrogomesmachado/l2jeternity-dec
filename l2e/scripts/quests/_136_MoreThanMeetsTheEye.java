package l2e.scripts.quests;

import java.util.HashMap;
import java.util.Map;
import l2e.commons.util.Util;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public final class _136_MoreThanMeetsTheEye extends Quest {
   private static final String qn = "_136_MoreThanMeetsTheEye";
   private static final int HARDIN = 30832;
   private static final int ERRICKIN = 30701;
   private static final int CLAYTON = 30464;
   private static final int ECTOPLASM = 9787;
   private static final int STABILIZED_ECTOPLASM = 9786;
   private static final int ORDER = 9788;
   private static final int GLASS_JAGUAR_CRYSTAL = 9789;
   private static final int BOOK_OF_SEAL = 9790;
   private static final int ADENA = 57;
   private static final int TRANSFORM_BOOK = 9648;
   private static final int[] KILLD_IDS_ECTOPLASM = new int[]{20636, 20637, 20638, 20639};
   private static final int GLASS_JAGUAR = 20250;
   private static Map<Integer, int[]> DROPLIST = new HashMap<>();

   public _136_MoreThanMeetsTheEye(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30832);
      this.addTalkId(30832);
      this.addTalkId(30701);
      this.addTalkId(30464);

      for(int mob : DROPLIST.keySet()) {
         this.addKillId(mob);
      }

      this.questItemIds = new int[]{9787, 9786, 9788, 9789, 9790};
   }

   @Override
   public final String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState("_136_MoreThanMeetsTheEye");
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("30832-02.htm")) {
            st.set("cond", "1");
            st.setState((byte)1);
            st.playSound("ItemSound.quest_accept");
         } else if (event.equalsIgnoreCase("30832-05.htm")) {
            st.set("cond", "2");
            st.playSound("ItemSound.quest_middle");
         } else if (event.equalsIgnoreCase("30832-10.htm")) {
            st.takeItems(9786, 1L);
            st.giveItems(9788, 1L);
            st.set("cond", "6");
            st.playSound("ItemSound.quest_middle");
         } else if (event.equalsIgnoreCase("30832-14.htm")) {
            st.takeItems(9790, 1L);
            st.giveItems(57, 67550L);
            st.giveItems(9648, 1L);
            st.playSound("ItemSound.quest_finish");
            st.setState((byte)2);
            st.exitQuest(false);
         } else if (event.equalsIgnoreCase("30701-02.htm")) {
            st.set("cond", "3");
            st.playSound("ItemSound.quest_middle");
         } else if (event.equalsIgnoreCase("30464-02.htm")) {
            st.takeItems(9788, 1L);
            st.set("cond", "7");
            st.playSound("ItemSound.quest_middle");
         }

         return event;
      }
   }

   @Override
   public final String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState("_136_MoreThanMeetsTheEye");
      if (st == null) {
         return htmltext;
      } else {
         int cond = st.getInt("cond");
         int npcId = npc.getId();
         byte id = st.getState();
         if (id == 2) {
            htmltext = getAlreadyCompletedMsg(player);
         } else if (npcId == 30832) {
            if (cond == 0) {
               if (player.getLevel() >= 50) {
                  htmltext = "30832-01.htm";
               } else {
                  htmltext = "30832-00.htm";
                  st.exitQuest(true);
               }
            } else if (cond == 1) {
               htmltext = "30832-02.htm";
            } else if (cond == 2 || cond == 3 || cond == 4) {
               htmltext = "30832-05.htm";
            } else if (cond == 5) {
               htmltext = "30832-06.htm";
            } else if (cond == 6 || cond == 7 || cond == 8) {
               htmltext = "30832-10.htm";
            } else if (cond == 9) {
               htmltext = "30832-11.htm";
            }
         } else if (npcId == 30701) {
            if (cond == 2) {
               htmltext = "30701-01.htm";
            } else if (cond == 4) {
               htmltext = "30701-03.htm";
               st.takeItems(9787, 35L);
               st.giveItems(9786, 1L);
               st.set("cond", "5");
               st.playSound("ItemSound.quest_middle");
            } else if (cond >= 5) {
               htmltext = "30701-04.htm";
            }
         } else if (npcId == 30464) {
            if (cond == 6) {
               htmltext = "30464-01.htm";
            } else if (cond == 7) {
               htmltext = "30464-04.htm";
            } else if (cond == 8) {
               htmltext = "30464-03.htm";
               st.takeItems(9789, 5L);
               st.giveItems(9790, 1L);
               st.set("cond", "9");
               st.playSound("ItemSound.quest_middle");
            } else if (cond == 9) {
               htmltext = "30464-05.htm";
            }
         }

         return htmltext;
      }
   }

   @Override
   public final String onKill(Npc npc, Player player, boolean isSummon) {
      Player partyMember = this.getRandomPartyMemberState(player, (byte)1);
      if (partyMember == null) {
         return null;
      } else {
         QuestState st = partyMember.getQuestState(this.getName());
         if (st == null) {
            return null;
         } else {
            int npcId = npc.getId();
            switch(st.getInt("cond")) {
               case 3:
                  if (Util.contains(KILLD_IDS_ECTOPLASM, npcId) && st.getQuestItemsCount(9787) < 35L) {
                     st.dropQuestItems(DROPLIST.get(npcId)[0], 1, 1, 35L, false, (float)((int[])DROPLIST.get(npcId))[1], true);
                     if (st.getQuestItemsCount(9787) >= 35L) {
                        st.set("cond", "4");
                     }
                  }
                  break;
               case 7:
                  if (npcId == 20250 && st.getQuestItemsCount(9789) < 5L) {
                     st.dropQuestItems(DROPLIST.get(npcId)[0], 1, 1, 5L, false, (float)((int[])DROPLIST.get(npcId))[1], true);
                     if (st.getQuestItemsCount(9789) >= 5L) {
                        st.set("cond", "8");
                     }
                  }
            }

            return null;
         }
      }
   }

   public static void main(String[] args) {
      new _136_MoreThanMeetsTheEye(136, "_136_MoreThanMeetsTheEye", "");
   }

   static {
      DROPLIST.put(20636, new int[]{9787, 100});
      DROPLIST.put(20637, new int[]{9787, 100});
      DROPLIST.put(20638, new int[]{9787, 100});
      DROPLIST.put(20639, new int[]{9787, 100});
      DROPLIST.put(20250, new int[]{9789, 33});
   }
}
