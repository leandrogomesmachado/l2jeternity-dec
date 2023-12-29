package l2e.scripts.teleports;

import l2e.commons.util.Util;
import l2e.gameserver.SevenSigns;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.service.BotFunctions;

public class GatekeeperSpirit extends Quest {
   public GatekeeperSpirit(int var1, String var2, String var3) {
      super(var1, var2, var3);
      this.addStartNpc(new int[]{31111, 31112});
      this.addFirstTalkId(new int[]{31111, 31112});
      this.addTalkId(new int[]{31111, 31112});
      this.addKillId(new int[]{25283, 25286});
   }

   @Override
   public String onAdvEvent(String var1, Npc var2, Player var3) {
      String var4 = var1;
      if (var1.equalsIgnoreCase("enter")) {
         int var5 = SevenSigns.getInstance().getPlayerCabal(var3.getObjectId());
         int var6 = SevenSigns.getInstance().getSealOwner(1);
         int var7 = SevenSigns.getInstance().getCabalHighestScore();
         boolean var8 = SevenSigns.getInstance().isSealValidationPeriod();
         if (var8 && var5 == var6 && var5 == var7) {
            if (var3.isInParty() && var3.getParty().isLeader(var3) && var3.getVarB("autoTeleport@", false)) {
               for(Player var10 : var3.getParty().getMembers()) {
                  if (var10 != null
                     && var10.getObjectId() != var3.getObjectId()
                     && Util.checkIfInRange(1000, var3, var10, true)
                     && BotFunctions.checkCondition(var10, false)
                     && var10.getIPAddress().equalsIgnoreCase(var3.getIPAddress())
                     && SevenSigns.getInstance().getPlayerCabal(var10.getObjectId()) == var6
                     && SevenSigns.getInstance().getPlayerCabal(var10.getObjectId()) == var7) {
                     switch(var6) {
                        case 2:
                           var10.teleToLocation(184448, -10112, -5504, false);
                        case 1:
                           var10.teleToLocation(184464, -13104, -5504, false);
                     }
                  }
               }
            }

            switch(var6) {
               case 1:
                  var3.teleToLocation(184464, -13104, -5504, false);
                  break;
               case 2:
                  var3.teleToLocation(184448, -10112, -5504, false);
            }

            return null;
         }

         var4 = "spirit_gate_q0506_01.htm";
      } else if (var1.equalsIgnoreCase("exit")) {
         if (var3.isInParty() && var3.getParty().isLeader(var3) && var3.getVarB("autoTeleport@", false)) {
            for(Player var12 : var3.getParty().getMembers()) {
               if (var12 != null
                  && var12.getObjectId() != var3.getObjectId()
                  && Util.checkIfInRange(1000, var3, var12, true)
                  && BotFunctions.checkCondition(var12, false)
                  && var12.getIPAddress().equalsIgnoreCase(var3.getIPAddress())) {
                  var12.teleToLocation(182960, -11904, -4897, true);
               }
            }
         }

         var3.teleToLocation(182960, -11904, -4897, true);
         return null;
      }

      return var4;
   }

   @Override
   public String onFirstTalk(Npc var1, Player var2) {
      String var3 = "";
      switch(var1.getId()) {
         case 31111:
            var3 = "spirit_gate001.htm";
            break;
         case 31112:
            var3 = "spirit_gate002.htm";
      }

      return var3;
   }

   @Override
   public String onKill(Npc var1, Player var2, boolean var3) {
      int var4 = var1.getId();
      if (var4 == 25283) {
         addSpawn(31112, 184410, -10111, -5488, 0, false, 900000L);
      } else if (var4 == 25286) {
         addSpawn(31112, 184410, -13102, -5488, 0, false, 900000L);
      }

      return super.onKill(var1, var2, var3);
   }

   public static void main(String[] var0) {
      new GatekeeperSpirit(-1, GatekeeperSpirit.class.getSimpleName(), "teleports");
   }
}
