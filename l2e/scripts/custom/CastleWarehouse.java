package l2e.scripts.custom;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;
import l2e.scripts.ai.AbstractNpcAI;

public class CastleWarehouse extends AbstractNpcAI {
   private static final int[] NPCS = new int[]{35099, 35141, 35183, 35225, 35273, 35315, 35362, 35508, 35554};
   private static final int BLOOD_OATH = 9910;
   private static final int BLOOD_ALLIANCE = 9911;

   private CastleWarehouse(String name, String descr) {
      super(name, descr);
      this.addStartNpc(NPCS);
      this.addTalkId(NPCS);
      this.addFirstTalkId(NPCS);
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      NpcHtmlMessage html = new NpcHtmlMessage(npc.getObjectId());
      switch(event) {
         case "warehouse-01.htm":
         case "warehouse-02.htm":
         case "warehouse-03.htm":
            break;
         case "warehouse-04.htm":
            if (npc.isMyLord(player)) {
               html.setFile(player, "data/scripts/custom/CastleWarehouse/" + player.getLang() + "/warehouse-04.htm");
               html.replace("%blood%", (long)player.getClan().getBloodAllianceCount());
            } else {
               html.setFile(player, player.getLang(), "data/scripts/custom/CastleWarehouse/" + player.getLang() + "/warehouse-no.htm");
            }

            player.sendPacket(html);
            return null;
         case "Receive":
            if (!npc.isMyLord(player)) {
               htmltext = "warehouse-no.htm";
            } else if (player.getClan().getBloodAllianceCount() == 0) {
               htmltext = "warehouse-05.htm";
            } else {
               giveItems(player, 9911, (long)player.getClan().getBloodAllianceCount());
               player.getClan().resetBloodAllianceCount();
               htmltext = "warehouse-06.htm";
            }
            break;
         case "Exchange":
            if (!npc.isMyLord(player)) {
               htmltext = "warehouse-no.htm";
            } else if (!hasQuestItems(player, 9911)) {
               htmltext = "warehouse-08.htm";
            } else {
               takeItems(player, 9911, 1L);
               giveItems(player, 9910, 30L);
               htmltext = "warehouse-07.htm";
            }
            break;
         default:
            htmltext = null;
      }

      return htmltext;
   }

   @Override
   public String onFirstTalk(Npc npc, Player player) {
      return "warehouse-01.htm";
   }

   public static void main(String[] args) {
      new CastleWarehouse(CastleWarehouse.class.getSimpleName(), "custom");
   }
}
