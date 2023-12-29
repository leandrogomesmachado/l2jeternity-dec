package l2e.gameserver.network.clientpackets;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import l2e.commons.util.Rnd;
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

public final class RequestExEnchantSkillChange extends GameClientPacket {
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
               Skill skill = SkillsParser.getInstance().getInfo(this._skillId, this._skillLvl);
               if (skill != null) {
                  int reqItemId = 9626;
                  EnchantSkillLearn s = EnchantSkillGroupsParser.getInstance().getSkillEnchantmentBySkillId(this._skillId);
                  if (s != null) {
                     int beforeEnchantSkillLevel = player.getSkillLevel(this._skillId);
                     if (beforeEnchantSkillLevel > 100) {
                        int currentEnchantLevel = beforeEnchantSkillLevel % 100;
                        if (currentEnchantLevel == this._skillLvl % 100) {
                           EnchantSkillGroup.EnchantSkillsHolder esd = s.getEnchantSkillsHolder(this._skillLvl);
                           int requiredSp = esd.getSpCost();
                           int requireditems = esd.getAdenaCost();
                           if (player.getSp() >= requiredSp) {
                              ItemInstance spb = player.getInventory().getItemByItemId(9626);
                              if (Config.ES_SP_BOOK_NEEDED && spb == null) {
                                 player.sendPacket(SystemMessageId.YOU_DONT_HAVE_ALL_ITENS_NEEDED_TO_CHANGE_SKILL_ENCHANT_ROUTE);
                                 return;
                              }

                              if (player.getInventory().getAdena() < (long)requireditems) {
                                 player.sendPacket(SystemMessageId.YOU_DONT_HAVE_ALL_OF_THE_ITEMS_NEEDED_TO_ENCHANT_THAT_SKILL);
                                 return;
                              }

                              boolean check = player.getStat().removeExpAndSp(0L, requiredSp, false);
                              if (Config.ES_SP_BOOK_NEEDED) {
                                 check &= player.destroyItem("Consume", spb.getObjectId(), 1L, player, true);
                              }

                              check &= player.destroyItemByItemId("Consume", 57, (long)requireditems, player, true);
                              if (!check) {
                                 player.sendPacket(SystemMessageId.YOU_DONT_HAVE_ALL_OF_THE_ITEMS_NEEDED_TO_ENCHANT_THAT_SKILL);
                                 return;
                              }

                              int levelPenalty = Rnd.get(Math.min(4, currentEnchantLevel));
                              this._skillLvl -= levelPenalty;
                              if (this._skillLvl % 100 == 0) {
                                 this._skillLvl = s.getBaseLevel();
                              }

                              skill = SkillsParser.getInstance().getInfo(this._skillId, this._skillLvl);
                              if (skill != null) {
                                 if (Config.LOG_SKILL_ENCHANTS) {
                                    LogRecord record = new LogRecord(Level.INFO, "Route Change");
                                    record.setParameters(new Object[]{player, skill, spb});
                                    record.setLoggerName("skill");
                                    _logEnchant.log(record);
                                 }

                                 player.addSkill(skill, true);
                                 player.sendPacket(ExEnchantSkillResult.valueOf(true));
                              }

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
                              if (levelPenalty == 0) {
                                 SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.SKILL_ENCHANT_CHANGE_SUCCESSFUL_S1_LEVEL_WILL_REMAIN);
                                 sm.addSkillName(this._skillId);
                                 player.sendPacket(sm);
                              } else {
                                 SystemMessage sm = SystemMessage.getSystemMessage(
                                    SystemMessageId.SKILL_ENCHANT_CHANGE_SUCCESSFUL_S1_LEVEL_WAS_DECREASED_BY_S2
                                 );
                                 sm.addSkillName(this._skillId);
                                 sm.addNumber(levelPenalty);
                                 player.sendPacket(sm);
                              }

                              player.sendSkillList(false);
                              int afterEnchantSkillLevel = player.getSkillLevel(this._skillId);
                              player.sendPacket(new ExEnchantSkillInfo(this._skillId, afterEnchantSkillLevel));
                              player.sendPacket(new ExEnchantSkillInfoDetail(3, this._skillId, afterEnchantSkillLevel, player));
                              player.updateShortCuts(this._skillId, afterEnchantSkillLevel);
                           } else {
                              SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_DONT_HAVE_ENOUGH_SP_TO_ENCHANT_THAT_SKILL);
                              player.sendPacket(sm);
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
