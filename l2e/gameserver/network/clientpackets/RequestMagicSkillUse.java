package l2e.gameserver.network.clientpackets;

import l2e.commons.util.Util;
import l2e.gameserver.Config;
import l2e.gameserver.ai.model.CtrlIntention;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.skills.SkillType;
import l2e.gameserver.model.skills.effects.EffectType;
import l2e.gameserver.model.skills.targets.TargetType;
import l2e.gameserver.network.SystemMessageId;

public final class RequestMagicSkillUse extends GameClientPacket {
   private int _magicId;
   private boolean _ctrlPressed;
   private boolean _shiftPressed;

   @Override
   protected void readImpl() {
      this._magicId = this.readD();
      this._ctrlPressed = this.readD() != 0;
      this._shiftPressed = this.readC() != 0;
   }

   @Override
   protected void runImpl() {
      Player activeChar = this.getActiveChar();
      if (activeChar != null) {
         if (System.currentTimeMillis() - activeChar.getLastRequestMagicPacket() >= Config.REQUEST_MAGIC_PACKET_DELAY) {
            activeChar.setLastRequestMagicPacket();
            if (activeChar.isOutOfControl()) {
               activeChar.sendActionFailed();
            } else {
               Skill skill = activeChar.getKnownSkill(this._magicId);
               if (skill == null) {
                  skill = activeChar.getCustomSkill(this._magicId);
                  if (skill == null) {
                     activeChar.sendActionFailed();
                     Util.handleIllegalPlayerAction(activeChar, "SkillId " + this._magicId + " not found in player: " + activeChar.getName() + "!");
                     return;
                  }
               }

               if (activeChar.isPlayable() && activeChar.isInAirShip()) {
                  activeChar.sendPacket(SystemMessageId.ACTION_PROHIBITED_WHILE_MOUNTED_OR_ON_AN_AIRSHIP);
                  activeChar.sendActionFailed();
               } else if ((activeChar.isTransformed() || activeChar.isInStance()) && !activeChar.hasTransformSkill(skill.getId())) {
                  activeChar.sendActionFailed();
               } else if (Config.ALT_GAME_KARMA_PLAYER_CAN_TELEPORT || activeChar.getKarma() <= 0 || !skill.hasEffectType(EffectType.TELEPORT)) {
                  if (!skill.isToggle() || !activeChar.isMounted()) {
                     if (skill.getSkillType() == SkillType.BUFF
                        && skill.getTargetType() == TargetType.SELF
                        && (!activeChar.isInAirShip() || !activeChar.isInBoat())) {
                        activeChar.getAI().setIntention(CtrlIntention.MOVING, activeChar.getLocation());
                     }

                     activeChar.useMagic(skill, this._ctrlPressed, this._shiftPressed, true);
                  }
               }
            }
         }
      }
   }
}
