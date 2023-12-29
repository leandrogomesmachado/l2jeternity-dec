package l2e.gameserver.instancemanager;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import l2e.commons.time.cron.SchedulingPattern;
import l2e.gameserver.Announcements;
import l2e.gameserver.Config;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.data.parser.DoorParser;
import l2e.gameserver.database.DatabaseFactory;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.instance.DoorInstance;
import l2e.gameserver.model.entity.underground_coliseum.UCArena;
import l2e.gameserver.model.entity.underground_coliseum.UCBestTeam;
import l2e.gameserver.model.entity.underground_coliseum.UCPoint;
import l2e.gameserver.model.entity.underground_coliseum.UCReward;
import l2e.gameserver.model.entity.underground_coliseum.UCTeam;
import l2e.gameserver.model.strings.server.ServerMessage;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public class UndergroundColiseumManager {
   private static final Logger _log = Logger.getLogger(UndergroundColiseumManager.class.getName());
   private final Map<Integer, UCArena> _arenas = new HashMap<>(5);
   private boolean _isStarted = false;
   private long _periodStartTime;
   private long _periodEndTime;
   private ScheduledFuture<?> _regTask = null;
   private final Map<Integer, UCBestTeam> _bestTeams = new HashMap<>(5);

   public static UndergroundColiseumManager getInstance() {
      return UndergroundColiseumManager.SingletonHolder._instance;
   }

   protected UndergroundColiseumManager() {
      this._periodStartTime = ServerVariables.getLong("UC_START_TIME", 0L);
      this._periodEndTime = ServerVariables.getLong("UC_STOP_TIME", 0L);
      if (this._periodStartTime < System.currentTimeMillis() && this._periodEndTime < System.currentTimeMillis()) {
         this.generateNewDate();
      }

      this.load();
      _log.info("UndergroundColiseum: Loaded " + this._arenas.size() + " coliseum arenas.");
      if (this._periodStartTime < System.currentTimeMillis() && this._periodEndTime > System.currentTimeMillis()) {
         this.switchStatus(true);
      } else {
         long nextTime = this._periodStartTime - System.currentTimeMillis();
         this._regTask = ThreadPoolManager.getInstance().schedule(new UndergroundColiseumManager.UCRegistrationTask(true), nextTime);
         _log.info("UndergroundColiseum: Battles will begin at: " + new Date(this._periodStartTime));
      }

      this.restoreBestTeams();
   }

   private void restoreBestTeams() {
      this._bestTeams.clear();

      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement("SELECT * FROM underground_colliseum_stats ORDER BY arenaId");
         ResultSet rset = statement.executeQuery();
      ) {
         while(rset.next()) {
            int arenaId = rset.getInt("arenaId");
            String leader = rset.getString("leader");
            int wins = rset.getInt("wins");
            this._bestTeams.put(arenaId, new UCBestTeam(arenaId, leader, wins));
         }
      } catch (SQLException var64) {
         _log.warning(this.getClass().getSimpleName() + ": Couldnt load underground_colliseum_stats table");
      } catch (Exception var65) {
         _log.log(
            Level.WARNING, this.getClass().getSimpleName() + ": Error while initializing UndergroundColiseumManager: " + var65.getMessage(), (Throwable)var65
         );
      }
   }

   private void saveBestTeam(UCBestTeam team, boolean isNew) {
      if (isNew) {
         try (
            Connection con = DatabaseFactory.getInstance().getConnection();
            PreparedStatement ps = con.prepareStatement("INSERT INTO underground_colliseum_stats (`arenaId`, `leader`, `wins`) VALUES (?,?,?) ");
         ) {
            ps.setInt(1, team.getArenaId());
            ps.setString(2, team.getLeaderName());
            ps.setInt(3, team.getWins());
            ps.executeUpdate();
         } catch (SQLException var63) {
            _log.warning(this.getClass().getSimpleName() + ": Could not save underground_colliseum_stats: " + var63.getMessage());
         }
      } else {
         try (Connection con = DatabaseFactory.getInstance().getConnection()) {
            PreparedStatement stmt = con.prepareStatement("UPDATE underground_colliseum_stats SET leader = ?, wins = ?  WHERE arenaId = ?");
            stmt.setInt(1, team.getArenaId());
            stmt.setInt(2, team.getWins());
            stmt.setInt(3, team.getArenaId());
            stmt.execute();
            stmt.close();
         } catch (Exception var59) {
            _log.warning("Warning: could not clean status for underground_colliseum_stats areanaId: " + team.getArenaId() + " in database!");
         }
      }
   }

   public UCBestTeam getBestTeam(int arenaId) {
      return this._bestTeams.get(arenaId);
   }

   public void updateBestTeam(int arenaId, String name, int wins) {
      if (this._bestTeams.containsKey(arenaId)) {
         UCBestTeam team = this.getBestTeam(arenaId);
         if (team != null) {
            team.setLeader(name);
            team.setWins(wins);
            this.saveBestTeam(team, false);
         }
      } else {
         UCBestTeam team = new UCBestTeam(arenaId, name, wins);
         this._bestTeams.put(arenaId, team);
         this.saveBestTeam(team, true);
      }
   }

   private void generateNewDate() {
      SchedulingPattern timePattern = new SchedulingPattern(Config.UC_START_TIME);
      this._periodStartTime = timePattern.next(System.currentTimeMillis());
      this._periodEndTime = this._periodStartTime + (long)(Config.UC_TIME_PERIOD * 3600000);
      ServerVariables.set("UC_START_TIME", this._periodStartTime);
      ServerVariables.set("UC_STOP_TIME", this._periodEndTime);
   }

   private void load() {
      try {
         DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
         factory.setValidating(false);
         factory.setIgnoringComments(true);
         File file = new File(Config.DATAPACK_ROOT + "/data/stats/npcs/spawnZones/underground_coliseum.xml");
         if (!file.exists()) {
            _log.info("The underground_coliseum.xml file is missing.");
            return;
         }

         Document doc = factory.newDocumentBuilder().parse(file);

         for(Node n = doc.getFirstChild(); n != null; n = n.getNextSibling()) {
            if ("list".equalsIgnoreCase(n.getNodeName())) {
               for(Node d = n.getFirstChild(); d != null; d = d.getNextSibling()) {
                  if ("arena".equalsIgnoreCase(d.getNodeName())) {
                     NamedNodeMap map = d.getAttributes();
                     int id = Integer.parseInt(map.getNamedItem("id").getNodeValue());
                     int min_level = Integer.parseInt(map.getNamedItem("minLvl").getNodeValue());
                     int max_level = Integer.parseInt(map.getNamedItem("maxLvl").getNodeValue());
                     int curator = Integer.parseInt(map.getNamedItem("curator").getNodeValue());
                     UCArena arena = new UCArena(id, curator, min_level, max_level);
                     int index = 0;
                     int index2 = 0;

                     for(Node und = d.getFirstChild(); und != null; und = und.getNextSibling()) {
                        if ("tower".equalsIgnoreCase(und.getNodeName())) {
                           map = und.getAttributes();
                           int npcId = Integer.parseInt(map.getNamedItem("id").getNodeValue());
                           int x = Integer.parseInt(map.getNamedItem("x").getNodeValue());
                           int y = Integer.parseInt(map.getNamedItem("y").getNodeValue());
                           int z = Integer.parseInt(map.getNamedItem("z").getNodeValue());
                           UCTeam team = new UCTeam(index, arena, x, y, z, npcId);
                           arena.setUCTeam(index, team);
                           ++index;
                        } else if ("spawn".equalsIgnoreCase(und.getNodeName())) {
                           map = und.getAttributes();
                           List<DoorInstance> doors = new ArrayList<>();
                           String doorList = map.getNamedItem("doors") != null ? map.getNamedItem("doors").getNodeValue() : "";
                           if (!doorList.isEmpty()) {
                              String[] doorSplint = doorList.split(",");

                              for(String doorId : doorSplint) {
                                 DoorInstance door = DoorParser.getInstance().getDoor(Integer.parseInt(doorId));
                                 if (door != null) {
                                    doors.add(door);
                                 }
                              }
                           }

                           int x = Integer.parseInt(map.getNamedItem("x").getNodeValue());
                           int y = Integer.parseInt(map.getNamedItem("y").getNodeValue());
                           int z = Integer.parseInt(map.getNamedItem("z").getNodeValue());
                           UCPoint point = new UCPoint(doors, new Location(x, y, z));
                           arena.setUCPoint(index2, point);
                           ++index2;
                        } else if ("rewards".equalsIgnoreCase(und.getNodeName())) {
                           for(Node c = und.getFirstChild(); c != null; c = c.getNextSibling()) {
                              if ("item".equalsIgnoreCase(c.getNodeName())) {
                                 int itemId = Integer.parseInt(c.getAttributes().getNamedItem("id").getNodeValue());
                                 long amount = Long.parseLong(c.getAttributes().getNamedItem("amount").getNodeValue());
                                 boolean useModifier = c.getAttributes().getNamedItem("useModifers") != null
                                    ? Boolean.parseBoolean(c.getAttributes().getNamedItem("useModifers").getNodeValue())
                                    : false;
                                 arena.setReward(new UCReward(itemId, amount, useModifier));
                              }
                           }
                        }
                     }

                     this._arenas.put(arena.getId(), arena);
                  }
               }
            }
         }
      } catch (DOMException | ParserConfigurationException | SAXException | NumberFormatException var23) {
         _log.log(Level.WARNING, "UndergroundColiseumManager: underground_coliseum.xml could not be initialized.", (Throwable)var23);
      } catch (IllegalArgumentException | IOException var24) {
         _log.log(Level.WARNING, "UndergroundColiseumManager: IOException or IllegalArgumentException.", (Throwable)var24);
      }
   }

   public UCArena getArena(int id) {
      return this._arenas.get(id);
   }

   public void setStarted(boolean started) {
      this._isStarted = started;

      for(UCArena arena : this.getAllArenas()) {
         arena.switchStatus(started);
      }

      if (Config.UC_ANNOUNCE_BATTLES) {
         if (this._isStarted) {
            ServerMessage msg = new ServerMessage("UC.STARTED", true);
            Announcements.getInstance().announceToAll(msg);
         } else {
            ServerMessage msg = new ServerMessage("UC.STOPED", true);
            Announcements.getInstance().announceToAll(msg);
         }
      }
   }

   public boolean isStarted() {
      return this._isStarted;
   }

   public Collection<UCArena> getAllArenas() {
      return this._arenas.values();
   }

   private void switchStatus(boolean isStart) {
      if (this._regTask != null) {
         this._regTask.cancel(false);
         this._regTask = null;
      }

      this.setStarted(isStart);
      if (isStart) {
         long nextTime = this._periodEndTime - System.currentTimeMillis();
         this._regTask = ThreadPoolManager.getInstance().schedule(new UndergroundColiseumManager.UCRegistrationTask(false), nextTime);
         _log.info("UndergroundColiseum: Battles will end at: " + new Date(this._periodEndTime));
      } else {
         this.generateNewDate();
         long nextTime = this._periodStartTime - System.currentTimeMillis();
         this._regTask = ThreadPoolManager.getInstance().schedule(new UndergroundColiseumManager.UCRegistrationTask(true), nextTime);
         _log.info("UndergroundColiseum: Battles will begin at: " + new Date(this._periodStartTime));
      }
   }

   private static class SingletonHolder {
      protected static final UndergroundColiseumManager _instance = new UndergroundColiseumManager();
   }

   public class UCRegistrationTask implements Runnable {
      private final boolean _status;

      public UCRegistrationTask(boolean status) {
         this._status = status;
      }

      @Override
      public void run() {
         UndergroundColiseumManager.this.switchStatus(this._status);
      }
   }
}
