package l2e.gameserver.handler.usercommandhandlers.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import l2e.gameserver.database.DatabaseFactory;
import l2e.gameserver.handler.usercommandhandlers.IUserCommandHandler;
import l2e.gameserver.model.Clan;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.SystemMessage;

public class ClanWarsList implements IUserCommandHandler {
   private static final Logger _log = Logger.getLogger(ClanWarsList.class.getName());
   private static final int[] COMMAND_IDS = new int[]{88, 89, 90};
   private static final String ATTACK_LIST = "SELECT clan_name,clan_id,ally_id,ally_name FROM clan_data,clan_wars WHERE clan1=? AND clan_id=clan2 AND clan2 NOT IN (SELECT clan1 FROM clan_wars WHERE clan2=?)";
   private static final String UNDER_ATTACK_LIST = "SELECT clan_name,clan_id,ally_id,ally_name FROM clan_data,clan_wars WHERE clan2=? AND clan_id=clan1 AND clan1 NOT IN (SELECT clan2 FROM clan_wars WHERE clan1=?)";
   private static final String WAR_LIST = "SELECT clan_name,clan_id,ally_id,ally_name FROM clan_data,clan_wars WHERE clan1=? AND clan_id=clan2 AND clan2 IN (SELECT clan1 FROM clan_wars WHERE clan2=?)";

   @Override
   public boolean useUserCommand(int id, Player activeChar) {
      if (id != COMMAND_IDS[0] && id != COMMAND_IDS[1] && id != COMMAND_IDS[2]) {
         return false;
      } else {
         Clan clan = activeChar.getClan();
         if (clan == null) {
            activeChar.sendPacket(SystemMessageId.NOT_JOINED_IN_ANY_CLAN);
            return false;
         } else {
            try (Connection con = DatabaseFactory.getInstance().getConnection()) {
               String query;
               if (id == 88) {
                  activeChar.sendPacket(SystemMessageId.CLANS_YOU_DECLARED_WAR_ON);
                  query = "SELECT clan_name,clan_id,ally_id,ally_name FROM clan_data,clan_wars WHERE clan1=? AND clan_id=clan2 AND clan2 NOT IN (SELECT clan1 FROM clan_wars WHERE clan2=?)";
               } else if (id == 89) {
                  activeChar.sendPacket(SystemMessageId.CLANS_THAT_HAVE_DECLARED_WAR_ON_YOU);
                  query = "SELECT clan_name,clan_id,ally_id,ally_name FROM clan_data,clan_wars WHERE clan2=? AND clan_id=clan1 AND clan1 NOT IN (SELECT clan2 FROM clan_wars WHERE clan1=?)";
               } else {
                  activeChar.sendPacket(SystemMessageId.WAR_LIST);
                  query = "SELECT clan_name,clan_id,ally_id,ally_name FROM clan_data,clan_wars WHERE clan1=? AND clan_id=clan2 AND clan2 IN (SELECT clan1 FROM clan_wars WHERE clan2=?)";
               }

               try (PreparedStatement ps = con.prepareStatement(query)) {
                  ps.setInt(1, clan.getId());
                  ps.setInt(2, clan.getId());

                  SystemMessage sm;
                  try (ResultSet rs = ps.executeQuery()) {
                     for(; rs.next(); activeChar.sendPacket(sm)) {
                        String clanName = rs.getString("clan_name");
                        int ally_id = rs.getInt("ally_id");
                        if (ally_id > 0) {
                           sm = SystemMessage.getSystemMessage(SystemMessageId.S1_S2_ALLIANCE);
                           sm.addString(clanName);
                           sm.addString(rs.getString("ally_name"));
                        } else {
                           sm = SystemMessage.getSystemMessage(SystemMessageId.S1_NO_ALLI_EXISTS);
                           sm.addString(clanName);
                        }
                     }
                  }
               }

               activeChar.sendPacket(SystemMessageId.FRIEND_LIST_FOOTER);
            } catch (Exception var65) {
               _log.log(Level.WARNING, "", (Throwable)var65);
            }

            return true;
         }
      }
   }

   @Override
   public int[] getUserCommandList() {
      return COMMAND_IDS;
   }
}
