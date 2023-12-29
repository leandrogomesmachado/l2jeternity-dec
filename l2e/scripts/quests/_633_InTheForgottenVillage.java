package l2e.scripts.quests;

import java.util.HashMap;
import java.util.Map;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _633_InTheForgottenVillage extends Quest {
   private static final String qn = "_633_InTheForgottenVillage";
   private static final int MINA = 31388;
   private static final int RIB_BONE = 7544;
   private static final int ZOMBIE_LIVER = 7545;
   private static final Map<Integer, Integer> MOBS = new HashMap<>();
   private static final Map<Integer, Integer> UNDEADS = new HashMap<>();

   public _633_InTheForgottenVillage(int questId, String name, String descr) {
      super(questId, name, descr);
      MOBS.put(21557, 328000);
      MOBS.put(21558, 328000);
      MOBS.put(21559, 337000);
      MOBS.put(21560, 337000);
      MOBS.put(21563, 342000);
      MOBS.put(21564, 348000);
      MOBS.put(21565, 351000);
      MOBS.put(21566, 359000);
      MOBS.put(21567, 359000);
      MOBS.put(21572, 365000);
      MOBS.put(21574, 383000);
      MOBS.put(21575, 383000);
      MOBS.put(21580, 385000);
      MOBS.put(21581, 395000);
      MOBS.put(21583, 397000);
      MOBS.put(21584, 401000);
      UNDEADS.put(21553, 347000);
      UNDEADS.put(21554, 347000);
      UNDEADS.put(21561, 450000);
      UNDEADS.put(21578, 501000);
      UNDEADS.put(21596, 359000);
      UNDEADS.put(21597, 370000);
      UNDEADS.put(21598, 441000);
      UNDEADS.put(21599, 395000);
      UNDEADS.put(21600, 408000);
      UNDEADS.put(21601, 411000);
      this.addStartNpc(31388);
      this.addTalkId(31388);

      for(int i : MOBS.keySet()) {
         this.addKillId(i);
      }

      for(int i : UNDEADS.keySet()) {
         this.addKillId(i);
      }

      this.questItemIds = new int[]{7544, 7545};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      QuestState st = player.getQuestState("_633_InTheForgottenVillage");
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("31388-04.htm")) {
            st.set("cond", "1");
            st.setState((byte)1);
            st.playSound("ItemSound.quest_accept");
         } else if (event.equalsIgnoreCase("31388-10.htm")) {
            st.takeItems(7544, -1L);
            st.playSound("ItemSound.quest_giveup");
            st.exitQuest(true);
         } else if (event.equalsIgnoreCase("31388-09.htm")) {
            if (st.getQuestItemsCount(7544) >= 200L) {
               htmltext = "31388-08.htm";
               st.takeItems(7544, 200L);
               st.rewardItems(57, 25000L);
               st.addExpAndSp(305235, 0);
               st.playSound("ItemSound.quest_finish");
            }

            st.set("cond", "1");
         }

         return htmltext;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState("_633_InTheForgottenVillage");
      if (st == null) {
         return htmltext;
      } else {
         switch(st.getState()) {
            case 0:
               if (player.getLevel() >= 65) {
                  htmltext = "31388-01.htm";
               } else {
                  htmltext = "31388-03.htm";
                  st.exitQuest(true);
               }
               break;
            case 1:
               int cond = st.getInt("cond");
               if (cond == 1) {
                  htmltext = "31388-06.htm";
               } else if (cond == 2) {
                  htmltext = "31388-05.htm";
               }
         }

         return htmltext;
      }
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      int npcId = npc.getId();
      if (UNDEADS.containsKey(npcId)) {
         Player partyMember = this.getRandomPartyMemberState(player, (byte)1);
         if (partyMember == null) {
            return null;
         }

         partyMember.getQuestState("_633_InTheForgottenVillage").dropItems(7545, 1, 0L, UNDEADS.get(npcId));
      } else if (MOBS.containsKey(npcId)) {
         Player partyMember = this.getRandomPartyMember(player, 1);
         if (partyMember == null) {
            return null;
         }

         QuestState st = partyMember.getQuestState("_633_InTheForgottenVillage");
         if (st.dropItems(7544, 1, 200L, MOBS.get(npcId))) {
            st.set("cond", "2");
         }
      }

      return null;
   }

   public static void main(String[] args) {
      new _633_InTheForgottenVillage(633, "_633_InTheForgottenVillage", "");
   }
}
