package l2e.gameserver.network.clientpackets;

import l2e.gameserver.Config;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.ExDuelAskStart;
import l2e.gameserver.network.serverpackets.SystemMessage;

public final class RequestDuelStart extends GameClientPacket {
   private String _player;
   private int _partyDuel;

   @Override
   protected void readImpl() {
      this._player = this.readS();
      this._partyDuel = this.readD();
   }

   @Override
   protected void runImpl() {
      Player activeChar = this.getClient().getActiveChar();
      Player targetChar = World.getInstance().getPlayer(this._player);
      if (activeChar != null) {
         if (activeChar.isActionsDisabled()) {
            activeChar.sendActionFailed();
         } else if (targetChar == null) {
            activeChar.sendPacket(SystemMessageId.THERE_IS_NO_OPPONENT_TO_RECEIVE_YOUR_CHALLENGE_FOR_A_DUEL);
         } else if (activeChar == targetChar) {
            activeChar.sendPacket(SystemMessageId.THERE_IS_NO_OPPONENT_TO_RECEIVE_YOUR_CHALLENGE_FOR_A_DUEL);
         } else if (!targetChar.isInFightEvent()) {
            if (!activeChar.canDuel()) {
               activeChar.sendPacket(SystemMessageId.YOU_ARE_UNABLE_TO_REQUEST_A_DUEL_AT_THIS_TIME);
            } else if (!targetChar.canDuel()) {
               activeChar.sendPacket(targetChar.getNoDuelReason());
            } else if (!activeChar.isInsideRadius(targetChar, 250, false, false)) {
               SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.C1_CANNOT_RECEIVE_A_DUEL_CHALLENGE_BECAUSE_C1_IS_TOO_FAR_AWAY);
               msg.addString(targetChar.getName());
               activeChar.sendPacket(msg);
            } else {
               if (this._partyDuel == 1) {
                  if (!activeChar.isInParty() || !activeChar.isInParty() || !activeChar.getParty().isLeader(activeChar)) {
                     activeChar.sendMessage("You have to be the leader of a party in order to request a party duel.");
                     return;
                  }

                  if (!targetChar.isInParty()) {
                     activeChar.sendPacket(SystemMessageId.SINCE_THE_PERSON_YOU_CHALLENGED_IS_NOT_CURRENTLY_IN_A_PARTY_THEY_CANNOT_DUEL_AGAINST_YOUR_PARTY);
                     return;
                  }

                  if (activeChar.getParty().containsPlayer(targetChar)) {
                     activeChar.sendMessage("This player is a member of your own party.");
                     return;
                  }

                  for(Player temp : activeChar.getParty().getMembers()) {
                     if (!temp.canDuel()) {
                        activeChar.sendMessage("Not all the members of your party are ready for a duel.");
                        return;
                     }
                  }

                  Player partyLeader = null;

                  for(Player temp : targetChar.getParty().getMembers()) {
                     if (partyLeader == null) {
                        partyLeader = temp;
                     }

                     if (!temp.canDuel()) {
                        activeChar.sendPacket(SystemMessageId.THE_OPPOSING_PARTY_IS_CURRENTLY_UNABLE_TO_ACCEPT_A_CHALLENGE_TO_A_DUEL);
                        return;
                     }
                  }

                  if (partyLeader != null) {
                     if (!partyLeader.isProcessingRequest()) {
                        activeChar.onTransactionRequest(partyLeader);
                        partyLeader.sendPacket(new ExDuelAskStart(activeChar.getName(), this._partyDuel));
                        if (Config.DEBUG) {
                           _log.fine(activeChar.getName() + " requested a duel with " + partyLeader.getName());
                        }

                        SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.C1_PARTY_HAS_BEEN_CHALLENGED_TO_A_DUEL);
                        msg.addString(partyLeader.getName());
                        activeChar.sendPacket(msg);
                        msg = SystemMessage.getSystemMessage(SystemMessageId.C1_PARTY_HAS_CHALLENGED_YOUR_PARTY_TO_A_DUEL);
                        msg.addString(activeChar.getName());
                        targetChar.sendPacket(msg);
                     } else {
                        SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_BUSY_TRY_LATER);
                        msg.addString(partyLeader.getName());
                        activeChar.sendPacket(msg);
                     }
                  }
               } else if (!targetChar.isProcessingRequest()) {
                  activeChar.onTransactionRequest(targetChar);
                  targetChar.sendPacket(new ExDuelAskStart(activeChar.getName(), this._partyDuel));
                  if (Config.DEBUG) {
                     _log.fine(activeChar.getName() + " requested a duel with " + targetChar.getName());
                  }

                  SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.C1_HAS_BEEN_CHALLENGED_TO_A_DUEL);
                  msg.addString(targetChar.getName());
                  activeChar.sendPacket(msg);
                  msg = SystemMessage.getSystemMessage(SystemMessageId.C1_HAS_CHALLENGED_YOU_TO_A_DUEL);
                  msg.addString(activeChar.getName());
                  targetChar.sendPacket(msg);
               } else {
                  SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_BUSY_TRY_LATER);
                  msg.addString(targetChar.getName());
                  activeChar.sendPacket(msg);
               }
            }
         }
      }
   }
}
