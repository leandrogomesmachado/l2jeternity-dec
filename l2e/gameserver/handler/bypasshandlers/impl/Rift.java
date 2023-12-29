package l2e.gameserver.handler.bypasshandlers.impl;

import java.util.logging.Level;
import l2e.gameserver.handler.bypasshandlers.IBypassHandler;
import l2e.gameserver.instancemanager.DimensionalRiftManager;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;

public class Rift implements IBypassHandler {
   private static final String[] COMMANDS = new String[]{"enterrift", "changeriftroom", "exitrift"};

   @Override
   public boolean useBypass(String command, Player activeChar, Creature target) {
      if (!target.isNpc()) {
         return false;
      } else if (command.toLowerCase().startsWith(COMMANDS[0])) {
         try {
            Byte b1 = Byte.parseByte(command.substring(10));
            DimensionalRiftManager.getInstance().start(activeChar, b1, (Npc)target);
            return true;
         } catch (Exception var5) {
            _log.log(Level.WARNING, "Exception in " + this.getClass().getSimpleName(), (Throwable)var5);
            return false;
         }
      } else {
         boolean inRift = activeChar.isInParty() && activeChar.getParty().isInDimensionalRift();
         if (command.toLowerCase().startsWith(COMMANDS[1])) {
            if (inRift) {
               activeChar.getParty().getDimensionalRift().manualTeleport(activeChar, (Npc)target);
            } else {
               DimensionalRiftManager.getInstance().handleCheat(activeChar, (Npc)target);
            }

            return true;
         } else {
            if (command.toLowerCase().startsWith(COMMANDS[2])) {
               if (inRift) {
                  activeChar.getParty().getDimensionalRift().manualExitRift(activeChar, (Npc)target);
               } else {
                  DimensionalRiftManager.getInstance().handleCheat(activeChar, (Npc)target);
               }
            }

            return true;
         }
      }
   }

   @Override
   public String[] getBypassList() {
      return COMMANDS;
   }
}
