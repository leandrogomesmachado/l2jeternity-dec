package l2e.gameserver.handler.admincommandhandlers.impl;

import java.util.logging.Logger;
import l2e.gameserver.Config;
import l2e.gameserver.handler.admincommandhandlers.IAdminCommandHandler;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.network.SystemMessageId;

public class Heal implements IAdminCommandHandler {
   private static Logger _log = Logger.getLogger(Res.class.getName());
   private static final String[] ADMIN_COMMANDS = new String[]{"admin_heal"};

   @Override
   public boolean useAdminCommand(String command, Player activeChar) {
      if (command.equals("admin_heal")) {
         this.handleHeal(activeChar);
      } else if (command.startsWith("admin_heal")) {
         try {
            String healTarget = command.substring(11);
            this.handleHeal(activeChar, healTarget);
         } catch (StringIndexOutOfBoundsException var4) {
            if (Config.DEVELOPER) {
               _log.warning("Heal error: " + var4);
            }

            activeChar.sendMessage("Incorrect target/radius specified.");
         }
      }

      return true;
   }

   @Override
   public String[] getAdminCommandList() {
      return ADMIN_COMMANDS;
   }

   private void handleHeal(Player activeChar) {
      this.handleHeal(activeChar, null);
   }

   private void handleHeal(Player activeChar, String player) {
      GameObject obj = activeChar.getTarget();
      if (player != null) {
         Player plyr = World.getInstance().getPlayer(player);
         if (plyr != null) {
            obj = plyr;
         } else {
            try {
               int radius = Integer.parseInt(player);

               for(Creature character : World.getInstance().getAroundCharacters(activeChar)) {
                  character.setCurrentHpMp(character.getMaxHp(), character.getMaxMp());
                  if (character.isPlayer()) {
                     character.setCurrentCp(character.getMaxCp());
                  }
               }

               activeChar.sendMessage("Healed within " + radius + " unit radius.");
               return;
            } catch (NumberFormatException var8) {
            }
         }
      }

      if (obj == null) {
         obj = activeChar;
      }

      if (obj instanceof Creature) {
         Creature target = (Creature)obj;
         target.setCurrentHpMp(target.getMaxHp(), target.getMaxMp());
         if (target instanceof Player) {
            target.setCurrentCp(target.getMaxCp());
         }

         if (Config.DEBUG) {
            _log.fine("GM: " + activeChar.getName() + "(" + activeChar.getObjectId() + ") healed character " + target.getName());
         }
      } else {
         activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
      }
   }
}
