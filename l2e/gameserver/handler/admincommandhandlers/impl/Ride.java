package l2e.gameserver.handler.admincommandhandlers.impl;

import l2e.gameserver.data.parser.TransformParser;
import l2e.gameserver.handler.admincommandhandlers.IAdminCommandHandler;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.network.SystemMessageId;

public class Ride implements IAdminCommandHandler {
   private static final String[] ADMIN_COMMANDS = new String[]{
      "admin_ride_horse",
      "admin_ride_bike",
      "admin_ride_wyvern",
      "admin_ride_strider",
      "admin_unride_wyvern",
      "admin_unride_strider",
      "admin_unride",
      "admin_ride_wolf",
      "admin_unride_wolf"
   };
   private int _petRideId;
   private static final int PURPLE_MANED_HORSE_TRANSFORMATION_ID = 106;
   private static final int JET_BIKE_TRANSFORMATION_ID = 20001;

   @Override
   public boolean useAdminCommand(String command, Player activeChar) {
      Player player = this.getRideTarget(activeChar);
      if (player == null) {
         return false;
      } else if (!command.startsWith("admin_ride")) {
         if (command.startsWith("admin_unride")) {
            if (player.getTransformationId() == 106) {
               player.untransform();
            }

            if (player.getTransformationId() == 20001) {
               player.untransform();
            } else {
               player.dismount();
            }
         }

         return true;
      } else if (!player.isMounted() && !player.hasSummon()) {
         if (command.startsWith("admin_ride_wyvern")) {
            this._petRideId = 12621;
         } else if (command.startsWith("admin_ride_strider")) {
            this._petRideId = 12526;
         } else {
            if (!command.startsWith("admin_ride_wolf")) {
               if (command.startsWith("admin_ride_horse")) {
                  if (!player.isTransformed() && !player.isInStance()) {
                     TransformParser.getInstance().transformPlayer(106, player);
                  } else {
                     activeChar.sendPacket(SystemMessageId.YOU_ALREADY_POLYMORPHED_AND_CANNOT_POLYMORPH_AGAIN);
                  }

                  return true;
               }

               if (!command.startsWith("admin_ride_bike")) {
                  activeChar.sendMessage("Command '" + command + "' not recognized");
                  return false;
               }

               if (!player.isTransformed() && !player.isInStance()) {
                  TransformParser.getInstance().transformPlayer(20001, player);
               } else {
                  activeChar.sendPacket(SystemMessageId.YOU_ALREADY_POLYMORPHED_AND_CANNOT_POLYMORPH_AGAIN);
               }

               return true;
            }

            this._petRideId = 16041;
         }

         player.mount(this._petRideId, 0, false);
         return false;
      } else {
         activeChar.sendMessage("Target already have a summon.");
         return false;
      }
   }

   private Player getRideTarget(Player activeChar) {
      Player player = null;
      if (activeChar.getTarget() != null && activeChar.getTarget().getObjectId() != activeChar.getObjectId() && activeChar.getTarget() instanceof Player) {
         player = (Player)activeChar.getTarget();
      } else {
         player = activeChar;
      }

      return player;
   }

   @Override
   public String[] getAdminCommandList() {
      return ADMIN_COMMANDS;
   }
}
