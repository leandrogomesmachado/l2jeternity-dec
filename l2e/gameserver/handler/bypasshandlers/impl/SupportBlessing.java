package l2e.gameserver.handler.bypasshandlers.impl;

import l2e.gameserver.data.parser.SkillsParser;
import l2e.gameserver.handler.bypasshandlers.IBypassHandler;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;

public class SupportBlessing implements IBypassHandler {
   private static final String[] COMMANDS = new String[]{"GiveBlessing"};

   @Override
   public boolean useBypass(String command, Player activeChar, Creature target) {
      if (!target.isNpc()) {
         return false;
      } else {
         Npc npc = (Npc)target;
         if (activeChar.getLevel() <= 39 && activeChar.getClassId().level() < 2) {
            npc.setTarget(activeChar);
            npc.doCast(SkillsParser.FrequentSkill.BLESSING_OF_PROTECTION.getSkill());
            return false;
         } else {
            npc.showChatWindow(activeChar, "data/html/default/SupportBlessingHighLevel.htm");
            return true;
         }
      }
   }

   @Override
   public String[] getBypassList() {
      return COMMANDS;
   }
}
