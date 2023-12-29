package l2e.gameserver.handler.usercommandhandlers.impl;

import java.util.Map;
import l2e.gameserver.handler.usercommandhandlers.IUserCommandHandler;
import l2e.gameserver.instancemanager.ReflectionManager;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.reflection.ReflectionWorld;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.SystemMessage;

public class InstanceZone implements IUserCommandHandler {
   private static final int[] COMMAND_IDS = new int[]{114};

   @Override
   public boolean useUserCommand(int id, Player activeChar) {
      if (id != COMMAND_IDS[0]) {
         return false;
      } else {
         ReflectionWorld world = ReflectionManager.getInstance().getPlayerWorld(activeChar);
         if (world != null && world.getTemplateId() >= 0) {
            SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.INSTANT_ZONE_CURRENTLY_INUSE_S1);
            sm.addInstanceName(world.getTemplateId());
            activeChar.sendPacket(sm);
         }

         Map<Integer, Long> instanceTimes = ReflectionManager.getInstance().getAllReflectionTimes(activeChar.getObjectId());
         boolean firstMessage = true;
         if (instanceTimes != null) {
            for(int instanceId : instanceTimes.keySet()) {
               long remainingTime = (instanceTimes.get(instanceId) - System.currentTimeMillis()) / 1000L;
               if (remainingTime > 60L) {
                  if (firstMessage) {
                     firstMessage = false;
                     activeChar.sendPacket(SystemMessageId.INSTANCE_ZONE_TIME_LIMIT);
                  }

                  int hours = (int)(remainingTime / 3600L);
                  int minutes = (int)(remainingTime % 3600L / 60L);
                  SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.AVAILABLE_AFTER_S1_S2_HOURS_S3_MINUTES);
                  sm.addInstanceName(instanceId);
                  sm.addNumber(hours);
                  sm.addNumber(minutes);
                  activeChar.sendPacket(sm);
               } else {
                  ReflectionManager.getInstance().deleteReflectionTime(activeChar.getObjectId(), instanceId);
               }
            }
         }

         if (firstMessage) {
            activeChar.sendPacket(SystemMessageId.NO_INSTANCEZONE_TIME_LIMIT);
         }

         return true;
      }
   }

   @Override
   public int[] getUserCommandList() {
      return COMMAND_IDS;
   }
}
