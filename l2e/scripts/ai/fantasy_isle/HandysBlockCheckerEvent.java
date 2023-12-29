package l2e.scripts.ai.fantasy_isle;

import l2e.gameserver.Config;
import l2e.gameserver.instancemanager.HandysBlockCheckerManager;
import l2e.gameserver.model.ArenaParticipantsHolder;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.ExBlockUpSetList;

public class HandysBlockCheckerEvent extends Quest {
   private static final String qn = "HandysBlockCheckerEvent";
   private static final int A_MANAGER_1 = 32521;
   private static final int A_MANAGER_2 = 32522;
   private static final int A_MANAGER_3 = 32523;
   private static final int A_MANAGER_4 = 32524;

   @Override
   public String onFirstTalk(Npc npc, Player player) {
      if (npc != null && player != null) {
         int arena = npc.getId() - 32521;
         if (this.eventIsFull(arena)) {
            player.sendPacket(SystemMessageId.CANNOT_REGISTER_CAUSE_QUEUE_FULL);
            return null;
         } else if (HandysBlockCheckerManager.getInstance().arenaIsBeingUsed(arena)) {
            player.sendPacket(SystemMessageId.MATCH_BEING_PREPARED_TRY_LATER);
            return null;
         } else {
            if (HandysBlockCheckerManager.getInstance().addPlayerToArena(player, arena)) {
               ArenaParticipantsHolder holder = HandysBlockCheckerManager.getInstance().getHolder(arena);
               ExBlockUpSetList tl = new ExBlockUpSetList(holder.getRedPlayers(), holder.getBluePlayers(), arena);
               player.sendPacket(tl);
               int countBlue = holder.getBlueTeamSize();
               int countRed = holder.getRedTeamSize();
               int minMembers = Config.MIN_BLOCK_CHECKER_TEAM_MEMBERS;
               if (countBlue >= minMembers && countRed >= minMembers) {
                  holder.updateEvent();
                  holder.broadCastPacketToTeam(new ExBlockUpSetList(false));
                  holder.broadCastPacketToTeam(new ExBlockUpSetList(10));
               }
            }

            return null;
         }
      } else {
         return null;
      }
   }

   private boolean eventIsFull(int arena) {
      return HandysBlockCheckerManager.getInstance().getHolder(arena).getAllPlayers().size() == 12;
   }

   public HandysBlockCheckerEvent(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addFirstTalkId(new int[]{32521, 32522, 32523, 32524});
   }

   public static void main(String[] args) {
      if (!Config.ENABLE_BLOCK_CHECKER_EVENT) {
         if (Config.DEBUG) {
            _log.info("Handy's Block Checker Event is disabled");
         }
      } else {
         new HandysBlockCheckerEvent(-1, "HandysBlockCheckerEvent", "Handy's Block Checker Event");
         HandysBlockCheckerManager.getInstance().startUpParticipantsQueue();
         if (Config.DEBUG) {
            _log.info("Handy's Block Checker Event is enabled");
         }
      }
   }
}
