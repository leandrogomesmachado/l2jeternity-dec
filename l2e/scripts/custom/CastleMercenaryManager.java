package l2e.scripts.custom;

import java.util.StringTokenizer;
import l2e.gameserver.SevenSigns;
import l2e.gameserver.model.PcCondOverride;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.instance.MerchantInstance;
import l2e.gameserver.model.entity.Castle;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;
import l2e.scripts.ai.AbstractNpcAI;

public class CastleMercenaryManager extends AbstractNpcAI {
   private static final int[] NPCS = new int[]{35102, 35144, 35186, 35228, 35276, 35318, 35365, 35511, 35557};

   private CastleMercenaryManager(String name, String descr) {
      super(name, descr);
      this.addStartNpc(NPCS);
      this.addTalkId(NPCS);
      this.addFirstTalkId(NPCS);
   }

   private boolean hasRights(Player player, Npc npc) {
      return player.canOverrideCond(PcCondOverride.CASTLE_CONDITIONS)
         || player.getId() == npc.getCastle().getOwnerId() && (player.getClanPrivileges() & 4194304) == 4194304;
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = null;
      StringTokenizer st = new StringTokenizer(event, " ");
      String var6 = st.nextToken();
      switch(var6) {
         case "limit":
            Castle castle = npc.getCastle();
            NpcHtmlMessage html = new NpcHtmlMessage(npc.getObjectId());
            if (castle.getName().equals("Aden")) {
               html.setFile(player, player.getLang(), "data/scripts/custom/CastleMercenaryManager/" + player.getLang() + "/mercmanager-aden-limit.htm");
            } else if (castle.getName().equals("Rune")) {
               html.setFile(player, player.getLang(), "data/scripts/custom/CastleMercenaryManager/" + player.getLang() + "/mercmanager-rune-limit.htm");
            } else {
               html.setFile(player, player.getLang(), "data/scripts/custom/CastleMercenaryManager/" + player.getLang() + "/mercmanager-limit.htm");
            }

            html.replace("%feud_name%", String.valueOf(1001000 + castle.getId()));
            player.sendPacket(html);
            break;
         case "buy":
            if (SevenSigns.getInstance().isSealValidationPeriod()) {
               htmltext = "mercmanager-ssq.htm";
            } else {
               int listId = Integer.parseInt(npc.getId() + st.nextToken());
               ((MerchantInstance)npc).showBuyWindow(player, listId, false);
            }
            break;
         case "main":
            htmltext = this.onFirstTalk(npc, player);
            break;
         case "mercmanager-01.htm":
            htmltext = event;
      }

      return htmltext;
   }

   @Override
   public String onFirstTalk(Npc npc, Player player) {
      String htmltext;
      if (this.hasRights(player, npc)) {
         if (npc.getCastle().getSiege().getIsInProgress()) {
            htmltext = "mercmanager-siege.htm";
         } else if (SevenSigns.getInstance().getSealOwner(3) == 1) {
            htmltext = "mercmanager-dusk.htm";
         } else if (SevenSigns.getInstance().getSealOwner(3) == 2) {
            htmltext = "mercmanager-dawn.htm";
         } else {
            htmltext = "mercmanager.htm";
         }
      } else {
         htmltext = "mercmanager-no.htm";
      }

      return htmltext;
   }

   public static void main(String[] args) {
      new CastleMercenaryManager(CastleMercenaryManager.class.getSimpleName(), "custom");
   }
}
