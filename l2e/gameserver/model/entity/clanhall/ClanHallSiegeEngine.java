package l2e.gameserver.model.entity.clanhall;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Logger;
import l2e.commons.util.Util;
import l2e.gameserver.Announcements;
import l2e.gameserver.Config;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.data.holder.ClanHolder;
import l2e.gameserver.data.parser.NpcsParser;
import l2e.gameserver.database.DatabaseFactory;
import l2e.gameserver.instancemanager.CHSiegeManager;
import l2e.gameserver.instancemanager.MapRegionManager;
import l2e.gameserver.model.Clan;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.SiegeClan;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.npc.NpcTemplate;
import l2e.gameserver.model.entity.Siegable;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.spawn.Spawner;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.NpcSay;
import l2e.gameserver.network.serverpackets.SystemMessage;

public abstract class ClanHallSiegeEngine extends Quest implements Siegable {
   private static final String SQL_LOAD_ATTACKERS = "SELECT attacker_id FROM clanhall_siege_attackers WHERE clanhall_id = ?";
   private static final String SQL_SAVE_ATTACKERS = "INSERT INTO clanhall_siege_attackers VALUES (?,?)";
   private static final String SQL_LOAD_GUARDS = "SELECT * FROM clanhall_siege_guards WHERE clanHallId = ?";
   public static final int FORTRESS_RESSISTANCE = 21;
   public static final int DEVASTATED_CASTLE = 34;
   public static final int BANDIT_STRONGHOLD = 35;
   public static final int RAINBOW_SPRINGS = 62;
   public static final int BEAST_FARM = 63;
   public static final int FORTRESS_OF_DEAD = 64;
   protected final Logger _log;
   private final Map<Integer, SiegeClan> _attackers = new ConcurrentHashMap<>();
   private List<Spawner> _guards;
   public SiegableHall _hall;
   public ScheduledFuture<?> _siegeTask;
   public boolean _missionAccomplished = false;

   public ClanHallSiegeEngine(int questId, String name, String descr, int hallId) {
      super(questId, name, descr);
      this._log = Logger.getLogger(this.getClass().getName());
      this._hall = CHSiegeManager.getInstance().getSiegableHall(hallId);
      this._hall.setSiege(this);
      this._siegeTask = ThreadPoolManager.getInstance()
         .schedule(new ClanHallSiegeEngine.PrepareOwner(), this._hall.getNextSiegeTime() - System.currentTimeMillis() - 3600000L);
      if (Config.DEBUG) {
         this._log.config(Util.clanHallName(null, this._hall.getId()) + " Siege scheduled for: " + this.getSiegeDate().getTime());
      }

      this.loadAttackers();
   }

   public void loadAttackers() {
      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement("SELECT attacker_id FROM clanhall_siege_attackers WHERE clanhall_id = ?");
      ) {
         statement.setInt(1, this._hall.getId());

         try (ResultSet rset = statement.executeQuery()) {
            while(rset.next()) {
               int id = rset.getInt("attacker_id");
               SiegeClan clan = new SiegeClan(id, SiegeClan.SiegeClanType.ATTACKER);
               this._attackers.put(id, clan);
            }
         }
      } catch (Exception var60) {
         this._log.warning(this.getName() + ": Could not load siege attackers!:");
      }
   }

   public final void saveAttackers() {
      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement delStatement = con.prepareStatement("DELETE FROM clanhall_siege_attackers WHERE clanhall_id = ?");
      ) {
         delStatement.setInt(1, this._hall.getId());
         delStatement.execute();
         if (this.getAttackers().size() > 0) {
            try (PreparedStatement insert = con.prepareStatement("INSERT INTO clanhall_siege_attackers VALUES (?,?)")) {
               for(SiegeClan clan : this.getAttackers().values()) {
                  insert.setInt(1, this._hall.getId());
                  insert.setInt(2, clan.getClanId());
                  insert.execute();
                  insert.clearParameters();
               }
            }
         }

         if (Config.DEBUG) {
            this._log.config(this.getName() + ": Sucessfully saved attackers down to database!");
         }
      } catch (Exception var60) {
         this._log.warning(this.getName() + ": Couldnt save attacker list!");
      }
   }

   public final void loadGuards() {
      if (this._guards == null) {
         this._guards = new ArrayList<>();

         try (
            Connection con = DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement = con.prepareStatement("SELECT * FROM clanhall_siege_guards WHERE clanHallId = ?");
         ) {
            statement.setInt(1, this._hall.getId());

            try (ResultSet rset = statement.executeQuery()) {
               while(rset.next()) {
                  int npcId = rset.getInt("npcId");
                  NpcTemplate template = NpcsParser.getInstance().getTemplate(npcId);
                  Spawner spawn = new Spawner(template);
                  spawn.setX(rset.getInt("x"));
                  spawn.setY(rset.getInt("y"));
                  spawn.setZ(rset.getInt("z"));
                  spawn.setHeading(rset.getInt("heading"));
                  spawn.setRespawnDelay(rset.getInt("respawnDelay"));
                  spawn.setAmount(1);
                  this._guards.add(spawn);
               }
            }
         } catch (Exception var61) {
            this._log.warning(this.getName() + ": Couldnt load siege guards!:");
         }
      }
   }

   private final void spawnSiegeGuards() {
      for(Spawner guard : this._guards) {
         if (guard != null) {
            guard.init();
         }
      }
   }

   private final void unSpawnSiegeGuards() {
      if (this._guards != null && this._guards.size() > 0) {
         for(Spawner guard : this._guards) {
            if (guard != null) {
               guard.stopRespawn();
               if (guard.getLastSpawn() != null) {
                  guard.getLastSpawn().deleteMe();
               }
            }
         }
      }
   }

   @Override
   public List<Npc> getFlag(Clan clan) {
      List<Npc> result = null;
      SiegeClan sClan = this.getAttackerClan(clan);
      if (sClan != null) {
         result = sClan.getFlag();
      }

      return result;
   }

   public final Map<Integer, SiegeClan> getAttackers() {
      return this._attackers;
   }

   @Override
   public boolean checkIsAttacker(Clan clan) {
      return clan == null ? false : this._attackers.containsKey(clan.getId());
   }

   @Override
   public boolean checkIsDefender(Clan clan) {
      return false;
   }

   @Override
   public SiegeClan getAttackerClan(int clanId) {
      return this._attackers.get(clanId);
   }

   @Override
   public SiegeClan getAttackerClan(Clan clan) {
      return this.getAttackerClan(clan.getId());
   }

   @Override
   public List<SiegeClan> getAttackerClans() {
      return new ArrayList<>(this._attackers.values());
   }

   @Override
   public List<Player> getAttackersInZone() {
      List<Player> attackers = new ArrayList<>();

      for(Player pc : this._hall.getSiegeZone().getPlayersInside()) {
         Clan clan = pc.getClan();
         if (clan != null && this.getAttackers().containsKey(clan.getId())) {
            attackers.add(pc);
         }
      }

      return attackers;
   }

   @Override
   public SiegeClan getDefenderClan(int clanId) {
      return null;
   }

   @Override
   public SiegeClan getDefenderClan(Clan clan) {
      return null;
   }

   @Override
   public List<SiegeClan> getDefenderClans() {
      return null;
   }

   public void prepareOwner() {
      if (this._hall.getOwnerId() > 0) {
         SiegeClan clan = new SiegeClan(this._hall.getOwnerId(), SiegeClan.SiegeClanType.ATTACKER);
         this.getAttackers().put(clan.getClanId(), new SiegeClan(clan.getClanId(), SiegeClan.SiegeClanType.ATTACKER));
      }

      this._hall.free();
      this._hall.banishForeigners();
      SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.REGISTRATION_TERM_FOR_S1_ENDED);
      msg.addString(this.getName());
      Announcements.getInstance().announceToAll(msg);
      this._hall.updateSiegeStatus(SiegeStatus.WAITING_BATTLE);
      this._siegeTask = ThreadPoolManager.getInstance().schedule(new ClanHallSiegeEngine.SiegeStarts(), 3600000L);
   }

   @Override
   public void startSiege() {
      if (this.getAttackers().size() < 1 && this._hall.getId() != 21) {
         this.onSiegeEnds();
         this.getAttackers().clear();
         this._hall.updateNextSiege();
         this._siegeTask = ThreadPoolManager.getInstance().schedule(new ClanHallSiegeEngine.PrepareOwner(), this._hall.getSiegeDate().getTimeInMillis());
         this._hall.updateSiegeStatus(SiegeStatus.WAITING_BATTLE);
         SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.SIEGE_OF_S1_HAS_BEEN_CANCELED_DUE_TO_LACK_OF_INTEREST);
         sm.addString(Util.clanHallName(null, this._hall.getId()));
         Announcements.getInstance().announceToAll(sm);
      } else {
         this._hall.spawnDoor();
         this.loadGuards();
         this.spawnSiegeGuards();
         this._hall.updateSiegeZone(true);
         byte state = 1;

         for(SiegeClan sClan : this.getAttackerClans()) {
            Clan clan = ClanHolder.getInstance().getClan(sClan.getClanId());
            if (clan != null) {
               for(Player pc : clan.getOnlineMembers(0)) {
                  if (pc != null) {
                     pc.setSiegeState((byte)1);
                     pc.broadcastUserInfo(true);
                     pc.setIsInHideoutSiege(true);
                  }
               }
            }
         }

         this._hall.updateSiegeStatus(SiegeStatus.RUNNING);
         this.onSiegeStarts();
         this._siegeTask = ThreadPoolManager.getInstance().schedule(new ClanHallSiegeEngine.SiegeEnds(), this._hall.getSiegeLenght());
      }
   }

   @Override
   public void endSiege() {
      SystemMessage end = SystemMessage.getSystemMessage(SystemMessageId.SIEGE_OF_S1_HAS_ENDED);
      end.addString(Util.clanHallName(null, this._hall.getId()));
      Announcements.getInstance().announceToAll(end);
      Clan winner = this.getWinner();
      SystemMessage finalMsg = null;
      if (this._missionAccomplished && winner != null) {
         this._hall.setOwner(winner);
         winner.setHideoutId(this._hall.getId());
         finalMsg = SystemMessage.getSystemMessage(SystemMessageId.CLAN_S1_VICTORIOUS_OVER_S2_S_SIEGE);
         finalMsg.addString(winner.getName());
         finalMsg.addString(Util.clanHallName(null, this._hall.getId()));
         Announcements.getInstance().announceToAll(finalMsg);
      } else {
         finalMsg = SystemMessage.getSystemMessage(SystemMessageId.SIEGE_S1_DRAW);
         finalMsg.addString(Util.clanHallName(null, this._hall.getId()));
         Announcements.getInstance().announceToAll(finalMsg);
      }

      this._missionAccomplished = false;
      this._hall.updateSiegeZone(false);
      this._hall.updateNextSiege();
      this._hall.spawnDoor(false);
      this._hall.banishForeigners();
      byte state = 0;

      for(SiegeClan sClan : this.getAttackerClans()) {
         Clan clan = ClanHolder.getInstance().getClan(sClan.getClanId());
         if (clan != null) {
            for(Player player : clan.getOnlineMembers(0)) {
               player.setSiegeState((byte)0);
               player.broadcastUserInfo(true);
               player.setIsInHideoutSiege(false);
            }
         }
      }

      for(Creature chr : this._hall.getSiegeZone().getCharactersInside()) {
         if (chr != null && chr.isPlayer()) {
            chr.getActingPlayer().startPvPFlag();
         }
      }

      this.getAttackers().clear();
      this.onSiegeEnds();
      this._siegeTask = ThreadPoolManager.getInstance()
         .schedule(new ClanHallSiegeEngine.PrepareOwner(), this._hall.getNextSiegeTime() - System.currentTimeMillis() - 3600000L);
      if (Config.DEBUG) {
         this._log.config("Siege of " + Util.clanHallName(null, this._hall.getId()) + " scheduled for: " + this._hall.getSiegeDate().getTime());
      }

      this._hall.updateSiegeStatus(SiegeStatus.REGISTERING);
      this.unSpawnSiegeGuards();
   }

   @Override
   public void updateSiege() {
      this.cancelSiegeTask();
      this._siegeTask = ThreadPoolManager.getInstance().schedule(new ClanHallSiegeEngine.PrepareOwner(), this._hall.getNextSiegeTime() - 3600000L);
      if (Config.DEBUG) {
         this._log.config(Util.clanHallName(null, this._hall.getId()) + " siege scheduled for: " + this._hall.getSiegeDate().getTime().toString());
      }
   }

   public void cancelSiegeTask() {
      if (this._siegeTask != null) {
         this._siegeTask.cancel(false);
      }
   }

   @Override
   public Calendar getSiegeDate() {
      return this._hall.getSiegeDate();
   }

   @Override
   public boolean giveFame() {
      return Config.CHS_ENABLE_FAME;
   }

   @Override
   public int getFameAmount() {
      return Config.CHS_FAME_AMOUNT;
   }

   @Override
   public int getFameFrequency() {
      return Config.CHS_FAME_FREQUENCY;
   }

   public final void broadcastNpcSay(Npc npc, int type, NpcStringId messageId) {
      NpcSay npcSay = new NpcSay(npc.getObjectId(), type, npc.getId(), messageId);
      int sourceRegion = MapRegionManager.getInstance().getMapRegionLocId(npc);

      for(Player pc : World.getInstance().getAllPlayers()) {
         if (pc != null && MapRegionManager.getInstance().getMapRegionLocId(pc) == sourceRegion) {
            pc.sendPacket(npcSay);
         }
      }
   }

   public Location getInnerSpawnLoc(Player player) {
      return null;
   }

   public boolean canPlantFlag() {
      return true;
   }

   public boolean doorIsAutoAttackable() {
      return true;
   }

   public void onSiegeStarts() {
   }

   public void onSiegeEnds() {
   }

   public abstract Clan getWinner();

   public class PrepareOwner implements Runnable {
      @Override
      public void run() {
         ClanHallSiegeEngine.this.prepareOwner();
      }
   }

   public class SiegeEnds implements Runnable {
      @Override
      public void run() {
         ClanHallSiegeEngine.this.endSiege();
      }
   }

   public class SiegeStarts implements Runnable {
      @Override
      public void run() {
         ClanHallSiegeEngine.this.startSiege();
      }
   }
}
