package l2e.scripts.custom;

import java.util.HashMap;
import java.util.Map;
import l2e.commons.util.Util;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class EchoCrystals extends Quest {
   private static final String qn = "EchoCrystals";
   private static final int[] NPCs = new int[]{31042, 31043};
   private static final int ADENA = 57;
   private static final int COST = 200;
   private static final Map<Integer, EchoCrystals.ScoreData> SCORES = new HashMap<>();

   public EchoCrystals(int questId, String name, String descr) {
      super(questId, name, descr);
      SCORES.put(4410, new EchoCrystals.ScoreData(4411, "01", "02", "03"));
      SCORES.put(4409, new EchoCrystals.ScoreData(4412, "04", "05", "06"));
      SCORES.put(4408, new EchoCrystals.ScoreData(4413, "07", "08", "09"));
      SCORES.put(4420, new EchoCrystals.ScoreData(4414, "10", "11", "12"));
      SCORES.put(4421, new EchoCrystals.ScoreData(4415, "13", "14", "15"));
      SCORES.put(4419, new EchoCrystals.ScoreData(4417, "16", "05", "06"));
      SCORES.put(4418, new EchoCrystals.ScoreData(4416, "17", "05", "06"));

      for(int npc : NPCs) {
         this.addStartNpc(npc);
         this.addTalkId(npc);
         this.addFirstTalkId(npc);
      }
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = "";
      QuestState st = player.getQuestState("EchoCrystals");
      if (st != null && Util.isDigit(event)) {
         int score = Integer.parseInt(event);
         if (SCORES.containsKey(score)) {
            int crystal = SCORES.get(score).getCrystalId();
            String ok = SCORES.get(score).getOkMsg();
            String noadena = SCORES.get(score).getNoAdenaMsg();
            String noscore = SCORES.get(score).getNoScoreMsg();
            if (!st.hasQuestItems(score)) {
               htmltext = npc.getId() + "-" + noscore + ".htm";
            } else if (st.getQuestItemsCount(57) < 200L) {
               htmltext = npc.getId() + "-" + noadena + ".htm";
            } else {
               st.takeItems(57, 200L);
               st.giveItems(crystal, 1L);
               htmltext = npc.getId() + "-" + ok + ".htm";
            }
         }

         return htmltext;
      } else {
         return htmltext;
      }
   }

   @Override
   public String onFirstTalk(Npc npc, Player player) {
      int npcId = npc.getId();
      if (npcId == 31042) {
         return "1.htm";
      } else {
         return npcId == 31043 ? "2.htm" : null;
      }
   }

   public static void main(String[] args) {
      new EchoCrystals(-1, "EchoCrystals", "custom");
   }

   private class ScoreData {
      private final int crystalId;
      private final String okMsg;
      private final String noAdenaMsg;
      private final String noScoreMsg;

      public ScoreData(int crystalId, String okMsg, String noAdenaMsg, String noScoreMsg) {
         this.crystalId = crystalId;
         this.okMsg = okMsg;
         this.noAdenaMsg = noAdenaMsg;
         this.noScoreMsg = noScoreMsg;
      }

      public int getCrystalId() {
         return this.crystalId;
      }

      public String getOkMsg() {
         return this.okMsg;
      }

      public String getNoAdenaMsg() {
         return this.noAdenaMsg;
      }

      public String getNoScoreMsg() {
         return this.noScoreMsg;
      }
   }
}
