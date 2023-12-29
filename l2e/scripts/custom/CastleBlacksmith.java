package l2e.scripts.custom;

import l2e.gameserver.model.PcCondOverride;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.scripts.ai.AbstractNpcAI;

public class CastleBlacksmith extends AbstractNpcAI {
   private static final int[] NPCS = new int[]{35098, 35140, 35182, 35224, 35272, 35314, 35361, 35507, 35553};

   private CastleBlacksmith(String name, String descr) {
      super(name, descr);
      this.addStartNpc(NPCS);
      this.addTalkId(NPCS);
      this.addFirstTalkId(NPCS);
   }

   private boolean hasRights(Player player, Npc npc) {
      return player.canOverrideCond(PcCondOverride.CASTLE_CONDITIONS)
         || npc.isMyLord(player)
         || player.getId() == npc.getCastle().getOwnerId() && (player.getClanPrivileges() & 131072) == 131072;
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      return event.equalsIgnoreCase(npc.getId() + "-02.htm") && this.hasRights(player, npc) ? event : null;
   }

   @Override
   public String onFirstTalk(Npc npc, Player player) {
      return this.hasRights(player, npc) ? npc.getId() + "-01.htm" : "no.htm";
   }

   public static void main(String[] args) {
      new CastleBlacksmith(CastleBlacksmith.class.getSimpleName(), "custom");
   }
}
