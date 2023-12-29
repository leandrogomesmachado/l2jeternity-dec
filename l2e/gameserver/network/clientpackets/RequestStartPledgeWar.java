package l2e.gameserver.network.clientpackets;

import l2e.gameserver.Config;
import l2e.gameserver.data.holder.ClanHolder;
import l2e.gameserver.model.Clan;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.SystemMessage;

public final class RequestStartPledgeWar extends GameClientPacket {
   private String _pledgeName;
   private Clan _clan;
   protected Player player;

   @Override
   protected void readImpl() {
      this._pledgeName = this.readS();
   }

   @Override
   protected void runImpl() {
      this.player = this.getClient().getActiveChar();
      if (this.player != null) {
         this._clan = this.getClient().getActiveChar().getClan();
         if (this._clan != null) {
            if (this._clan.getLevel() < 3 || this._clan.getMembersCount() < Config.ALT_CLAN_MEMBERS_FOR_WAR) {
               SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.CLAN_WAR_DECLARED_IF_CLAN_LVL3_OR_15_MEMBER);
               this.player.sendPacket(sm);
               this.player.sendActionFailed();
               sm = null;
            } else if ((this.player.getClanPrivileges() & 32) != 32) {
               this.player.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
               this.player.sendActionFailed();
            } else {
               Clan clan = ClanHolder.getInstance().getClanByName(this._pledgeName);
               if (clan == null) {
                  SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.CLAN_WAR_CANNOT_DECLARED_CLAN_NOT_EXIST);
                  this.player.sendPacket(sm);
                  this.player.sendActionFailed();
               } else if (this._clan.getAllyId() == clan.getAllyId() && this._clan.getAllyId() != 0) {
                  SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.CLAN_WAR_AGAINST_A_ALLIED_CLAN_NOT_WORK);
                  this.player.sendPacket(sm);
                  this.player.sendActionFailed();
                  sm = null;
               } else if (clan.getLevel() < 3 || clan.getMembersCount() < Config.ALT_CLAN_MEMBERS_FOR_WAR) {
                  SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.CLAN_WAR_DECLARED_IF_CLAN_LVL3_OR_15_MEMBER);
                  this.player.sendPacket(sm);
                  this.player.sendActionFailed();
                  sm = null;
               } else if (this._clan.isAtWarWith(clan.getId())) {
                  SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.ALREADY_AT_WAR_WITH_S1_WAIT_5_DAYS);
                  sm.addString(clan.getName());
                  this.player.sendPacket(sm);
                  this.player.sendActionFailed();
                  SystemMessage var5 = null;
               } else {
                  ClanHolder.getInstance().storeclanswars(this.player.getClanId(), clan.getId());
                  this._clan.getOnlineMembers(0).forEach(member -> member.broadcastCharInfo());
                  clan.getOnlineMembers(0).forEach(member -> member.broadcastCharInfo());
               }
            }
         }
      }
   }
}
