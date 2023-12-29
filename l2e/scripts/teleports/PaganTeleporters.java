package l2e.scripts.teleports;

import l2e.gameserver.data.parser.DoorParser;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.service.BotFunctions;

public class PaganTeleporters extends Quest {
   public PaganTeleporters(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(new int[]{32034, 32035, 32036, 32037, 32039, 32040});
      this.addTalkId(new int[]{32034, 32035, 32036, 32037, 32039, 32040});
      this.addFirstTalkId(new int[]{32034, 32039, 32040});
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      if (event.equalsIgnoreCase("Close_Door1")) {
         DoorParser.getInstance().getDoor(19160001).closeMe();
      } else if (event.equalsIgnoreCase("Close_Door2")) {
         DoorParser.getInstance().getDoor(19160010).closeMe();
         DoorParser.getInstance().getDoor(19160011).closeMe();
      }

      return "";
   }

   @Override
   public String onFirstTalk(Npc npc, Player player) {
      switch(npc.getId()) {
         case 32034:
            if (player.destroyItemByItemId("Mark", 8064, 1L, player, false)) {
               player.addItem("Mark", 8065, 1L, player, true);
            }

            npc.showChatWindow(player);
            return null;
         case 32039:
            if (BotFunctions.getInstance().isAutoTpByIdEnable(player)) {
               BotFunctions.getInstance().getAutoTeleportById(player, player.getLocation(), new Location(36640, -51218, 718), 1000);
               return null;
            }

            player.teleToLocation(36640, -51218, 718, true);
            break;
         case 32040:
            if (BotFunctions.getInstance().isAutoTpByIdEnable(player)) {
               BotFunctions.getInstance().getAutoTeleportById(player, player.getLocation(), new Location(-12766, -35840, -10856), 1000);
               return null;
            }

            player.teleToLocation(-12766, -35840, -10856, true);
      }

      return "";
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = "";
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return null;
      } else {
         switch(npc.getId()) {
            case 32034:
               if (!st.hasQuestItems(8064) && !st.hasQuestItems(8065) && !st.hasQuestItems(8067)) {
                  htmltext = "noItem.htm";
               } else {
                  htmltext = "FadedMark.htm";
                  DoorParser.getInstance().getDoor(19160001).openMe();
                  this.startQuestTimer("Close_Door1", 10000L, null, null);
               }
               break;
            case 32035:
               DoorParser.getInstance().getDoor(19160001).openMe();
               this.startQuestTimer("Close_Door1", 10000L, null, null);
               htmltext = "FadedMark.htm";
               break;
            case 32036:
               if (!st.hasQuestItems(8067)) {
                  htmltext = "noMark.htm";
               } else {
                  htmltext = "openDoor.htm";
                  this.startQuestTimer("Close_Door2", 10000L, null, null);
                  DoorParser.getInstance().getDoor(19160010).openMe();
                  DoorParser.getInstance().getDoor(19160011).openMe();
               }
               break;
            case 32037:
               DoorParser.getInstance().getDoor(19160010).openMe();
               DoorParser.getInstance().getDoor(19160011).openMe();
               this.startQuestTimer("Close_Door2", 10000L, null, null);
               htmltext = "FadedMark.htm";
         }

         st.exitQuest(true);
         return htmltext;
      }
   }

   public static void main(String[] args) {
      new PaganTeleporters(-1, PaganTeleporters.class.getSimpleName(), "teleports");
   }
}
