package l2e.gameserver.handler.bypasshandlers.impl;

import l2e.gameserver.handler.bypasshandlers.IBypassHandler;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.instance.AdventurerInstance;
import l2e.gameserver.network.serverpackets.ExShowQuestInfo;

public class QuestList implements IBypassHandler {
   private static final String[] COMMANDS = new String[]{"questlist"};

   @Override
   public boolean useBypass(String command, Player activeChar, Creature target) {
      if (!(target instanceof AdventurerInstance)) {
         return false;
      } else {
         activeChar.sendPacket(ExShowQuestInfo.STATIC_PACKET);
         return true;
      }
   }

   @Override
   public String[] getBypassList() {
      return COMMANDS;
   }
}
