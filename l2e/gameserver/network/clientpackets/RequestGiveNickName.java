package l2e.gameserver.network.clientpackets;

import l2e.gameserver.model.ClanMember;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.network.SystemMessageId;

public class RequestGiveNickName extends GameClientPacket {
   private String _target;
   private String _title;

   @Override
   protected void readImpl() {
      this._target = this.readS();
      this._title = this.readS();
   }

   @Override
   protected void runImpl() {
      Player activeChar = this.getClient().getActiveChar();
      if (activeChar != null) {
         if (activeChar.isNoble() && this._target.equalsIgnoreCase(activeChar.getName())) {
            activeChar.setTitle(this._title);
            activeChar.sendPacket(SystemMessageId.TITLE_CHANGED);
            activeChar.broadcastTitleInfo();
         } else if ((activeChar.getClanPrivileges() & 4) == 4) {
            if (activeChar.getClan().getLevel() < 3) {
               activeChar.sendPacket(SystemMessageId.CLAN_LVL_3_NEEDED_TO_ENDOWE_TITLE);
               return;
            }

            ClanMember member1 = activeChar.getClan().getClanMember(this._target);
            if (member1 != null) {
               Player member = member1.getPlayerInstance();
               if (member != null) {
                  member.setTitle(this._title);
                  member.sendPacket(SystemMessageId.TITLE_CHANGED);
                  member.broadcastTitleInfo();
               } else {
                  activeChar.sendPacket(SystemMessageId.TARGET_IS_NOT_FOUND_IN_THE_GAME);
               }
            } else {
               activeChar.sendPacket(SystemMessageId.TARGET_MUST_BE_IN_CLAN);
            }
         }
      }
   }
}
