package l2e.scripts.teleports;

import l2e.commons.util.Util;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.service.BotFunctions;
import l2e.gameserver.model.skills.Skill;
import l2e.scripts.ai.AbstractNpcAI;

public class CleftTeleport extends AbstractNpcAI {
   public CleftTeleport(String var1, String var2) {
      super(var1, var2);
      this.addStartNpc(36570);
      this.addTalkId(36570);
   }

   @Override
   public String onTalk(Npc var1, Player var2) {
      String var3 = "";
      switch(var1.getId()) {
         case 36570:
            if (canTeleport(var2)) {
               if (var2.isInParty() && var2.getParty().isLeader(var2) && var2.getVarB("autoTeleport@", false)) {
                  for(Player var5 : var2.getParty().getMembers()) {
                     if (var5 != null
                        && var5.getObjectId() != var2.getObjectId()
                        && Util.checkIfInRange(1000, var2, var5, true)
                        && canTeleport(var5)
                        && BotFunctions.checkCondition(var5, false)
                        && var5.getIPAddress().equalsIgnoreCase(var2.getIPAddress())) {
                        var5.teleToLocation(-204288, 242026, 1744, true);
                     }
                  }
               }

               var2.teleToLocation(-204288, 242026, 1744, true);
            } else {
               var3 = "36570-1.htm";
            }
         default:
            return var3;
      }
   }

   private static boolean canTeleport(Player var0) {
      boolean var1 = false;

      for(Skill var3 : var0.getAllSkills()) {
         if (var3 != null && (var3.getId() == 840 || var3.getId() == 841 || var3.getId() == 842)) {
            var1 = true;
         }
      }

      return var1;
   }

   public static void main(String[] var0) {
      new CleftTeleport(CleftTeleport.class.getSimpleName(), "teleports");
   }
}
