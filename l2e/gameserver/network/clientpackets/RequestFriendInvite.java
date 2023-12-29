package l2e.gameserver.network.clientpackets;

import l2e.gameserver.model.BlockedList;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.FriendAddRequest;
import l2e.gameserver.network.serverpackets.SystemMessage;

public final class RequestFriendInvite extends GameClientPacket {
   private String _name;

   @Override
   protected void readImpl() {
      this._name = this.readS();
   }

   @Override
   protected void runImpl() {
      Player activeChar = this.getClient().getActiveChar();
      if (activeChar != null) {
         activeChar.isntAfk();
         Player friend = World.getInstance().getPlayer(this._name);
         if (friend == null || !friend.isOnline() || friend.isInvisible()) {
            activeChar.sendPacket(SystemMessageId.THE_USER_YOU_REQUESTED_IS_NOT_IN_GAME);
         } else if (friend == activeChar) {
            activeChar.sendPacket(SystemMessageId.YOU_CANNOT_ADD_YOURSELF_TO_OWN_FRIEND_LIST);
         } else if (BlockedList.isBlocked(activeChar, friend)) {
            SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.BLOCKED_C1);
            sm.addCharName(friend);
            activeChar.sendPacket(sm);
         } else if (BlockedList.isBlocked(friend, activeChar)) {
            activeChar.sendMessage("You are in target's block list.");
         } else if (activeChar.isInOlympiadMode() || friend.isInOlympiadMode()) {
            activeChar.sendPacket(SystemMessageId.A_USER_CURRENTLY_PARTICIPATING_IN_THE_OLYMPIAD_CANNOT_SEND_PARTY_AND_FRIEND_INVITATIONS);
         } else if (friend.getFriendInviteRefusal()) {
            activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_IS_BUSY_TRY_LATER).addString(friend.getName()));
         } else if (activeChar.getFriendList().contains(friend.getObjectId())) {
            SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_ALREADY_IN_FRIENDS_LIST);
            sm.addString(this._name);
            activeChar.sendPacket(sm);
         } else {
            SystemMessage sm;
            if (!friend.isProcessingRequest()) {
               activeChar.onTransactionRequest(friend);
               sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_REQUESTED_C1_TO_BE_FRIEND);
               sm.addString(this._name);
               FriendAddRequest ajf = new FriendAddRequest(activeChar.getName());
               friend.sendPacket(ajf);
            } else {
               sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_BUSY_TRY_LATER);
               sm.addString(this._name);
            }

            activeChar.sendPacket(sm);
         }
      }
   }
}
