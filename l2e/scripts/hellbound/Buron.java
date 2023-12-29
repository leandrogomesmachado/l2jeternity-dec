package l2e.scripts.hellbound;

import l2e.gameserver.instancemanager.HellboundManager;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class Buron extends Quest {
   private static final int BURON = 32345;
   private static final int HELMET = 9669;
   private static final int TUNIC = 9670;
   private static final int PANTS = 9671;
   private static final int DARION_BADGE = 9674;

   @Override
   public final String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext;
      if ("Rumor".equalsIgnoreCase(event)) {
         htmltext = "32345-" + HellboundManager.getInstance().getLevel() + "r.htm";
      } else if (HellboundManager.getInstance().getLevel() < 2) {
         htmltext = "32345-lowlvl.htm";
      } else {
         QuestState qs = player.getQuestState(this.getName());
         if (qs == null) {
            qs = this.newQuestState(player);
         }

         if (qs.getQuestItemsCount(9674) >= 10L) {
            qs.takeItems(9674, 10L);
            if (event.equalsIgnoreCase("Tunic")) {
               player.addItem("Quest", 9670, 1L, npc, true);
            } else if (event.equalsIgnoreCase("Helmet")) {
               player.addItem("Quest", 9669, 1L, npc, true);
            } else if (event.equalsIgnoreCase("Pants")) {
               player.addItem("Quest", 9671, 1L, npc, true);
            }

            htmltext = null;
         } else {
            htmltext = "32345-noitems.htm";
         }
      }

      return htmltext;
   }

   @Override
   public final String onFirstTalk(Npc npc, Player player) {
      if (player.getQuestState(this.getName()) == null) {
         this.newQuestState(player);
      }

      switch(HellboundManager.getInstance().getLevel()) {
         case 1:
            return "32345-01.htm";
         case 2:
         case 3:
         case 4:
            return "32345-02.htm";
         default:
            return "32345-01a.htm";
      }
   }

   public Buron(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addFirstTalkId(32345);
      this.addStartNpc(32345);
      this.addTalkId(32345);
   }

   public static void main(String[] args) {
      new Buron(-1, "Buron", "hellbound");
   }
}
