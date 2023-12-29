package l2e.scripts.teleports;

import l2e.gameserver.model.Location;
import l2e.gameserver.model.Party;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class StakatoNest extends Quest {
   private static final Location[] _locations = new Location[]{
      new Location(80456, -52322, -5640),
      new Location(88718, -46214, -4640),
      new Location(87464, -54221, -5120),
      new Location(80848, -49426, -5128),
      new Location(87682, -43291, -4128)
   };

   public StakatoNest(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(32640);
      this.addTalkId(32640);
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         st = this.newQuestState(player);
      }

      int index = Integer.parseInt(event) - 1;
      if (_locations.length > index) {
         Location loc = _locations[index];
         Party party = player.getParty();
         if (party != null) {
            Location playerLoc = player.getLocation();

            for(Player member : party.getMembers()) {
               if (member != null && member.isInsideRadius(playerLoc.getX(), playerLoc.getY(), playerLoc.getZ(), 1000, true, true)) {
                  member.teleToLocation(loc, true);
               }
            }
         } else {
            player.teleToLocation(loc, true);
         }

         st.exitQuest(true);
      }

      return "";
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = "";
      QuestState accessQuest = player.getQuestState("_240_ImTheOnlyOneYouCanTrust");
      if (accessQuest != null && accessQuest.isCompleted()) {
         htmltext = "32640.htm";
      } else {
         htmltext = "32640-no.htm";
      }

      return htmltext;
   }

   public static void main(String[] args) {
      new StakatoNest(-1, StakatoNest.class.getSimpleName(), "teleports");
   }
}
