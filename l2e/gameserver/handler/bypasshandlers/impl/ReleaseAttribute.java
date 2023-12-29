package l2e.gameserver.handler.bypasshandlers.impl;

import l2e.gameserver.handler.bypasshandlers.IBypassHandler;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.network.serverpackets.ExShowBaseAttributeCancelWindow;

public class ReleaseAttribute implements IBypassHandler {
   private static final String[] COMMANDS = new String[]{"ReleaseAttribute"};

   @Override
   public boolean useBypass(String command, Player activeChar, Creature target) {
      if (!target.isNpc()) {
         return false;
      } else {
         activeChar.sendPacket(new ExShowBaseAttributeCancelWindow(activeChar));
         return true;
      }
   }

   @Override
   public String[] getBypassList() {
      return COMMANDS;
   }
}
