package l2e.gameserver.handler.voicedcommandhandlers.impl;

import l2e.gameserver.handler.voicedcommandhandlers.IVoicedCommandHandler;
import l2e.gameserver.instancemanager.CastleManager;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.instance.DoorInstance;
import l2e.gameserver.model.entity.Castle;
import l2e.gameserver.network.SystemMessageId;

public class CastleVCmd implements IVoicedCommandHandler {
   private static final String[] VOICED_COMMANDS = new String[]{"opendoors", "closedoors", "ridewyvern"};

   @Override
   public boolean useVoicedCommand(String command, Player activeChar, String params) {
      switch(command) {
         case "opendoors":
            if (!params.equals("castle")) {
               activeChar.sendMessage("Only Castle doors can be open.");
               return false;
            }

            if (!activeChar.isClanLeader()) {
               activeChar.sendPacket(SystemMessageId.ONLY_CLAN_LEADER_CAN_ISSUE_COMMANDS);
               return false;
            }

            DoorInstance door = (DoorInstance)activeChar.getTarget();
            if (door == null) {
               activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
               return false;
            }

            Castle castle = CastleManager.getInstance().getCastleById(activeChar.getClan().getCastleId());
            if (castle == null) {
               activeChar.sendMessage("Your clan does not own a castle.");
               return false;
            }

            if (castle.getSiege().getIsInProgress()) {
               activeChar.sendPacket(SystemMessageId.GATES_NOT_OPENED_CLOSED_DURING_SIEGE);
               return false;
            }

            if (castle.checkIfInZone(door.getX(), door.getY(), door.getZ())) {
               activeChar.sendPacket(SystemMessageId.GATE_IS_OPENING);
               door.openMe();
            }
            break;
         case "closedoors":
            if (!params.equals("castle")) {
               activeChar.sendMessage("Only Castle doors can be closed.");
               return false;
            }

            if (!activeChar.isClanLeader()) {
               activeChar.sendPacket(SystemMessageId.ONLY_CLAN_LEADER_CAN_ISSUE_COMMANDS);
               return false;
            }

            DoorInstance door2 = (DoorInstance)activeChar.getTarget();
            if (door2 == null) {
               activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
               return false;
            }

            Castle castle2 = CastleManager.getInstance().getCastleById(activeChar.getClan().getCastleId());
            if (castle2 == null) {
               activeChar.sendMessage("Your clan does not own a castle.");
               return false;
            }

            if (castle2.getSiege().getIsInProgress()) {
               activeChar.sendPacket(SystemMessageId.GATES_NOT_OPENED_CLOSED_DURING_SIEGE);
               return false;
            }

            if (castle2.checkIfInZone(door2.getX(), door2.getY(), door2.getZ())) {
               activeChar.sendMessage("The gate is being closed.");
               door2.closeMe();
            }
            break;
         case "ridewyvern":
            if (activeChar.isClanLeader() && activeChar.getClan().getCastleId() > 0) {
               activeChar.mount(12621, 0, true);
            }
      }

      return true;
   }

   @Override
   public String[] getVoicedCommandList() {
      return VOICED_COMMANDS;
   }
}
