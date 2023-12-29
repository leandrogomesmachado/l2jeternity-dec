package l2e.gameserver.handler.admincommandhandlers.impl;

import java.util.StringTokenizer;
import l2e.gameserver.Config;
import l2e.gameserver.handler.admincommandhandlers.IAdminCommandHandler;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.stat.PcStat;

public class Vitality implements IAdminCommandHandler {
   private static final String[] ADMIN_COMMANDS = new String[]{
      "admin_set_vitality", "admin_set_vitality_level", "admin_full_vitality", "admin_empty_vitality", "admin_get_vitality"
   };

   @Override
   public boolean useAdminCommand(String command, Player activeChar) {
      if (activeChar == null) {
         return false;
      } else if (!Config.ENABLE_VITALITY) {
         activeChar.sendMessage("Vitality is not enabled on the server!");
         return false;
      } else {
         int level = 0;
         int vitality = 0;
         StringTokenizer st = new StringTokenizer(command, " ");
         String cmd = st.nextToken();
         if (!(activeChar.getTarget() instanceof Player)) {
            activeChar.sendMessage("Target not found or not a player");
            return false;
         } else {
            Player target = (Player)activeChar.getTarget();
            if (cmd.equals("admin_set_vitality")) {
               try {
                  vitality = Integer.parseInt(st.nextToken());
               } catch (Exception var10) {
                  activeChar.sendMessage("Incorrect vitality");
               }

               target.setVitalityPoints(vitality, true);
               target.sendMessage("Admin set your Vitality points to " + vitality);
            } else if (cmd.equals("admin_set_vitality_level")) {
               try {
                  level = Integer.parseInt(st.nextToken());
               } catch (Exception var9) {
                  activeChar.sendMessage("Incorrect vitality level (0-4)");
               }

               if (level >= 0 && level <= 4) {
                  if (level == 0) {
                     vitality = 1;
                  } else {
                     vitality = PcStat.VITALITY_LEVELS[level - 1];
                  }

                  target.setVitalityPoints(vitality, true);
                  target.sendMessage("Admin set your Vitality level to " + level);
               } else {
                  activeChar.sendMessage("Incorrect vitality level (0-4)");
               }
            } else if (cmd.equals("admin_full_vitality")) {
               target.setVitalityPoints(PcStat.MAX_VITALITY_POINTS, true);
               target.sendMessage("Admin completly recharged your Vitality");
            } else if (cmd.equals("admin_empty_vitality")) {
               target.setVitalityPoints(1, true);
               target.sendMessage("Admin completly emptied your Vitality");
            } else if (cmd.equals("admin_get_vitality")) {
               level = target.getVitalityLevel();
               vitality = target.getVitalityPoints();
               activeChar.sendMessage("Player vitality level: " + level);
               activeChar.sendMessage("Player vitality points: " + vitality);
            }

            return true;
         }
      }
   }

   @Override
   public String[] getAdminCommandList() {
      return ADMIN_COMMANDS;
   }

   public static void main(String[] args) {
      new Vitality();
   }
}
