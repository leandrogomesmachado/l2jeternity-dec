package l2e.gameserver.network.clientpackets;

import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.TargetUnselected;

public final class RequestTargetCancel extends GameClientPacket {
   private int _unselect;

   @Override
   protected void readImpl() {
      this._unselect = this.readH();
   }

   @Override
   protected void runImpl() {
      Player activeChar = this.getClient().getActiveChar();
      if (activeChar != null) {
         if (activeChar.isLockedTarget()) {
            activeChar.sendPacket(SystemMessageId.FAILED_DISABLE_TARGET);
         } else {
            if (this._unselect == 0) {
               if (activeChar.isCastingNow()) {
                  Skill skill = activeChar.getCastingSkill();
                  if (skill != null && skill.getHitTime() > 1000) {
                     activeChar.abortCast();
                  }
               } else if (activeChar.getTarget() != null) {
                  activeChar.setTarget(null);
               }
            } else if (activeChar.getTarget() != null) {
               activeChar.setTarget(null);
            } else if (activeChar.isInAirShip()) {
               activeChar.broadcastPacket(new TargetUnselected(activeChar));
            }
         }
      }
   }
}
