package l2e.gameserver.network.serverpackets;

import java.util.Collection;
import l2e.gameserver.data.parser.SkillsParser;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.skills.Skill;

public class GMViewSkillInfo extends GameServerPacket {
   private final Player _activeChar;
   private final Collection<Skill> _skills;

   public GMViewSkillInfo(Player cha) {
      this._activeChar = cha;
      this._skills = this._activeChar.getAllSkills();
   }

   @Override
   protected final void writeImpl() {
      this.writeS(this._activeChar.getName());
      this.writeD(this._skills.size());
      boolean isDisabled = this._activeChar.getClan() != null ? this._activeChar.getClan().getReputationScore() < 0 : false;

      for(Skill skill : this._skills) {
         this.writeD(skill.isPassive() ? 1 : 0);
         this.writeD(skill.getDisplayLevel());
         this.writeD(skill.getDisplayId());
         this.writeC(isDisabled && skill.isClanSkill() ? 1 : 0);
         this.writeC(SkillsParser.getInstance().isEnchantable(skill.getDisplayId()) ? 1 : 0);
      }
   }
}
