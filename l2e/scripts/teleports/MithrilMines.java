package l2e.scripts.teleports;

import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.service.BotFunctions;

public class MithrilMines extends Quest {
   private static final Location[] _locs = new Location[]{
      new Location(171946, -173352, 3440),
      new Location(175499, -181586, -904),
      new Location(173462, -174011, 3480),
      new Location(179299, -182831, -224),
      new Location(178591, -184615, 360),
      new Location(175499, -181586, -904)
   };

   public MithrilMines(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(32652);
      this.addFirstTalkId(32652);
      this.addTalkId(32652);
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = "";
      QuestState st = player.getQuestState(this.getName());
      int index = Integer.parseInt(event) - 1;
      if (_locs.length > index) {
         Location loc = _locs[index];
         if (BotFunctions.getInstance().isAutoTpByIdEnable(player)) {
            BotFunctions.getInstance().getAutoTeleportById(player, player.getLocation(), loc, 1000);
            st.exitQuest(true);
            return null;
         }

         player.teleToLocation(loc, false);
         st.exitQuest(true);
      }

      return "";
   }

   @Override
   public String onFirstTalk(Npc npc, Player player) {
      String htmltext = "";
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         st = this.newQuestState(player);
      }

      if (npc.isInsideRadius(173147, -173762, 150, true)) {
         htmltext = "32652-01.htm";
      } else if (npc.isInsideRadius(181941, -174614, 150, true)) {
         htmltext = "32652-02.htm";
      } else if (npc.isInsideRadius(179560, -182956, 150, true)) {
         htmltext = "32652-03.htm";
      }

      return htmltext;
   }

   public static void main(String[] args) {
      new MithrilMines(-1, MithrilMines.class.getSimpleName(), "teleports");
   }
}
