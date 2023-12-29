package l2e.gameserver.network.clientpackets;

import l2e.gameserver.Config;
import l2e.gameserver.data.parser.SkillsParser;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.skills.effects.Effect;
import l2e.gameserver.model.skills.effects.EffectType;

public class RequestDispel extends GameClientPacket {
   private int _objectId;
   private int _skillId;
   private int _skillLevel;

   @Override
   protected void readImpl() {
      this._objectId = this.readD();
      this._skillId = this.readD();
      this._skillLevel = this.readD();
   }

   @Override
   protected void runImpl() {
      if (this._skillId > 0 && this._skillLevel > 0) {
         Player activeChar = this.getClient().getActiveChar();
         if (activeChar != null) {
            Effect[] effects = activeChar.getAllEffects();
            Skill skill = SkillsParser.getInstance().getInfo(this._skillId, this._skillLevel);
            if (skill != null) {
               if (skill.canBeDispeled() && !skill.isStayAfterDeath() && !skill.isDebuff() && !skill.hasEffectType(EffectType.STUN)) {
                  for(Effect eff : effects) {
                     if (eff.getAbnormalType() == "TRANSFORM") {
                        return;
                     }
                  }

                  if (!skill.isDance() || Config.DANCE_CANCEL_BUFF) {
                     if (activeChar.getObjectId() == this._objectId) {
                        activeChar.stopSkillEffects(this._skillId);
                     } else if (activeChar.hasSummon() && activeChar.getSummon().getObjectId() == this._objectId) {
                        activeChar.getSummon().stopSkillEffects(this._skillId);
                     }
                  }
               }
            }
         }
      }
   }
}
