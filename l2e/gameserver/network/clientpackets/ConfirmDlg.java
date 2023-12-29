package l2e.gameserver.network.clientpackets;

import l2e.commons.util.GMAudit;
import l2e.gameserver.Config;
import l2e.gameserver.data.parser.AdminParser;
import l2e.gameserver.handler.admincommandhandlers.AdminCommandHandler;
import l2e.gameserver.handler.admincommandhandlers.IAdminCommandHandler;
import l2e.gameserver.handler.admincommandhandlers.impl.Teleports;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.holders.DoorRequestHolder;
import l2e.gameserver.model.holders.SummonRequestHolder;
import l2e.gameserver.network.SystemMessageId;

public final class ConfirmDlg extends GameClientPacket {
   private int _messageId;
   private int _answer;
   private int _requesterId;

   @Override
   protected void readImpl() {
      this._messageId = this.readD();
      this._answer = this.readD();
      this._requesterId = this.readD();
   }

   @Override
   public void runImpl() {
      Player activeChar = this.getClient().getActiveChar();
      if (activeChar != null) {
         if (this._messageId == SystemMessageId.S1.getId()) {
            String _command = activeChar.getAdminConfirmCmd();
            if (_command == null) {
               activeChar.scriptAswer(this._answer);
            } else {
               activeChar.setAdminConfirmCmd(null);
               if (this._answer == 0) {
                  return;
               }

               String command = _command.split(" ")[0];
               IAdminCommandHandler ach = AdminCommandHandler.getInstance().getHandler(command);
               if (AdminParser.getInstance().hasAccess(command, activeChar.getAccessLevel())) {
                  if (Config.GMAUDIT) {
                     GMAudit.auditGMAction(
                        activeChar.getName() + " [" + activeChar.getObjectId() + "]",
                        _command,
                        activeChar.getTarget() != null ? activeChar.getTarget().getName() : "no-target"
                     );
                  }

                  ach.useAdminCommand(_command, activeChar);
               }
            }
         } else if (this._messageId == SystemMessageId.RESSURECTION_REQUEST_BY_C1_FOR_S2_XP.getId()
            || this._messageId == SystemMessageId.RESURRECT_USING_CHARM_OF_COURAGE.getId()) {
            activeChar.reviveAnswer(this._answer);
         } else if (this._messageId == SystemMessageId.C1_WISHES_TO_SUMMON_YOU_FROM_S2_DO_YOU_ACCEPT.getId()) {
            SummonRequestHolder holder = activeChar.removeScript(SummonRequestHolder.class);
            if (this._answer == 1 && holder != null && holder.getTarget().getObjectId() == this._requesterId) {
               if (holder.isAdminRecall()) {
                  Teleports.teleportCharacter(activeChar, holder.getTarget().getX(), holder.getTarget().getY(), holder.getTarget().getZ(), holder.getTarget());
               } else {
                  activeChar.teleToLocation(holder.getTarget().getX(), holder.getTarget().getY(), holder.getTarget().getZ(), true);
               }
            }
         } else if (this._messageId == SystemMessageId.WOULD_YOU_LIKE_TO_OPEN_THE_GATE.getId()) {
            DoorRequestHolder holder = activeChar.removeScript(DoorRequestHolder.class);
            if (holder != null && holder.getDoor() == activeChar.getTarget() && this._answer == 1) {
               holder.getDoor().openMe();
            }
         } else if (this._messageId == SystemMessageId.WOULD_YOU_LIKE_TO_CLOSE_THE_GATE.getId()) {
            DoorRequestHolder holder = activeChar.removeScript(DoorRequestHolder.class);
            if (holder != null && holder.getDoor() == activeChar.getTarget() && this._answer == 1) {
               holder.getDoor().closeMe();
            }
         }
      }
   }
}
