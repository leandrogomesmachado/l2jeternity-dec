package l2e.gameserver.handler.admincommandhandlers.impl;

import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import l2e.gameserver.Config;
import l2e.gameserver.data.parser.SkillsParser;
import l2e.gameserver.handler.admincommandhandlers.IAdminCommandHandler;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.network.serverpackets.MagicSkillUse;

public class Test implements IAdminCommandHandler {
   private static final String[] ADMIN_COMMANDS = new String[]{"admin_skill_test", "admin_known"};

   @Override
   public boolean useAdminCommand(String command, Player activeChar) {
      if (command.startsWith("admin_skill_test")) {
         try {
            StringTokenizer st = new StringTokenizer(command);
            st.nextToken();
            int id = Integer.parseInt(st.nextToken());
            if (command.startsWith("admin_skill_test")) {
               this.adminTestSkill(activeChar, id, true);
            } else {
               this.adminTestSkill(activeChar, id, false);
            }
         } catch (NumberFormatException var5) {
            activeChar.sendMessage("Command format is //skill_test <ID>");
         } catch (NoSuchElementException var6) {
            activeChar.sendMessage("Command format is //skill_test <ID>");
         }
      } else if (command.equals("admin_known on")) {
         Config.CHECK_KNOWN = true;
      } else if (command.equals("admin_known off")) {
         Config.CHECK_KNOWN = false;
      }

      return true;
   }

   private void adminTestSkill(Player activeChar, int id, boolean msu) {
      GameObject target = activeChar.getTarget();
      Creature caster;
      if (!(target instanceof Creature)) {
         caster = activeChar;
      } else {
         caster = (Creature)target;
      }

      Skill _skill = SkillsParser.getInstance().getInfo(id, 1);
      if (_skill != null) {
         caster.setTarget(activeChar);
         if (msu) {
            caster.broadcastPacket(new MagicSkillUse(caster, activeChar, id, 1, _skill.getHitTime(), _skill.getReuseDelay()));
         } else {
            caster.doCast(_skill);
         }
      }
   }

   @Override
   public String[] getAdminCommandList() {
      return ADMIN_COMMANDS;
   }
}
