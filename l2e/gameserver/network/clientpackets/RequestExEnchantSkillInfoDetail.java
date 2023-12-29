package l2e.gameserver.network.clientpackets;

import l2e.gameserver.data.parser.EnchantSkillGroupsParser;
import l2e.gameserver.model.EnchantSkillLearn;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.network.serverpackets.ExEnchantSkillInfoDetail;

public final class RequestExEnchantSkillInfoDetail extends GameClientPacket {
   private int _type;
   private int _skillId;
   private int _skillLvl;

   @Override
   protected void readImpl() {
      this._type = this.readD();
      this._skillId = this.readD();
      this._skillLvl = this.readD();
   }

   @Override
   protected void runImpl() {
      if (this._skillId > 0 && this._skillLvl > 0) {
         Player activeChar = this.getClient().getActiveChar();
         if (activeChar != null) {
            int reqSkillLvl = -2;
            if (this._type == 0 || this._type == 1) {
               reqSkillLvl = this._skillLvl - 1;
            } else if (this._type == 2) {
               reqSkillLvl = this._skillLvl + 1;
            } else if (this._type == 3) {
               reqSkillLvl = this._skillLvl;
            }

            int playerSkillLvl = activeChar.getSkillLevel(this._skillId);
            if (playerSkillLvl != -1) {
               if (reqSkillLvl % 100 == 0) {
                  EnchantSkillLearn esl = EnchantSkillGroupsParser.getInstance().getSkillEnchantmentBySkillId(this._skillId);
                  if (esl == null) {
                     return;
                  }

                  if (playerSkillLvl != esl.getBaseLevel()) {
                     return;
                  }
               } else if (playerSkillLvl != reqSkillLvl && this._type == 3 && playerSkillLvl % 100 != this._skillLvl % 100) {
                  return;
               }

               ExEnchantSkillInfoDetail esd = new ExEnchantSkillInfoDetail(this._type, this._skillId, this._skillLvl, activeChar);
               activeChar.sendPacket(esd);
            }
         }
      }
   }
}
