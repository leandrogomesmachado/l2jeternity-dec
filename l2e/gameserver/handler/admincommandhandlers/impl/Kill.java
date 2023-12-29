package l2e.gameserver.handler.admincommandhandlers.impl;

import java.util.StringTokenizer;
import java.util.logging.Logger;
import l2e.gameserver.Config;
import l2e.gameserver.ai.model.CtrlEvent;
import l2e.gameserver.handler.admincommandhandlers.IAdminCommandHandler;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.instance.ControllableMobInstance;
import l2e.gameserver.network.SystemMessageId;

public class Kill implements IAdminCommandHandler {
   private static Logger _log = Logger.getLogger(Kill.class.getName());
   private static final String[] ADMIN_COMMANDS = new String[]{"admin_kill", "admin_kill_monster"};

   @Override
   public boolean useAdminCommand(String command, Player activeChar) {
      if (command.startsWith("admin_kill")) {
         StringTokenizer st = new StringTokenizer(command, " ");
         st.nextToken();
         if (st.hasMoreTokens()) {
            String firstParam = st.nextToken();
            Player plyr = World.getInstance().getPlayer(firstParam);
            if (plyr == null) {
               try {
                  int radius = Integer.parseInt(firstParam);

                  for(Creature knownChar : World.getInstance().getAroundPlayers(activeChar, radius, 200)) {
                     if (!(knownChar instanceof ControllableMobInstance) && knownChar != activeChar) {
                        this.kill(activeChar, knownChar);
                     }
                  }

                  activeChar.sendMessage("Killed all characters within a " + radius + " unit radius.");
                  return true;
               } catch (NumberFormatException var10) {
                  activeChar.sendMessage("Usage: //kill <player_name | radius>");
                  return false;
               }
            }

            if (st.hasMoreTokens()) {
               try {
                  int radius = Integer.parseInt(st.nextToken());

                  for(Creature knownChar : World.getInstance().getAroundCharacters(activeChar, radius, 200)) {
                     if (!(knownChar instanceof ControllableMobInstance) && knownChar != activeChar) {
                        this.kill(activeChar, knownChar);
                     }
                  }

                  activeChar.sendMessage("Killed all characters within a " + radius + " unit radius.");
                  return true;
               } catch (NumberFormatException var9) {
                  activeChar.sendMessage("Invalid radius.");
                  return false;
               }
            }

            this.kill(activeChar, plyr);
         } else {
            GameObject obj = activeChar.getTarget();
            if (obj != null && !(obj instanceof ControllableMobInstance) && obj.isCreature()) {
               this.kill(activeChar, (Creature)obj);
            } else {
               activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
            }
         }
      }

      return true;
   }

   private void kill(Player activeChar, Creature target) {
      boolean targetIsInvul = false;
      if (target.isInvul()) {
         targetIsInvul = true;
         target.setIsInvul(false);
      }

      if (target.isMonster() && target.hasAI()) {
         target.getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, activeChar, Double.valueOf(target.getCurrentHp()));
      }

      target.doDie(activeChar);
      if (targetIsInvul) {
         target.setIsInvul(true);
      }

      if (Config.DEBUG) {
         _log.fine("GM: " + activeChar.getName() + "(" + activeChar.getObjectId() + ") killed character " + target.getObjectId());
      }
   }

   @Override
   public String[] getAdminCommandList() {
      return ADMIN_COMMANDS;
   }
}
