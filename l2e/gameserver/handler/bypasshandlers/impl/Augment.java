package l2e.gameserver.handler.bypasshandlers.impl;

import java.util.logging.Level;
import l2e.gameserver.handler.bypasshandlers.IBypassHandler;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.network.serverpackets.ExShowVariationCancelWindow;
import l2e.gameserver.network.serverpackets.ExShowVariationMakeWindow;

public class Augment implements IBypassHandler {
   private static final String[] COMMANDS = new String[]{"Augment"};

   @Override
   public boolean useBypass(String command, Player activeChar, Creature target) {
      if (!target.isNpc()) {
         return false;
      } else {
         try {
            switch(Integer.parseInt(command.substring(8, 9).trim())) {
               case 1:
                  activeChar.sendPacket(ExShowVariationMakeWindow.STATIC_PACKET);
                  return true;
               case 2:
                  activeChar.sendPacket(ExShowVariationCancelWindow.STATIC_PACKET);
                  return true;
            }
         } catch (Exception var5) {
            _log.log(Level.WARNING, "Exception in " + this.getClass().getSimpleName(), (Throwable)var5);
         }

         return false;
      }
   }

   @Override
   public String[] getBypassList() {
      return COMMANDS;
   }
}
