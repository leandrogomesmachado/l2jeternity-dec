package l2e.commons.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import l2e.commons.util.lib.Log;
import l2e.gameserver.Config;
import l2e.gameserver.data.parser.AdminParser;
import l2e.gameserver.instancemanager.PunishmentManager;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.punishment.PunishmentAffect;
import l2e.gameserver.model.punishment.PunishmentTemplate;
import l2e.gameserver.model.punishment.PunishmentType;
import l2e.gameserver.network.communication.AuthServerCommunication;
import l2e.gameserver.network.communication.gameserverpackets.ChangeAccessLevel;

public final class IllegalPlayerAction {
   private static final Logger _log = Logger.getLogger(Log.class.getName());

   public static void IllegalAction(Player actor, List<String> messages, int punishment) {
      if (messages != null && !messages.isEmpty()) {
         String actions = "";
         String lastActions = messages.get(messages.size() - 1);

         for(String action : messages) {
            actions = actions + action + Config.EOL;
         }

         File file = new File("log/IllegalActions/" + actor.getName() + ".txt");

         try (FileWriter save = new FileWriter(file, true)) {
            save.write(actions);
         } catch (IOException var19) {
            _log.log(Level.SEVERE, "IllegalAction for char " + actor.getName() + " could not be saved: ", (Throwable)var19);
         }

         AdminParser.getInstance().broadcastMessageToGMs(lastActions);
         actor.getBannedActions().clear();
         switch(punishment) {
            case 1:
               actor.sendMessage("You are using illegal actions!");
               break;
            case 2:
               actor.sendMessage("You will be kicked for illegal actions! GM informed.");
               actor.logout(false);
               break;
            case 3:
               actor.setAccessLevel(-1);
               long banExpire = System.currentTimeMillis() + Config.SECOND_AUTH_BAN_TIME * 60L * 1000L;
               int expire = (int)(System.currentTimeMillis() / 1000L + Config.SECOND_AUTH_BAN_TIME * 60L);
               int accessLvl = Config.SECOND_AUTH_BAN_TIME > 0L ? 0 : -1;
               AuthServerCommunication.getInstance().sendPacket(new ChangeAccessLevel(actor.getAccountName(), accessLvl, expire));
               actor.sendMessage("You are banned for illegal actions! GM informed.");
               PunishmentManager.getInstance()
                  .addPunishment(
                     actor,
                     new PunishmentTemplate(
                        String.valueOf(actor.getObjectId()),
                        PunishmentAffect.CHARACTER,
                        PunishmentType.BAN,
                        banExpire,
                        "IllegalPlayerAction!",
                        actor.getName()
                     ),
                     true
                  );
               break;
            case 4:
               actor.sendMessage("Illegal actions performed!");
               actor.sendMessage("You will be teleported to GM Consultation Service area and jailed.");
               PunishmentManager.getInstance()
                  .addPunishment(
                     actor,
                     new PunishmentTemplate(
                        String.valueOf(actor.getObjectId()),
                        PunishmentAffect.CHARACTER,
                        PunishmentType.JAIL,
                        System.currentTimeMillis() + (long)(Config.DEFAULT_PUNISH_PARAM * 1000),
                        "IllegalPlayerAction!",
                        actor.getName()
                     ),
                     true
                  );
         }
      }
   }

   static {
      new File("log/IllegalActions").mkdirs();
   }
}
