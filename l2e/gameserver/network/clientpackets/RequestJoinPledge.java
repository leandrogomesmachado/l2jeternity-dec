package l2e.gameserver.network.clientpackets;

import l2e.gameserver.model.Clan;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.AskJoinPledge;
import l2e.gameserver.network.serverpackets.SystemMessage;

public final class RequestJoinPledge extends GameClientPacket {
   private int _target;
   private int _pledgeType;

   @Override
   protected void readImpl() {
      this._target = this.readD();
      this._pledgeType = this.readD();
   }

   @Override
   protected void runImpl() {
      Player activeChar = this.getClient().getActiveChar();
      if (activeChar != null) {
         Clan clan = activeChar.getClan();
         if (clan != null) {
            Player target = World.getInstance().getPlayer(this._target);
            if (target == null) {
               activeChar.sendPacket(SystemMessageId.YOU_HAVE_INVITED_THE_WRONG_TARGET);
            } else if (activeChar.isInFightEvent() && !activeChar.getFightEvent().canReceiveInvitations(activeChar, target)) {
               activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_IS_BUSY_TRY_LATER).addString(target.getName()));
            } else if (clan.checkClanJoinCondition(activeChar, target, this._pledgeType)) {
               if (activeChar.getRequest().setRequest(target, this)) {
                  String pledgeName = activeChar.getClan().getName();
                  String subPledgeName = activeChar.getClan().getSubPledge(this._pledgeType) != null
                     ? activeChar.getClan().getSubPledge(this._pledgeType).getName()
                     : null;
                  target.sendPacket(new AskJoinPledge(activeChar.getObjectId(), subPledgeName, this._pledgeType, pledgeName));
               }
            }
         }
      }
   }

   public int getPledgeType() {
      return this._pledgeType;
   }
}
