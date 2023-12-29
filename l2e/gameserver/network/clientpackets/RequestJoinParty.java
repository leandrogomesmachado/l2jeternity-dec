package l2e.gameserver.network.clientpackets;

import l2e.gameserver.Config;
import l2e.gameserver.model.BlockedList;
import l2e.gameserver.model.Party;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.entity.events.cleft.AerialCleftEvent;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.AskJoinParty;
import l2e.gameserver.network.serverpackets.SystemMessage;

public final class RequestJoinParty extends GameClientPacket {
   private String _name;
   private int _itemDistribution;

   @Override
   protected void readImpl() {
      this._name = this.readS();
      this._itemDistribution = this.readD();
   }

   @Override
   protected void runImpl() {
      Player requestor = this.getClient().getActiveChar();
      Player target = World.getInstance().getPlayer(this._name);
      if (requestor != null) {
         if (target == null) {
            requestor.sendPacket(SystemMessageId.FIRST_SELECT_USER_TO_INVITE_TO_PARTY);
         } else if (target.isFakePlayer()) {
            requestor.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_IS_BUSY_TRY_LATER).addString(target.getName()));
         } else if (target.getClient() == null || target.getClient().isDetached()) {
            requestor.sendMessage("Player is in offline mode.");
         } else if (requestor.isPartyBanned()) {
            requestor.sendPacket(SystemMessageId.YOU_HAVE_BEEN_REPORTED_SO_PARTY_NOT_ALLOWED);
            requestor.sendActionFailed();
         } else if (target.isPartyBanned()) {
            SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_REPORTED_AND_CANNOT_PARTY);
            sm.addCharName(target);
            requestor.sendPacket(sm);
         } else if ((AerialCleftEvent.getInstance().isStarted() || AerialCleftEvent.getInstance().isRewarding())
            && AerialCleftEvent.getInstance().isPlayerParticipant(target.getObjectId())) {
            requestor.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
         } else if (!target.isVisibleFor(requestor)) {
            requestor.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
         } else if (target.isInParty()) {
            SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_ALREADY_IN_PARTY);
            sm.addString(target.getName());
            requestor.sendPacket(sm);
         } else if (BlockedList.isBlocked(target, requestor)) {
            SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_ADDED_YOU_TO_IGNORE_LIST);
            sm.addCharName(target);
            requestor.sendPacket(sm);
         } else if (target == requestor) {
            requestor.sendPacket(SystemMessageId.YOU_HAVE_INVITED_THE_WRONG_TARGET);
         } else if (target.getPartyInviteRefusal()) {
            requestor.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_IS_BUSY_TRY_LATER).addString(target.getName()));
         } else if (!target.isCursedWeaponEquipped() && !requestor.isCursedWeaponEquipped()) {
            if (target.isInFightEvent() && !target.getFightEvent().canJoinParty(requestor, target)) {
               requestor.sendPacket(SystemMessageId.INCORRECT_TARGET);
            } else if (!target.isJailed() && !requestor.isJailed()) {
               if (!target.isInOlympiadMode() && !requestor.isInOlympiadMode()
                  || target.isInOlympiadMode() == requestor.isInOlympiadMode()
                     && target.getOlympiadGameId() == requestor.getOlympiadGameId()
                     && target.getOlympiadSide() == requestor.getOlympiadSide()) {
                  SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_INVITED_TO_PARTY);
                  sm.addCharName(target);
                  requestor.sendPacket(sm);
                  if (!requestor.isInParty()) {
                     this.createNewParty(target, requestor);
                  } else if (requestor.getParty().isInDimensionalRift()) {
                     requestor.sendMessage("You cannot invite a player when you are in the Dimensional Rift.");
                  } else {
                     this.addTargetToParty(target, requestor);
                  }
               } else {
                  requestor.sendPacket(SystemMessageId.A_USER_CURRENTLY_PARTICIPATING_IN_THE_OLYMPIAD_CANNOT_SEND_PARTY_AND_FRIEND_INVITATIONS);
               }
            } else {
               requestor.sendMessage("You cannot invite a player while is in Jail.");
            }
         } else {
            requestor.sendPacket(SystemMessageId.INCORRECT_TARGET);
         }
      }
   }

   private void addTargetToParty(Player target, Player requestor) {
      Party party = requestor.getParty();
      if (!party.isLeader(requestor)) {
         requestor.sendPacket(SystemMessageId.ONLY_LEADER_CAN_INVITE);
      } else if (party.getMemberCount() >= 9) {
         requestor.sendPacket(SystemMessageId.PARTY_FULL);
      } else if (party.getPendingInvitation() && !party.isInvitationRequestExpired()) {
         requestor.sendPacket(SystemMessageId.WAITING_FOR_ANOTHER_REPLY);
      } else {
         if (!target.isProcessingRequest()) {
            requestor.onTransactionRequest(target);
            target.sendPacket(new AskJoinParty(requestor.getName(), party.getLootDistribution()));
            party.setPendingInvitation(true);
            if (Config.DEBUG) {
               _log.fine("sent out a party invitation to:" + target.getName());
            }
         } else {
            SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_BUSY_TRY_LATER);
            sm.addString(target.getName());
            requestor.sendPacket(sm);
            if (Config.DEBUG) {
               _log.warning(requestor.getName() + " already received a party invitation");
            }
         }
      }
   }

   private void createNewParty(Player target, Player requestor) {
      if (!target.isProcessingRequest()) {
         requestor.setParty(new Party(requestor, this._itemDistribution));
         requestor.onTransactionRequest(target);
         target.sendPacket(new AskJoinParty(requestor.getName(), this._itemDistribution));
         requestor.getParty().setPendingInvitation(true);
         if (Config.DEBUG) {
            _log.fine("sent out a party invitation to:" + target.getName());
         }
      } else {
         requestor.sendPacket(SystemMessageId.WAITING_FOR_ANOTHER_REPLY);
         if (Config.DEBUG) {
            _log.warning(requestor.getName() + " already received a party invitation");
         }
      }
   }
}
