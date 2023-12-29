package l2e.gameserver.handler.bypasshandlers.impl;

import java.util.StringTokenizer;
import java.util.logging.Level;
import l2e.gameserver.handler.bypasshandlers.IBypassHandler;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.holders.SkillHolder;

public class ElcardiaBuff implements IBypassHandler {
   private static final String[] COMMANDS = new String[]{"Request_Blessing"};
   private final int[][] BUFFS = new int[][]{{6714, 6715, 6716, 6718, 6719, 6720, 6727, 6729}, {6714, 6717, 6720, 6721, 6722, 6723, 6727, 6729}};

   @Override
   public boolean useBypass(String command, Player activeChar, Creature target) {
      if (!target.isNpc()) {
         return false;
      } else {
         Npc npc = (Npc)target;
         StringTokenizer st = new StringTokenizer(command);

         try {
            String cmd = st.nextToken();
            if (cmd.equalsIgnoreCase(COMMANDS[0])) {
               for(int skillId : this.BUFFS[activeChar.isMageClass() ? 1 : 0]) {
                  SkillHolder skill = new SkillHolder(skillId, 1);
                  if (skill.getSkill() != null) {
                     npc.setTarget(activeChar);
                     npc.doCast(skill.getSkill());
                  }
               }

               return true;
            }
         } catch (Exception var12) {
            _log.log(Level.WARNING, "Exception in " + this.getClass().getSimpleName(), (Throwable)var12);
         }

         return false;
      }
   }

   @Override
   public String[] getBypassList() {
      return COMMANDS;
   }
}
