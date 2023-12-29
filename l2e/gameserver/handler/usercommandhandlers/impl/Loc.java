package l2e.gameserver.handler.usercommandhandlers.impl;

import l2e.gameserver.handler.usercommandhandlers.IUserCommandHandler;
import l2e.gameserver.instancemanager.MapRegionManager;
import l2e.gameserver.instancemanager.ZoneManager;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.base.Race;
import l2e.gameserver.model.zone.type.RespawnZone;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.SystemMessage;

public class Loc implements IUserCommandHandler {
   private static final int[] COMMAND_IDS = new int[]{0};

   @Override
   public boolean useUserCommand(int id, Player activeChar) {
      RespawnZone zone = ZoneManager.getInstance().getZone(activeChar, RespawnZone.class);
      int region;
      if (zone != null) {
         region = MapRegionManager.getInstance().getRestartRegion(activeChar, zone.getAllRespawnPoints().get(Race.Human)).getLocId();
      } else {
         region = MapRegionManager.getInstance().getMapRegionLocId(activeChar);
      }

      SystemMessage sm;
      if (region > 0) {
         sm = SystemMessage.getSystemMessage(region);
         if (sm.getSystemMessageId().getParamCount() == 3) {
            sm.addNumber(activeChar.getX());
            sm.addNumber(activeChar.getY());
            sm.addNumber(activeChar.getZ());
         }
      } else {
         sm = SystemMessage.getSystemMessage(SystemMessageId.CURRENT_LOCATION_S1);
         sm.addString(activeChar.getX() + ", " + activeChar.getY() + ", " + activeChar.getZ());
      }

      activeChar.sendPacket(sm);
      return true;
   }

   @Override
   public int[] getUserCommandList() {
      return COMMAND_IDS;
   }
}
