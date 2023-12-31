package l2e.gameserver.handler.admincommandhandlers.impl;

import java.util.List;
import java.util.StringTokenizer;
import l2e.commons.util.StringUtil;
import l2e.gameserver.data.htm.HtmCache;
import l2e.gameserver.handler.admincommandhandlers.IAdminCommandHandler;
import l2e.gameserver.instancemanager.MapRegionManager;
import l2e.gameserver.instancemanager.ZoneManager;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.TeleportWhereType;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.zone.ZoneId;
import l2e.gameserver.model.zone.ZoneType;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;

public class Zones implements IAdminCommandHandler {
   private static final String[] ADMIN_COMMANDS = new String[]{"admin_zone_check", "admin_zone_reload", "admin_zone_visual", "admin_zone_visual_clear"};

   @Override
   public boolean useAdminCommand(String command, Player activeChar) {
      if (activeChar == null) {
         return false;
      } else {
         StringTokenizer st = new StringTokenizer(command, " ");
         String actualCommand = st.nextToken();
         if (actualCommand.equalsIgnoreCase("admin_zone_check")) {
            showHtml(activeChar);
            activeChar.sendMessage(
               "MapRegion: x:"
                  + MapRegionManager.getInstance().getMapRegionX(activeChar.getX())
                  + " y:"
                  + MapRegionManager.getInstance().getMapRegionY(activeChar.getY())
                  + " ("
                  + MapRegionManager.getInstance().getMapRegionLocId(activeChar)
                  + ")"
            );
            getGeoRegionXY(activeChar);
            activeChar.sendMessage("Closest Town: " + MapRegionManager.getInstance().getClosestTownName(activeChar));
            Location loc = MapRegionManager.getInstance().getTeleToLocation(activeChar, TeleportWhereType.CASTLE);
            activeChar.sendMessage("TeleToLocation (Castle): x:" + loc.getX() + " y:" + loc.getY() + " z:" + loc.getZ());
            loc = MapRegionManager.getInstance().getTeleToLocation(activeChar, TeleportWhereType.CLANHALL);
            activeChar.sendMessage("TeleToLocation (ClanHall): x:" + loc.getX() + " y:" + loc.getY() + " z:" + loc.getZ());
            loc = MapRegionManager.getInstance().getTeleToLocation(activeChar, TeleportWhereType.SIEGEFLAG);
            activeChar.sendMessage("TeleToLocation (SiegeFlag): x:" + loc.getX() + " y:" + loc.getY() + " z:" + loc.getZ());
            loc = MapRegionManager.getInstance().getTeleToLocation(activeChar, TeleportWhereType.TOWN);
            activeChar.sendMessage("TeleToLocation (Town): x:" + loc.getX() + " y:" + loc.getY() + " z:" + loc.getZ());
         } else if (actualCommand.equalsIgnoreCase("admin_zone_reload")) {
            ZoneManager.getInstance().reload();
            ZoneManager.getInstance().createZoneReflections();
            activeChar.sendMessage("All Zones have been reloaded");
         } else if (actualCommand.equalsIgnoreCase("admin_zone_visual")) {
            String next = st.nextToken();
            if (next.equalsIgnoreCase("all")) {
               List<ZoneType> zones = ZoneManager.getInstance().getZones(activeChar);
               if (zones != null && !zones.isEmpty()) {
                  for(ZoneType zone : zones) {
                     if (zone != null) {
                        zone.visualizeZone(activeChar.getZ());
                     }
                  }
               }

               showHtml(activeChar);
            } else {
               int zoneId = Integer.parseInt(next);
               ZoneManager.getInstance().getZoneById(zoneId).visualizeZone(activeChar.getZ());
            }
         } else if (actualCommand.equalsIgnoreCase("admin_zone_visual_clear")) {
            ZoneManager.getInstance().clearDebugItems();
            showHtml(activeChar);
         }

         return true;
      }
   }

   private static void showHtml(Player activeChar) {
      String htmContent = HtmCache.getInstance().getHtm(activeChar, activeChar.getLang(), "data/html/admin/zone.htm");
      NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
      adminReply.setHtml(activeChar, htmContent);
      adminReply.replace("%PEACE%", activeChar.isInsideZone(ZoneId.PEACE) ? "<font color=\"LEVEL\">YES</font>" : "NO");
      adminReply.replace("%PVP%", activeChar.isInsideZone(ZoneId.PVP) ? "<font color=\"LEVEL\">YES</font>" : "NO");
      adminReply.replace("%SIEGE%", activeChar.isInsideZone(ZoneId.SIEGE) ? "<font color=\"LEVEL\">YES</font>" : "NO");
      adminReply.replace("%TOWN%", activeChar.isInsideZone(ZoneId.TOWN) ? "<font color=\"LEVEL\">YES</font>" : "NO");
      adminReply.replace("%CASTLE%", activeChar.isInsideZone(ZoneId.CASTLE) ? "<font color=\"LEVEL\">YES</font>" : "NO");
      adminReply.replace("%FORT%", activeChar.isInsideZone(ZoneId.FORT) ? "<font color=\"LEVEL\">YES</font>" : "NO");
      adminReply.replace("%HQ%", activeChar.isInsideZone(ZoneId.HQ) ? "<font color=\"LEVEL\">YES</font>" : "NO");
      adminReply.replace("%CLANHALL%", activeChar.isInsideZone(ZoneId.CLAN_HALL) ? "<font color=\"LEVEL\">YES</font>" : "NO");
      adminReply.replace("%LAND%", activeChar.isInsideZone(ZoneId.LANDING) ? "<font color=\"LEVEL\">YES</font>" : "NO");
      adminReply.replace("%NOLAND%", activeChar.isInsideZone(ZoneId.NO_LANDING) ? "<font color=\"LEVEL\">YES</font>" : "NO");
      adminReply.replace("%NOSUMMON%", activeChar.isInsideZone(ZoneId.NO_SUMMON_FRIEND) ? "<font color=\"LEVEL\">YES</font>" : "NO");
      adminReply.replace("%WATER%", activeChar.isInWater() ? "<font color=\"LEVEL\">YES</font>" : "NO");
      adminReply.replace("%SWAMP%", activeChar.isInsideZone(ZoneId.SWAMP) ? "<font color=\"LEVEL\">YES</font>" : "NO");
      adminReply.replace("%DANGER%", activeChar.isInsideZone(ZoneId.DANGER_AREA) ? "<font color=\"LEVEL\">YES</font>" : "NO");
      adminReply.replace("%SCRIPT%", activeChar.isInsideZone(ZoneId.SCRIPT) ? "<font color=\"LEVEL\">YES</font>" : "NO");
      StringBuilder zones = new StringBuilder(100);
      List<ZoneType> zoneList = ZoneManager.getInstance().getZones(activeChar.getX(), activeChar.getY(), activeChar.getZ());
      if (zoneList != null && !zoneList.isEmpty()) {
         for(ZoneType zone : zoneList) {
            if (zone != null && zone.isCharacterInZone(activeChar)) {
               if (zone.getName() != null) {
                  StringUtil.append(zones, zone.getName() + "<br1>");
                  if (zone.getId() < 300000) {
                     StringUtil.append(zones, "(", String.valueOf(zone.getId()), ")");
                  }
               } else {
                  StringUtil.append(zones, String.valueOf(zone.getId()));
               }

               StringUtil.append(zones, " ");
            }
         }
      }

      adminReply.replace("%ZLIST%", zones.toString());
      activeChar.sendPacket(adminReply);
   }

   private static void getGeoRegionXY(Player activeChar) {
      int worldX = activeChar.getX();
      int worldY = activeChar.getY();
      int geoX = (worldX - -327680 >> 4 >> 11) + 10;
      int geoY = (worldY - -262144 >> 4 >> 11) + 10;
      activeChar.sendMessage("GeoRegion: " + geoX + "_" + geoY + "");
   }

   @Override
   public String[] getAdminCommandList() {
      return ADMIN_COMMANDS;
   }
}
