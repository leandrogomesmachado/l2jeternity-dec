package l2e.gameserver.model.olympiad;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import l2e.gameserver.Config;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.instancemanager.DoubleSessionManager;
import l2e.gameserver.model.Party;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.ExOlympiadMode;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;
import l2e.gameserver.network.serverpackets.SystemMessage;

public class OlympiadManager {
   private final List<Integer> _nonClassBasedRegisters = new CopyOnWriteArrayList<>();
   private final Map<Integer, List<Integer>> _classBasedRegisters = new ConcurrentHashMap<>();
   private final List<List<Integer>> _teamsBasedRegisters = new CopyOnWriteArrayList<>();
   private final List<Integer> _tempClassBasedRegisters = new CopyOnWriteArrayList<>();

   protected OlympiadManager() {
   }

   public static final OlympiadManager getInstance() {
      return OlympiadManager.SingletonHolder._instance;
   }

   public final List<Integer> getRegisteredNonClassBased() {
      return this._nonClassBasedRegisters;
   }

   public final Map<Integer, List<Integer>> getRegisteredClassBased() {
      return this._classBasedRegisters;
   }

   public final List<List<Integer>> getRegisteredTeamsBased() {
      return this._teamsBasedRegisters;
   }

   protected final List<List<Integer>> hasEnoughRegisteredClassed() {
      List<List<Integer>> result = null;

      for(Entry<Integer, List<Integer>> classList : this._classBasedRegisters.entrySet()) {
         if (classList.getValue() != null && classList.getValue().size() >= Config.ALT_OLY_CLASSED) {
            if (result == null) {
               result = new CopyOnWriteArrayList<>();
            }

            result.add(classList.getValue());
         }
      }

      return result;
   }

   protected final boolean hasEnoughRegisteredNonClassed() {
      return this._nonClassBasedRegisters.size() >= Config.ALT_OLY_NONCLASSED;
   }

   protected final boolean hasEnoughRegisteredTeams() {
      return this._teamsBasedRegisters.size() >= Config.ALT_OLY_TEAMS;
   }

   protected final void clearRegistered() {
      this._nonClassBasedRegisters.clear();
      this._classBasedRegisters.clear();
      this._teamsBasedRegisters.clear();
      this._tempClassBasedRegisters.clear();
      DoubleSessionManager.getInstance().clear(100);
   }

   public final boolean isRegistered(Player noble) {
      return this.isRegistered(noble, noble, false);
   }

   private final boolean isRegistered(Player noble, Player player, boolean showMessage) {
      Integer objId = noble.getObjectId();

      for(List<Integer> team : this._teamsBasedRegisters) {
         if (team != null && team.contains(objId)) {
            if (showMessage) {
               SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_ALREADY_REGISTERED_NON_CLASS_LIMITED_EVENT_TEAMS);
               sm.addPcName(noble);
               player.sendPacket(sm);
            }

            return true;
         }
      }

      if (this._nonClassBasedRegisters.contains(objId)) {
         if (showMessage) {
            SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_ALREADY_REGISTERED_ON_THE_NON_CLASS_LIMITED_MATCH_WAITING_LIST);
            sm.addPcName(noble);
            player.sendPacket(sm);
         }

         return true;
      } else {
         List<Integer> classed = this._classBasedRegisters.get(noble.getBaseClass());
         if (classed != null && classed.contains(objId)) {
            if (showMessage) {
               SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_ALREADY_REGISTERED_ON_THE_CLASS_MATCH_WAITING_LIST);
               sm.addPcName(noble);
               player.sendPacket(sm);
            }

            return true;
         } else {
            return false;
         }
      }
   }

   public final boolean isRegisteredInComp(Player noble) {
      return this.isRegistered(noble, noble, false) || this.isInCompetition(noble, noble, false);
   }

   private final boolean isInCompetition(Player noble, Player player, boolean showMessage) {
      if (!Olympiad._inCompPeriod) {
         return false;
      } else {
         int i = OlympiadGameManager.getInstance().getNumberOfStadiums();

         while(--i >= 0) {
            AbstractOlympiadGame game = OlympiadGameManager.getInstance().getOlympiadTask(i).getGame();
            if (game != null && game.containsParticipant(noble.getObjectId())) {
               if (!showMessage) {
                  return true;
               }

               switch(game.getType()) {
                  case CLASSED: {
                     SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_ALREADY_REGISTERED_ON_THE_CLASS_MATCH_WAITING_LIST);
                     sm.addPcName(noble);
                     player.sendPacket(sm);
                     break;
                  }
                  case NON_CLASSED: {
                     SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_ALREADY_REGISTERED_ON_THE_NON_CLASS_LIMITED_MATCH_WAITING_LIST);
                     sm.addPcName(noble);
                     player.sendPacket(sm);
                     break;
                  }
                  case TEAMS: {
                     SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_ALREADY_REGISTERED_NON_CLASS_LIMITED_EVENT_TEAMS);
                     sm.addPcName(noble);
                     player.sendPacket(sm);
                  }
               }

               return true;
            }
         }

         return false;
      }
   }

   public final boolean registerNoble(Player player, CompetitionType type) {
      if (!Olympiad._inCompPeriod) {
         player.sendPacket(SystemMessageId.THE_OLYMPIAD_GAME_IS_NOT_CURRENTLY_IN_PROGRESS);
         return false;
      } else if (Olympiad.getInstance().getMillisToCompEnd() < 600000L) {
         player.sendPacket(SystemMessageId.GAME_REQUEST_CANNOT_BE_MADE);
         return false;
      } else {
         int charId = player.getObjectId();
         if (Olympiad.getInstance().getRemainingWeeklyMatches(charId) < 1) {
            player.sendPacket(SystemMessageId.MAX_OLY_WEEKLY_MATCHES_REACHED);
            return false;
         } else {
            switch(type) {
               case CLASSED:
                  if (!this.checkNoble(player, player)) {
                     return false;
                  }

                  if (Olympiad.getInstance().getRemainingWeeklyMatchesClassed(charId) < 1) {
                     player.sendPacket(SystemMessageId.MAX_OLY_WEEKLY_MATCHES_REACHED_60_NON_CLASSED_30_CLASSED_10_TEAM);
                     return false;
                  }

                  List<Integer> classed = this._classBasedRegisters.get(player.getBaseClass());
                  if (classed != null) {
                     classed.add(charId);
                  } else {
                     List<Integer> var12 = new CopyOnWriteArrayList();
                     var12.add(charId);
                     this._tempClassBasedRegisters.add(charId);
                     this._classBasedRegisters.put(player.getBaseClass(), var12);
                  }

                  player.sendPacket(SystemMessageId.YOU_HAVE_BEEN_REGISTERED_IN_A_WAITING_LIST_OF_CLASSIFIED_GAMES);
                  break;
               case NON_CLASSED:
                  if (!this.checkNoble(player, player)) {
                     return false;
                  }

                  if (Olympiad.getInstance().getRemainingWeeklyMatchesNonClassed(charId) < 1) {
                     player.sendPacket(SystemMessageId.MAX_OLY_WEEKLY_MATCHES_REACHED_60_NON_CLASSED_30_CLASSED_10_TEAM);
                     return false;
                  }

                  this._nonClassBasedRegisters.add(charId);
                  player.sendPacket(SystemMessageId.YOU_HAVE_BEEN_REGISTERED_IN_A_WAITING_LIST_OF_NO_CLASS_GAMES);
                  break;
               case TEAMS:
                  Party party = player.getParty();
                  if (party == null || party.getMemberCount() != 3) {
                     player.sendPacket(SystemMessageId.PARTY_REQUIREMENTS_NOT_MET);
                     return false;
                  }

                  if (!party.isLeader(player)) {
                     player.sendPacket(SystemMessageId.ONLY_PARTY_LEADER_CAN_REQUEST_TEAM_MATCH);
                     return false;
                  }

                  int teamPoints = 0;
                  ArrayList<Integer> team = new ArrayList<>(party.getMemberCount());

                  for(Player noble : party.getMembers()) {
                     if (!this.checkNoble(noble, player)) {
                        if (Config.DOUBLE_SESSIONS_CHECK_MAX_OLYMPIAD_PARTICIPANTS > 0) {
                           for(Player unreg : party.getMembers()) {
                              if (unreg == noble) {
                                 break;
                              }

                              DoubleSessionManager.getInstance().removePlayer(100, unreg);
                           }
                        }

                        return false;
                     }

                     if (Olympiad.getInstance().getRemainingWeeklyMatchesTeam(noble.getObjectId()) < 1) {
                        player.sendPacket(SystemMessageId.MAX_OLY_WEEKLY_MATCHES_REACHED_60_NON_CLASSED_30_CLASSED_10_TEAM);
                        return false;
                     }

                     team.add(noble.getObjectId());
                     teamPoints += Olympiad.getInstance().getNoblePoints(noble.getObjectId());
                  }

                  if (teamPoints < 10) {
                     player.sendMessage("Your team must have at least 10 points in total.");
                     if (Config.DOUBLE_SESSIONS_CHECK_MAX_OLYMPIAD_PARTICIPANTS > 0) {
                        for(Player unreg : party.getMembers()) {
                           DoubleSessionManager.getInstance().removePlayer(100, unreg);
                        }
                     }

                     return false;
                  }

                  party.broadCast(SystemMessage.getSystemMessage(SystemMessageId.YOU_HAVE_REGISTERED_IN_A_WAITING_LIST_OF_TEAM_GAMES));
                  this._teamsBasedRegisters.add(team);
            }

            return true;
         }
      }
   }

   public final boolean unRegisterNoble(Player noble) {
      if (!Olympiad._inCompPeriod) {
         noble.sendPacket(SystemMessageId.THE_OLYMPIAD_GAME_IS_NOT_CURRENTLY_IN_PROGRESS);
         return false;
      } else if (!noble.isNoble()) {
         SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_DOES_NOT_MEET_REQUIREMENTS_ONLY_NOBLESS_CAN_PARTICIPATE_IN_THE_OLYMPIAD);
         sm.addString(noble.getName());
         noble.sendPacket(sm);
         return false;
      } else if (!this.isRegistered(noble, noble, false)) {
         noble.sendPacket(SystemMessageId.YOU_HAVE_NOT_BEEN_REGISTERED_IN_A_WAITING_LIST_OF_A_GAME);
         return false;
      } else if (this.isInCompetition(noble, noble, false)) {
         return false;
      } else {
         Integer objId = noble.getObjectId();
         if (this._nonClassBasedRegisters.remove(objId)) {
            if (Config.DOUBLE_SESSIONS_CHECK_MAX_OLYMPIAD_PARTICIPANTS > 0) {
               DoubleSessionManager.getInstance().removePlayer(100, noble);
            }

            noble.sendPacket(SystemMessageId.YOU_HAVE_BEEN_DELETED_FROM_THE_WAITING_LIST_OF_A_GAME);
            return true;
         } else {
            List<Integer> classed = this._classBasedRegisters.get(noble.getBaseClass());
            if (classed != null && classed.remove(objId)) {
               this._classBasedRegisters.put(noble.getBaseClass(), classed);
               this._tempClassBasedRegisters.remove(objId);
               if (Config.DOUBLE_SESSIONS_CHECK_MAX_OLYMPIAD_PARTICIPANTS > 0) {
                  DoubleSessionManager.getInstance().removePlayer(100, noble);
               }

               noble.sendPacket(SystemMessageId.YOU_HAVE_BEEN_DELETED_FROM_THE_WAITING_LIST_OF_A_GAME);
               return true;
            } else {
               for(List<Integer> team : this._teamsBasedRegisters) {
                  if (team != null && team.contains(objId)) {
                     this._teamsBasedRegisters.remove(team);
                     ThreadPoolManager.getInstance().execute(new OlympiadManager.AnnounceUnregToTeam(team));
                     return true;
                  }
               }

               return false;
            }
         }
      }
   }

   public final void removeDisconnectedCompetitor(Player player) {
      OlympiadGameTask task = OlympiadGameManager.getInstance().getOlympiadTask(player.getOlympiadGameId());
      if (task != null && task.isGameStarted()) {
         task.getGame().handleDisconnect(player);
         if (player.getLastX() != 0 && player.getLastY() != 0) {
            player.setIsInOlympiadMode(false);
            player.setIsOlympiadStart(false);
            player.setOlympiadSide(-1);
            player.setOlympiadGameId(-1);
            player.setOlympiadGame(null);
            player.sendPacket(new ExOlympiadMode(0));
            player.setReflectionId(0);
            player.teleToLocation(player.getLastX(), player.getLastY(), player.getLastZ(), true);
            player.setLastCords(0, 0, 0);
         }
      }

      Integer objId = player.getObjectId();
      if (!this._nonClassBasedRegisters.remove(objId)) {
         List<Integer> classed = this._classBasedRegisters.get(player.getBaseClass());
         if (classed != null && classed.remove(objId)) {
            this._tempClassBasedRegisters.remove(objId);
         } else {
            for(List<Integer> team : this._teamsBasedRegisters) {
               if (team != null && team.contains(objId)) {
                  this._teamsBasedRegisters.remove(team);
                  ThreadPoolManager.getInstance().execute(new OlympiadManager.AnnounceUnregToTeam(team));
                  return;
               }
            }
         }
      }
   }

   private final boolean checkNoble(Player noble, Player player) {
      if (!noble.isNoble()) {
         SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_DOES_NOT_MEET_REQUIREMENTS_ONLY_NOBLESS_CAN_PARTICIPATE_IN_THE_OLYMPIAD);
         sm.addPcName(noble);
         player.sendPacket(sm);
         return false;
      } else if (noble.isSubClassActive()) {
         SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_CANT_JOIN_THE_OLYMPIAD_WITH_A_SUB_CLASS_CHARACTER);
         sm.addPcName(noble);
         player.sendPacket(sm);
         return false;
      } else if (noble.isCursedWeaponEquipped()) {
         SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_CANNOT_JOIN_OLYMPIAD_POSSESSING_S2);
         sm.addPcName(noble);
         sm.addItemName(noble.getCursedWeaponEquippedId());
         player.sendPacket(sm);
         return false;
      } else if (!noble.isInventoryUnder90(true)) {
         SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_CANNOT_PARTICIPATE_IN_OLYMPIAD_INVENTORY_SLOT_EXCEEDS_80_PERCENT);
         sm.addPcName(noble);
         player.sendPacket(sm);
         return false;
      } else {
         int charId = noble.getObjectId();
         if (noble.isOnEvent()) {
            player.sendMessage("You can't join olympiad while participating on Event.");
            return false;
         } else if (this.isRegistered(noble, player, true)) {
            return false;
         } else if (this.isInCompetition(noble, player, true)) {
            return false;
         } else {
            Olympiad.addNoble(noble);
            int points = Olympiad.getInstance().getNoblePoints(charId);
            if (points <= 0) {
               NpcHtmlMessage message = new NpcHtmlMessage(0);
               message.setFile(player, player.getLang(), "data/html/olympiad/noble_nopoints1.htm");
               message.replace("%objectId%", String.valueOf(noble.getTargetId()));
               player.sendPacket(message);
               return false;
            } else if (Config.DOUBLE_SESSIONS_CHECK_MAX_OLYMPIAD_PARTICIPANTS > 0
               && !DoubleSessionManager.getInstance().tryAddPlayer(100, noble, Config.DOUBLE_SESSIONS_CHECK_MAX_OLYMPIAD_PARTICIPANTS)) {
               NpcHtmlMessage message = new NpcHtmlMessage(0);
               message.setFile(player, player.getLang(), "data/html/mods/OlympiadIPRestriction.htm");
               message.replace("%max%", String.valueOf(Config.DOUBLE_SESSIONS_CHECK_MAX_OLYMPIAD_PARTICIPANTS));
               player.sendPacket(message);
               return false;
            } else if (player.getClassId().level() != 3) {
               NpcHtmlMessage message = new NpcHtmlMessage(0);
               message.setFile(player, player.getLang(), "data/html/olympiad/invalid_class.htm");
               player.sendPacket(message);
               return false;
            } else {
               return true;
            }
         }
      }
   }

   public int getCountOpponents() {
      return this._nonClassBasedRegisters.size() + this._tempClassBasedRegisters.size() + this._teamsBasedRegisters.size();
   }

   private static final class AnnounceUnregToTeam implements Runnable {
      private final List<Integer> _team;

      public AnnounceUnregToTeam(List<Integer> t) {
         this._team = t;
      }

      @Override
      public final void run() {
         SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_HAVE_BEEN_DELETED_FROM_THE_WAITING_LIST_OF_A_GAME);

         for(int objectId : this._team) {
            Player teamMember = World.getInstance().getPlayer(objectId);
            if (teamMember != null) {
               teamMember.sendPacket(sm);
               if (Config.DOUBLE_SESSIONS_CHECK_MAX_OLYMPIAD_PARTICIPANTS > 0) {
                  DoubleSessionManager.getInstance().removePlayer(100, teamMember);
               }
            }
         }

         Player teamMember = null;
      }
   }

   private static class SingletonHolder {
      protected static final OlympiadManager _instance = new OlympiadManager();
   }
}
