package l2e.gameserver.instancemanager;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.entity.Duel;
import l2e.gameserver.network.serverpackets.GameServerPacket;

public class DuelManager {
   private final Map<Integer, Duel> _duels = new ConcurrentHashMap<>();
   private final AtomicInteger _currentDuelId = new AtomicInteger();

   protected DuelManager() {
   }

   public Duel getDuel(int duelId) {
      return this._duels.get(duelId);
   }

   public void addDuel(Player playerA, Player playerB, int partyDuel) {
      if (playerA != null && playerB != null) {
         String engagedInPvP = "The duel was canceled because a duelist engaged in PvP combat.";
         if (partyDuel == 1) {
            boolean playerInPvP = false;

            for(Player temp : playerA.getParty().getMembers()) {
               if (temp.getPvpFlag() != 0) {
                  playerInPvP = true;
                  break;
               }
            }

            if (!playerInPvP) {
               for(Player temp : playerB.getParty().getMembers()) {
                  if (temp.getPvpFlag() != 0) {
                     playerInPvP = true;
                     break;
                  }
               }
            }

            if (playerInPvP) {
               for(Player temp : playerA.getParty().getMembers()) {
                  temp.sendMessage("The duel was canceled because a duelist engaged in PvP combat.");
               }

               for(Player temp : playerB.getParty().getMembers()) {
                  temp.sendMessage("The duel was canceled because a duelist engaged in PvP combat.");
               }

               return;
            }
         } else if (playerA.getPvpFlag() != 0 || playerB.getPvpFlag() != 0) {
            playerA.sendMessage("The duel was canceled because a duelist engaged in PvP combat.");
            playerB.sendMessage("The duel was canceled because a duelist engaged in PvP combat.");
            return;
         }

         int duelId = this._currentDuelId.incrementAndGet();
         this._duels.put(duelId, new Duel(playerA, playerB, partyDuel, duelId));
      }
   }

   public void removeDuel(Duel duel) {
      this._duels.remove(duel.getId());
   }

   public void doSurrender(Player player) {
      if (player != null && player.isInDuel()) {
         Duel duel = this.getDuel(player.getDuelId());
         duel.doSurrender(player);
      }
   }

   public void onPlayerDefeat(Player player) {
      if (player != null && player.isInDuel()) {
         Duel duel = this.getDuel(player.getDuelId());
         if (duel != null) {
            duel.onPlayerDefeat(player);
         }
      }
   }

   public void onRemoveFromParty(Player player) {
      if (player != null && player.isInDuel()) {
         Duel duel = this.getDuel(player.getDuelId());
         if (duel != null) {
            duel.onRemoveFromParty(player);
         }
      }
   }

   public void broadcastToOppositTeam(Player player, GameServerPacket packet) {
      if (player != null && player.isInDuel()) {
         Duel duel = this.getDuel(player.getDuelId());
         if (duel != null) {
            if (duel.getPlayerA() != null && duel.getPlayerB() != null) {
               if (duel.getPlayerA() == player) {
                  duel.broadcastToTeam2(packet);
               } else if (duel.getPlayerB() == player) {
                  duel.broadcastToTeam1(packet);
               } else if (duel.isPartyDuel()) {
                  if (duel.getPlayerA().getParty() != null && duel.getPlayerA().getParty().getMembers().contains(player)) {
                     duel.broadcastToTeam2(packet);
                  } else if (duel.getPlayerB().getParty() != null && duel.getPlayerB().getParty().getMembers().contains(player)) {
                     duel.broadcastToTeam1(packet);
                  }
               }
            }
         }
      }
   }

   public static final DuelManager getInstance() {
      return DuelManager.SingletonHolder._instance;
   }

   private static class SingletonHolder {
      protected static final DuelManager _instance = new DuelManager();
   }
}
