package l2e.gameserver.network.clientpackets;

import l2e.gameserver.model.Clan;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.AskJoinAlliance;
import l2e.gameserver.network.serverpackets.SystemMessage;

public final class RequestJoinAlly extends GameClientPacket {
   private int _id;

   @Override
   protected void readImpl() {
      this._id = this.readD();
   }

   @Override
   protected void runImpl() {
      Player activeChar = this.getClient().getActiveChar();
      if (activeChar != null) {
         Player ob = World.getInstance().getPlayer(this._id);
         if (ob == null) {
            activeChar.sendPacket(SystemMessageId.YOU_HAVE_INVITED_THE_WRONG_TARGET);
         } else if (activeChar.getClan() == null) {
            activeChar.sendPacket(SystemMessageId.YOU_ARE_NOT_A_CLAN_MEMBER);
         } else if (activeChar.isInFightEvent() && !activeChar.getFightEvent().canReceiveInvitations(activeChar, ob)) {
            activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_IS_BUSY_TRY_LATER).addString(ob.getName()));
         } else {
            Clan clan = activeChar.getClan();
            if (clan.checkAllyJoinCondition(activeChar, ob)) {
               if (activeChar.getRequest().setRequest(ob, this)) {
                  SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S2_ALLIANCE_LEADER_OF_S1_REQUESTED_ALLIANCE);
                  sm.addString(activeChar.getClan().getAllyName());
                  sm.addString(activeChar.getName());
                  ob.sendPacket(sm);
                  SystemMessage var7 = null;
                  AskJoinAlliance aja = new AskJoinAlliance(activeChar.getObjectId(), activeChar.getName(), activeChar.getClan().getAllyName());
                  ob.sendPacket(aja);
               }
            }
         }
      }
   }
}
