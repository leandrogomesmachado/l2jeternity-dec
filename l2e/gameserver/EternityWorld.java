package l2e.gameserver;

import java.util.logging.Logger;

public class EternityWorld {
   public static final int _revision = 2210;

   public static void getTeamInfo(Logger log) {
      int revision = 2210;
      String ver = Config.isActivate ? "(Activated)" : "(Trial Version)";
      log.info("    Project Owner: ........... LordWinter");
      log.info("           Server: ........... High Five 5");
      log.info("         Revision: ........... 2210");
      log.info("          License: ........... " + Config.USER_NAME + " " + ver + "");
      log.info("        Copyright: ........... 2010-2021 Eternity Project Team");
      log.info("Project Community: ........... www.l2jeternity.com");
   }
}
