package l2e.scripts.custom;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.EtcStatusUpdate;
import l2e.scripts.ai.AbstractNpcAI;

public class BlackJudge extends AbstractNpcAI {
   private static final int[] COSTS = new int[]{3600, 8640, 25200, 50400, 86400, 144000};

   private BlackJudge() {
      super(BlackJudge.class.getSimpleName(), "custom");
      this.addStartNpc(30981);
      this.addTalkId(30981);
      this.addFirstTalkId(30981);
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = null;
      int level = player.getExpertiseLevel() < 5 ? player.getExpertiseLevel() : 5;
      switch(event) {
         case "remove_info":
            htmltext = "30981-0" + (level + 1) + ".htm";
            break;
         case "remove_dp":
            if (player.getDeathPenaltyBuffLevel() > 0) {
               int cost = COSTS[level];
               if (player.getAdena() >= (long)cost) {
                  takeItems(player, 57, (long)cost);
                  player.setDeathPenaltyBuffLevel(player.getDeathPenaltyBuffLevel() - 1);
                  player.sendPacket(SystemMessageId.DEATH_PENALTY_LIFTED);
                  player.sendPacket(new EtcStatusUpdate(player));
               } else {
                  htmltext = "30981-07.htm";
               }
            } else {
               htmltext = "30981-08.htm";
            }
      }

      return htmltext;
   }

   public static void main(String[] args) {
      new BlackJudge();
   }
}
