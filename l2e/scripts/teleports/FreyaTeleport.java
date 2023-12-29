package l2e.scripts.teleports;

import l2e.commons.util.Util;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.service.BotFunctions;
import l2e.gameserver.model.strings.server.ServerStorage;
import l2e.scripts.ai.AbstractNpcAI;

public class FreyaTeleport extends AbstractNpcAI {
   public FreyaTeleport(String var1, String var2) {
      super(var1, var2);
      this.addStartNpc(32734);
      this.addTalkId(32734);
   }

   @Override
   public String onTalk(Npc var1, Player var2) {
      String var3 = "";
      switch(var1.getId()) {
         case 32734:
            if (var2.getLevel() < 80) {
               return "<html><body>" + ServerStorage.getInstance().getString(var2.getLang(), "SeedOfAnnihilation.CANT_TELE") + "</body></html>";
            } else {
               if (var2.isInParty() && var2.getParty().isLeader(var2) && var2.getVarB("autoTeleport@", false)) {
                  for(Player var5 : var2.getParty().getMembers()) {
                     if (var5 != null
                        && var5.getObjectId() != var2.getObjectId()
                        && Util.checkIfInRange(1000, var2, var5, true)
                        && var5.getLevel() >= 80
                        && BotFunctions.checkCondition(var5, false)
                        && var5.getIPAddress().equalsIgnoreCase(var2.getIPAddress())) {
                        var5.teleToLocation(-180218, 185923, -10576, true);
                     }
                  }
               }

               var2.teleToLocation(-180218, 185923, -10576, true);
            }
         default:
            return "";
      }
   }

   public static void main(String[] var0) {
      new FreyaTeleport(FreyaTeleport.class.getSimpleName(), "teleports");
   }
}
