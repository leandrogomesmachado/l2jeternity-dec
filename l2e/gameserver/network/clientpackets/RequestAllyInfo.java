package l2e.gameserver.network.clientpackets;

import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.ClanInfoTemplate;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.AllianceInfo;
import l2e.gameserver.network.serverpackets.SystemMessage;

public final class RequestAllyInfo extends GameClientPacket {
   @Override
   public void readImpl() {
   }

   @Override
   protected void runImpl() {
      Player activeChar = this.getClient().getActiveChar();
      if (activeChar != null) {
         int allianceId = activeChar.getAllyId();
         if (allianceId > 0) {
            AllianceInfo ai = new AllianceInfo(allianceId);
            activeChar.sendPacket(ai);
            SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.ALLIANCE_INFO_HEAD);
            activeChar.sendPacket(sm);
            sm = SystemMessage.getSystemMessage(SystemMessageId.ALLIANCE_NAME_S1);
            sm.addString(ai.getName());
            activeChar.sendPacket(sm);
            sm = SystemMessage.getSystemMessage(SystemMessageId.ALLIANCE_LEADER_S2_OF_S1);
            sm.addString(ai.getLeaderC());
            sm.addString(ai.getLeaderP());
            activeChar.sendPacket(sm);
            sm = SystemMessage.getSystemMessage(SystemMessageId.CONNECTION_S1_TOTAL_S2);
            sm.addNumber(ai.getOnline());
            sm.addNumber(ai.getTotal());
            activeChar.sendPacket(sm);
            sm = SystemMessage.getSystemMessage(SystemMessageId.ALLIANCE_CLAN_TOTAL_S1);
            sm.addNumber(ai.getAllies().length);
            activeChar.sendPacket(sm);
            sm = SystemMessage.getSystemMessage(SystemMessageId.CLAN_INFO_HEAD);

            for(ClanInfoTemplate aci : ai.getAllies()) {
               activeChar.sendPacket(sm);
               sm = SystemMessage.getSystemMessage(SystemMessageId.CLAN_INFO_NAME_S1);
               sm.addString(aci.getClan().getName());
               activeChar.sendPacket(sm);
               sm = SystemMessage.getSystemMessage(SystemMessageId.CLAN_INFO_LEADER_S1);
               sm.addString(aci.getClan().getLeaderName());
               activeChar.sendPacket(sm);
               sm = SystemMessage.getSystemMessage(SystemMessageId.CLAN_INFO_LEVEL_S1);
               sm.addNumber(aci.getClan().getLevel());
               activeChar.sendPacket(sm);
               sm = SystemMessage.getSystemMessage(SystemMessageId.CONNECTION_S1_TOTAL_S2);
               sm.addNumber(aci.getOnline());
               sm.addNumber(aci.getTotal());
               activeChar.sendPacket(sm);
               sm = SystemMessage.getSystemMessage(SystemMessageId.CLAN_INFO_SEPARATOR);
            }

            sm = SystemMessage.getSystemMessage(SystemMessageId.CLAN_INFO_FOOT);
            activeChar.sendPacket(sm);
         } else {
            activeChar.sendPacket(SystemMessageId.NO_CURRENT_ALLIANCES);
         }
      }
   }
}
