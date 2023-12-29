package l2e.scripts.teleports;

import l2e.commons.util.Util;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.service.BotFunctions;

public class ToIVortex extends Quest {
   public ToIVortex(int var1, String var2, String var3) {
      super(var1, var2, var3);
      this.addStartNpc(new int[]{30949, 30950, 30951, 30952, 30953, 30954});
      this.addTalkId(new int[]{30949, 30950, 30951, 30952, 30953, 30954});
   }

   @Override
   public String onAdvEvent(String var1, Npc var2, Player var3) {
      int var4 = var2.getId();
      switch(var1) {
         case "1":
            if (!hasQuestItems(var3, 4401)) {
               return "no-stones.htm";
            }

            if (var3.isInParty() && var3.getParty().isLeader(var3) && var3.getVarB("autoTeleport@", false)) {
               for(Player var26 : var3.getParty().getMembers()) {
                  if (var26 != null
                     && var26.getObjectId() != var3.getObjectId()
                     && Util.checkIfInRange(1000, var3, var26, true)
                     && var26.getInventory().getItemByItemId(4401) != null
                     && BotFunctions.checkCondition(var26, false)
                     && var26.getIPAddress().equalsIgnoreCase(var3.getIPAddress())) {
                     takeItems(var26, 4401, 1L);
                     var26.teleToLocation(114356, 13423, -5096, true);
                  }
               }
            }

            takeItems(var3, 4401, 1L);
            var3.teleToLocation(114356, 13423, -5096, true);
            break;
         case "2":
            if (!hasQuestItems(var3, 4401)) {
               return "no-stones.htm";
            }

            if (var3.isInParty() && var3.getParty().isLeader(var3) && var3.getVarB("autoTeleport@", false)) {
               for(Player var25 : var3.getParty().getMembers()) {
                  if (var25 != null
                     && var25.getObjectId() != var3.getObjectId()
                     && Util.checkIfInRange(1000, var3, var25, true)
                     && var25.getInventory().getItemByItemId(4401) != null
                     && BotFunctions.checkCondition(var25, false)
                     && var25.getIPAddress().equalsIgnoreCase(var3.getIPAddress())) {
                     takeItems(var25, 4401, 1L);
                     var25.teleToLocation(114666, 13380, -3608, true);
                  }
               }
            }

            takeItems(var3, 4401, 1L);
            var3.teleToLocation(114666, 13380, -3608, true);
            break;
         case "3":
            if (!hasQuestItems(var3, 4401)) {
               return "no-stones.htm";
            }

            if (var3.isInParty() && var3.getParty().isLeader(var3) && var3.getVarB("autoTeleport@", false)) {
               for(Player var24 : var3.getParty().getMembers()) {
                  if (var24 != null
                     && var24.getObjectId() != var3.getObjectId()
                     && Util.checkIfInRange(1000, var3, var24, true)
                     && var24.getInventory().getItemByItemId(4401) != null
                     && BotFunctions.checkCondition(var24, false)
                     && var24.getIPAddress().equalsIgnoreCase(var3.getIPAddress())) {
                     takeItems(var24, 4401, 1L);
                     var24.teleToLocation(111982, 16028, -2120, true);
                  }
               }
            }

            takeItems(var3, 4401, 1L);
            var3.teleToLocation(111982, 16028, -2120, true);
            break;
         case "4":
            if (!hasQuestItems(var3, 4402)) {
               return "no-stones.htm";
            }

            if (var3.isInParty() && var3.getParty().isLeader(var3) && var3.getVarB("autoTeleport@", false)) {
               for(Player var23 : var3.getParty().getMembers()) {
                  if (var23 != null
                     && var23.getObjectId() != var3.getObjectId()
                     && Util.checkIfInRange(1000, var3, var23, true)
                     && var23.getInventory().getItemByItemId(4402) != null
                     && BotFunctions.checkCondition(var23, false)
                     && var23.getIPAddress().equalsIgnoreCase(var3.getIPAddress())) {
                     takeItems(var23, 4402, 1L);
                     var23.teleToLocation(114636, 13413, -640, true);
                  }
               }
            }

            takeItems(var3, 4402, 1L);
            var3.teleToLocation(114636, 13413, -640, true);
            break;
         case "5":
            if (!hasQuestItems(var3, 4402)) {
               return "no-stones.htm";
            }

            if (var3.isInParty() && var3.getParty().isLeader(var3) && var3.getVarB("autoTeleport@", false)) {
               for(Player var22 : var3.getParty().getMembers()) {
                  if (var22 != null
                     && var22.getObjectId() != var3.getObjectId()
                     && Util.checkIfInRange(1000, var3, var22, true)
                     && var22.getInventory().getItemByItemId(4402) != null
                     && BotFunctions.checkCondition(var22, false)
                     && var22.getIPAddress().equalsIgnoreCase(var3.getIPAddress())) {
                     takeItems(var22, 4402, 1L);
                     var22.teleToLocation(114152, 19902, 928, true);
                  }
               }
            }

            takeItems(var3, 4402, 1L);
            var3.teleToLocation(114152, 19902, 928, true);
            break;
         case "6":
            if (!hasQuestItems(var3, 4402)) {
               return "no-stones.htm";
            }

            if (var3.isInParty() && var3.getParty().isLeader(var3) && var3.getVarB("autoTeleport@", false)) {
               for(Player var21 : var3.getParty().getMembers()) {
                  if (var21 != null
                     && var21.getObjectId() != var3.getObjectId()
                     && Util.checkIfInRange(1000, var3, var21, true)
                     && var21.getInventory().getItemByItemId(4402) != null
                     && BotFunctions.checkCondition(var21, false)
                     && var21.getIPAddress().equalsIgnoreCase(var3.getIPAddress())) {
                     takeItems(var21, 4402, 1L);
                     var21.teleToLocation(117131, 16044, 1944, true);
                  }
               }
            }

            takeItems(var3, 4402, 1L);
            var3.teleToLocation(117131, 16044, 1944, true);
            break;
         case "7":
            if (!hasQuestItems(var3, 4403)) {
               return "no-stones.htm";
            }

            if (var3.isInParty() && var3.getParty().isLeader(var3) && var3.getVarB("autoTeleport@", false)) {
               for(Player var20 : var3.getParty().getMembers()) {
                  if (var20 != null
                     && var20.getObjectId() != var3.getObjectId()
                     && Util.checkIfInRange(1000, var3, var20, true)
                     && var20.getInventory().getItemByItemId(4403) != null
                     && BotFunctions.checkCondition(var20, false)
                     && var20.getIPAddress().equalsIgnoreCase(var3.getIPAddress())) {
                     takeItems(var20, 4403, 1L);
                     var20.teleToLocation(113026, 17687, 2952, true);
                  }
               }
            }

            takeItems(var3, 4403, 1L);
            var3.teleToLocation(113026, 17687, 2952, true);
            break;
         case "8":
            if (!hasQuestItems(var3, 4403)) {
               return "no-stones.htm";
            }

            if (var3.isInParty() && var3.getParty().isLeader(var3) && var3.getVarB("autoTeleport@", false)) {
               for(Player var19 : var3.getParty().getMembers()) {
                  if (var19 != null
                     && var19.getObjectId() != var3.getObjectId()
                     && Util.checkIfInRange(1000, var3, var19, true)
                     && var19.getInventory().getItemByItemId(4403) != null
                     && BotFunctions.checkCondition(var19, false)
                     && var19.getIPAddress().equalsIgnoreCase(var3.getIPAddress())) {
                     takeItems(var19, 4403, 1L);
                     var19.teleToLocation(115571, 13723, 3960, true);
                  }
               }
            }

            takeItems(var3, 4403, 1L);
            var3.teleToLocation(115571, 13723, 3960, true);
            break;
         case "9":
            if (!hasQuestItems(var3, 4403)) {
               return "no-stones.htm";
            }

            if (var3.isInParty() && var3.getParty().isLeader(var3) && var3.getVarB("autoTeleport@", false)) {
               for(Player var18 : var3.getParty().getMembers()) {
                  if (var18 != null
                     && var18.getObjectId() != var3.getObjectId()
                     && Util.checkIfInRange(1000, var3, var18, true)
                     && var18.getInventory().getItemByItemId(4403) != null
                     && BotFunctions.checkCondition(var18, false)
                     && var18.getIPAddress().equalsIgnoreCase(var3.getIPAddress())) {
                     takeItems(var18, 4403, 1L);
                     var18.teleToLocation(114649, 14144, 4976, true);
                  }
               }
            }

            takeItems(var3, 4403, 1L);
            var3.teleToLocation(114649, 14144, 4976, true);
            break;
         case "10":
            if (!hasQuestItems(var3, 4403)) {
               return "no-stones.htm";
            }

            if (var3.isInParty() && var3.getParty().isLeader(var3) && var3.getVarB("autoTeleport@", false)) {
               for(Player var8 : var3.getParty().getMembers()) {
                  if (var8 != null
                     && var8.getObjectId() != var3.getObjectId()
                     && Util.checkIfInRange(1000, var3, var8, true)
                     && var8.getInventory().getItemByItemId(4403) != null
                     && BotFunctions.checkCondition(var8, false)
                     && var8.getIPAddress().equalsIgnoreCase(var3.getIPAddress())) {
                     takeItems(var8, 4403, 1L);
                     var8.teleToLocation(118507, 16605, 5984, true);
                  }
               }
            }

            takeItems(var3, 4403, 1L);
            var3.teleToLocation(118507, 16605, 5984, true);
            break;
         case "GREEN":
            if (var3.getAdena() < 10000L) {
               return var4 + "no-adena.htm";
            }

            takeItems(var3, 57, 10000L);
            giveItems(var3, 4401, 1L);
            break;
         case "BLUE":
            if (var3.getAdena() < 10000L) {
               return var4 + "no-adena.htm";
            }

            takeItems(var3, 57, 10000L);
            giveItems(var3, 4402, 1L);
            break;
         case "RED":
            if (var3.getAdena() < 10000L) {
               return var4 + "no-adena.htm";
            }

            takeItems(var3, 57, 10000L);
            giveItems(var3, 4403, 1L);
      }

      return super.onAdvEvent(var1, var2, var3);
   }

   public static void main(String[] var0) {
      new ToIVortex(-1, ToIVortex.class.getSimpleName(), "teleports");
   }
}
