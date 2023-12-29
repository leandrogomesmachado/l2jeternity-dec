package l2e.gameserver.network.clientpackets;

import l2e.gameserver.data.parser.EnchantSkillGroupsParser;
import l2e.gameserver.data.parser.SkillsParser;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.network.serverpackets.ExEnchantSkillInfo;

public final class RequestExEnchantSkillInfo extends GameClientPacket {
   private int _skillId;
   private int _skillLvl;

   @Override
   protected void readImpl() {
      this._skillId = this.readD();
      this._skillLvl = this.readD();
   }

   @Override
   protected void runImpl() {
      if (this._skillId > 0 && this._skillLvl > 0) {
         Player activeChar = this.getClient().getActiveChar();
         if (activeChar != null) {
            if (activeChar.getLevel() >= 76) {
               Skill skill = SkillsParser.getInstance().getInfo(this._skillId, this._skillLvl);
               if (skill != null && skill.getId() == this._skillId) {
                  if (EnchantSkillGroupsParser.getInstance().getSkillEnchantmentBySkillId(this._skillId) != null) {
                     int playerSkillLvl = activeChar.getSkillLevel(this._skillId);
                     if (playerSkillLvl != -1 && playerSkillLvl == this._skillLvl) {
                        activeChar.sendPacket(new ExEnchantSkillInfo(this._skillId, this._skillLvl));
                     }
                  }
               }
            }
         }
      }
   }
}
