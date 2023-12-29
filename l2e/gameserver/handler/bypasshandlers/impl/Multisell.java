package l2e.gameserver.handler.bypasshandlers.impl;

import java.util.logging.Level;
import l2e.gameserver.data.parser.MultiSellParser;
import l2e.gameserver.handler.bypasshandlers.IBypassHandler;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;

public class Multisell implements IBypassHandler {
   private static final String[] COMMANDS = new String[]{"multisell", "exc_multisell"};

   @Override
   public boolean useBypass(String command, Player activeChar, Creature target) {
      if (!target.isNpc()) {
         return false;
      } else {
         try {
            if (command.toLowerCase().startsWith(COMMANDS[0])) {
               int listId = Integer.parseInt(command.substring(9).trim());
               MultiSellParser.getInstance().separateAndSend(listId, activeChar, (Npc)target, false);
               return true;
            } else if (command.toLowerCase().startsWith(COMMANDS[1])) {
               int listId = Integer.parseInt(command.substring(13).trim());
               MultiSellParser.getInstance().separateAndSend(listId, activeChar, (Npc)target, true);
               return true;
            } else {
               return false;
            }
         } catch (Exception var5) {
            _log.log(Level.WARNING, "Exception in " + this.getClass().getSimpleName(), (Throwable)var5);
            return false;
         }
      }
   }

   @Override
   public String[] getBypassList() {
      return COMMANDS;
   }
}
