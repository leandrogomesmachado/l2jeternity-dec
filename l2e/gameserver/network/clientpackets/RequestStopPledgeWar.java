package l2e.gameserver.network.clientpackets;

import l2e.gameserver.data.holder.ClanHolder;
import l2e.gameserver.model.Clan;
import l2e.gameserver.model.ClanMember;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.taskmanager.AttackStanceTaskManager;

public final class RequestStopPledgeWar extends GameClientPacket {
   private String _pledgeName;

   @Override
   protected void readImpl() {
      this._pledgeName = this.readS();
   }

   @Override
   protected void runImpl() {
      Player player = this.getClient().getActiveChar();
      if (player != null) {
         Clan playerClan = player.getClan();
         if (playerClan != null) {
            Clan clan = ClanHolder.getInstance().getClanByName(this._pledgeName);
            if (clan == null) {
               player.sendMessage("No such clan.");
               player.sendActionFailed();
            } else if (!playerClan.isAtWarWith(clan.getId())) {
               player.sendMessage("You aren't at war with this clan.");
               player.sendActionFailed();
            } else if ((player.getClanPrivileges() & 32) != 32) {
               player.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
            } else {
               for(ClanMember member : playerClan.getMembers()) {
                  if (member != null
                     && member.getPlayerInstance() != null
                     && AttackStanceTaskManager.getInstance().hasAttackStanceTask(member.getPlayerInstance())) {
                     player.sendPacket(SystemMessageId.CANT_STOP_CLAN_WAR_WHILE_IN_COMBAT);
                     return;
                  }
               }

               ClanHolder.getInstance().deleteclanswars(playerClan.getId(), clan.getId());
               playerClan.getOnlineMembers(0).forEach(memberx -> memberx.broadcastCharInfo());
               clan.getOnlineMembers(0).forEach(memberx -> memberx.broadcastCharInfo());
            }
         }
      }
   }
}
