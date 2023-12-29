package l2e.gameserver.handler.bypasshandlers.impl;

import l2e.gameserver.data.parser.CategoryParser;
import l2e.gameserver.handler.bypasshandlers.IBypassHandler;
import l2e.gameserver.model.CategoryType;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.holders.SkillHolder;

public class SupportMagic implements IBypassHandler {
   private static final String[] COMMANDS = new String[]{"supportmagicservitor", "supportmagic"};
   private static final SkillHolder HASTE_1 = new SkillHolder(4327, 1);
   private static final SkillHolder HASTE_2 = new SkillHolder(5632, 1);
   private static final SkillHolder CUBIC = new SkillHolder(4338, 1);
   private static final SkillHolder[] FIGHTER_BUFFS = new SkillHolder[]{
      new SkillHolder(4322, 1), new SkillHolder(4323, 1), new SkillHolder(4324, 1), new SkillHolder(4325, 1), new SkillHolder(4326, 1)
   };
   private static final SkillHolder[] MAGE_BUFFS = new SkillHolder[]{
      new SkillHolder(4322, 1),
      new SkillHolder(4323, 1),
      new SkillHolder(4328, 1),
      new SkillHolder(4329, 1),
      new SkillHolder(4330, 1),
      new SkillHolder(4331, 1)
   };
   private static final SkillHolder[] SUMMON_BUFFS = new SkillHolder[]{
      new SkillHolder(4322, 1),
      new SkillHolder(4323, 1),
      new SkillHolder(4324, 1),
      new SkillHolder(4325, 1),
      new SkillHolder(4326, 1),
      new SkillHolder(4328, 1),
      new SkillHolder(4329, 1),
      new SkillHolder(4330, 1),
      new SkillHolder(4331, 1)
   };
   private static final int LOWEST_LEVEL = 6;
   private static final int HIGHEST_LEVEL = 75;
   private static final int CUBIC_LOWEST = 16;
   private static final int CUBIC_HIGHEST = 34;
   private static final int HASTE_LEVEL_2 = 40;

   @Override
   public boolean useBypass(String command, Player activeChar, Creature target) {
      if (target.isNpc() && !activeChar.isCursedWeaponEquipped()) {
         if (command.equalsIgnoreCase(COMMANDS[0])) {
            makeSupportMagic(activeChar, (Npc)target, true);
         } else if (command.equalsIgnoreCase(COMMANDS[1])) {
            makeSupportMagic(activeChar, (Npc)target, false);
         }

         return true;
      } else {
         return false;
      }
   }

   private static void makeSupportMagic(Player player, Npc npc, boolean isSummon) {
      int level = player.getLevel();
      if (isSummon && !player.hasServitor()) {
         npc.showChatWindow(player, "data/html/default/SupportMagicNoSummon.htm");
      } else if (level > 75) {
         npc.showChatWindow(player, "data/html/default/SupportMagicHighLevel.htm");
      } else if (level < 6) {
         npc.showChatWindow(player, "data/html/default/SupportMagicLowLevel.htm");
      } else if (player.getClassId().level() == 3) {
         player.sendMessage("Only adventurers who have not completed their 3rd class transfer may receive these buffs.");
      } else {
         if (isSummon) {
            npc.setTarget(player.getSummon());

            for(SkillHolder skill : SUMMON_BUFFS) {
               npc.doCast(skill.getSkill());
            }

            if (level >= 40) {
               npc.doCast(HASTE_2.getSkill());
            } else {
               npc.doCast(HASTE_1.getSkill());
            }
         } else {
            npc.setTarget(player);
            if (CategoryParser.getInstance().isInCategory(CategoryType.BEGINNER_MAGE, player.getClassId().getId())) {
               for(SkillHolder skill : MAGE_BUFFS) {
                  npc.doCast(skill.getSkill());
               }
            } else {
               for(SkillHolder skill : FIGHTER_BUFFS) {
                  npc.doCast(skill.getSkill());
               }

               if (level >= 40) {
                  npc.doCast(HASTE_2.getSkill());
               } else {
                  npc.doCast(HASTE_1.getSkill());
               }
            }

            if (level >= 16 && level <= 34) {
               player.doSimultaneousCast(CUBIC.getSkill());
            }
         }
      }
   }

   @Override
   public String[] getBypassList() {
      return COMMANDS;
   }
}
