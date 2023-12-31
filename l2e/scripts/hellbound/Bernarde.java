package l2e.scripts.hellbound;

import l2e.gameserver.instancemanager.HellboundManager;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;

public class Bernarde extends Quest {
   private static final int BERNARDE = 32300;
   private static final int NATIVE_TRANSFORM = 101;
   private static final int HOLY_WATER = 9673;
   private static final int DARION_BADGE = 9674;
   private static final int TREASURE = 9684;

   private static final boolean isTransformed(Player player) {
      return player.isTransformed() && player.getTransformation().getId() == 101;
   }

   @Override
   public final String onAdvEvent(String event, Npc npc, Player player) {
      if ("HolyWater".equalsIgnoreCase(event)) {
         if (HellboundManager.getInstance().getLevel() == 2
            && player.getInventory().getInventoryItemCount(9674, -1, false) >= 5L
            && player.exchangeItemsById("Quest", npc, 9674, 5L, 9673, 1L, true)) {
            return "32300-02b.htm";
         }

         event = "32300-02c.htm";
      } else if ("Treasure".equalsIgnoreCase(event)) {
         if (HellboundManager.getInstance().getLevel() == 3
            && player.getInventory().getInventoryItemCount(9684, -1, false) > 0L
            && player.destroyItemByItemId("Quest", 9684, player.getInventory().getInventoryItemCount(9684, -1, false), npc, true)) {
            HellboundManager.getInstance().updateTrust((int)(player.getInventory().getInventoryItemCount(9684, -1, false) * 1000L), true);
            return "32300-02d.htm";
         }

         event = "32300-02e.htm";
      } else if ("rumors".equalsIgnoreCase(event)) {
         event = "32300-" + HellboundManager.getInstance().getLevel() + "r.htm";
      }

      return event;
   }

   @Override
   public final String onFirstTalk(Npc npc, Player player) {
      if (player.getQuestState(this.getName()) == null) {
         this.newQuestState(player);
      }

      switch(HellboundManager.getInstance().getLevel()) {
         case 0:
         case 1:
            return isTransformed(player) ? "32300-01a.htm" : "32300-01.htm";
         case 2:
            return isTransformed(player) ? "32300-02.htm" : "32300-03.htm";
         case 3:
            return isTransformed(player) ? "32300-01c.htm" : "32300-03.htm";
         case 4:
            return isTransformed(player) ? "32300-01d.htm" : "32300-03.htm";
         default:
            return isTransformed(player) ? "32300-01f.htm" : "32300-03.htm";
      }
   }

   public Bernarde(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addFirstTalkId(32300);
      this.addStartNpc(32300);
      this.addTalkId(32300);
   }

   public static void main(String[] args) {
      new Bernarde(-1, Bernarde.class.getSimpleName(), "hellbound");
   }
}
