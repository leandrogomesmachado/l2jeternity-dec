package l2e.gameserver.handler.admincommandhandlers.impl;

import l2e.gameserver.Config;
import l2e.gameserver.geodata.GeoEngine;
import l2e.gameserver.handler.admincommandhandlers.IAdminCommandHandler;
import l2e.gameserver.model.actor.Player;

public class Geodata implements IAdminCommandHandler {
   private static final String[] ADMIN_COMMANDS = new String[]{"admin_geo_z", "admin_geo_type", "admin_geo_nswe", "admin_geo_los", "admin_geo_position"};

   @Override
   public boolean useAdminCommand(String command, Player activeChar) {
      if (!Config.GEODATA) {
         activeChar.sendMessage("Geo Engine is Turned Off!");
         return true;
      } else {
         if (command.equals("admin_geo_z")) {
            activeChar.sendMessage(
               "GeoEngine: Geo_Z = "
                  + GeoEngine.getHeight(activeChar.getX(), activeChar.getY(), activeChar.getZ(), activeChar.getGeoIndex())
                  + " Loc_Z = "
                  + activeChar.getZ()
            );
         } else if (command.equals("admin_geo_type")) {
            short type = GeoEngine.getType(activeChar.getX(), activeChar.getY(), activeChar.getGeoIndex());
            activeChar.sendMessage("GeoEngine: Geo_Type = " + type);
            int height = GeoEngine.getHeight(activeChar.getX(), activeChar.getY(), activeChar.getZ(), activeChar.getGeoIndex());
            activeChar.sendMessage("GeoEngine: height = " + height);
         } else if (command.equals("admin_geo_nswe")) {
            String result = "";
            short nswe = (short)GeoEngine.getNSWE(activeChar.getX(), activeChar.getY(), activeChar.getZ(), activeChar.getGeoIndex());
            if ((nswe & 8) == 0) {
               result = result + " N";
            }

            if ((nswe & 4) == 0) {
               result = result + " S";
            }

            if ((nswe & 2) == 0) {
               result = result + " W";
            }

            if ((nswe & 1) == 0) {
               result = result + " E";
            }

            activeChar.sendMessage("GeoEngine: Geo_NSWE -> " + nswe + "->" + result);
         } else if (command.equals("admin_geo_los")) {
            if (activeChar.getTarget() != null) {
               if (GeoEngine.canSeeTarget(activeChar, activeChar.getTarget(), false)) {
                  activeChar.sendMessage("GeoEngine: Can See Target");
               } else {
                  activeChar.sendMessage("GeoEngine: Can't See Target");
               }
            } else {
               activeChar.sendMessage("None Target!");
            }
         } else if (command.equals("admin_geo_position")) {
            activeChar.sendMessage("GeoEngine: Your current position: ");
            activeChar.sendMessage(".... world coords: x: " + activeChar.getX() + " y: " + activeChar.getY() + " z: " + activeChar.getZ());
         }

         return true;
      }
   }

   @Override
   public String[] getAdminCommandList() {
      return ADMIN_COMMANDS;
   }
}
