package l2e.scripts.custom;

import l2e.gameserver.instancemanager.MapRegionManager;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.entity.Siege;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;
import l2e.gameserver.network.serverpackets.NpcSay;
import l2e.scripts.ai.AbstractNpcAI;

public class CastleTeleporter extends AbstractNpcAI {
   private static final int[] NPCS = new int[]{35095, 35137, 35179, 35221, 35266, 35311, 35355, 35502, 35547};

   private CastleTeleporter(String name, String descr) {
      super(name, descr);
      this.addStartNpc(NPCS);
      this.addTalkId(NPCS);
      this.addFirstTalkId(NPCS);
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      if (event.equalsIgnoreCase("teleporter-03.htm")) {
         if (npc.isScriptValue(0)) {
            Siege siege = npc.getCastle().getSiege();
            int time = siege.getIsInProgress() && siege.getControlTowerCount() == 0 ? 480000 : 30000;
            this.startQuestTimer("teleport", (long)time, npc, null);
            npc.getVariables().set("teleport", System.currentTimeMillis() + (long)time);
            npc.setScriptValue(1);
         }

         this.replaceInfo(npc, player, player.getLang(), "teleporter-03.htm");
         return null;
      } else {
         if (event.equalsIgnoreCase("teleport")) {
            int region = MapRegionManager.getInstance().getMapRegionLocId(npc.getX(), npc.getY());
            NpcSay msg = new NpcSay(npc, 23, NpcStringId.THE_DEFENDERS_OF_S1_CASTLE_WILL_BE_TELEPORTED_TO_THE_INNER_CASTLE);
            msg.addStringParameter(npc.getCastle().getName());
            npc.getCastle().oustAllPlayers();
            npc.setScriptValue(0);

            for(Player pl : World.getInstance().getAllPlayers()) {
               if (region == MapRegionManager.getInstance().getMapRegionLocId(pl)) {
                  pl.sendPacket(msg);
               }
            }
         }

         return null;
      }
   }

   @Override
   public String onFirstTalk(Npc npc, Player player) {
      Siege siege = npc.getCastle().getSiege();
      if (npc.isScriptValue(1)) {
         this.replaceInfo(npc, player, player.getLang(), "teleporter-03.htm");
         return null;
      } else {
         return siege.getIsInProgress() && siege.getControlTowerCount() == 0 ? "teleporter-02.htm" : "teleporter-01.htm";
      }
   }

   private void replaceInfo(Npc npc, Player player, String lang, String htmlFile) {
      NpcHtmlMessage html = new NpcHtmlMessage(npc.getObjectId());
      long delay = (npc.getVariables().getLong("teleport") - System.currentTimeMillis()) / 1000L;
      int mins = (int)(delay / 60L);
      int secs = (int)(delay - (long)(mins * 60));
      String Strmins = mins > 0 ? "" + mins + ":" : "";
      String Strsecs = "" + secs + "";
      html.setFile(player, "data/scripts/custom/CastleTeleporter/" + player.getLang() + "/" + htmlFile);
      html.replace("%time%", "<font color=\"LEVEL\">" + Strmins + "" + Strsecs + "</font>");
      player.sendPacket(html);
   }

   public static void main(String[] args) {
      new CastleTeleporter(CastleTeleporter.class.getSimpleName(), "custom");
   }
}
