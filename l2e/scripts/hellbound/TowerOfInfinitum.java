package l2e.scripts.hellbound;

import java.util.HashMap;
import java.util.Map;
import l2e.commons.util.Util;
import l2e.gameserver.instancemanager.HellboundManager;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.Party;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;

public class TowerOfInfinitum extends Quest {
   private static final int JERIAN = 32302;
   private static final int GK_FIRST = 32745;
   private static final int GK_LAST = 32752;
   private static final int PASS_SKILL = 2357;
   private static final Map<Integer, Location[]> TELE_COORDS = new HashMap<>();

   public TowerOfInfinitum(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(32302);
      this.addTalkId(32302);

      for(int i = 32745; i <= 32752; ++i) {
         this.addStartNpc(i);
         this.addTalkId(i);
      }

      TELE_COORDS.put(32745, new Location[]{new Location(-22208, 277122, -13376), null});
      TELE_COORDS.put(32746, new Location[]{new Location(-22208, 277106, -11648), new Location(-22208, 277074, -15040)});
      TELE_COORDS.put(32747, new Location[]{new Location(-22208, 277120, -9920), new Location(-22208, 277120, -13376)});
      TELE_COORDS.put(32748, new Location[]{new Location(-19024, 277126, -8256), new Location(-22208, 277106, -11648)});
      TELE_COORDS.put(32749, new Location[]{new Location(-19024, 277106, -9920), new Location(-22208, 277122, -9920)});
      TELE_COORDS.put(32750, new Location[]{new Location(-19008, 277100, -11648), new Location(-19024, 277122, -8256)});
      TELE_COORDS.put(32751, new Location[]{new Location(-19008, 277100, -13376), new Location(-19008, 277106, -9920)});
      TELE_COORDS.put(32752, new Location[]{new Location(14602, 283179, -7500), new Location(-19008, 277100, -11648)});
   }

   @Override
   public final String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      int npcId = npc.getId();
      if (event.equalsIgnoreCase("enter") && npcId == 32302) {
         if (HellboundManager.getInstance().getLevel() < 11) {
            htmltext = "32302-02b.htm";
         } else {
            Party party = player.getParty();
            if (party != null && party.getLeaderObjectId() == player.getObjectId()) {
               for(Player partyMember : party.getMembers()) {
                  if (!Util.checkIfInRange(300, partyMember, npc, true) || partyMember.getFirstEffect(2357) == null) {
                     return "32302-02.htm";
                  }
               }

               for(Player partyMember : party.getMembers()) {
                  partyMember.teleToLocation(-22204, 277056, -15023, true);
               }

               htmltext = null;
            } else {
               htmltext = "32302-02a.htm";
            }
         }
      } else if ((event.equalsIgnoreCase("up") || event.equalsIgnoreCase("down")) && npcId >= 32745 && npcId <= 32752) {
         int direction = event.equalsIgnoreCase("up") ? 0 : 1;
         Party party = player.getParty();
         if (party == null) {
            htmltext = "gk-noparty.htm";
         } else if (party.getLeaderObjectId() != player.getObjectId()) {
            htmltext = "gk-noreq.htm";
         } else {
            for(Player partyMember : party.getMembers()) {
               if (!Util.checkIfInRange(1000, partyMember, npc, false) || Math.abs(partyMember.getZ() - npc.getZ()) > 100) {
                  return "gk-noreq.htm";
               }
            }

            Location tele = TELE_COORDS.get(npcId)[direction];
            if (tele != null) {
               for(Player partyMember : party.getMembers()) {
                  partyMember.teleToLocation(tele, true);
               }
            }

            htmltext = null;
         }
      }

      return htmltext;
   }

   public static void main(String[] args) {
      new TowerOfInfinitum(-1, TowerOfInfinitum.class.getSimpleName(), "hellbound");
   }
}
