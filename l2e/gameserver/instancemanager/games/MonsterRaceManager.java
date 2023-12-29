package l2e.gameserver.instancemanager.games;

import java.lang.reflect.Constructor;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import l2e.commons.util.Broadcast;
import l2e.commons.util.Rnd;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.data.parser.NpcsParser;
import l2e.gameserver.data.parser.SpawnParser;
import l2e.gameserver.database.DatabaseFactory;
import l2e.gameserver.idfactory.IdFactory;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.templates.HistoryInfoTemplate;
import l2e.gameserver.model.actor.templates.npc.NpcTemplate;
import l2e.gameserver.model.spawn.Spawner;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.DeleteObject;
import l2e.gameserver.network.serverpackets.MonRaceInfo;
import l2e.gameserver.network.serverpackets.PlaySound;
import l2e.gameserver.network.serverpackets.SystemMessage;

public class MonsterRaceManager {
   private static final Logger _log = Logger.getLogger(MonsterRaceManager.class.getName());
   private static final PlaySound SOUND_1 = new PlaySound(1, "S_Race");
   private static final PlaySound SOUND_2 = new PlaySound("ItemSound2.race_start");
   private static final int[][] CODES = new int[][]{{-1, 0}, {0, 15322}, {13765, -1}};
   private final List<Integer> _npcTemplates = new ArrayList<>();
   private final List<HistoryInfoTemplate> _history = new ArrayList<>();
   private final Map<Integer, Long> _betsPerLane = new ConcurrentHashMap<>();
   private final List<Double> _odds = new ArrayList<>();
   private Npc _manager = null;
   private int _raceNumber = 1;
   private int _finalCountdown = 0;
   private MonsterRaceManager.RaceState _state = MonsterRaceManager.RaceState.RACE_END;
   private MonRaceInfo _packet;
   private final Npc[] _monsters;
   private Constructor<?> _constructor;
   private int[][] _speeds;
   private final int[] _first;
   private final int[] _second;

   protected MonsterRaceManager() {
      this.loadHistory();
      this.loadBets();

      for(int i = 31003; i < 31027; ++i) {
         this._npcTemplates.add(i);
      }

      this._monsters = new Npc[8];
      this._speeds = new int[8][20];
      this._first = new int[2];
      this._second = new int[2];

      for(Spawner spawn : SpawnParser.getInstance().getSpawnData()) {
         if (spawn != null && spawn.getId() == 30995) {
            this._manager = spawn.getLastSpawn();
         }
      }

      ThreadPoolManager.getInstance().scheduleAtFixedRate(new MonsterRaceManager.Announcement(), 0L, 1000L);
   }

   public Npc[] getMonsters() {
      return this._monsters;
   }

   public int[][] getSpeeds() {
      return this._speeds;
   }

   public int getFirstPlace() {
      return this._first[0];
   }

   public int getSecondPlace() {
      return this._second[0];
   }

   public MonRaceInfo getRacePacket() {
      return this._packet;
   }

   public MonsterRaceManager.RaceState getCurrentRaceState() {
      return this._state;
   }

   public int getRaceNumber() {
      return this._raceNumber;
   }

   public List<HistoryInfoTemplate> getHistory() {
      return this._history;
   }

   public List<Double> getOdds() {
      return this._odds;
   }

   public void newRace() {
      this._history.add(new HistoryInfoTemplate(this._raceNumber, 0, 0, 0.0));
      Collections.shuffle(this._npcTemplates);

      for(int i = 0; i < 8; ++i) {
         try {
            NpcTemplate template = NpcsParser.getInstance().getTemplate(this._npcTemplates.get(i));
            this._constructor = Class.forName("l2e.gameserver.model.actor.instance." + template.getType() + "Instance").getConstructors()[0];
            this._monsters[i] = (Npc)this._constructor.newInstance(IdFactory.getInstance().getNextId(), template);
         } catch (Exception var3) {
            _log.log(Level.SEVERE, "Failed generating MonsterRace monster.", (Throwable)var3);
         }
      }
   }

   public void newSpeeds() {
      this._speeds = new int[8][20];
      int total = 0;
      this._first[1] = 0;
      this._second[1] = 0;

      for(int i = 0; i < 8; ++i) {
         total = 0;

         for(int j = 0; j < 20; ++j) {
            if (j == 19) {
               this._speeds[i][j] = 100;
            } else {
               this._speeds[i][j] = Rnd.get(60) + 65;
            }

            total += this._speeds[i][j];
         }

         if (total >= this._first[1]) {
            this._second[0] = this._first[0];
            this._second[1] = this._first[1];
            this._first[0] = 8 - i;
            this._first[1] = total;
         } else if (total >= this._second[1]) {
            this._second[0] = 8 - i;
            this._second[1] = total;
         }
      }
   }

   protected void loadHistory() {
      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement ps = con.prepareStatement("SELECT * FROM monster_race_history");
         ResultSet rs = ps.executeQuery();
      ) {
         while(rs.next()) {
            this._history.add(new HistoryInfoTemplate(rs.getInt("race_id"), rs.getInt("first"), rs.getInt("second"), rs.getDouble("odd_rate")));
            ++this._raceNumber;
         }
      } catch (SQLException var59) {
         _log.log(Level.SEVERE, "Can't load Monster Race history.", (Throwable)var59);
      }

      _log.info("Loaded " + this._history.size() + " Monster Race records, currently on race #" + this._raceNumber);
   }

   protected void saveHistory(HistoryInfoTemplate history) {
      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement ps = con.prepareStatement("INSERT INTO monster_race_history (race_id, first, second, odd_rate) VALUES (?,?,?,?)");
      ) {
         ps.setInt(1, history.getRaceId());
         ps.setInt(2, history.getFirst());
         ps.setInt(3, history.getSecond());
         ps.setDouble(4, history.getOddRate());
         ps.execute();
      } catch (SQLException var34) {
         _log.log(Level.SEVERE, "Can't save Monster Race history.", (Throwable)var34);
      }
   }

   protected void loadBets() {
      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement ps = con.prepareStatement("SELECT * FROM monster_race_bets");
         ResultSet rs = ps.executeQuery();
      ) {
         while(rs.next()) {
            this.setBetOnLane(rs.getInt("lane_id"), rs.getLong("bet"), false);
         }
      } catch (SQLException var59) {
         _log.log(Level.SEVERE, "Can't load Monster Race bets.", (Throwable)var59);
      }
   }

   protected void saveBet(int lane, long sum) {
      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement ps = con.prepareStatement("REPLACE INTO monster_race_bets (lane_id, bet) VALUES (?,?)");
      ) {
         ps.setInt(1, lane);
         ps.setLong(2, sum);
         ps.execute();
      } catch (SQLException var36) {
         _log.log(Level.SEVERE, "Can't save Monster Race bet.", (Throwable)var36);
      }
   }

   protected void clearBets() {
      for(int key : this._betsPerLane.keySet()) {
         this._betsPerLane.put(key, 0L);
      }

      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement ps = con.prepareStatement("UPDATE monster_race_bets SET bet = 0");
      ) {
         ps.execute();
      } catch (SQLException var33) {
         _log.log(Level.SEVERE, "Can't clear Monster Race bets.", (Throwable)var33);
      }
   }

   public void setBetOnLane(int lane, long amount, boolean saveOnDb) {
      long sum = this._betsPerLane.getOrDefault(lane, 0L) + amount;
      this._betsPerLane.put(lane, sum);
      if (saveOnDb) {
         this.saveBet(lane, sum);
      }
   }

   protected void calculateOdds() {
      this._odds.clear();
      Map<Integer, Long> sortedLanes = new TreeMap<>(this._betsPerLane);
      long sumOfAllLanes = 0L;

      for(long amount : sortedLanes.values()) {
         sumOfAllLanes += amount;
      }

      for(long amount : sortedLanes.values()) {
         this._odds.add(amount == 0L ? 0.0 : Math.max(1.25, (double)sumOfAllLanes * 0.7 / (double)amount));
      }
   }

   public static MonsterRaceManager getInstance() {
      return MonsterRaceManager.SingletonHolder.instance;
   }

   private class Announcement implements Runnable {
      public Announcement() {
      }

      @Override
      public void run() {
         if (MonsterRaceManager.this._manager != null) {
            if (MonsterRaceManager.this._finalCountdown > 1200) {
               MonsterRaceManager.this._finalCountdown = 0;
            }

            switch(MonsterRaceManager.this._finalCountdown) {
               case 0:
                  MonsterRaceManager.this.newRace();
                  MonsterRaceManager.this.newSpeeds();
                  MonsterRaceManager.this._state = MonsterRaceManager.RaceState.ACCEPTING_BETS;
                  MonsterRaceManager.this._packet = new MonRaceInfo(
                     MonsterRaceManager.CODES[0][0],
                     MonsterRaceManager.CODES[0][1],
                     MonsterRaceManager.this.getMonsters(),
                     MonsterRaceManager.this.getSpeeds()
                  );
                  Broadcast.toKnownPlayersInRadius(
                     MonsterRaceManager.this._manager,
                     4000,
                     MonsterRaceManager.this._packet,
                     SystemMessage.getSystemMessage(SystemMessageId.MONSRACE_TICKETS_AVAILABLE_FOR_S1_RACE).addNumber(MonsterRaceManager.this._raceNumber)
                  );
                  break;
               case 30:
               case 60:
               case 90:
               case 120:
               case 150:
               case 180:
               case 210:
               case 240:
               case 270:
               case 330:
               case 360:
               case 390:
               case 420:
               case 450:
               case 480:
               case 510:
               case 540:
               case 570:
               case 630:
               case 660:
               case 690:
               case 720:
               case 750:
               case 780:
               case 810:
               case 870:
                  Broadcast.toKnownPlayersInRadius(
                     MonsterRaceManager.this._manager,
                     4000,
                     SystemMessage.getSystemMessage(SystemMessageId.MONSRACE_TICKETS_NOW_AVAILABLE_FOR_S1_RACE).addNumber(MonsterRaceManager.this._raceNumber)
                  );
                  break;
               case 300:
                  Broadcast.toKnownPlayersInRadius(
                     MonsterRaceManager.this._manager,
                     4000,
                     SystemMessage.getSystemMessage(SystemMessageId.MONSRACE_TICKETS_NOW_AVAILABLE_FOR_S1_RACE).addNumber(MonsterRaceManager.this._raceNumber),
                     SystemMessage.getSystemMessage(SystemMessageId.MONSRACE_TICKETS_STOP_IN_S1_MINUTES).addNumber(10)
                  );
                  break;
               case 600:
                  Broadcast.toKnownPlayersInRadius(
                     MonsterRaceManager.this._manager,
                     4000,
                     SystemMessage.getSystemMessage(SystemMessageId.MONSRACE_TICKETS_NOW_AVAILABLE_FOR_S1_RACE).addNumber(MonsterRaceManager.this._raceNumber),
                     SystemMessage.getSystemMessage(SystemMessageId.MONSRACE_TICKETS_STOP_IN_S1_MINUTES).addNumber(5)
                  );
                  break;
               case 840:
                  Broadcast.toKnownPlayersInRadius(
                     MonsterRaceManager.this._manager,
                     4000,
                     SystemMessage.getSystemMessage(SystemMessageId.MONSRACE_TICKETS_NOW_AVAILABLE_FOR_S1_RACE).addNumber(MonsterRaceManager.this._raceNumber),
                     SystemMessage.getSystemMessage(SystemMessageId.MONSRACE_TICKETS_STOP_IN_S1_MINUTES).addNumber(1)
                  );
                  break;
               case 900:
                  MonsterRaceManager.this._state = MonsterRaceManager.RaceState.WAITING;
                  MonsterRaceManager.this.calculateOdds();
                  Broadcast.toKnownPlayersInRadius(
                     MonsterRaceManager.this._manager,
                     4000,
                     SystemMessage.getSystemMessage(SystemMessageId.MONSRACE_TICKETS_NOW_AVAILABLE_FOR_S1_RACE).addNumber(MonsterRaceManager.this._raceNumber),
                     SystemMessage.getSystemMessage(SystemMessageId.MONSRACE_S1_TICKET_SALES_CLOSED)
                  );
                  break;
               case 960:
               case 1020:
                  int minutes = MonsterRaceManager.this._finalCountdown == 960 ? 2 : 1;
                  Broadcast.toKnownPlayersInRadius(
                     MonsterRaceManager.this._manager,
                     4000,
                     SystemMessage.getSystemMessage(SystemMessageId.MONSRACE_S2_BEGINS_IN_S1_MINUTES).addNumber(minutes)
                  );
                  break;
               case 1050:
                  Broadcast.toKnownPlayersInRadius(
                     MonsterRaceManager.this._manager, 4000, SystemMessage.getSystemMessage(SystemMessageId.MONSRACE_S1_BEGINS_IN_30_SECONDS)
                  );
                  break;
               case 1070:
                  Broadcast.toKnownPlayersInRadius(
                     MonsterRaceManager.this._manager, 4000, SystemMessage.getSystemMessage(SystemMessageId.MONSRACE_S1_COUNTDOWN_IN_FIVE_SECONDS)
                  );
                  break;
               case 1075:
               case 1076:
               case 1077:
               case 1078:
               case 1079:
                  int seconds = 1080 - MonsterRaceManager.this._finalCountdown;
                  Broadcast.toKnownPlayersInRadius(
                     MonsterRaceManager.this._manager, 4000, SystemMessage.getSystemMessage(SystemMessageId.MONSRACE_BEGINS_IN_S1_SECONDS).addNumber(seconds)
                  );
                  break;
               case 1080:
                  MonsterRaceManager.this._state = MonsterRaceManager.RaceState.STARTING_RACE;
                  MonsterRaceManager.this._packet = new MonRaceInfo(
                     MonsterRaceManager.CODES[1][0],
                     MonsterRaceManager.CODES[1][1],
                     MonsterRaceManager.this.getMonsters(),
                     MonsterRaceManager.this.getSpeeds()
                  );
                  Broadcast.toKnownPlayersInRadius(
                     MonsterRaceManager.this._manager,
                     4000,
                     SystemMessage.getSystemMessage(SystemMessageId.MONSRACE_RACE_START),
                     MonsterRaceManager.SOUND_1,
                     MonsterRaceManager.SOUND_2,
                     MonsterRaceManager.this._packet
                  );
                  break;
               case 1085:
                  MonsterRaceManager.this._packet = new MonRaceInfo(
                     MonsterRaceManager.CODES[2][0],
                     MonsterRaceManager.CODES[2][1],
                     MonsterRaceManager.this.getMonsters(),
                     MonsterRaceManager.this.getSpeeds()
                  );
                  Broadcast.toKnownPlayersInRadius(MonsterRaceManager.this._manager, 4000, MonsterRaceManager.this._packet);
                  break;
               case 1115:
                  MonsterRaceManager.this._state = MonsterRaceManager.RaceState.RACE_END;
                  HistoryInfoTemplate info = MonsterRaceManager.this._history.get(MonsterRaceManager.this._history.size() - 1);
                  info.setFirst(MonsterRaceManager.this.getFirstPlace());
                  info.setSecond(MonsterRaceManager.this.getSecondPlace());
                  info.setOddRate(MonsterRaceManager.this._odds.get(MonsterRaceManager.this.getFirstPlace() - 1));
                  MonsterRaceManager.this.saveHistory(info);
                  MonsterRaceManager.this.clearBets();
                  Broadcast.toKnownPlayersInRadius(
                     MonsterRaceManager.this._manager,
                     4000,
                     SystemMessage.getSystemMessage(SystemMessageId.MONSRACE_FIRST_PLACE_S1_SECOND_S2)
                        .addNumber(MonsterRaceManager.this.getFirstPlace())
                        .addNumber(MonsterRaceManager.this.getSecondPlace()),
                     SystemMessage.getSystemMessage(SystemMessageId.MONSRACE_S1_RACE_END).addNumber(MonsterRaceManager.this._raceNumber)
                  );
                  MonsterRaceManager.this._raceNumber++;
                  break;
               case 1140:
                  Broadcast.toKnownPlayersInRadius(
                     MonsterRaceManager.this._manager,
                     4000,
                     new DeleteObject(MonsterRaceManager.this.getMonsters()[0]),
                     new DeleteObject(MonsterRaceManager.this.getMonsters()[1]),
                     new DeleteObject(MonsterRaceManager.this.getMonsters()[2]),
                     new DeleteObject(MonsterRaceManager.this.getMonsters()[3]),
                     new DeleteObject(MonsterRaceManager.this.getMonsters()[4]),
                     new DeleteObject(MonsterRaceManager.this.getMonsters()[5]),
                     new DeleteObject(MonsterRaceManager.this.getMonsters()[6]),
                     new DeleteObject(MonsterRaceManager.this.getMonsters()[7])
                  );
            }

            MonsterRaceManager.this._finalCountdown = MonsterRaceManager.this._finalCountdown + 1;
         }
      }
   }

   public static enum RaceState {
      ACCEPTING_BETS,
      WAITING,
      STARTING_RACE,
      RACE_END;
   }

   private static class SingletonHolder {
      protected static final MonsterRaceManager instance = new MonsterRaceManager();
   }
}
