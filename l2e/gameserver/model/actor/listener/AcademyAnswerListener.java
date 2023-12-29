package l2e.gameserver.model.actor.listener;

import l2e.gameserver.listener.talk.OnAnswerListener;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.service.academy.AcademyList;
import l2e.gameserver.model.strings.server.ServerMessage;
import l2e.gameserver.model.strings.server.ServerStorage;
import l2e.gameserver.network.serverpackets.CreatureSay;

public class AcademyAnswerListener implements OnAnswerListener {
   private final Player _activeChar;
   private final Player _academyChar;

   public AcademyAnswerListener(Player activeChar, Player academyChar) {
      this._activeChar = activeChar;
      this._academyChar = academyChar;
   }

   @Override
   public void sayYes() {
      Player activeChar = this._activeChar;
      Player academyChar = this._academyChar;
      if (activeChar != null && academyChar != null) {
         AcademyList.inviteInAcademy(activeChar, academyChar);
      }
   }

   @Override
   public void sayNo() {
      Player activeChar = this._activeChar;
      Player academyChar = this._academyChar;
      if (activeChar != null && academyChar != null) {
         ServerMessage msg = new ServerMessage("CommunityAcademy.REFUSED", activeChar.getLang());
         msg.add(academyChar.getName());
         activeChar.sendPacket(
            new CreatureSay(
               activeChar.getObjectId(), 20, ServerStorage.getInstance().getString(activeChar.getLang(), "CommunityAcademy.ACADEMY"), msg.toString()
            )
         );
      }
   }
}
