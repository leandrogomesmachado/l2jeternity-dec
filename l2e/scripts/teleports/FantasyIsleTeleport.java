package l2e.scripts.teleports;

import java.util.HashMap;
import java.util.Map;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;

public class FantasyIsleTeleport extends Quest {
   private static final Map<String, Location> _locations = new HashMap<>();

   public FantasyIsleTeleport(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(new int[]{4317, 4318, 4319, 4320, 4321, 4322, 4323});
      this.addTalkId(new int[]{4317, 4318, 4319, 4320, 4321, 4322, 4323});
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      if (_locations.containsKey(event)) {
         player.teleToLocation(_locations.get(event), true);
      }

      return super.onAdvEvent(event, npc, player);
   }

   public static void main(String[] args) {
      new FantasyIsleTeleport(-1, FantasyIsleTeleport.class.getSimpleName(), "teleports");
   }

   static {
      _locations.put("1", new Location(-57525, -54523, -1576));
      _locations.put("2", new Location(-58151, -53110, -1688));
      _locations.put("3", new Location(-55748, -56327, -1336));
      _locations.put("4", new Location(-55646, -56314, -1296));
      _locations.put("5", new Location(-55545, -56310, -1256));
      _locations.put("6", new Location(-55355, -56305, -1112));
      _locations.put("7", new Location(-55223, -58832, -1680));
      _locations.put("8", new Location(-59075, -59464, -1464));
      _locations.put("9", new Location(-61926, -59504, -1728));
      _locations.put("10", new Location(-61288, -57736, -1600));
   }
}
