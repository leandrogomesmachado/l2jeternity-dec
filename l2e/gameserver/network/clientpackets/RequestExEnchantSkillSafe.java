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

public final class RequestExEnchantSkillSafe extends GameClientPacket {
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
            } else if (player.isSellingBuffs()) {
               player.sendMessage("You can not use the skill enchanting function while you selling buffs.");
            } else {
               Skill skill = SkillsParser.getInstance().getInfo(this._skillId, this._skillLvl);
               if (skill != null) {
                  int costMultiplier = EnchantSkillGroupsParser.SAFE_ENCHANT_COST_MULTIPLIER;
                  int reqItemId = 9627;
                  EnchantSkillLearn s = EnchantSkillGroupsParser.getInstance().getSkillEnchantmentBySkillId(this._skillId);
                  if (s != null) {
                     EnchantSkillGroup.EnchantSkillsHolder esd = s.getEnchantSkillsHolder(this._skillLvl);
                     int beforeEnchantSkillLevel = player.getSkillLevel(this._skillId);
                     if (beforeEnchantSkillLevel == s.getMinSkillLevel(this._skillLvl)) {
                        int requiredSp = esd.getSpCost() * costMultiplier;
                        int requireditems = esd.getAdenaCost() * costMultiplier;
                        int rate = esd.getRate(player);
                        if (player.getSp() >= requiredSp) {
                           ItemInstance spb = player.getInventory().getItemByItemId(9627);
                           if (spb == null) {
                              player.sendPacket(SystemMessageId.YOU_DONT_HAVE_ALL_OF_THE_ITEMS_NEEDED_TO_ENCHANT_THAT_SKILL);
                              return;
                           }

                           if (player.getInventory().getAdena() < (long)requireditems) {
                              player.sendPacket(SystemMessageId.YOU_DONT_HAVE_ALL_OF_THE_ITEMS_NEEDED_TO_ENCHANT_THAT_SKILL);
                              return;
                           }

                           boolean check = player.getStat().removeExpAndSp(0L, requiredSp, false);
                           check &= player.destroyItem("Consume", spb.getObjectId(), 1L, player, true);
                           check &= player.destroyItemByItemId("Consume", 57, (long)requireditems, player, true);
                           if (!check) {
                              player.sendPacket(SystemMessageId.YOU_DONT_HAVE_ALL_OF_THE_ITEMS_NEEDED_TO_ENCHANT_THAT_SKILL);
                              return;
                           }

                           if (Rnd.get(100) <= rate) {
                              if (Config.LOG_SKILL_ENCHANTS) {
                                 LogRecord record = new LogRecord(Level.INFO, "Safe Success");
                                 record.setParameters(new Object[]{player, skill, spb, rate});
                                 record.setLoggerName("skill");
                                 _logEnchant.log(record);
                              }

                              player.addSkill(skill, true);
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

                              player.sendPacket(ExEnchantSkillResult.valueOf(true));
                              SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_HAVE_SUCCEEDED_IN_ENCHANTING_THE_SKILL_S1);
                              sm.addSkillName(this._skillId);
                              player.sendPacket(sm);
                           } else {
                              if (Config.LOG_SKILL_ENCHANTS) {
                                 LogRecord record = new LogRecord(Level.INFO, "Safe Fail");
                                 record.setParameters(new Object[]{player, skill, spb, rate});
                                 record.setLoggerName("skill");
                                 _logEnchant.log(record);
                              }

                              SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.SKILL_ENCHANT_FAILED_S1_LEVEL_WILL_REMAIN);
                              sm.addSkillName(this._skillId);
                              player.sendPacket(sm);
                              player.sendPacket(ExEnchantSkillResult.valueOf(false));
                           }

                           player.sendUserInfo();
                           player.sendSkillList(false);
                           int afterEnchantSkillLevel = player.getSkillLevel(this._skillId);
                           player.sendPacket(new ExEnchantSkillInfo(this._skillId, afterEnchantSkillLevel));
                           player.sendPacket(new ExEnchantSkillInfoDetail(1, this._skillId, afterEnchantSkillLevel + 1, player));
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
