package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _371_ShriekOfGhosts extends Quest {
   private static final String qn = "_371_ShriekOfGhosts";
   private static final int REVA = 30867;
   private static final int PATRIN = 30929;
   private static final int URN = 5903;
   private static final int PORCELAIN = 6002;
   private static final int HALLATE_WARRIOR = 20818;
   private static final int HALLATE_KNIGHT = 20820;
   private static final int HALLATE_COMMANDER = 20824;

   public _371_ShriekOfGhosts(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30867);
      this.addTalkId(new int[]{30867, 30929});
      this.addKillId(new int[]{20818, 20820, 20824});
      this.questItemIds = new int[]{5903, 6002};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      QuestState st = player.getQuestState("_371_ShriekOfGhosts");
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("30867-03.htm")) {
            st.set("cond", "1");
            st.setState((byte)1);
            st.playSound("ItemSound.quest_accept");
         } else if (event.equalsIgnoreCase("30867-07.htm")) {
            long urns = st.getQuestItemsCount(5903);
            if (urns > 0L) {
               st.takeItems(5903, urns);
               if (urns >= 100L) {
                  urns += 13L;
                  htmltext = "30867-08.htm";
               } else {
                  urns += 7L;
               }

               st.rewardItems(57, urns * 1000L);
            }
         } else if (event.equalsIgnoreCase("30867-10.htm")) {
            st.playSound("ItemSound.quest_giveup");
            st.exitQuest(true);
         } else if (event.equalsIgnoreCase("APPR")) {
            if (st.hasQuestItems(6002)) {
               int chance = getRandom(100);
               st.takeItems(6002, 1L);
               if (chance < 2) {
                  st.giveItems(6003, 1L);
                  htmltext = "30929-03.htm";
               } else if (chance < 32) {
                  st.giveItems(6004, 1L);
                  htmltext = "30929-04.htm";
               } else if (chance < 62) {
                  st.giveItems(6005, 1L);
                  htmltext = "30929-05.htm";
               } else if (chance < 77) {
                  st.giveItems(6006, 1L);
                  htmltext = "30929-06.htm";
               } else {
                  htmltext = "30929-07.htm";
               }
            } else {
               htmltext = "30929-02.htm";
            }
         }

         return htmltext;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      QuestState st = player.getQuestState("_371_ShriekOfGhosts");
      String htmltext = getNoQuestMsg(player);
      if (st == null) {
         return htmltext;
      } else {
         switch(st.getState()) {
            case 0:
               if (player.getLevel() >= 59) {
                  htmltext = "30867-02.htm";
               } else {
                  htmltext = "30867-01.htm";
                  st.exitQuest(true);
               }
               break;
            case 1:
               switch(npc.getId()) {
                  case 30867:
                     if (st.hasQuestItems(5903)) {
                        htmltext = st.hasQuestItems(6002) ? "30867-05.htm" : "30867-04.htm";
                     } else {
                        htmltext = "30867-06.htm";
                     }
                     break;
                  case 30929:
                     htmltext = "30929-01.htm";
               }
         }

         return htmltext;
      }
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      Player partyMember = this.getRandomPartyMemberState(player, (byte)1);
      if (partyMember == null) {
         return null;
      } else {
         QuestState st = partyMember.getQuestState("_371_ShriekOfGhosts");
         int chance = getRandom(100);
         switch(npc.getId()) {
            case 20818:
               if (chance < 43) {
                  st.giveItems(chance < 38 ? 5903 : 6002, 1L);
                  st.playSound("ItemSound.quest_itemget");
               }
               break;
            case 20820:
               if (chance < 56) {
                  st.giveItems(chance < 48 ? 5903 : 6002, 1L);
                  st.playSound("ItemSound.quest_itemget");
               }
               break;
            case 20824:
               if (chance < 58) {
                  st.giveItems(chance < 50 ? 5903 : 6002, 1L);
                  st.playSound("ItemSound.quest_itemget");
               }
         }

         return null;
      }
   }

   public static void main(String[] args) {
      new _371_ShriekOfGhosts(371, "_371_ShriekOfGhosts", "");
   }
}
