package l2e.gameserver.network.clientpackets;

import l2e.commons.util.Util;
import l2e.gameserver.data.parser.SkillsParser;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.skills.Skill;

public final class RequestExMagicSkillUseGround extends GameClientPacket {
   private int _x;
   private int _y;
   private int _z;
   private int _skillId;
   private boolean _ctrlPressed;
   private boolean _shiftPressed;

   @Override
   protected void readImpl() {
      this._x = this.readD();
      this._y = this.readD();
      this._z = this.readD();
      this._skillId = this.readD();
      this._ctrlPressed = this.readD() != 0;
      this._shiftPressed = this.readC() != 0;
   }

   @Override
   protected void runImpl() {
      Player activeChar = this.getClient().getActiveChar();
      if (activeChar != null) {
         if (activeChar.isOutOfControl()) {
            activeChar.sendActionFailed();
         } else {
            int level = activeChar.getSkillLevel(this._skillId);
            if (level <= 0) {
               activeChar.sendActionFailed();
            } else {
               Skill skill = SkillsParser.getInstance().getInfo(this._skillId, level);
               if (skill != null) {
                  activeChar.setCurrentSkillWorldPosition(new Location(this._x, this._y, this._z));
                  activeChar.setHeading(Util.calculateHeadingFrom(activeChar.getX(), activeChar.getY(), this._x, this._y));
                  activeChar.useMagic(skill, this._ctrlPressed, this._shiftPressed, true);
               } else {
                  activeChar.sendActionFailed();
                  _log.warning("No skill found with id " + this._skillId + " and level " + level + " !!");
               }
            }
         }
      }
   }
}
