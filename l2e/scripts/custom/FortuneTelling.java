package l2e.scripts.custom;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;

public class FortuneTelling extends Quest {
   private static final String qn = "FortuneTelling";
   private static final int NPC_ID = 32616;
   private static final int COST = 1000;

   public FortuneTelling(int id, String name, String desc) {
      super(id, name, desc);
      this.addStartNpc(32616);
      this.addTalkId(32616);
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      NpcHtmlMessage html = new NpcHtmlMessage(npc.getObjectId());
      if (player.getAdena() < 1000L) {
         html.setFile(player, "data/scripts/custom/FortuneTelling/" + player.getLang() + "/lowadena.htm");
         player.sendPacket(html);
      } else {
         takeItems(player, 57, 1000L);
         html.setFile(player, "data/scripts/custom/FortuneTelling/" + player.getLang() + "/fortune.htm");
         html.replace("%fortune%", "<fstring>" + (1800309 + getRandom(386)) + "</fstring>");
         player.sendPacket(html);
      }

      return "";
   }

   public static void main(String[] args) {
      new FortuneTelling(-1, "FortuneTelling", "custom");
   }
}
