package l2e.gameserver.handler.bypasshandlers.impl;

import l2e.gameserver.handler.bypasshandlers.IBypassHandler;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.instance.SymbolMakerInstance;
import l2e.gameserver.model.actor.templates.items.Henna;
import l2e.gameserver.network.serverpackets.HennaEquipList;
import l2e.gameserver.network.serverpackets.HennaUnequipList;

public class Hennas implements IBypassHandler {
   private static final String[] COMMANDS = new String[]{"Draw", "RemoveList"};

   @Override
   public boolean useBypass(String command, Player activeChar, Creature target) {
      if (!(target instanceof SymbolMakerInstance)) {
         return false;
      } else {
         if (command.equals("Draw")) {
            activeChar.sendPacket(new HennaEquipList(activeChar));
         } else if (command.equals("RemoveList")) {
            for(Henna henna : activeChar.getHennaList()) {
               if (henna != null) {
                  activeChar.sendPacket(new HennaUnequipList(activeChar));
                  break;
               }
            }
         }

         return true;
      }
   }

   @Override
   public String[] getBypassList() {
      return COMMANDS;
   }
}
