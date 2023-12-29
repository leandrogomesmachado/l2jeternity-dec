package l2e.scripts.quests;

import l2e.gameserver.Config;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _612_WarWithKetraOrcs extends Quest {
   private static final String qn = "_612_WarWithKetraOrcs";
   private final int ASHAS = 31377;
   private final int[] VARKA_MOBS = new int[]{
      21350, 21351, 21353, 21354, 21355, 21357, 21358, 21360, 21361, 21362, 21369, 21370, 21364, 21365, 21366, 21368, 21371, 21372, 21373, 21374, 21375
   };
   private final int[] KETRA_ORCS = new int[]{21324, 21327, 21328, 21329, 21331, 21332, 21334, 21336, 21338, 21339, 21340, 21342, 21343, 21345, 21347};
   private final int[][] CHANCE = new int[][]{
      {21324, 500},
      {21327, 510},
      {21328, 522},
      {21329, 519},
      {21331, 529},
      {21332, 664},
      {21334, 539},
      {21336, 529},
      {21338, 558},
      {21339, 568},
      {21340, 568},
      {21342, 578},
      {21343, 548},
      {21345, 713},
      {21347, 738}
   };
   private final int SEED = 7187;
   private final int MOLAR = 7234;

   public _612_WarWithKetraOrcs(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(31377);
      this.addTalkId(31377);

      for(int i = 0; i < this.KETRA_ORCS.length; ++i) {
         this.addKillId(this.KETRA_ORCS[i]);
      }

      for(int i = 0; i < this.VARKA_MOBS.length; ++i) {
         this.addKillId(this.VARKA_MOBS[i]);
      }

      this.questItemIds = new int[]{7234};
   }

   @Override
   public final String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      QuestState st = player.getQuestState("_612_WarWithKetraOrcs");
      if (st == null) {
         return event;
      } else {
         long molars = st.getQuestItemsCount(7234);
         if (event.equalsIgnoreCase("31377-03.htm")) {
            st.set("cond", "1");
            st.set("id", "1");
            st.setState((byte)1);
            st.playSound("ItemSound.quest_accept");
         } else if (event.equalsIgnoreCase("31377-06.htm")) {
            htmltext = "31377-06.htm";
         } else if (event.equalsIgnoreCase("31377-07.htm")) {
            if (molars >= 100L) {
               htmltext = "31377-07.htm";
               st.takeItems(7234, 100L);
               st.giveItems(7187, 20L);
            } else {
               htmltext = "31377-08.htm";
            }
         } else if (event.equalsIgnoreCase("31377-09.htm")) {
            htmltext = "31377-09.htm";
            st.unset("id");
            st.takeItems(7234, -1L);
            st.exitQuest(true);
         }

         return htmltext;
      }
   }

   @Override
   public final String onTalk(Npc npc, Player player) {
      QuestState st = player.getQuestState("_612_WarWithKetraOrcs");
      String htmltext = getNoQuestMsg(player);
      if (st == null) {
         return htmltext;
      } else {
         switch(st.getState()) {
            case 0:
               htmltext = player.getLevel() >= 75 ? "31377-01.htm" : "31377-02.htm";
               break;
            case 1:
               htmltext = st.hasQuestItems(7234) ? "31377-04.htm" : "31377-05.htm";
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
         QuestState st = partyMember.getQuestState("_612_WarWithKetraOrcs");
         if (st == null) {
            return null;
         } else {
            int npcId = npc.getId();
            long count = st.getQuestItemsCount(7234);
            QuestState st2 = partyMember.getQuestState("_605_AllianceWithKetraOrcs");
            if (this.checkArray(this.KETRA_ORCS, npcId)) {
               if (st2 == null) {
                  int chance = (int)(Config.RATE_QUEST_DROP * (float)this.getValue(this.CHANCE, npcId));
                  int numItems = chance / 100;
                  if (st.getRandom(1000) < chance) {
                     ++numItems;
                  }

                  if (numItems != 0) {
                     if ((count + (long)numItems) / 100L > count / 100L) {
                        st.playSound("ItemSound.quest_middle");
                     } else {
                        st.playSound("ItemSound.quest_itemget");
                     }

                     st.giveItems(7234, (long)numItems);
                  }
               }
            } else if (this.checkArray(this.VARKA_MOBS, npcId)) {
               st.unset("id");
               st.takeItems(7234, -1L);
               st.exitQuest(true);
            }

            return null;
         }
      }
   }

   private int getValue(int[][] array, int value) {
      for(int i = 0; i < array.length; ++i) {
         if (array[i][0] == value) {
            return array[i][1];
         }
      }

      return 0;
   }

   private boolean checkArray(int[] array, int value) {
      for(int i = 0; i < array.length; ++i) {
         if (array[i] == value) {
            return true;
         }
      }

      return false;
   }

   public static void main(String[] args) {
      new _612_WarWithKetraOrcs(612, "_612_WarWithKetraOrcs", "");
   }
}
