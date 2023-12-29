package l2e.scripts.teleports;

import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.service.BotFunctions;

public class DelusionTeleport extends Quest {
   public DelusionTeleport(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(new int[]{32484, 32658, 32659, 32660, 32661, 32662, 32663});
      this.addTalkId(new int[]{32484, 32658, 32659, 32660, 32661, 32662, 32663});
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      QuestState st = player.getQuestState(this.getName());
      int npcId = npc.getId();
      if (npcId == 32484) {
         if (BotFunctions.getInstance().isAutoTpByIdEnable(player)) {
            player.setVar("delusion_coords", "" + player.getX() + " " + player.getY() + " " + player.getZ() + "");
            BotFunctions.getInstance().getAutoTeleportById(player, player.getLocation(), new Location(-114592, -152509, -6723), 1000);
            return null;
         }

         player.setVar("delusion_coords", "" + player.getX() + " " + player.getY() + " " + player.getZ() + "");
         player.teleToLocation(-114592, -152509, -6723, true);
         if (player.hasSummon()) {
            player.getSummon().teleToLocation(-114592, -152509, -6723, true);
         }
      } else if (npcId == 32658 || npcId == 32659 || npcId == 32660 || npcId == 32661 || npcId == 32663 || npcId == 32662) {
         String locInfo = player.getVar("delusion_coords");
         if (locInfo != null) {
            Location loc = Location.parseLoc(locInfo);
            if (loc != null) {
               if (BotFunctions.getInstance().isAutoTpByIdEnable(player)) {
                  BotFunctions.getInstance().getAutoTeleportById(player, player.getLocation(), new Location(loc.getX(), loc.getY(), loc.getZ()), 1000);
                  return null;
               }

               player.teleToLocation(loc.getX(), loc.getY(), loc.getZ(), true);
               if (player.hasSummon()) {
                  player.getSummon().teleToLocation(loc.getX(), loc.getY(), loc.getZ(), true);
               }
            }

            player.unsetVar("delusion_coords");
         }

         st.exitQuest(true);
      }

      return "";
   }

   public static void main(String[] args) {
      new DelusionTeleport(-1, DelusionTeleport.class.getSimpleName(), "teleports");
   }
}
