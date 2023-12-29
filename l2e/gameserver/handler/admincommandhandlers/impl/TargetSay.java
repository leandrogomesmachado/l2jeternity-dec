package l2e.gameserver.handler.admincommandhandlers.impl;

import l2e.gameserver.handler.admincommandhandlers.IAdminCommandHandler;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.instance.StaticObjectInstance;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.CreatureSay;

public class TargetSay implements IAdminCommandHandler {
   private static final String[] ADMIN_COMMANDS = new String[]{"admin_targetsay"};

   @Override
   public boolean useAdminCommand(String command, Player activeChar) {
      if (command.startsWith("admin_targetsay")) {
         try {
            GameObject obj = activeChar.getTarget();
            if (obj instanceof StaticObjectInstance || !(obj instanceof Creature)) {
               activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
               return false;
            }

            String message = command.substring(16);
            Creature target = (Creature)obj;
            target.broadcastPacket(new CreatureSay(target.getObjectId(), target.isPlayer() ? 0 : 22, target.getName(), message));
         } catch (StringIndexOutOfBoundsException var6) {
            activeChar.sendMessage("Usage: //targetsay <text>");
            return false;
         }
      }

      return true;
   }

   @Override
   public String[] getAdminCommandList() {
      return ADMIN_COMMANDS;
   }
}
