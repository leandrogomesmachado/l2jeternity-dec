package l2e.scripts.teleports;

import l2e.gameserver.Config;
import l2e.gameserver.instancemanager.EpicBossManager;
import l2e.gameserver.instancemanager.ZoneManager;
import l2e.gameserver.model.CommandChannel;
import l2e.gameserver.model.Party;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.zone.type.BossZone;

public class SteelCitadelTeleport extends Quest {
   public SteelCitadelTeleport(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(32376);
      this.addTalkId(32376);
      this.addSpawnId(new int[]{32376});
   }

   @Override
   public final String onSpawn(Npc npc) {
      this.startQuestTimer("despawn", 600000L, npc, null);
      return null;
   }

   @Override
   public final String onAdvEvent(String event, Npc npc, Player player) {
      if (npc == null) {
         return null;
      } else {
         if (event.equalsIgnoreCase("despawn") && npc.getId() == 32376) {
            npc.deleteMe();
         }

         return null;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      switch(npc.getId()) {
         case 32376:
            if (EpicBossManager.getInstance().getBossStatus(29118) == 3) {
               return "32376-02.htm";
            } else {
               if (Config.BELETH_NO_CC) {
                  Party party = player.getParty() == null ? null : player.getParty();
                  if (party == null || party.getLeader().getObjectId() != player.getObjectId() || party.getMemberCount() < Config.BELETH_MIN_PLAYERS) {
                     return "32376-02a.htm";
                  }
               } else {
                  CommandChannel channel = player.getParty() == null ? null : player.getParty().getCommandChannel();
                  if (channel == null || channel.getLeader().getObjectId() != player.getObjectId() || channel.getMemberCount() < Config.BELETH_MIN_PLAYERS) {
                     return "32376-02a.htm";
                  }
               }

               if (EpicBossManager.getInstance().getBossStatus(29118) > 0) {
                  return "32376-03.htm";
               } else {
                  BossZone zone = (BossZone)ZoneManager.getInstance().getZoneById(12018);
                  if (zone != null) {
                     EpicBossManager.getInstance().setBossStatus(29118, 1, true);
                     if (Config.BELETH_NO_CC) {
                        if (player.getParty() != null) {
                           for(Player pl : player.getParty().getMembers()) {
                              if (pl.isInsideRadius(npc.getX(), npc.getY(), npc.getZ(), 3000, true, false)) {
                                 zone.allowPlayerEntry(pl, 30);
                                 pl.teleToLocation(16342, 209557, -9352, true);
                              }
                           }
                        }
                     } else {
                        for(Party party : player.getParty().getCommandChannel().getPartys()) {
                           if (party != null) {
                              for(Player pl : party.getMembers()) {
                                 if (pl.isInsideRadius(npc.getX(), npc.getY(), npc.getZ(), 3000, true, false)) {
                                    zone.allowPlayerEntry(pl, 30);
                                    pl.teleToLocation(16342, 209557, -9352, true);
                                 }
                              }
                           }
                        }
                     }
                  }
               }
            }
         default:
            return null;
      }
   }

   public static void main(String[] args) {
      new SteelCitadelTeleport(-1, SteelCitadelTeleport.class.getSimpleName(), "teleports");
   }
}
