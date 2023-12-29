package l2e.gameserver.handler.admincommandhandlers.impl;

import java.util.StringTokenizer;
import l2e.gameserver.data.parser.ExperienceParser;
import l2e.gameserver.handler.admincommandhandlers.IAdminCommandHandler;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.actor.Playable;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.network.SystemMessageId;

public class Level implements IAdminCommandHandler {
   private static final String[] ADMIN_COMMANDS = new String[]{"admin_add_level", "admin_set_level"};

   @Override
   public boolean useAdminCommand(String command, Player activeChar) {
      GameObject targetChar = activeChar.getTarget();
      StringTokenizer st = new StringTokenizer(command, " ");
      String actualCommand = st.nextToken();
      String val = "";
      if (st.countTokens() >= 1) {
         val = st.nextToken();
      }

      if (actualCommand.equalsIgnoreCase("admin_add_level")) {
         try {
            if (targetChar instanceof Playable) {
               ((Playable)targetChar).getStat().addLevel(Byte.parseByte(val), true);
            }
         } catch (NumberFormatException var13) {
            activeChar.sendMessage("Wrong Number Format");
         }
      } else if (actualCommand.equalsIgnoreCase("admin_set_level")) {
         try {
            if (!(targetChar instanceof Player)) {
               activeChar.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
               return false;
            }

            Player targetPlayer = (Player)targetChar;
            byte lvl = Byte.parseByte(val);
            if (lvl < 1 || lvl > ExperienceParser.getInstance().getMaxLevel()) {
               activeChar.sendMessage("You must specify level between 1 and " + ExperienceParser.getInstance().getMaxLevel() + ".");
               return false;
            }

            long pXp = targetPlayer.getExp();
            long tXp = ExperienceParser.getInstance().getExpForLevel(lvl);
            if (pXp > tXp) {
               targetPlayer.removeExpAndSp(pXp - tXp, 0);
            } else if (pXp < tXp) {
               targetPlayer.addExpAndSp(tXp - pXp, 0);
            }
         } catch (NumberFormatException var14) {
            activeChar.sendMessage("You must specify level between 1 and " + ExperienceParser.getInstance().getMaxLevel() + ".");
            return false;
         }
      }

      return true;
   }

   @Override
   public String[] getAdminCommandList() {
      return ADMIN_COMMANDS;
   }
}
