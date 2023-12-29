package l2e.gameserver;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import l2e.commons.util.Broadcast;
import l2e.gameserver.data.parser.SkillsParser;
import l2e.gameserver.data.parser.SpawnParser;
import l2e.gameserver.database.DatabaseFactory;
import l2e.gameserver.instancemanager.CastleManager;
import l2e.gameserver.instancemanager.QuestManager;
import l2e.gameserver.instancemanager.RaidBossSpawnManager;
import l2e.gameserver.instancemanager.TerritoryWarManager;
import l2e.gameserver.model.AutoSpawnHandler;
import l2e.gameserver.model.TeleportWhereType;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.entity.Castle;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.stats.StatsSet;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.SSQInfo;
import l2e.gameserver.network.serverpackets.SystemMessage;

public class SevenSigns {
   protected static final Logger _log = Logger.getLogger(SevenSigns.class.getName());
   public static final String SEVEN_SIGNS_HTML_PATH = "data/html/seven_signs/";
   public static final int CABAL_NULL = 0;
   public static final int CABAL_DUSK = 1;
   public static final int CABAL_DAWN = 2;
   public static final int SEAL_NULL = 0;
   public static final int SEAL_AVARICE = 1;
   public static final int SEAL_GNOSIS = 2;
   public static final int SEAL_STRIFE = 3;
   public static final int PERIOD_COMP_RECRUITING = 0;
   public static final int PERIOD_COMPETITION = 1;
   public static final int PERIOD_COMP_RESULTS = 2;
   public static final int PERIOD_SEAL_VALIDATION = 3;
   public static final int PERIOD_START_HOUR = 18;
   public static final int PERIOD_START_MINS = 0;
   public static final int PERIOD_START_DAY = 2;
   public static final int PERIOD_MINOR_LENGTH = 900000;
   public static final int PERIOD_MAJOR_LENGTH = 603900000;
   public static final int RECORD_SEVEN_SIGNS_ID = 5707;
   public static final int RECORD_SEVEN_SIGNS_COST = 500;
   public static final int ORATOR_NPC_ID = 31094;
   public static final int PREACHER_NPC_ID = 31093;
   public static final int MAMMON_MERCHANT_ID = 31113;
   public static final int MAMMON_BLACKSMITH_ID = 31126;
   public static final int MAMMON_MARKETEER_ID = 31092;
   public static final int SPIRIT_IN_ID = 31111;
   public static final int SPIRIT_OUT_ID = 31112;
   public static final int LILITH_NPC_ID = 25283;
   public static final int ANAKIM_NPC_ID = 25286;
   public static final int CREST_OF_DAWN_ID = 31170;
   public static final int CREST_OF_DUSK_ID = 31171;
   public static final int SEAL_STONE_BLUE_ID = 6360;
   public static final int SEAL_STONE_GREEN_ID = 6361;
   public static final int SEAL_STONE_RED_ID = 6362;
   public static final int[] SEAL_STONE_IDS = new int[]{6360, 6361, 6362};
   public static final int SEAL_STONE_BLUE_VALUE = 3;
   public static final int SEAL_STONE_GREEN_VALUE = 5;
   public static final int SEAL_STONE_RED_VALUE = 10;
   public static final int BLUE_CONTRIB_POINTS = 3;
   public static final int GREEN_CONTRIB_POINTS = 5;
   public static final int RED_CONTRIB_POINTS = 10;
   private final Calendar _nextPeriodChange = Calendar.getInstance();
   protected int _activePeriod;
   protected int _currentCycle;
   protected double _dawnStoneScore;
   protected double _duskStoneScore;
   protected int _dawnFestivalScore;
   protected int _duskFestivalScore;
   protected int _compWinner;
   protected int _previousWinner;
   protected Calendar _lastSave = Calendar.getInstance();
   protected Map<Integer, StatsSet> _signsPlayerData = new LinkedHashMap<>();
   private final Map<Integer, Integer> _signsSealOwners = new LinkedHashMap<>();
   private final Map<Integer, Integer> _signsDuskSealTotals = new LinkedHashMap<>();
   private final Map<Integer, Integer> _signsDawnSealTotals = new LinkedHashMap<>();
   private AutoSpawnHandler.AutoSpawnInstance _merchantSpawn;
   private AutoSpawnHandler.AutoSpawnInstance _blacksmithSpawn;
   private AutoSpawnHandler.AutoSpawnInstance _spiritInSpawn;
   private AutoSpawnHandler.AutoSpawnInstance _spiritOutSpawn;
   private AutoSpawnHandler.AutoSpawnInstance _lilithSpawn;
   private AutoSpawnHandler.AutoSpawnInstance _anakimSpawn;
   private static final String LOAD_DATA = "SELECT charId, cabal, seal, red_stones, green_stones, blue_stones, ancient_adena_amount, contribution_score FROM seven_signs";
   private static final String LOAD_STATUS = "SELECT * FROM seven_signs_status WHERE id=0";
   private static final String INSERT_PLAYER = "INSERT INTO seven_signs (charId, cabal, seal) VALUES (?,?,?)";
   private static final String UPDATE_PLAYER = "UPDATE seven_signs SET cabal=?, seal=?, red_stones=?, green_stones=?, blue_stones=?, ancient_adena_amount=?, contribution_score=? WHERE charId=?";
   private static final String UPDATE_STATUS = "UPDATE seven_signs_status SET current_cycle=?, active_period=?, previous_winner=?, dawn_stone_score=?, dawn_festival_score=?, dusk_stone_score=?, dusk_festival_score=?, avarice_owner=?, gnosis_owner=?, strife_owner=?, avarice_dawn_score=?, gnosis_dawn_score=?, strife_dawn_score=?, avarice_dusk_score=?, gnosis_dusk_score=?, strife_dusk_score=?, festival_cycle=?, accumulated_bonus0=?, accumulated_bonus1=?, accumulated_bonus2=?,accumulated_bonus3=?, accumulated_bonus4=?, date=? WHERE id=0";

   protected void despawnAll() {
      SpawnParser.getInstance().despawnGroup("dawn_spawn");
      SpawnParser.getInstance().despawnGroup("dusk_spawn");
      SpawnParser.getInstance().despawnGroup("dawn_victory_spawn");
      SpawnParser.getInstance().despawnGroup("dusk_victory_spawn");
   }

   private void changePeriodSpawn() {
      int mode = 0;
      if (this.isSealValidationPeriod()) {
         mode = this.getCabalHighestScore();
      }

      this.despawnAll();
      switch(mode) {
         case 0:
            SpawnParser.getInstance().spawnGroup("dawn_spawn");
            SpawnParser.getInstance().spawnGroup("dusk_spawn");
            break;
         case 1:
            SpawnParser.getInstance().spawnGroup("dusk_spawn");
            SpawnParser.getInstance().spawnGroup("dusk_victory_spawn");
            break;
         case 2:
            SpawnParser.getInstance().spawnGroup("dawn_spawn");
            SpawnParser.getInstance().spawnGroup("dawn_victory_spawn");
      }
   }

   protected SevenSigns() {
      try {
         this.restoreSevenSignsData();
      } catch (Exception var11) {
         _log.log(Level.SEVERE, "SevenSigns: Failed to load configuration: " + var11.getMessage(), (Throwable)var11);
      }

      _log.info("SevenSigns: Currently in the " + this.getCurrentPeriodName() + " period!");
      this.initializeSeals();
      if (this.isSealValidationPeriod()) {
         if (this.getCabalHighestScore() == 0) {
            _log.info("SevenSigns: The competition ended with a tie last week.");
         } else {
            _log.info("SevenSigns: The " + getCabalName(this.getCabalHighestScore()) + " were victorious last week.");
         }
      } else if (this.getCabalHighestScore() == 0) {
         _log.info("SevenSigns: If trend continues, will end in a tie this week.");
      } else {
         _log.info("SevenSigns: The " + getCabalName(this.getCabalHighestScore()) + " are in the lead this week.");
      }

      long milliToChange = 0L;
      if (this.isNextPeriodChangeInPast()) {
         _log.info("SevenSigns: Next period change was in the past (server was offline), changing periods now!");
      } else {
         this.setCalendarForNextPeriodChange();
         milliToChange = this.getMilliToPeriodChange();
      }

      SevenSigns.SevenSignsPeriodChange sspc = new SevenSigns.SevenSignsPeriodChange();
      ThreadPoolManager.getInstance().schedule(sspc, milliToChange);
      double numSecs = (double)(milliToChange / 1000L % 60L);
      double countDown = ((double)milliToChange / 1000.0 - numSecs) / 60.0;
      int numMins = (int)Math.floor(countDown % 60.0);
      countDown = (countDown - (double)numMins) / 60.0;
      int numHours = (int)Math.floor(countDown % 24.0);
      int numDays = (int)Math.floor((countDown - (double)numHours) / 24.0);
      this.changePeriodSpawn();
      _log.info("SevenSigns: Next period begins in " + numDays + " days, " + numHours + " hours and " + numMins + " mins.");
   }

   private boolean isNextPeriodChangeInPast() {
      Calendar lastPeriodChange = Calendar.getInstance();
      switch(this.getCurrentPeriod()) {
         case 0:
         case 2:
            lastPeriodChange.setTimeInMillis(this._lastSave.getTimeInMillis() + 900000L);
            break;
         case 1:
         case 3:
            lastPeriodChange.set(7, 2);
            lastPeriodChange.set(11, 18);
            lastPeriodChange.set(12, 0);
            lastPeriodChange.set(13, 0);
            if (Calendar.getInstance().before(lastPeriodChange)) {
               lastPeriodChange.add(10, -168);
            }
      }

      return this._lastSave.getTimeInMillis() > 7L && this._lastSave.before(lastPeriodChange);
   }

   public void spawnSevenSignsNPC() {
      this._merchantSpawn = AutoSpawnHandler.getInstance().getAutoSpawnInstance(31113, false);
      this._blacksmithSpawn = AutoSpawnHandler.getInstance().getAutoSpawnInstance(31126, false);
      this._spiritInSpawn = AutoSpawnHandler.getInstance().getAutoSpawnInstance(31111, false);
      this._spiritOutSpawn = AutoSpawnHandler.getInstance().getAutoSpawnInstance(31112, false);
      this._lilithSpawn = AutoSpawnHandler.getInstance().getAutoSpawnInstance(25283, false);
      this._anakimSpawn = AutoSpawnHandler.getInstance().getAutoSpawnInstance(25286, false);
      List<AutoSpawnHandler.AutoSpawnInstance> marketeerSpawns = AutoSpawnHandler.getInstance().getAutoSpawnInstances(31092);
      List<AutoSpawnHandler.AutoSpawnInstance> crestofdawnspawns = AutoSpawnHandler.getInstance().getAutoSpawnInstances(31170);
      List<AutoSpawnHandler.AutoSpawnInstance> crestofduskspawns = AutoSpawnHandler.getInstance().getAutoSpawnInstances(31171);
      List<AutoSpawnHandler.AutoSpawnInstance> oratorSpawns = AutoSpawnHandler.getInstance().getAutoSpawnInstances(31094);
      List<AutoSpawnHandler.AutoSpawnInstance> preacherSpawns = AutoSpawnHandler.getInstance().getAutoSpawnInstances(31093);
      if (!this.isSealValidationPeriod() && !this.isCompResultsPeriod()) {
         AutoSpawnHandler.getInstance().setSpawnActive(this._merchantSpawn, false);
         AutoSpawnHandler.getInstance().setSpawnActive(this._blacksmithSpawn, false);
         AutoSpawnHandler.getInstance().setSpawnActive(this._lilithSpawn, false);
         AutoSpawnHandler.getInstance().setSpawnActive(this._anakimSpawn, false);
         AutoSpawnHandler.getInstance().setSpawnActive(this._spiritInSpawn, false);
         AutoSpawnHandler.getInstance().setSpawnActive(this._spiritOutSpawn, false);

         for(AutoSpawnHandler.AutoSpawnInstance dawnCrest : crestofdawnspawns) {
            AutoSpawnHandler.getInstance().setSpawnActive(dawnCrest, false);
         }

         for(AutoSpawnHandler.AutoSpawnInstance duskCrest : crestofduskspawns) {
            AutoSpawnHandler.getInstance().setSpawnActive(duskCrest, false);
         }

         for(AutoSpawnHandler.AutoSpawnInstance spawnInst : oratorSpawns) {
            AutoSpawnHandler.getInstance().setSpawnActive(spawnInst, false);
         }

         for(AutoSpawnHandler.AutoSpawnInstance spawnInst : preacherSpawns) {
            AutoSpawnHandler.getInstance().setSpawnActive(spawnInst, false);
         }

         for(AutoSpawnHandler.AutoSpawnInstance spawnInst : marketeerSpawns) {
            AutoSpawnHandler.getInstance().setSpawnActive(spawnInst, false);
         }
      } else {
         for(AutoSpawnHandler.AutoSpawnInstance spawnInst : marketeerSpawns) {
            AutoSpawnHandler.getInstance().setSpawnActive(spawnInst, true);
         }

         if (this.getSealOwner(2) == this.getCabalHighestScore() && this.getSealOwner(2) != 0) {
            if (!Config.ANNOUNCE_MAMMON_SPAWN) {
               this._blacksmithSpawn.setBroadcast(false);
            }

            if (!AutoSpawnHandler.getInstance().getAutoSpawnInstance(this._blacksmithSpawn.getObjectId(), true).isSpawnActive()) {
               AutoSpawnHandler.getInstance().setSpawnActive(this._blacksmithSpawn, true);
            }

            for(AutoSpawnHandler.AutoSpawnInstance spawnInst : oratorSpawns) {
               if (!AutoSpawnHandler.getInstance().getAutoSpawnInstance(spawnInst.getObjectId(), true).isSpawnActive()) {
                  AutoSpawnHandler.getInstance().setSpawnActive(spawnInst, true);
               }
            }

            for(AutoSpawnHandler.AutoSpawnInstance spawnInst : preacherSpawns) {
               if (!AutoSpawnHandler.getInstance().getAutoSpawnInstance(spawnInst.getObjectId(), true).isSpawnActive()) {
                  AutoSpawnHandler.getInstance().setSpawnActive(spawnInst, true);
               }
            }
         } else {
            AutoSpawnHandler.getInstance().setSpawnActive(this._blacksmithSpawn, false);

            for(AutoSpawnHandler.AutoSpawnInstance spawnInst : oratorSpawns) {
               AutoSpawnHandler.getInstance().setSpawnActive(spawnInst, false);
            }

            for(AutoSpawnHandler.AutoSpawnInstance spawnInst : preacherSpawns) {
               AutoSpawnHandler.getInstance().setSpawnActive(spawnInst, false);
            }
         }

         if (this.getSealOwner(1) == this.getCabalHighestScore() && this.getSealOwner(1) != 0) {
            if (!Config.ANNOUNCE_MAMMON_SPAWN) {
               this._merchantSpawn.setBroadcast(false);
            }

            if (!AutoSpawnHandler.getInstance().getAutoSpawnInstance(this._merchantSpawn.getObjectId(), true).isSpawnActive()) {
               AutoSpawnHandler.getInstance().setSpawnActive(this._merchantSpawn, true);
            }

            if (!AutoSpawnHandler.getInstance().getAutoSpawnInstance(this._spiritInSpawn.getObjectId(), true).isSpawnActive()) {
               AutoSpawnHandler.getInstance().setSpawnActive(this._spiritInSpawn, true);
            }

            if (!AutoSpawnHandler.getInstance().getAutoSpawnInstance(this._spiritOutSpawn.getObjectId(), true).isSpawnActive()) {
               AutoSpawnHandler.getInstance().setSpawnActive(this._spiritOutSpawn, true);
            }

            switch(this.getCabalHighestScore()) {
               case 1:
                  if (!AutoSpawnHandler.getInstance().getAutoSpawnInstance(this._anakimSpawn.getObjectId(), true).isSpawnActive()) {
                     AutoSpawnHandler.getInstance().setSpawnActive(this._anakimSpawn, true);
                  }

                  AutoSpawnHandler.getInstance().setSpawnActive(this._lilithSpawn, false);

                  for(AutoSpawnHandler.AutoSpawnInstance duskCrest : crestofduskspawns) {
                     if (!AutoSpawnHandler.getInstance().getAutoSpawnInstance(duskCrest.getObjectId(), true).isSpawnActive()) {
                        AutoSpawnHandler.getInstance().setSpawnActive(duskCrest, true);
                     }
                  }

                  for(AutoSpawnHandler.AutoSpawnInstance dawnCrest : crestofdawnspawns) {
                     AutoSpawnHandler.getInstance().setSpawnActive(dawnCrest, false);
                  }
                  break;
               case 2:
                  if (!AutoSpawnHandler.getInstance().getAutoSpawnInstance(this._lilithSpawn.getObjectId(), true).isSpawnActive()) {
                     AutoSpawnHandler.getInstance().setSpawnActive(this._lilithSpawn, true);
                  }

                  AutoSpawnHandler.getInstance().setSpawnActive(this._anakimSpawn, false);

                  for(AutoSpawnHandler.AutoSpawnInstance dawnCrest : crestofdawnspawns) {
                     if (!AutoSpawnHandler.getInstance().getAutoSpawnInstance(dawnCrest.getObjectId(), true).isSpawnActive()) {
                        AutoSpawnHandler.getInstance().setSpawnActive(dawnCrest, true);
                     }
                  }

                  for(AutoSpawnHandler.AutoSpawnInstance duskCrest : crestofduskspawns) {
                     AutoSpawnHandler.getInstance().setSpawnActive(duskCrest, false);
                  }
            }
         } else {
            AutoSpawnHandler.getInstance().setSpawnActive(this._merchantSpawn, false);
            AutoSpawnHandler.getInstance().setSpawnActive(this._spiritInSpawn, false);
            AutoSpawnHandler.getInstance().setSpawnActive(this._spiritOutSpawn, false);
            AutoSpawnHandler.getInstance().setSpawnActive(this._lilithSpawn, false);
            AutoSpawnHandler.getInstance().setSpawnActive(this._anakimSpawn, false);

            for(AutoSpawnHandler.AutoSpawnInstance dawnCrest : crestofdawnspawns) {
               AutoSpawnHandler.getInstance().setSpawnActive(dawnCrest, false);
            }

            for(AutoSpawnHandler.AutoSpawnInstance duskCrest : crestofduskspawns) {
               AutoSpawnHandler.getInstance().setSpawnActive(duskCrest, false);
            }
         }
      }
   }

   public static SevenSigns getInstance() {
      return SevenSigns.SingletonHolder._instance;
   }

   public static long calcContributionScore(long blueCount, long greenCount, long redCount) {
      long contrib = blueCount * 3L;
      contrib += greenCount * 5L;
      return contrib + redCount * 10L;
   }

   public static long calcAncientAdenaReward(long blueCount, long greenCount, long redCount) {
      long reward = blueCount * 3L;
      reward += greenCount * 5L;
      return reward + redCount * 10L;
   }

   public static final String getCabalShortName(int cabal) {
      switch(cabal) {
         case 1:
            return "dusk";
         case 2:
            return "dawn";
         default:
            return "No Cabal";
      }
   }

   public static int getCabalNumber(String cabal) {
      switch(cabal) {
         case "dawn":
            return 2;
         case "dusk":
            return 1;
         default:
            return 0;
      }
   }

   public static final String getCabalName(int cabal) {
      switch(cabal) {
         case 1:
            return "Revolutionaries of Dusk";
         case 2:
            return "Lords of Dawn";
         default:
            return "No Cabal";
      }
   }

   public static final String getSealName(int seal, boolean shortName) {
      String sealName = !shortName ? "Seal of " : "";
      switch(seal) {
         case 1:
            sealName = sealName + "Avarice";
            break;
         case 2:
            sealName = sealName + "Gnosis";
            break;
         case 3:
            sealName = sealName + "Strife";
      }

      return sealName;
   }

   public final int getCurrentCycle() {
      return this._currentCycle;
   }

   public final int getCurrentPeriod() {
      return this._activePeriod;
   }

   private final int getDaysToPeriodChange() {
      int numDays = this._nextPeriodChange.get(7) - 2;
      return numDays < 0 ? 0 - numDays : 7 - numDays;
   }

   public final long getMilliToPeriodChange() {
      return this._nextPeriodChange.getTimeInMillis() - System.currentTimeMillis();
   }

   protected void setCalendarForNextPeriodChange() {
      switch(this.getCurrentPeriod()) {
         case 0:
         case 2:
            this._nextPeriodChange.add(14, 900000);
            break;
         case 1:
         case 3:
            int daysToChange = this.getDaysToPeriodChange();
            if (daysToChange == 7) {
               if (this._nextPeriodChange.get(11) < 18) {
                  daysToChange = 0;
               } else if (this._nextPeriodChange.get(11) == 18 && this._nextPeriodChange.get(12) < 0) {
                  daysToChange = 0;
               }
            }

            if (daysToChange > 0) {
               this._nextPeriodChange.add(5, daysToChange);
            }

            this._nextPeriodChange.set(11, 18);
            this._nextPeriodChange.set(12, 0);
      }

      _log.info("SevenSigns: Next period change set to " + this._nextPeriodChange.getTime());
   }

   public final String getCurrentPeriodName() {
      String periodName = null;
      switch(this._activePeriod) {
         case 0:
            periodName = "Quest Event Initialization";
            break;
         case 1:
            periodName = "Competition (Quest Event)";
            break;
         case 2:
            periodName = "Quest Event Results";
            break;
         case 3:
            periodName = "Seal Validation";
      }

      return periodName;
   }

   public final boolean isCompetitionPeriod() {
      return this._activePeriod == 1;
   }

   public final boolean isSealValidationPeriod() {
      return this._activePeriod == 3;
   }

   public final boolean isCompResultsPeriod() {
      return this._activePeriod == 2;
   }

   public boolean isDateInSealValidPeriod(Calendar date) {
      long nextPeriodChange = this.getMilliToPeriodChange();
      long nextQuestStart = 0L;
      long nextValidStart = 0L;
      long tillDate = date.getTimeInMillis() - Calendar.getInstance().getTimeInMillis();

      while(1209600000L < tillDate) {
         tillDate -= 1209600000L;
      }

      while(tillDate < 0L) {
         tillDate += 1209600000L;
      }

      switch(this.getCurrentPeriod()) {
         case 0:
            nextValidStart = nextPeriodChange + 603900000L;
            nextQuestStart = nextValidStart + 603900000L + 900000L;
            break;
         case 1:
            nextValidStart = nextPeriodChange;
            nextQuestStart = nextPeriodChange + 603900000L + 900000L;
            break;
         case 2:
            nextQuestStart = nextPeriodChange + 603900000L;
            nextValidStart = nextQuestStart + 603900000L + 900000L;
            break;
         case 3:
            nextQuestStart = nextPeriodChange;
            nextValidStart = nextPeriodChange + 603900000L + 900000L;
      }

      return (nextQuestStart >= tillDate || tillDate >= nextValidStart)
         && (nextValidStart >= nextQuestStart || tillDate >= nextValidStart && nextQuestStart >= tillDate);
   }

   public final int getCurrentScore(int cabal) {
      double totalStoneScore = this._dawnStoneScore + this._duskStoneScore;
      switch(cabal) {
         case 0:
            return 0;
         case 1:
            return Math.round((float)(this._duskStoneScore / ((float)totalStoneScore == 0.0F ? 1.0 : totalStoneScore)) * 500.0F) + this._duskFestivalScore;
         case 2:
            return Math.round((float)(this._dawnStoneScore / ((float)totalStoneScore == 0.0F ? 1.0 : totalStoneScore)) * 500.0F) + this._dawnFestivalScore;
         default:
            return 0;
      }
   }

   public final double getCurrentStoneScore(int cabal) {
      switch(cabal) {
         case 0:
            return 0.0;
         case 1:
            return this._duskStoneScore;
         case 2:
            return this._dawnStoneScore;
         default:
            return 0.0;
      }
   }

   public final int getCurrentFestivalScore(int cabal) {
      switch(cabal) {
         case 0:
            return 0;
         case 1:
            return this._duskFestivalScore;
         case 2:
            return this._dawnFestivalScore;
         default:
            return 0;
      }
   }

   public final int getCabalHighestScore() {
      if (this.getCurrentScore(1) == this.getCurrentScore(2)) {
         return 0;
      } else {
         return this.getCurrentScore(1) > this.getCurrentScore(2) ? 1 : 2;
      }
   }

   public final int getSealOwner(int seal) {
      return this._signsSealOwners.get(seal);
   }

   public final int getSealProportion(int seal, int cabal) {
      if (cabal == 0) {
         return 0;
      } else {
         return cabal == 1 ? this._signsDuskSealTotals.get(seal) : this._signsDawnSealTotals.get(seal);
      }
   }

   public final int getTotalMembers(int cabal) {
      int cabalMembers = 0;
      String cabalName = getCabalShortName(cabal);

      for(StatsSet sevenDat : this._signsPlayerData.values()) {
         if (sevenDat != null && sevenDat.getString("cabal").equals(cabalName)) {
            ++cabalMembers;
         }
      }

      return cabalMembers;
   }

   public int getPlayerStoneContrib(int objectId) {
      StatsSet currPlayer = this._signsPlayerData.get(objectId);
      if (currPlayer == null) {
         return 0;
      } else {
         int stoneCount = 0;
         stoneCount += currPlayer.getInteger("red_stones");
         stoneCount += currPlayer.getInteger("green_stones");
         return stoneCount + currPlayer.getInteger("blue_stones");
      }
   }

   public int getPlayerContribScore(int objectId) {
      StatsSet currPlayer = this._signsPlayerData.get(objectId);
      return currPlayer == null ? 0 : currPlayer.getInteger("contribution_score");
   }

   public int getPlayerAdenaCollect(int objectId) {
      StatsSet currPlayer = this._signsPlayerData.get(objectId);
      return currPlayer == null ? 0 : currPlayer.getInteger("ancient_adena_amount");
   }

   public int getPlayerSeal(int objectId) {
      StatsSet currPlayer = this._signsPlayerData.get(objectId);
      return currPlayer == null ? 0 : currPlayer.getInteger("seal");
   }

   public int getPlayerCabal(int objectId) {
      StatsSet currPlayer = this._signsPlayerData.get(objectId);
      if (currPlayer == null) {
         return 0;
      } else {
         String playerCabal = currPlayer.getString("cabal");
         if (playerCabal.equalsIgnoreCase("dawn")) {
            return 2;
         } else {
            return playerCabal.equalsIgnoreCase("dusk") ? 1 : 0;
         }
      }
   }

   protected void restoreSevenSignsData() {
      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         Statement s = con.createStatement();
         ResultSet rs = s.executeQuery(
            "SELECT charId, cabal, seal, red_stones, green_stones, blue_stones, ancient_adena_amount, contribution_score FROM seven_signs"
         );
      ) {
         StatsSet sevenDat = null;

         while(rs.next()) {
            int charObjId = rs.getInt("charId");
            sevenDat = new StatsSet();
            sevenDat.set("charId", charObjId);
            sevenDat.set("cabal", rs.getString("cabal"));
            sevenDat.set("seal", rs.getInt("seal"));
            sevenDat.set("red_stones", rs.getInt("red_stones"));
            sevenDat.set("green_stones", rs.getInt("green_stones"));
            sevenDat.set("blue_stones", rs.getInt("blue_stones"));
            sevenDat.set("ancient_adena_amount", rs.getDouble("ancient_adena_amount"));
            sevenDat.set("contribution_score", rs.getDouble("contribution_score"));
            this._signsPlayerData.put(charObjId, sevenDat);
         }

         try (
            Statement sx = con.createStatement();
            ResultSet rsx = sx.executeQuery("SELECT * FROM seven_signs_status WHERE id=0");
         ) {
            while(rsx.next()) {
               this._currentCycle = rsx.getInt("current_cycle");
               this._activePeriod = rsx.getInt("active_period");
               this._previousWinner = rsx.getInt("previous_winner");
               this._dawnStoneScore = rsx.getDouble("dawn_stone_score");
               this._dawnFestivalScore = rsx.getInt("dawn_festival_score");
               this._duskStoneScore = rsx.getDouble("dusk_stone_score");
               this._duskFestivalScore = rsx.getInt("dusk_festival_score");
               this._signsSealOwners.put(1, rsx.getInt("avarice_owner"));
               this._signsSealOwners.put(2, rsx.getInt("gnosis_owner"));
               this._signsSealOwners.put(3, rsx.getInt("strife_owner"));
               this._signsDawnSealTotals.put(1, rsx.getInt("avarice_dawn_score"));
               this._signsDawnSealTotals.put(2, rsx.getInt("gnosis_dawn_score"));
               this._signsDawnSealTotals.put(3, rsx.getInt("strife_dawn_score"));
               this._signsDuskSealTotals.put(1, rsx.getInt("avarice_dusk_score"));
               this._signsDuskSealTotals.put(2, rsx.getInt("gnosis_dusk_score"));
               this._signsDuskSealTotals.put(3, rsx.getInt("strife_dusk_score"));
               this._lastSave.setTimeInMillis(rsx.getLong("date"));
            }
         }
      } catch (SQLException var129) {
         _log.log(Level.SEVERE, "SevenSigns: Unable to load Seven Signs data from database: " + var129.getMessage(), (Throwable)var129);
      }
   }

   public void saveSevenSignsData() {
      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement ps = con.prepareStatement(
            "UPDATE seven_signs SET cabal=?, seal=?, red_stones=?, green_stones=?, blue_stones=?, ancient_adena_amount=?, contribution_score=? WHERE charId=?"
         );
      ) {
         for(StatsSet sevenDat : this._signsPlayerData.values()) {
            ps.setString(1, sevenDat.getString("cabal"));
            ps.setInt(2, sevenDat.getInteger("seal"));
            ps.setInt(3, sevenDat.getInteger("red_stones"));
            ps.setInt(4, sevenDat.getInteger("green_stones"));
            ps.setInt(5, sevenDat.getInteger("blue_stones"));
            ps.setDouble(6, sevenDat.getDouble("ancient_adena_amount"));
            ps.setDouble(7, sevenDat.getDouble("contribution_score"));
            ps.setInt(8, sevenDat.getInteger("charId"));
            ps.execute();
            ps.clearParameters();
         }
      } catch (SQLException var34) {
         _log.log(Level.SEVERE, "SevenSigns: Unable to save data to database: " + var34.getMessage(), (Throwable)var34);
      }
   }

   public final void saveSevenSignsData(int objectId) {
      StatsSet sevenDat = this._signsPlayerData.get(objectId);
      if (sevenDat != null) {
         try (
            Connection con = DatabaseFactory.getInstance().getConnection();
            PreparedStatement ps = con.prepareStatement(
               "UPDATE seven_signs SET cabal=?, seal=?, red_stones=?, green_stones=?, blue_stones=?, ancient_adena_amount=?, contribution_score=? WHERE charId=?"
            );
         ) {
            ps.setString(1, sevenDat.getString("cabal"));
            ps.setInt(2, sevenDat.getInteger("seal"));
            ps.setInt(3, sevenDat.getInteger("red_stones"));
            ps.setInt(4, sevenDat.getInteger("green_stones"));
            ps.setInt(5, sevenDat.getInteger("blue_stones"));
            ps.setDouble(6, sevenDat.getDouble("ancient_adena_amount"));
            ps.setDouble(7, sevenDat.getDouble("contribution_score"));
            ps.setInt(8, sevenDat.getInteger("charId"));
            ps.execute();
         } catch (SQLException var35) {
            _log.log(Level.SEVERE, "SevenSigns: Unable to save data to database: " + var35.getMessage(), (Throwable)var35);
         }
      }
   }

   public final void saveSevenSignsStatus() {
      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement ps = con.prepareStatement(
            "UPDATE seven_signs_status SET current_cycle=?, active_period=?, previous_winner=?, dawn_stone_score=?, dawn_festival_score=?, dusk_stone_score=?, dusk_festival_score=?, avarice_owner=?, gnosis_owner=?, strife_owner=?, avarice_dawn_score=?, gnosis_dawn_score=?, strife_dawn_score=?, avarice_dusk_score=?, gnosis_dusk_score=?, strife_dusk_score=?, festival_cycle=?, accumulated_bonus0=?, accumulated_bonus1=?, accumulated_bonus2=?,accumulated_bonus3=?, accumulated_bonus4=?, date=? WHERE id=0"
         );
      ) {
         ps.setInt(1, this._currentCycle);
         ps.setInt(2, this._activePeriod);
         ps.setInt(3, this._previousWinner);
         ps.setDouble(4, this._dawnStoneScore);
         ps.setInt(5, this._dawnFestivalScore);
         ps.setDouble(6, this._duskStoneScore);
         ps.setInt(7, this._duskFestivalScore);
         ps.setInt(8, this._signsSealOwners.get(1));
         ps.setInt(9, this._signsSealOwners.get(2));
         ps.setInt(10, this._signsSealOwners.get(3));
         ps.setInt(11, this._signsDawnSealTotals.get(1));
         ps.setInt(12, this._signsDawnSealTotals.get(2));
         ps.setInt(13, this._signsDawnSealTotals.get(3));
         ps.setInt(14, this._signsDuskSealTotals.get(1));
         ps.setInt(15, this._signsDuskSealTotals.get(2));
         ps.setInt(16, this._signsDuskSealTotals.get(3));
         ps.setInt(17, SevenSignsFestival.getInstance().getCurrentFestivalCycle());

         for(int i = 0; i < 5; ++i) {
            ps.setInt(18 + i, SevenSignsFestival.getInstance().getAccumulatedBonus(i));
         }

         this._lastSave = Calendar.getInstance();
         ps.setLong(23, this._lastSave.getTimeInMillis());
         ps.execute();
      } catch (SQLException var33) {
         _log.log(Level.SEVERE, "SevenSigns: Unable to save data to database: " + var33.getMessage(), (Throwable)var33);
      }
   }

   protected void resetPlayerData() {
      for(StatsSet sevenDat : this._signsPlayerData.values()) {
         int charObjId = sevenDat.getInteger("charId");
         sevenDat.set("cabal", "");
         sevenDat.set("seal", 0);
         sevenDat.set("contribution_score", 0);
         this._signsPlayerData.put(charObjId, sevenDat);
      }
   }

   public int setPlayerInfo(int objectId, int chosenCabal, int chosenSeal) {
      StatsSet currPlayerData = this._signsPlayerData.get(objectId);
      if (currPlayerData != null) {
         currPlayerData.set("cabal", getCabalShortName(chosenCabal));
         currPlayerData.set("seal", chosenSeal);
         this._signsPlayerData.put(objectId, currPlayerData);
      } else {
         currPlayerData = new StatsSet();
         currPlayerData.set("charId", objectId);
         currPlayerData.set("cabal", getCabalShortName(chosenCabal));
         currPlayerData.set("seal", chosenSeal);
         currPlayerData.set("red_stones", 0);
         currPlayerData.set("green_stones", 0);
         currPlayerData.set("blue_stones", 0);
         currPlayerData.set("ancient_adena_amount", 0);
         currPlayerData.set("contribution_score", 0);
         this._signsPlayerData.put(objectId, currPlayerData);

         try (
            Connection con = DatabaseFactory.getInstance().getConnection();
            PreparedStatement ps = con.prepareStatement("INSERT INTO seven_signs (charId, cabal, seal) VALUES (?,?,?)");
         ) {
            ps.setInt(1, objectId);
            ps.setString(2, getCabalShortName(chosenCabal));
            ps.setInt(3, chosenSeal);
            ps.execute();
         } catch (SQLException var37) {
            _log.log(Level.SEVERE, "SevenSigns: Failed to save data: " + var37.getMessage(), (Throwable)var37);
         }
      }

      if ("dawn".equals(currPlayerData.getString("cabal"))) {
         this._signsDawnSealTotals.put(chosenSeal, this._signsDawnSealTotals.get(chosenSeal) + 1);
      } else {
         this._signsDuskSealTotals.put(chosenSeal, this._signsDuskSealTotals.get(chosenSeal) + 1);
      }

      if (!Config.ALT_SEVENSIGNS_LAZY_UPDATE) {
         this.saveSevenSignsStatus();
      }

      return chosenCabal;
   }

   public int getAncientAdenaReward(int objectId, boolean removeReward) {
      StatsSet currPlayer = this._signsPlayerData.get(objectId);
      int rewardAmount = currPlayer.getInteger("ancient_adena_amount");
      currPlayer.set("red_stones", 0);
      currPlayer.set("green_stones", 0);
      currPlayer.set("blue_stones", 0);
      currPlayer.set("ancient_adena_amount", 0);
      if (removeReward) {
         this._signsPlayerData.put(objectId, currPlayer);
         if (!Config.ALT_SEVENSIGNS_LAZY_UPDATE) {
            this.saveSevenSignsData(objectId);
            this.saveSevenSignsStatus();
         }
      }

      return rewardAmount;
   }

   public long addPlayerStoneContrib(int objectId, long blueCount, long greenCount, long redCount) {
      StatsSet currPlayer = this._signsPlayerData.get(objectId);
      long contribScore = calcContributionScore(blueCount, greenCount, redCount);
      long totalAncientAdena = currPlayer.getLong("ancient_adena_amount") + calcAncientAdenaReward(blueCount, greenCount, redCount);
      long totalContribScore = currPlayer.getLong("contribution_score") + contribScore;
      if (totalContribScore > (long)Config.ALT_MAXIMUM_PLAYER_CONTRIB) {
         return -1L;
      } else {
         currPlayer.set("red_stones", (long)currPlayer.getInteger("red_stones") + redCount);
         currPlayer.set("green_stones", (long)currPlayer.getInteger("green_stones") + greenCount);
         currPlayer.set("blue_stones", (long)currPlayer.getInteger("blue_stones") + blueCount);
         currPlayer.set("ancient_adena_amount", totalAncientAdena);
         currPlayer.set("contribution_score", totalContribScore);
         this._signsPlayerData.put(objectId, currPlayer);
         switch(this.getPlayerCabal(objectId)) {
            case 1:
               this._duskStoneScore += (double)contribScore;
               break;
            case 2:
               this._dawnStoneScore += (double)contribScore;
         }

         if (!Config.ALT_SEVENSIGNS_LAZY_UPDATE) {
            this.saveSevenSignsData(objectId);
            this.saveSevenSignsStatus();
         }

         return contribScore;
      }
   }

   public void addFestivalScore(int cabal, int amount) {
      if (cabal == 1) {
         this._duskFestivalScore += amount;
         if (this._dawnFestivalScore >= amount) {
            this._dawnFestivalScore -= amount;
         }
      } else {
         this._dawnFestivalScore += amount;
         if (this._duskFestivalScore >= amount) {
            this._duskFestivalScore -= amount;
         }
      }
   }

   public void sendCurrentPeriodMsg(Player player) {
      SystemMessage sm = null;
      switch(this.getCurrentPeriod()) {
         case 0:
            sm = SystemMessage.getSystemMessage(SystemMessageId.PREPARATIONS_PERIOD_BEGUN);
            break;
         case 1:
            sm = SystemMessage.getSystemMessage(SystemMessageId.COMPETITION_PERIOD_BEGUN);
            break;
         case 2:
            sm = SystemMessage.getSystemMessage(SystemMessageId.RESULTS_PERIOD_BEGUN);
            break;
         case 3:
            sm = SystemMessage.getSystemMessage(SystemMessageId.VALIDATION_PERIOD_BEGUN);
      }

      player.sendPacket(sm);
   }

   public void sendMessageToAll(SystemMessageId sysMsgId) {
      Broadcast.toAllOnlinePlayers(SystemMessage.getSystemMessage(sysMsgId));
   }

   protected void initializeSeals() {
      for(Entry<Integer, Integer> e : this._signsSealOwners.entrySet()) {
         if (e.getValue() != 0) {
            if (this.isSealValidationPeriod()) {
               _log.info("SevenSigns: The " + getCabalName(e.getValue()) + " have won the " + getSealName(e.getKey(), false) + ".");
            } else {
               _log.info("SevenSigns: The " + getSealName(e.getKey(), false) + " is currently owned by " + getCabalName(e.getValue()) + ".");
            }
         } else {
            _log.info("SevenSigns: The " + getSealName(e.getKey(), false) + " remains unclaimed.");
         }
      }
   }

   protected void resetSeals() {
      this._signsDawnSealTotals.put(1, 0);
      this._signsDawnSealTotals.put(2, 0);
      this._signsDawnSealTotals.put(3, 0);
      this._signsDuskSealTotals.put(1, 0);
      this._signsDuskSealTotals.put(2, 0);
      this._signsDuskSealTotals.put(3, 0);
   }

   protected void calcNewSealOwners() {
      for(Integer currSeal : this._signsDawnSealTotals.keySet()) {
         int newSealOwner;
         int prevSealOwner = this._signsSealOwners.get(currSeal);
         newSealOwner = 0;
         int dawnProportion = this.getSealProportion(currSeal, 2);
         int totalDawnMembers = this.getTotalMembers(2) == 0 ? 1 : this.getTotalMembers(2);
         int dawnPercent = Math.round((float)dawnProportion / (float)totalDawnMembers * 100.0F);
         int duskProportion = this.getSealProportion(currSeal, 1);
         int totalDuskMembers = this.getTotalMembers(1) == 0 ? 1 : this.getTotalMembers(1);
         int duskPercent = Math.round((float)duskProportion / (float)totalDuskMembers * 100.0F);
         label93:
         switch(prevSealOwner) {
            case 0:
               switch(this.getCabalHighestScore()) {
                  case 0:
                     newSealOwner = 0;
                     break label93;
                  case 1:
                     if (duskPercent >= 35) {
                        newSealOwner = 1;
                     } else {
                        newSealOwner = 0;
                     }
                     break label93;
                  case 2:
                     if (dawnPercent >= 35) {
                        newSealOwner = 2;
                     } else {
                        newSealOwner = 0;
                     }
                  default:
                     break label93;
               }
            case 1:
               switch(this.getCabalHighestScore()) {
                  case 0:
                     if (duskPercent >= 10) {
                        newSealOwner = 1;
                     } else {
                        newSealOwner = 0;
                     }
                     break label93;
                  case 1:
                     if (duskPercent >= 10) {
                        newSealOwner = 1;
                     } else {
                        newSealOwner = 0;
                     }
                     break label93;
                  case 2:
                     if (dawnPercent >= 35) {
                        newSealOwner = 2;
                     } else if (duskPercent >= 10) {
                        newSealOwner = 1;
                     } else {
                        newSealOwner = 0;
                     }
                  default:
                     break label93;
               }
            case 2:
               switch(this.getCabalHighestScore()) {
                  case 0:
                     if (dawnPercent >= 10) {
                        newSealOwner = 2;
                     } else {
                        newSealOwner = 0;
                     }
                     break;
                  case 1:
                     if (duskPercent >= 35) {
                        newSealOwner = 1;
                     } else if (dawnPercent >= 10) {
                        newSealOwner = 2;
                     } else {
                        newSealOwner = 0;
                     }
                     break;
                  case 2:
                     if (dawnPercent >= 10) {
                        newSealOwner = 2;
                     } else {
                        newSealOwner = 0;
                     }
               }
         }

         this._signsSealOwners.put(currSeal, newSealOwner);
         switch(currSeal) {
            case 1:
               if (newSealOwner == 2) {
                  this.sendMessageToAll(SystemMessageId.DAWN_OBTAINED_AVARICE);
               } else if (newSealOwner == 1) {
                  this.sendMessageToAll(SystemMessageId.DUSK_OBTAINED_AVARICE);
               }
               break;
            case 2:
               if (newSealOwner == 2) {
                  this.sendMessageToAll(SystemMessageId.DAWN_OBTAINED_GNOSIS);
               } else if (newSealOwner == 1) {
                  this.sendMessageToAll(SystemMessageId.DUSK_OBTAINED_GNOSIS);
               }
               break;
            case 3:
               if (newSealOwner == 2) {
                  this.sendMessageToAll(SystemMessageId.DAWN_OBTAINED_STRIFE);
               } else if (newSealOwner == 1) {
                  this.sendMessageToAll(SystemMessageId.DUSK_OBTAINED_STRIFE);
               }

               CastleManager.getInstance().validateTaxes(newSealOwner);
         }
      }
   }

   protected void teleLosingCabalFromDungeons(String compWinner) {
      for(Player player : World.getInstance().getAllPlayers()) {
         if (player != null) {
            StatsSet currPlayer = this._signsPlayerData.get(player.getObjectId());
            if (!this.isSealValidationPeriod() && !this.isCompResultsPeriod()) {
               if (!player.isGM() && player.isIn7sDungeon() && (currPlayer == null || !currPlayer.getString("cabal").isEmpty())) {
                  player.teleToLocation(TeleportWhereType.TOWN, true);
                  player.setIsIn7sDungeon(false);
                  player.sendMessage("You have been teleported to the nearest town because you have not signed for any cabal.");
               }
            } else if (!player.isGM() && player.isIn7sDungeon() && (currPlayer == null || !currPlayer.getString("cabal").equals(compWinner))) {
               player.teleToLocation(TeleportWhereType.TOWN, true);
               player.setIsIn7sDungeon(false);
               player.sendMessage("You have been teleported to the nearest town due to the beginning of the Seal Validation period.");
            }
         }
      }
   }

   public boolean checkIsDawnPostingTicket(int itemId) {
      if (itemId > 6114 && itemId < 6175) {
         return true;
      } else if (itemId > 6801 && itemId < 6812) {
         return true;
      } else if (itemId > 7997 && itemId < 8008) {
         return true;
      } else if (itemId > 7940 && itemId < 7951) {
         return true;
      } else if (itemId > 6294 && itemId < 6307) {
         return true;
      } else if (itemId > 6831 && itemId < 6834) {
         return true;
      } else if (itemId > 8027 && itemId < 8030) {
         return true;
      } else {
         return itemId > 7970 && itemId < 7973;
      }
   }

   public boolean checkIsRookiePostingTicket(int itemId) {
      if (itemId > 6174 && itemId < 6295) {
         return true;
      } else if (itemId > 6811 && itemId < 6832) {
         return true;
      } else if (itemId > 7950 && itemId < 7971) {
         return true;
      } else {
         return itemId > 8007 && itemId < 8028;
      }
   }

   public void giveCPMult(int strifeOwner) {
      for(Player player : World.getInstance().getAllPlayers()) {
         if (player != null) {
            int cabal = this.getPlayerCabal(player.getObjectId());
            if (cabal != 0) {
               if (cabal == strifeOwner) {
                  player.addSkill(SkillsParser.FrequentSkill.THE_VICTOR_OF_WAR.getSkill());
               } else {
                  player.addSkill(SkillsParser.FrequentSkill.THE_VANQUISHED_OF_WAR.getSkill());
               }
            }
         }
      }
   }

   public void removeCPMult() {
      for(Player player : World.getInstance().getAllPlayers()) {
         if (player != null) {
            player.removeSkill(SkillsParser.FrequentSkill.THE_VICTOR_OF_WAR.getSkill());
            player.removeSkill(SkillsParser.FrequentSkill.THE_VANQUISHED_OF_WAR.getSkill());
         }
      }
   }

   public boolean checkSummonConditions(Player activeChar) {
      if (activeChar == null) {
         return true;
      } else if (this.isSealValidationPeriod() && this.getSealOwner(3) == 2 && this.getPlayerCabal(activeChar.getObjectId()) == 1) {
         activeChar.sendMessage("You cannot summon Siege Golem or Cannon while Seal of Strife posessed by Lords of Dawn.");
         return true;
      } else {
         return false;
      }
   }

   protected class SevenSignsPeriodChange implements Runnable {
      @Override
      public void run() {
         int periodEnded = SevenSigns.this.getCurrentPeriod();
         ++SevenSigns.this._activePeriod;
         switch(periodEnded) {
            case 0:
               SevenSignsFestival.getInstance().startFestivalManager();
               SevenSigns.this.sendMessageToAll(SystemMessageId.QUEST_EVENT_PERIOD_BEGUN);
               RaidBossSpawnManager.getInstance().distributeRewards();
               break;
            case 1:
               SevenSigns.this.sendMessageToAll(SystemMessageId.QUEST_EVENT_PERIOD_ENDED);
               int compWinner = SevenSigns.this.getCabalHighestScore();
               SevenSignsFestival.getInstance().getFestivalManagerSchedule().cancel(false);
               SevenSignsFestival.getInstance().rewardHighestRanked();
               SevenSigns.this.calcNewSealOwners();
               switch(compWinner) {
                  case 1:
                     SevenSigns.this.sendMessageToAll(SystemMessageId.DUSK_WON);
                     break;
                  case 2:
                     SevenSigns.this.sendMessageToAll(SystemMessageId.DAWN_WON);
               }

               SevenSigns.this._previousWinner = compWinner;

               for(Castle castle : CastleManager.getInstance().getCastles()) {
                  castle.setTicketBuyCount(0);
               }
               break;
            case 2:
               SevenSigns.this.initializeSeals();
               SevenSigns.this.giveCPMult(SevenSigns.this.getSealOwner(3));
               SevenSigns.this.sendMessageToAll(SystemMessageId.SEAL_VALIDATION_PERIOD_BEGUN);
               RaidBossSpawnManager.getInstance().distributeRewards();
               Quest twQuest = QuestManager.getInstance().getQuest(TerritoryWarManager.qn);
               if (twQuest != null) {
                  twQuest.startQuestTimer("setNextTWDate", 30000L, null, null);
               }

               SevenSigns._log
                  .info(
                     "SevenSigns: The "
                        + SevenSigns.getCabalName(SevenSigns.this._previousWinner)
                        + " have won the competition with "
                        + SevenSigns.this.getCurrentScore(SevenSigns.this._previousWinner)
                        + " points!"
                  );
               break;
            case 3:
               SevenSigns.this._activePeriod = 0;
               SevenSigns.this.sendMessageToAll(SystemMessageId.SEAL_VALIDATION_PERIOD_ENDED);
               SevenSigns.this.removeCPMult();
               SevenSigns.this.resetPlayerData();
               SevenSigns.this.resetSeals();
               ++SevenSigns.this._currentCycle;
               SevenSignsFestival.getInstance().resetFestivalData(false);
               SevenSigns.this._dawnStoneScore = 0.0;
               SevenSigns.this._duskStoneScore = 0.0;
               SevenSigns.this._dawnFestivalScore = 0;
               SevenSigns.this._duskFestivalScore = 0;
         }

         SevenSigns.this.saveSevenSignsData();
         SevenSigns.this.saveSevenSignsStatus();
         SevenSigns._log.info("SevenSigns: Change Catacomb spawn...");
         SevenSigns.this.changePeriodSpawn();
         SevenSigns.this.teleLosingCabalFromDungeons(SevenSigns.getCabalShortName(SevenSigns.this.getCabalHighestScore()));
         SSQInfo ss = new SSQInfo();
         Broadcast.toAllOnlinePlayers(ss);
         SevenSigns.this.spawnSevenSignsNPC();
         SevenSigns._log.info("SevenSigns: The " + SevenSigns.this.getCurrentPeriodName() + " period has begun!");
         SevenSigns.this.setCalendarForNextPeriodChange();

         for(Castle castle : CastleManager.getInstance().getCastles()) {
            castle.getSiege().correctSiegeDateTime();
         }

         SevenSigns.SevenSignsPeriodChange sspc = SevenSigns.this.new SevenSignsPeriodChange();
         ThreadPoolManager.getInstance().schedule(sspc, SevenSigns.this.getMilliToPeriodChange());
      }
   }

   private static class SingletonHolder {
      protected static final SevenSigns _instance = new SevenSigns();
   }
}
