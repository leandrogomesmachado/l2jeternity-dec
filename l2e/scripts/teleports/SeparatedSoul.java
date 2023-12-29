package l2e.scripts.teleports;

import java.util.HashMap;
import java.util.Map;
import l2e.commons.util.Util;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.service.BotFunctions;

public class SeparatedSoul extends Quest {
   private static final Map<String, Location> LOCATIONS = new HashMap<>();

   public SeparatedSoul(int var1, String var2, String var3) {
      super(var1, var2, var3);
      this.addStartNpc(new int[]{32864, 32865, 32866, 32867, 32868, 32869, 32870, 32891});
      this.addTalkId(new int[]{32864, 32865, 32866, 32867, 32868, 32869, 32870, 32891});
   }

   @Override
   public String onAdvEvent(String var1, Npc var2, Player var3) {
      if (LOCATIONS.containsKey(var1)) {
         if (var3.getLevel() < 80) {
            return "no-level.htm";
         }

         if (var3.isInParty() && var3.getParty().isLeader(var3) && var3.getVarB("autoTeleport@", false)) {
            for(Player var5 : var3.getParty().getMembers()) {
               if (var5 != null
                  && var5.getObjectId() != var3.getObjectId()
                  && Util.checkIfInRange(1000, var3, var5, true)
                  && var5.getLevel() >= 80
                  && BotFunctions.checkCondition(var5, false)
                  && var5.getIPAddress().equalsIgnoreCase(var3.getIPAddress())) {
                  var5.teleToLocation(LOCATIONS.get(var1), true);
               }
            }
         }

         var3.teleToLocation(LOCATIONS.get(var1), true);
      } else if ("Synthesis".equals(var1)) {
         if (!hasQuestItems(var3, 17266) || !hasQuestItems(var3, 17267)) {
            return "no-items.htm";
         }

         takeItems(var3, 17266, 1L);
         takeItems(var3, 17267, 1L);
         giveItems(var3, 17268, 1L);
      }

      return super.onAdvEvent(var1, var2, var3);
   }

   public static void main(String[] var0) {
      new SeparatedSoul(-1, SeparatedSoul.class.getSimpleName(), "teleports");
   }

   static {
      LOCATIONS.put("HuntersVillage", new Location(117031, 76769, -2696));
      LOCATIONS.put("AntharasLair", new Location(131116, 114333, -3704));
      LOCATIONS.put("AntharasLairDeep", new Location(148447, 110582, -3944));
      LOCATIONS.put("AntharasLairMagicForceFieldBridge", new Location(146129, 111232, -3568));
      LOCATIONS.put("DragonValley", new Location(73122, 118351, -3714));
      LOCATIONS.put("DragonValleyCenter", new Location(99218, 110283, -3696));
      LOCATIONS.put("DragonValleyNorth", new Location(116992, 113716, -3056));
      LOCATIONS.put("DragonValleySouth", new Location(113203, 121063, -3712));
   }
}
