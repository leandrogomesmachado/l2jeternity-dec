package l2e.gameserver.network.clientpackets;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import l2e.gameserver.Config;
import l2e.gameserver.data.parser.EnchantSkillGroupsParser;
import l2e.gameserver.data.parser.SkillsParser;
import l2e.gameserver.model.EnchantSkillGroup;
import l2e.gameserver.model.EnchantSkillLearn;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.ExEnchantSkillInfo;
import l2e.gameserver.network.serverpackets.ExEnchantSkillInfoDetail;
import l2e.gameserver.network.serverpackets.ExEnchantSkillResult;
import l2e.gameserver.network.serverpackets.SystemMessage;

public final class RequestExEnchantSkillUntrain extends GameClientPacket {
   private static final Logger _logEnchant = Logger.getLogger("enchant");
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
         Player player = this.getClient().getActiveChar();
         if (player != null) {
            if (player.getClassId().level() < 3) {
               player.sendPacket(SystemMessageId.YOU_CANNOT_USE_SKILL_ENCHANT_IN_THIS_CLASS);
            } else if (player.getLevel() < 76) {
               player.sendPacket(SystemMessageId.YOU_CANNOT_USE_SKILL_ENCHANT_ON_THIS_LEVEL);
            } else if (!player.isAllowedToEnchantSkills()) {
               player.sendPacket(SystemMessageId.YOU_CANNOT_USE_SKILL_ENCHANT_ATTACKING_TRANSFORMED_BOAT);
            } else {
               EnchantSkillLearn s = EnchantSkillGroupsParser.getInstance().getSkillEnchantmentBySkillId(this._skillId);
               if (s != null) {
                  if (this._skillLvl % 100 == 0) {
                     this._skillLvl = s.getBaseLevel();
                  }

                  Skill skill = SkillsParser.getInstance().getInfo(this._skillId, this._skillLvl);
                  if (skill != null) {
                     int reqItemId = 9625;
                     int beforeUntrainSkillLevel = player.getSkillLevel(this._skillId);
                     if (beforeUntrainSkillLevel - 1 == this._skillLvl || beforeUntrainSkillLevel % 100 == 1 && this._skillLvl == s.getBaseLevel()) {
                        EnchantSkillGroup.EnchantSkillsHolder esd = s.getEnchantSkillsHolder(beforeUntrainSkillLevel);
                        int requiredSp = esd.getSpCost();
                        int requireditems = esd.getAdenaCost();
                        ItemInstance spb = player.getInventory().getItemByItemId(9625);
                        if (Config.ES_SP_BOOK_NEEDED && spb == null) {
                           player.sendPacket(SystemMessageId.YOU_DONT_HAVE_ALL_OF_THE_ITEMS_NEEDED_TO_ENCHANT_THAT_SKILL);
                        } else if (player.getInventory().getAdena() < (long)requireditems) {
                           player.sendPacket(SystemMessageId.YOU_DONT_HAVE_ALL_OF_THE_ITEMS_NEEDED_TO_ENCHANT_THAT_SKILL);
                        } else {
                           boolean check = true;
                           if (Config.ES_SP_BOOK_NEEDED) {
                              check &= player.destroyItem("Consume", spb.getObjectId(), 1L, player, true);
                           }

                           check &= player.destroyItemByItemId("Consume", 57, (long)requireditems, player, true);
                           if (!check) {
                              player.sendPacket(SystemMessageId.YOU_DONT_HAVE_ALL_OF_THE_ITEMS_NEEDED_TO_ENCHANT_THAT_SKILL);
                           } else {
                              player.getStat().addSp((int)((double)requiredSp * 0.8));
                              if (Config.LOG_SKILL_ENCHANTS) {
                                 LogRecord record = new LogRecord(Level.INFO, "Untrain");
                                 record.setParameters(new Object[]{player, skill, spb});
                                 record.setLoggerName("skill");
                                 _logEnchant.log(record);
                              }

                              player.addSkill(skill, true);
                              player.sendPacket(ExEnchantSkillResult.valueOf(true));
                              if (Config.DEBUG) {
                                 _log.fine(
                                    "Learned skill ID: "
                                       + this._skillId
                                       + " Level: "
                                       + this._skillLvl
                                       + " for "
                                       + requiredSp
                                       + " SP, "
                                       + requireditems
                                       + " Adena."
                                 );
                              }

                              player.sendUserInfo();
                              if (this._skillLvl > 100) {
                                 SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.UNTRAIN_SUCCESSFUL_SKILL_S1_ENCHANT_LEVEL_DECREASED_BY_ONE);
                                 sm.addSkillName(this._skillId);
                                 player.sendPacket(sm);
                              } else {
                                 SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.UNTRAIN_SUCCESSFUL_SKILL_S1_ENCHANT_LEVEL_RESETED);
                                 sm.addSkillName(this._skillId);
                                 player.sendPacket(sm);
                              }

                              player.sendSkillList(false);
                              int afterUntrainSkillLevel = player.getSkillLevel(this._skillId);
                              player.sendPacket(new ExEnchantSkillInfo(this._skillId, afterUntrainSkillLevel));
                              player.sendPacket(new ExEnchantSkillInfoDetail(2, this._skillId, afterUntrainSkillLevel - 1, player));
                              player.updateShortCuts(this._skillId, afterUntrainSkillLevel);
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }
}
